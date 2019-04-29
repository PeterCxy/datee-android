package ee.dat.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ee.dat.R
import ee.dat.util.LoginStateManager
import ee.dat.util.LoginStateManager.LoginState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (LoginStateManager.getLoginState()) {
            LoginState.LoggedOut -> startWelcomeActivity()
            // TODO: Manage the other cases
        }
    }

    private fun startWelcomeActivity() {
        startActivity(Intent(this, WelcomeActivity::class.java))
    }
}
