package com.example.lilwiki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateAccountActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = Firebase.auth
    private val tag = "CreateAccountActivity"
    private val authFacade = AuthFacade(this, tag)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        findViewById<Button>(R.id.buttonCreateAccount).
        setOnClickListener { createAccountListener() }
    }

    private fun createAccountListener() {
        val email = findViewById<EditText>(R.id.editEmail).text.toString()
        val password = findViewById<EditText>(R.id.editPassword).text.toString()
        if (authFacade.validateEmail(email) && authFacade.validatePassword(password)) {
            authFacade.createAccount(email, password)
            authFacade.signInAccount(email, password)
            authFacade.saveUserInfo(email, password)
            Toast.makeText(this, "A new user has been created!",
                Toast.LENGTH_SHORT).show()
            // ...
        } else {
            authFacade.invalidEmailPasswordMessage(email, password)
        }
    }
}