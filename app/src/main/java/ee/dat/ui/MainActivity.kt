package ee.dat.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.util.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

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
            else -> initializeAsync()
        }
    }

    private fun startWelcomeActivity() {
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    private fun initializeAsync() = GlobalScope.async(Dispatchers.Main) {
        val dialog = ProgressDialog(this@MainActivity)
        with(dialog) {
            setCancelable(false)
            setMessage(getString(R.string.please_wait))
            isIndeterminate = true
            show()
        }

        Log.d(TAG, "access_token=${LoginStateManager.accessToken}")
        Log.d(TAG, "refresh_token=${LoginStateManager.refreshToken}")
        if (!startRefreshToken()) {
            dialog.hide()
            return@async
        }
        Log.d(TAG, "refreshed!")
        Log.d(TAG, "access_token=${LoginStateManager.accessToken}")
        Log.d(TAG, "refresh_token=${LoginStateManager.refreshToken}")
        dialog.hide()
    }

    private suspend fun startRefreshToken() = coroutineScope {
        // We refresh token each time we start (during the start job)
        // This is the first step in the initialization sequence
        val refreshTask = DateeApi.api.authRefresh(refresh_token = LoginStateManager.refreshToken!!)
        val result = withContext(Dispatchers.IO) {
            refreshTask.executeMaybe()
        }.processResultGeneral {
            // TODO: This is sh*t. When network breaks, the login state will be cleared.
            //  Refactor this somehow to accommodate for network failure.
            LoginStateManager.clearTokens()
            Toast.makeText(this@MainActivity, R.string.refresh_failure, Toast.LENGTH_LONG).show()
            finish()
            return@coroutineScope false
        }
        LoginStateManager.setAccessToken(result!!.accessToken, result.expiresIn)
        LoginStateManager.refreshToken = result.refreshToken
        true
    }
}
