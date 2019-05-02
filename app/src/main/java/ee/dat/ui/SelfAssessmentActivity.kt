package ee.dat.ui

import android.os.Bundle
import android.widget.Toast
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.bean.SelfAssessment
import ee.dat.util.*
import kotlinx.android.synthetic.main.user_prefs_common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelfAssessmentActivity: WizardActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_self_assessment)
    }

    override fun onSupportNavigateUp(): Boolean {
        // Just don't allow the user to return at all
        return false
    }

    override fun onFabClick() {
        if (!mpref_root.assertSeekBarAllNotZero()) {
            Toast.makeText(this, R.string.please_fill_in_all, Toast.LENGTH_SHORT).show()
            return
        }
        working = true
        GlobalScope.launch(Dispatchers.Main) {
            val task = DateeApi.api.setSelfAssessment(SelfAssessment(
                openness = mpref_openness.progress,
                romance = mpref_romance.progress,
                warmheartedness = mpref_warmheartedness.progress
            ))

            withContext(Dispatchers.IO) {
                task.executeMaybe()
            }.onErr { showErrorToast(it); working = false; return@launch }

            // Succeeded, return to main
            finish()
            return@launch
        }
    }
}