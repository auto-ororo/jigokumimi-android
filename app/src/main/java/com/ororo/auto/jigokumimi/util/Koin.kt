package com.ororo.auto.jigokumimi.util

import androidx.preference.PreferenceManager
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.ororo.auto.jigokumimi.firebase.FirestoreService
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.*
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoLocationRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoMusicRepository
import com.ororo.auto.jigokumimi.ui.MainViewModel
import com.ororo.auto.jigokumimi.ui.history.HistoryViewModel
import com.ororo.auto.jigokumimi.ui.login.LoginViewModel
import com.ororo.auto.jigokumimi.ui.result.MiniPlayerViewModel
import com.ororo.auto.jigokumimi.ui.result.ResultViewModel
import com.ororo.auto.jigokumimi.ui.search.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val prefModule = module {
    factory { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
}

val firebaseModule = module {
    factory {
        if (BuildConfig.DEBUG) {
            // DEBUGビルド時はエミュレータを使用
            FirebaseFirestore.getInstance().apply {
                useEmulator(
                    "10.0.2.2", 8080
                )
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
            }
        } else {
            FirebaseFirestore.getInstance()
        }
    }
}

val serviceModule = module {
    single { SpotifyApi.retrofitService }
    factory { FirestoreService(get()) }
}

val repositoryModule = module {
    single<ILocationRepository> { LocationRepository(androidApplication()) }
    single<IMusicRepository> { MusicRepository(get(), get(), get()) }
    single<IAuthRepository> { AuthRepository(get(), get(), get()) }
    single<IDeviceRepository> { DeviceRepository(get())}
}

val demoRepositoryModule = module {
    single<ILocationRepository> { DemoLocationRepository(androidApplication()) }
    single<IMusicRepository> { DemoMusicRepository(get()) }
    single<IAuthRepository> { DemoAuthRepository() }
}

val viewModelModule = module {
    viewModel {
        HistoryViewModel(
            androidApplication(),
            get(),
            get()
        )
    }
    viewModel {
        LoginViewModel(
            androidApplication(),
            get()
        )
    }
    viewModel {
        MainViewModel(
            androidApplication(),
            get(),
            get()
        )
    }
    viewModel {
        ResultViewModel(
            androidApplication(),
            get(),
            get()
        )
    }
    viewModel {
        MiniPlayerViewModel(
            androidApplication(),
            get(),
            get()
        )
    }
    viewModel {
        SearchViewModel(
            androidApplication(),
            get(),
            get(),
            get()
        )
    }
}

val koinModules = listOf(
    prefModule,
    firebaseModule,
    serviceModule,
    repositoryModule,
    viewModelModule
)

