package ee.dat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ee.dat.R
import ee.dat.util.*

class MainActivity : AppCompatActivity() {
    var initialzed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (!initialzed) tryInitialize()
    }

    private fun tryInitialize() {
        when (LoginStateManager.loginState) {
            LoginStateManager.LoginState.LoggedOut -> startWelcomeActivity()
            // TODO: Manage the other cases
        }
    }

    private fun startWelcomeActivity() {
        startActivity(Intent(this, WelcomeActivity::class.java))
    }
}
