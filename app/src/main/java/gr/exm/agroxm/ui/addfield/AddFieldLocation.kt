package gr.exm.agroxm.ui.addfield

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.qifan.powerpermission.coroutines.awaitAskPermissionsAllGranted
import com.qifan.powerpermission.rationale.createDialogRationale
import gr.exm.agroxm.R
import gr.exm.agroxm.ui.DataProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class AddFieldLocation : SupportMapFragment(), DataProvider {

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
                    center.position = map.cameraPosition.target
                }

                // Get location permission
                val hasPermission = awaitAskPermissionsAllGranted(
                    ACCESS_COARSE_LOCATION,
                    rationaleDelegate = createDialogRationale(
                        R.string.permission_location_title,
                        ACCESS_COARSE_LOCATION,
                        getString(R.string.permission_location_rationale)
                    )
                )

                // Enable user location on map
                map.isMyLocationEnabled = hasPermission

                // Get the current user location
                if (hasPermission) {
                    Timber.d("Trying to get current location")
                    locationClient.lastLocation
                        .addOnSuccessListener {
                            if (it == null) {
                                Timber.d("Current location is null. Requesting fresh location.")

                                locationClient.requestLocationUpdates(
                                    LocationRequest().apply {
                                        numUpdates = 1
                                        interval = TimeUnit.SECONDS.toMillis(2)
                                        fastestInterval = 0
                                        maxWaitTime = TimeUnit.SECONDS.toMillis(5)
                                        priority = PRIORITY_BALANCED_POWER_ACCURACY
                                    },
                                    object : LocationCallback() {
                                        override fun onLocationResult(result: LocationResult?) {
                                            Timber.d("New location result: ${result?.lastLocation?.latitude}, ${result?.lastLocation?.longitude}")
                                            result?.lastLocation?.let { it -> setFieldLocation(it) }
                                            updateCamera(result?.lastLocation)
                                        }
                                    },
                                    null
                                )
                            } else {
                                Timber.d("Got current location: ${it.latitude}, ${it.longitude}")
                                setFieldLocation(it)
                                updateCamera(it)
                            }
                        }
                        .addOnFailureListener {
                            Timber.d(it, "Could not get current location.")
                            Toast.makeText(
                                requireActivity(),
                                "Could not get current location.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } catch (e: Exception) {
                Timber.d(e, "Something went wrong")
            } finally {
                Timber.d("Destroying")
            }
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