package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val tag = "LoginActivity"
    private val authFacade = AuthFacade(this, tag)
    private lateinit var sqlAdapter : SQLiteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)
        sqlAdapter = SQLiteAdapter(this, sharedPrefs
            .getString(getString(R.string.preferences_key_email),
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

        auth = Firebase.auth
        if (auth.currentUser != null) {
            toDisciplineActivity()
        }
        //val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
        //    Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND")
        val password = sharedPrefs.getString(getString(R.string.preferences_key_password),
            "NOT FOUND")
        if (userName != "NOT FOUND" && password != "NOT FOUND") {
            authFacade.signInAccount(userName.toString(), password.toString())
            toDisciplineActivity()
        }
        findViewById<Button>(R.id.buttonSignIn).setOnClickListener { signInListener() }
        findViewById<TextView>(R.id.textSignUp).setOnClickListener { signUpListener() }

    }

    private fun changeTheme(newState : Boolean) {
        SettingsActivity.isDarkTheme = newState
        if (SettingsActivity.isDarkTheme)
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
        //Toast.makeText(this, "create", Toast.LENGTH_SHORT).show()
        toCreateAccountActivity()
        //toDisciplineActivity()
    }
}