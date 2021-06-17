package gr.exm.agroxm.data.repository

import gr.exm.agroxm.data.Location
import gr.exm.agroxm.data.datasource.LocationDataSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.shareIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class LocationRepository : KoinComponent {

    private val dataSource: LocationDataSource by inject()

    private val flow: Flow<Location> by lazy {
        Timber.d("Creating location flow in repository")
        dataSource.getLocationUpdates()
            .shareIn(GlobalScope, SharingStarted.WhileSubscribed(5000))
    }

    suspend fun getLastLocation(): Location = dataSource.getLastLocation()

    fun getLocationUpdates(): Flow<Location> = flow.conflate()
}
