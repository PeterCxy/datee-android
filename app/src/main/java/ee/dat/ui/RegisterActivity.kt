package ee.dat.ui

import android.os.Bundle
import android.widget.Toast
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.bean.City
import ee.dat.bean.Country
import ee.dat.bean.Gender
import ee.dat.bean.RegisterUserInfo
import ee.dat.util.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RegisterActivity: WizardActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_register)
        setupValidators()
    }

    override fun onFabClick() {
        if (working) return
        if (!register_root.assertTextNoErrorAndFilled()) return
        working = true
        GlobalScope.async(Dispatchers.Main) job@{
            val registerTask = DateeApi.api.register(RegisterUserInfo(
                email = txt_reg_email.str,
                password = txt_reg_passwd.str,
                firstName = txt_reg_first_name.str,
                lastName = txt_reg_last_name.str,
                age = txt_reg_age.str.toInt(),
                gender = if (reg_gender_male.isChecked) { Gender.Male } else { Gender.Female },
                // TODO: ACTUALLY MAKE THESE WORK
                country = Country.China,
                city = City.Suzhou
            ))
            withContext(Dispatchers.IO) {
                    registerTask.executeMaybe()
            }.processResult { showErrorToast(it); working = false; return@job }
            // Successful
            Toast.makeText(this@RegisterActivity, R.string.reg_success, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupValidators() {
        txt_reg_email.validate({ it.isValidEmail() },
            getString(R.string.invalid, getString(R.string.reg_email)))
        txt_reg_passwd.validate({ it.length >= 4 }, getString(R.string.password_weak))
        txt_reg_first_name.validate({ !it.contains(" ") },
            getString(R.string.invalid, getString(R.string.reg_first_name)))
        txt_reg_last_name.validate({ !it.contains(" ") },
            getString(R.string.invalid, getString(R.string.reg_first_name)))
        txt_reg_age.validate({ it.toInt() in (18..60) },
            getString(R.string.invalid, getString(R.string.reg_age)))
    }
}