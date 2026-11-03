package jp.benkyosuruzo

import android.app.Application
import jp.benkyosuruzo.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application entry point. Owns the app-wide coroutine scope used for
 * asynchronous history recording and warms up the database.
 */
class BenkyoSuruzoApplication : Application() {

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}
