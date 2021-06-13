package gr.exm.agroxm.data

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.squareup.moshi.Moshi
import gr.exm.agroxm.BuildConfig
import gr.exm.agroxm.data.datasource.*
import gr.exm.agroxm.data.network.ApiService
import gr.exm.agroxm.data.network.AuthService
import gr.exm.agroxm.data.network.interceptor.ApiRequestInterceptor
import gr.exm.agroxm.data.network.interceptor.AuthRequestInterceptor
import gr.exm.agroxm.data.network.interceptor.AuthTokenAuthenticator
import gr.exm.agroxm.data.repository.AuthRepository
import gr.exm.agroxm.data.repository.AuthRepositoryImpl
import gr.exm.agroxm.ui.Navigator
import gr.exm.agroxm.util.Validator
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


private const val PREFERENCE_AUTH_TOKEN = "PREFS_AUTH_TOKEN"
private const val PREFERENCE_CREDENTIALS = "PREFS_CREDENTIALS"
private const val NETWORK_CACHE_SIZE = 50L * 1024L * 1024L // 50MB

private val datasources = module {
    single<LocationDataSource> {
        LocationDataSourceImpl()
    }

    single<SharedPreferences>(named(PREFERENCE_AUTH_TOKEN)) {
        androidContext().getSharedPreferences(PREFERENCE_AUTH_TOKEN, Context.MODE_PRIVATE)
    }

    single<SharedPreferences>(named(PREFERENCE_CREDENTIALS)) {
        androidContext().getSharedPreferences(PREFERENCE_CREDENTIALS, Context.MODE_PRIVATE)
    }

    single<AuthTokenDataSource> {
        AuthTokenDataSourceImpl(get(named(PREFERENCE_AUTH_TOKEN)))
    }

    single<CredentialsDataSource> {
        CredentialsDataSourceImpl(get(named(PREFERENCE_CREDENTIALS)))
    }
}

private val repositories = module {
    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }
}

private val location = module {
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    single<SettingsClient> {
        LocationServices.getSettingsClient(androidContext())
    }
}

private val network = module {
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) Level.BODY else Level.NONE)
    }

    single<MoshiConverterFactory> {
        MoshiConverterFactory.create(Moshi.Builder().build()).asLenient()
    }

    single<AuthService> {
        // Create client
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(get() as HttpLoggingInterceptor)
            .addInterceptor(AuthRequestInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // Create retrofit instance
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.AUTH_URL)
            .addConverterFactory(get() as MoshiConverterFactory)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .client(client)
            .build()

        // Create service
        retrofit.create(AuthService::class.java)
    }

    single<ApiService> {
        // Install HTTP cache
        val cache = Cache(androidContext().cacheDir, NETWORK_CACHE_SIZE)

        // Create client
        val client: OkHttpClient = OkHttpClient.Builder()
            .authenticator(AuthTokenAuthenticator())
            .addInterceptor(ApiRequestInterceptor())
            .addInterceptor(get() as HttpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(cache)
            .build()

        // Create retrofit instance
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(get() as MoshiConverterFactory)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .client(client)
            .build()

        // Create service
        retrofit.create(ApiService::class.java)
    }
}

val validator = module {
    single {
        Validator()
    }
}

val navigator = module {
    single {
        Navigator()
    }
}

val modules = listOf(
    datasources, repositories, location, network, navigator, validator
)
