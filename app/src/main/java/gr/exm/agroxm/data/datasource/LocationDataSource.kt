package gr.exm.agroxm.data.datasource

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import gr.exm.agroxm.data.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface LocationDataSource {
    suspend fun getLastLocation(): Location
    fun getLocationUpdates(): Flow<Location>
}

class LocationDataSourceImpl : LocationDataSource, KoinComponent {

    companion object {
        /**
         * Foreground location request desired interval in milliseconds.
         * Inexact. Updates may be more or less frequent.
         * Default value is 30 seconds.
         */
        private val FOREGROUND_INTERVAL = TimeUnit.SECONDS.toMillis(10)

        /**
         * Foreground location request fastest interval in milliseconds.
         * Updates will never be more frequent than this value.
         * Default value is 10 seconds.
         */
        private val FOREGROUND_FASTEST_INTERVAL = TimeUnit.SECONDS.toMillis(5)

        /**
         * Foreground location request minimum displacement in meters.
         * Default value is 10 meters.
         */
        private const val FOREGROUND_MINIMUM_DISPLACEMENT: Long = 1
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun getLastLocation(): Location {
        return fusedLocationProviderClient.awaitLastLocation()
    }

    @ExperimentalCoroutinesApi
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun getLocationUpdates(): Flow<Location> {
        return fusedLocationProviderClient.locationFlow()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun FusedLocationProviderClient.awaitLastLocation() =
        suspendCancellableCoroutine<Location> { continuation ->
            lastLocation.addOnSuccessListener { location ->
                @Suppress("SENSELESS_COMPARISON")
                if (location != null) {
                    Timber.d("Found last location: $location")
                    continuation.resume(
                        Location(location.latitude, location.longitude)
                    )
                } else {
                    Timber.d("Last location is empty")
                    continuation.resume(Location.empty())
                }
            }.addOnFailureListener { e ->
                Timber.d(e, "Could not get last location.")
                continuation.resumeWithException(e)
            }
        }

    @ExperimentalCoroutinesApi
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
        // Create callback for sending location updates to the flow
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                // Ignore null responses
                if (result == null) {
                    Timber.d("Skipping null location")
                    return
                }

                // Send available locations to the flow
                for (location in result.locations) {
                    try {
                        Timber.d("Offering location: $location")
                        offer(Location(location.latitude, location.longitude))
                    } catch (e: Throwable) {
                        Timber.d(e, "Could not offer location to the flow")
                    }
                }
            }
        }

        // Register for location updates
        requestLocationUpdates(
            getForegroundLocationRequest(),
            callback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            Timber.d("Registered for location flow updates.")
        }.addOnFailureListener { e ->
            // In case of exception, close the Flow
            Timber.d(e, "Location flow error.")
            close(e)
        }

        // Wait for the consumer to cancel the coroutine and unregister
        // the callback. This suspends the coroutine until the flow is closed.
        awaitClose {
            // Remove location updates when flow collection ends
            Timber.d("Removing location updates from flow.")
            removeLocationUpdates(callback)
        }
    }.onStart {
        Timber.d("Location flow started")
    }.onCompletion {
        Timber.d("Location flow completed")
    }

    private fun getForegroundLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(FOREGROUND_MINIMUM_DISPLACEMENT.toFloat())
            .setInterval(FOREGROUND_INTERVAL)
            .setFastestInterval(FOREGROUND_FASTEST_INTERVAL)
    }
}