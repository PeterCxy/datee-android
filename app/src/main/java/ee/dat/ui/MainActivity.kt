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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var initialized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (!initialized) tryInitialize()
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

    private fun startSelfAssessmentActivity() {
        startActivity(Intent(this, SelfAssessmentActivity::class.java))
    }

    private fun startMatchingPrefsActivity() {
        startActivity(Intent(this, MatchingPrefsActivity::class.java))
    }

    private fun enterRatingState() {
        supportFragmentManager.beginTransaction().replace(R.id.main_frame, RatingFragment()).commit()
        initialized = true
        DateeApplication.curUser!!.apply {
            if (state == State.MatchingPreferencesSet) {
                // Show information to let the user wait for verification
                btn_match.text = getString(R.string.waiting_verification)
                btn_match.isEnabled = false
            } else if (state == State.Idle) {
                // Idle state means the user hasn't been matched yet
                // The system will match the user every day so the user can get a match every day
                // so if it has been consumed then no more matches can be made anymore
                // TODO: Maybe we need something to notify the user about an available match?
                btn_match.text = getString(R.string.match_cooling_down)
                btn_match.isEnabled = false
            }
        }
        btn_match.setOnClickListener {
            startActivity(Intent(this, MatchInfoActivity::class.java))
        }
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
                // Photo Uploaded, needs to do self-assessment
                State.PhotoUploaded -> startSelfAssessmentActivity()
                // SelfAssessment done, needs to set preferences
                State.SelfAssessmentDone -> startMatchingPrefsActivity()
                // Otherwise: at least MatchingPreferences are set, drop the user into the rating fragment
                // TODO: Allow the user to query for matches somehow after being activated
                else -> enterRatingState()
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
