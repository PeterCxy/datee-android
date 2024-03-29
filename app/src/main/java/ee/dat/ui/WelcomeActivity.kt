package ee.dat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ee.dat.R
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        btn_register.setOnClickListener { this.startRegisterActivity() }
        btn_login.setOnClickListener { this.startLoginActivity() }
    }

    override fun onBackPressed() {
        // Block BACK button.
    }

    override fun onResume() {
        super.onResume()
        // If the user has logged in, exit this activity
        if (LoginStateManager.loginState != LoginStateManager.LoginState.LoggedOut) {
            finish()
        }
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}