package com.ororo.auto.jigokumimi.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ororo.auto.jigokumimi.firebase.FirestoreService
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.launch

class MainViewModel(
    private val app: Application,
    authRepository: IAuthRepository
) :
    BaseAndroidViewModel(app, authRepository) {

    /**
     * ログアウト実行
     */
    fun logout() {
        viewModelScope.launch {
            try {
                AuthorizationClient.clearCookies(app.applicationContext)
            } catch (e: Exception) {
                handleAuthException(e)
            }
        }
    }

    /**
     * AuthRepositoryをセット
     */
    fun setAuthRepository() {
        authRepository = AuthRepository(
            PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
            SpotifyApi.retrofitService,
            FirestoreService(FirebaseFirestore.getInstance())
        )
    }

    /**
     * Demo用AuthRepositoryをセット
     */
    fun setDemoAuthRepository() {
        authRepository = DemoAuthRepository(app)
    }

}