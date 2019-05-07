package ee.dat.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import com.squareup.picasso.Callback
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_match.*
import kotlinx.android.synthetic.main.activity_wizard.*
import kotlinx.coroutines.*

class MatchInfoActivity: WizardActivity() {
    companion object {
        const val TAG = "MatchInfoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_match)
        GlobalScope.launch(Dispatchers.Main) {
            initialize()
        }
        // TODO: How do the users exchange information on when and where to date?
        // TODO: How to notify the users of the accept / reject status of the other person?
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_match, menu)
        return true
    }

    override fun onFabClick() {
        Toast.makeText(this, "Accept: Not Implemented Yet", Toast.LENGTH_SHORT).show()
    }

    private suspend fun initialize() = coroutineScope {
        if (!working) working = true
        val uid = withContext(Dispatchers.IO) {
            DateeApi.api.getMatchedUid().executeMaybe()
        }.onErr { showErrorToast(it); finish(); return@coroutineScope }!!
        Log.d(TAG, "matched uid = $uid")
        val user = withContext(Dispatchers.IO) {
            DateeApi.api.getUser(uid).executeMaybe()
        }.onErr { showErrorToast(it); finish(); return@coroutineScope }!!
        Log.d(TAG, "matched user = $user")
        title = "${user.firstName} ${user.lastName}"

        // Show info
        match_descr.text = getString(R.string.match_descr,
            user.firstName, user.lastName, user.age, user.city, user.country, user.gender)

        // Load the photo of the matched person
        val photos = withContext(Dispatchers.IO) {
            DateeApi.api.listPhotos(uid).executeMaybe()
        }.onErr { showErrorToast(it); finish(); return@coroutineScope }!!
        DateeApi.picasso
            .load(DateeApi.buildPhotoUrl(photos[0]))
            .fit()
            .centerCrop()
            .into(wizard_header, object : Callback {
                override fun onSuccess() {
                    // Allow the photo to be displayed behind the status bar
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    working = false
                }

                override fun onError(e: Exception?) {
                    e?.message?.also {
                        Toast.makeText(this@MatchInfoActivity, it, Toast.LENGTH_LONG).show()
                    }
                    finish()
                }
            })
    }
}