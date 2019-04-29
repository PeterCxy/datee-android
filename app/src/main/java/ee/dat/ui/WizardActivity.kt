package ee.dat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ee.dat.R
import kotlinx.android.synthetic.main.activity_wizard.*

abstract class WizardActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wizard)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        window.decorView.viewTreeObserver.addOnPreDrawListener {
            appbar.layoutParams.height = (window.decorView.height * 0.4).toInt()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}