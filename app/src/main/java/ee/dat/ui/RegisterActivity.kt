package ee.dat.ui

import android.os.Bundle
import ee.dat.R

class RegisterActivity: WizardActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWizardContentView(R.layout.activity_register)
    }
}