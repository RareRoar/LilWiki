package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.lilwiki.patterns.AuthFacade
import com.example.lilwiki.patterns.SQLiteAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val tag = "LoginActivity"
    private val authFacade = AuthFacade(this, tag)
    private lateinit var sqlAdapter : SQLiteAdapter
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            toDisciplineActivity()
        }

        val userName = sharedPrefs.getString(getString(R.string.preferences_key_email),
            getString(R.string.not_found))
        val password = sharedPrefs.getString(getString(R.string.preferences_key_password),
            getString(R.string.not_found))
        if (userName.toString() != getString(R.string.not_found) &&
            password.toString() != getString(R.string.not_found)) {
            authFacade.signInAccount(userName.toString(), password.toString())
            loadSettings(sharedPrefs)
            toDisciplineActivity()
        }
        findViewById<Button>(R.id.buttonSignIn).setOnClickListener { signInListener() }
        findViewById<TextView>(R.id.textSignUp).setOnClickListener { signUpListener() }

    }

    private fun loadSettings(sharedPrefs : SharedPreferences) {
        sqlAdapter = SQLiteAdapter(this, sharedPrefs
            .getString(getString(R.string.preferences_key_email),
                getString(R.string.not_found)).toString())
        Log.i(tag, "Loading init settings")
        val initialSetting = sqlAdapter.getSettings()
        Log.i(tag, "Loaded")
        if (initialSetting["theme"] == "dark") {
            changeTheme(true)
        }
        else {
            changeTheme(false)
        }
        with (sharedPrefs.edit()) {
            putString(getString(R.string.preferences_search_mode),
                initialSetting["mode"])
            apply()
        }
    }

    private fun changeTheme(newState : Boolean) {
        if (newState)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun toTestActivity() {
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }

    private fun toDisciplineActivity() {
        val intent = Intent(this, DisciplineActivity::class.java)
        startActivity(intent)
    }

    private fun signInListener() {
        val email = findViewById<EditText>(R.id.emailEdit).text.toString()
        val password = findViewById<EditText>(R.id.passwordEdit).text.toString()

        if (authFacade.validateEmail(email) && authFacade.validatePassword(password)) {
            authFacade.signInAccount(email, password)
            authFacade.saveUserInfo(email, password)
            loadSettings(sharedPrefs)
            toDisciplineActivity()
        } else {
            val message = StringBuilder("")
            if (!authFacade.validateEmail(email))
                message.append("Irrelevant email format!")
            if (!authFacade.validatePassword(password)) {
                if (message.isNotEmpty())
                    message.append("\n")
                message.append("Password must include minimum 5 characters!")
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toCreateAccountActivity() {
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    private fun signUpListener() {
        toCreateAccountActivity()
    }
}