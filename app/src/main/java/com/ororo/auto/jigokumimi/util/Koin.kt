package com.ororo.auto.jigokumimi.util

import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.network.JigokumimiApi
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.*
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoLocationRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoMusicRepository
import com.ororo.auto.jigokumimi.ui.MainViewModel
import com.ororo.auto.jigokumimi.ui.history.HistoryViewModel
import com.ororo.auto.jigokumimi.ui.login.LoginViewModel
import com.ororo.auto.jigokumimi.ui.result.ResultViewModel
import com.ororo.auto.jigokumimi.ui.search.SearchViewModel
import com.ororo.auto.jigokumimi.ui.setting.SettingViewModel
import com.ororo.auto.jigokumimi.ui.signup.SignUpViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val prefModule = module {
    factory { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
}

val apiModule = module {
    single { SpotifyApi.retrofitService }
    single { JigokumimiApi.retrofitService }
}

val repositoryModule = module {
    single<ILocationRepository> { LocationRepository(androidApplication()) }
    single<IMusicRepository> { MusicRepository(get(), get(), get()) }
    single<IAuthRepository> { AuthRepository(get(), get(), get()) }
}

val demoRepositoryModule = module {
    single<ILocationRepository> { DemoLocationRepository(androidApplication()) }
    single<IMusicRepository> { DemoMusicRepository(get()) }
    single<IAuthRepository> { DemoAuthRepository(get()) }
}

val viewModelModule = module {
    viewModel {
        HistoryViewModel(
            androidApplication(),
            get(),
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
        SearchViewModel(
            androidApplication(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SettingViewModel(
            androidApplication(),
            get()
        )
    }
    viewModel {
        SignUpViewModel(
            androidApplication(),
            get()
        )
    }
}

val koinModules = listOf(
    prefModule,
    apiModule,
    repositoryModule,
    viewModelModule
)

