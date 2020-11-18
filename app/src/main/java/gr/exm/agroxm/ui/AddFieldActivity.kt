package gr.exm.agroxm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import gr.exm.agroxm.R

class AddFieldActivity : AppCompatActivity(R.layout.activity_add_field) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val map = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        map.getMapAsync {
            val athens = LatLng(37.980646, 23.710478)
            it.addMarker(
                MarkerOptions()
                    .position(athens)
                    .title("Marker in Athens")
            )
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(athens, 18F))
        }
    }

}