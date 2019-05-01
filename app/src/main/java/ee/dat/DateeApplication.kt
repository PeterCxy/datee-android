package ee.dat

import android.app.Application
import android.content.Context
import ee.dat.bean.User

class DateeApplication: Application() {
    // GlobalContext to be retrieved anywhere
    // in the application
    companion object GlobalContext {
        private var appObj: Application? = null
        val context: Context
                get() {
                    return appObj!!
                }
        // A field in global object to save the current logged-in user
        // This only works if the component requiring the user is running
        // in the main process; if we add a service or something in another
        // process, it needs to initialize this itself
        var curUser: User? = null
    }

    override fun onCreate() {
        super.onCreate()
        appObj = this
    }
}