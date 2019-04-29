package ee.dat.ui

import android.os.Bundle
import android.widget.Toast
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity: WizardActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_login)
        setupValidators()
    }

    override fun onFabClick() {
        if (working) return
        if (!login_root.assertTextNoErrorAndFilled()) return
        working = true
        GlobalScope.launch(Dispatchers.Main) job@{
            val loginTask = DateeApi.api.authLogin(
                username = txt_login_email.str,
                password = txt_login_passwd.str
            )
            val result = withContext(Dispatchers.IO) {
                loginTask.executeMaybe()
            }.processResultGeneral {
                Toast.makeText(this@LoginActivity, R.string.login_failure, Toast.LENGTH_SHORT).show()
                working = false
                return@job
            }
            // Login success, store the information
            LoginStateManager.setAccessToken(
                result!!.accessToken, System.currentTimeMillis() + result.expiresIn)
            LoginStateManager.refreshToken = result.refreshToken
            finish()
        }
    }

    private fun setupValidators() {
        txt_login_email.validate({ it.isValidEmail() },
            getString(R.string.invalid, getString(R.string.reg_email)))
        txt_login_passwd.validate({ it.length >= 8 }, getString(R.string.password_weak))
    }
}