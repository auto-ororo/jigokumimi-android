import android.app.Application
import android.system.Os.close
import androidx.annotation.VisibleForTesting
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.database.MusicDao
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.network.JigokumimiApi
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.*
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    @Volatile
    var authRepository: IAuthRepository? = null
        @VisibleForTesting set

    @Volatile
    var musicRepository: IMusicRepository? = null
        @VisibleForTesting set

    @Volatile
    var locationRepository: ILocationRepository? = null
        @VisibleForTesting set

    fun getAuthRepository(app: Application): IAuthRepository {
        return authRepository ?: synchronized(this) {
            AuthRepository(
                PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
                JigokumimiApi.retrofitService,
                SpotifyApi.retrofitService
            ).also {
                authRepository = it
            }
        }
    }

    fun getMusicRepository(app: Application): IMusicRepository {
        return musicRepository ?: synchronized(this) {
            MusicRepository(
                getDatabase(app.applicationContext).musicDao,
                PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
                SpotifyApi.retrofitService,
                JigokumimiApi.retrofitService
            ).also {
                musicRepository = it
            }
        }
    }

    fun getLocationRepository(app: Application): ILocationRepository {
        return locationRepository ?: synchronized(this) {
            LocationRepository(
                app
            ).also {
                locationRepository = it
            }
        }
    }

}