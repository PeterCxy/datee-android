package ee.dat.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ee.dat.DateeApplication
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.bean.State
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

    private fun startPhotoUploadActivity() {
        startActivity(Intent(this, PhotoUploadActivity::class.java))
    }

    private fun initializeAsync() = GlobalScope.launch(Dispatchers.Main) {
        ProgressDialog(this@MainActivity).apply {
            setCancelable(false)
            setMessage(getString(R.string.please_wait))
            isIndeterminate = true
            show()
        }.also {
            Log.d(TAG, "access_token=${LoginStateManager.accessToken}")
            Log.d(TAG, "refresh_token=${LoginStateManager.refreshToken}")
            if (!startRefreshToken()) {
                return@also
            }
            Log.d(TAG, "refreshed!")
            Log.d(TAG, "access_token=${LoginStateManager.accessToken}")
            Log.d(TAG, "refresh_token=${LoginStateManager.refreshToken}")

            val user = withContext(Dispatchers.IO) {
                DateeApi.api.whoami().executeMaybe()
            }.onErr { showErrorToast(it); finish(); return@also }!!

            Log.d(TAG, user.toString())

            // Save to the global context
            DateeApplication.curUser = user

            when (user.state) {
                // Registered, but photos aren't uploaded yet or haven't uploaded enough
                State.Registered -> startPhotoUploadActivity()
            }
        }.hide()
    }

    private suspend fun startRefreshToken() = coroutineScope {
        // We refresh token each time we start (during the start job)
        // This is the first step in the initialization sequence
        val refreshTask = DateeApi.api.authRefresh(refresh_token = LoginStateManager.refreshToken!!)
        val result = withContext(Dispatchers.IO) {
            refreshTask.executeMaybe()
        }.onErrGeneral {
            if (LoginStateManager.loginState == LoginStateManager.LoginState.LoggedIn) {
                // If it failed but we didn't need refresh anyway, just return and pretend nothing happened
                // but the application will probably fail if even refreshing fails...
                return@coroutineScope true
            }
            // TODO: This is sh*t. When network breaks, but refresh is needed, the login state will be cleared.
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
