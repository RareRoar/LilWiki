package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.settings_activity.*


class SettingsActivity : AppCompatActivity() {

    private val tag = "SettingsActivity"
    private lateinit var sqlAdapter : SQLiteAdapter

    enum class SearchMode {
        PRIVATE,
        PUBLIC
    }

    enum class Language {
        ENG,
        RUS
    }

    companion object {
        var isDarkTheme = true
        var language = Language.ENG
        var searchMode = SearchMode.PUBLIC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        sqlAdapter = SQLiteAdapter(this, sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND").toString())

        Log.i(tag, "Loading init settings")

        val initialSetting = sqlAdapter.getSettings()

        Log.i(tag, "Loaded")
        if (initialSetting["theme"] == "dark") {
            changeTheme(true)
        }
        else {
            changeTheme(false)
        }

        Log.i(tag, "Changes theme")
        //TODO initial
        switchTheme.isChecked = isDarkTheme
        switchTheme.setOnCheckedChangeListener { _: CompoundButton,
                                                 newState: Boolean ->
            isDarkTheme = newState

        }
        buttonSaveSettings.setOnClickListener { saveSettings() }
    }

    private fun changeTheme(newState : Boolean) {
        isDarkTheme = newState
        if (isDarkTheme)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
    }

    private fun saveSettings() {
        val theme = if (isDarkTheme) "dark" else "light"
        //val language = if (spinnerLanguages.selectedItem.toString() == "English") "en" else "ru"
        val searchMode = if (spinnerModes.selectedItemPosition == 0) "private" else "public"
        sqlAdapter.saveSettings(theme, searchMode)
        changeTheme(isDarkTheme)
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)
        with (sharedPrefs.edit()) {
            putString(getString(com.example.lilwiki.R.string.preferences_search_mode),
                searchMode)
            apply()
        }
        toDisciplineActivity()
    }

    private fun toDisciplineActivity() {
        val intent = Intent(this, DisciplineActivity::class.java)
        startActivity(intent)
    }
}