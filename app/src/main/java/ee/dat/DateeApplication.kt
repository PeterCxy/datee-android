package ee.dat

import android.app.Application
import android.content.Context

class DateeApplication: Application() {
    // GlobalContext to be retrieved anywhere
    // in the application
    companion object GlobalContext {
        private var appObj: Application? = null
        val context: Context
                get() {
                    return appObj!!
                }
    }

    override fun onCreate() {
        super.onCreate()
        appObj = this
    }
}