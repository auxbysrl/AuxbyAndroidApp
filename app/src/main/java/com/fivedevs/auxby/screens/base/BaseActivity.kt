package com.fivedevs.auxby.screens.base

import android.content.Context
import android.view.MotionEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.LocalizationHandler
import com.fivedevs.auxby.domain.utils.extensions.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val preferencesService = PreferencesService(newBase)
        val language = preferencesService.getSelectedLanguageApp()
        super.attachBaseContext(
            LocalizationHandler.wrap(newBase, language)
        )
    }

    override fun onResume() {
        super.onResume()
        val preferencesService = PreferencesService(this)
        val language = preferencesService.getSelectedLanguageApp()
        if (language.isNotEmpty() && !this.resources.configuration.locales[0].language.equals(language, true)) {
            recreateActivity()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText) {
            val coordinates = IntArray(2)
            view.getLocationOnScreen(coordinates)
            val x = ev.rawX + view.left - coordinates[0]
            val y = ev.rawY + view.top - coordinates[1]
            if (x < view.left || x > view.right || y < view.top || y > view.bottom)
                view.hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

    fun recreateActivity() {
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}