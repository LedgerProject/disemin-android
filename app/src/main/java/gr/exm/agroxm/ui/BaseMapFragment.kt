package gr.exm.agroxm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.android.ktx.awaitMap

abstract class BaseMapFragment : Fragment() {

    private lateinit var mapView: MapView

    @LayoutRes
    abstract fun getLayoutResId(): Int

    @IdRes
    abstract fun getMapViewResId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutResId(), container, false)
        mapView = view.findViewById(getMapViewResId())
        val state: Bundle? = savedInstanceState?.getBundle(STATE_MAP_VIEW)
        mapView.onCreate(state)
        return view
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var state = outState.getBundle(STATE_MAP_VIEW)
        if (state == null) {
            state = Bundle()
        }
        mapView.onSaveInstanceState(state)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    suspend fun getMap(callback: OnMapReadyCallback?): GoogleMap {
        return mapView.awaitMap()
    }

    companion object {
        private const val STATE_MAP_VIEW = "map_view_state"
    }
}