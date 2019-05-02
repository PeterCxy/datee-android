package ee.dat.ui

import android.os.Bundle
import android.widget.Toast
import ee.dat.DateeApplication
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.bean.Gender
import ee.dat.bean.MatchingPreferences
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_matching_prefs.*
import kotlinx.android.synthetic.main.user_prefs_common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchingPrefsActivity: WizardActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_matching_prefs)
        setupValidators()

        // Set default gender preferences
        when (DateeApplication.curUser!!.gender) {
            Gender.Male -> mpref_gender_female.isChecked = true
            Gender.Female -> mpref_gender_male.isChecked = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Just don't allow the user to return at all
        return false
    }

    override fun onFabClick() {
        if (!matching_preferences_root.assertTextNoErrorAndFilled()
            || !matching_preferences_root.assertSeekBarAllNotZero()) {
            Toast.makeText(this, R.string.please_fill_in_all, Toast.LENGTH_SHORT).show()
            return
        }
        working = true

        GlobalScope.launch(Dispatchers.Main) {
            val task = DateeApi.api.setMatchingPrefs(MatchingPreferences(
                minAge = txt_mpref_min_age.str.toInt(),
                maxAge = txt_mpref_max_age.str.toInt(),
                gender = if (mpref_gender_male.isChecked) { Gender.Male } else { Gender.Female },
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

    private fun setupValidators() {
        txt_mpref_min_age.validate(::isAllowedAge,
            getString(R.string.invalid, getString(R.string.mpref_min_age)))
        txt_mpref_max_age.validate(::isAllowedAge,
            getString(R.string.invalid, getString(R.string.mpref_max_age)))
    }

}