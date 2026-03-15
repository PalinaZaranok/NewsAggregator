package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeRadioGroup = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val langRadioGroup = findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val btnApply = findViewById<Button>(R.id.btnApply)

        when (ThemeManager.getThemeMode(this)) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> themeRadioGroup.check(R.id.radioSystem)
            AppCompatDelegate.MODE_NIGHT_NO -> themeRadioGroup.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> themeRadioGroup.check(R.id.radioDark)
        }

        when (LanguageManager.getLanguage(this)) {
            "en" -> langRadioGroup.check(R.id.radioEnglish)
            "ru" -> langRadioGroup.check(R.id.radioRussian)
        }

        btnApply.setOnClickListener {
            val selectedTheme = when (themeRadioGroup.checkedRadioButtonId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            ThemeManager.saveThemeMode(this, selectedTheme)
            AppCompatDelegate.setDefaultNightMode(selectedTheme)

            val selectedLang = when (langRadioGroup.checkedRadioButtonId) {
                R.id.radioRussian -> "ru"
                else -> "en"
            }
            LanguageManager.saveLanguage(this, selectedLang)

            recreate()
        }
    }
}