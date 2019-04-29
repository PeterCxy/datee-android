package ee.dat.ui

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import ee.dat.R
import kotlinx.android.synthetic.main.activity_wizard.*

abstract class WizardActivity: AppCompatActivity() {
    var lastKeyboardState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wizard)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        window.decorView.viewTreeObserver.addOnPreDrawListener {
            appbar.layoutParams.height = (window.decorView.height * 0.4).toInt()
            true
        }
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
            val keyboardState = keyboardShown()
            if (lastKeyboardState != keyboardState) {
                if (keyboardState) {
                    appbar.setExpanded(false, true)
                } else {
                    appbar.setExpanded(true, true)
                }
            }
            lastKeyboardState = keyboardState
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun keyboardShown(): Boolean {
        val softKeyboardHeight = 100
        var r = Rect()
        window.decorView.getWindowVisibleDisplayFrame(r)
        val dm = window.decorView.resources.displayMetrics
        val heightDiff = window.decorView.bottom - r.bottom
        return heightDiff > softKeyboardHeight * dm.density
    }

    fun setWizardContentView(resid: Int) {
        val inflater = LayoutInflater.from(this)
        inflater.inflate(resid, wizard_frame, true)
    }
}