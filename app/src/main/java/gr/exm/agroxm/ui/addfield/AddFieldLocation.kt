package gr.exm.agroxm.ui.addfield

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import gr.exm.agroxm.R
import gr.exm.agroxm.ui.common.checkPermissionsAndThen
import gr.exm.agroxm.ui.common.toast
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class AddFieldLocation : SupportMapFragment(), DataProvider, KoinComponent {

    private lateinit var locationClient: FusedLocationProviderClient

    private var fieldLocation: Location = Location("Europe").apply {
        latitude = 37.971623052839405
        longitude = 23.72626297535234
    }

    init {
        lifecycle.coroutineScope.launchWhenCreated {
            try {
                locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

                // Get Google map
                val map = awaitMap()

                // Set default location
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(fieldLocation.latitude, fieldLocation.longitude), 1F
                    )
                )

                // Move camera to given location
                fun updateCamera(location: Location?) {
                    location?.let {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude), 10F
                            )
                        )
                    }
                }

                // Add a center map marker
                val center = map.addMarker {
                    position(map.cameraPosition.target)
                }

                // Move the center map marker, whenever the map is moved
                map.setOnCameraMoveListener {
                    center?.position = map.cameraPosition.target
                }

                checkPermissionsAndThen(
                    permissions = arrayOf(ACCESS_COARSE_LOCATION),
                    rationaleTitle = getString(R.string.permission_location_title),
                    rationaleMessage = getString(R.string.permission_location_rationale),
                    onGranted = {
                        // Get last location
                        getLocationAndThen { location ->
                            Timber.d("Got user locationt: $location")
                            setFieldLocation(location)
                            updateCamera(location)
                        }

                        // Enable user location on map
                        map.isMyLocationEnabled = true
                    },
                    onDenied = {

                    }
                )
            } catch (e: Exception) {
                Timber.d(e, "Something went wrong")
            } finally {
                Timber.d("Destroying")
            }
        }
    }

    private fun getLocationAndThen(onLocation: (location: Location) -> Unit) {
        locationClient.lastLocation
            .addOnSuccessListener {
                if (it == null) {
                    Timber.d("Current location is null. Requesting fresh location.")

                    locationClient.requestLocationUpdates(
                        LocationRequest.create()
                            .setNumUpdates(1)
                            .setInterval(TimeUnit.SECONDS.toMillis(2))
                            .setFastestInterval(0)
                            .setMaxWaitTime(TimeUnit.SECONDS.toMillis(5))
                            .setPriority(PRIORITY_BALANCED_POWER_ACCURACY),
                        object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult?) {
                                result?.lastLocation?.let { location ->
                                    onLocation.invoke(location)
                                } ?: toast("Could not get current location.")
                            }
                        },
                        Looper.getMainLooper()
                    )
                } else {
                    Timber.d("Got current location: ${it.latitude}, ${it.longitude}")
                    onLocation.invoke(it)
                }
            }
            .addOnFailureListener {
                Timber.d(it, "Could not get current location.")
                toast("Could not get current location.")
            }
    }

    fun setFieldLocation(location: Location) {
        this.fieldLocation = location
    }

    companion object {
        const val ARG_LOCATION = "location"
    }

    override fun isDataValid(): Boolean {
        return true
    }

    override fun getData(): Bundle {
        return Bundle().apply {
            putParcelable(ARG_LOCATION, fieldLocation)
        }
    }
}