package ee.dat.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import ee.dat.R
import ee.dat.util.*
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
        wizard_fab.setOnClickListener { onFabClick() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    open fun onFabClick() {}

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
        // The DONE event on the last EditText implies submit
        // this event is sent from IME
        wizard_frame.findLastEditText()?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onFabClick()
                true
            }
            false
        }
    }

    var working = false
        set(value) {
            if (value != field) {
                if (value) {
                    wizard_progress.visibility = View.VISIBLE
                    Utility.disableEnableControls(false, wizard_frame)
                } else {
                    wizard_progress.visibility = View.GONE
                    Utility.disableEnableControls(true, wizard_frame)
                }
            }
            field = value
        }
}