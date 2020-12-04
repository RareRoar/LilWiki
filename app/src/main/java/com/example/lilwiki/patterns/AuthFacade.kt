package com.example.lilwiki.patterns


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.agog.mathdisplay.render.getSansSerif
import com.example.lilwiki.R
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.lang.StringBuilder

class AuthFacade(activityParam : AppCompatActivity, tagParam : String) {

    private var auth: FirebaseAuth = Firebase.auth
    private var tag : String = tagParam
    private val activity = activityParam
    private val databaseRef = FirebaseDatabase.getInstance().reference

    public fun appendUserList(email: String) {
        databaseRef.child("userList").orderByKey().
        addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var index = 0
                var isUpdate = false
                var bufferString: String? = dataSnapshot.child(index.toString()).getValue<String>()
                while (bufferString != null) {
                    if (bufferString == email) {
                        isUpdate = true
                        break
                    }
                    index += 1
                    bufferString = dataSnapshot.child(index.toString()).getValue<String>()
                }
                databaseRef.child("userList")
                    .child(index.toString()).setValue(email.replace('.', '\\'))
                Log.i(tag, "Successfully written username.")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(tag, "userList.append failed.", databaseError.toException())
            }
        })
    }

    public fun validateEmail(email: String) : Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    public fun validatePassword(password: String) : Boolean {
        return password.length > 5
    }

    public fun signOutAccount() {
        auth.signOut()
        Log.d(tag, "signOut")
        val sharedPref = activity.getSharedPreferences(activity.getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(activity.getString(R.string.preferences_key_email),
                "NOT FOUND")
            putString(activity.getString(R.string.preferences_key_password),
                "NOT FOUND")
            apply()
        }
    }

    public fun signInAccount(email : String, password : String) {
        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity)
            { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "signInWithEmail:success")
                    val user = auth.currentUser
                    // ...
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(tag, "signInWithEmail:failure", task.exception)
                    Toast.makeText(activity.baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    // ...
                }
                // ...
            }
        } catch (emailException: FirebaseAuthInvalidUserException) {
            Log.w(tag, "FirebaseAuthInvalidUserException has been thrown")
            Toast.makeText(activity,
                "Login failed: email does not exist!", Toast.LENGTH_SHORT).show()
        } catch (passwordException: FirebaseAuthInvalidCredentialsException) {
            Log.w(tag, "FirebaseAuthInvalidCredentialsException has been thrown")
            Toast.makeText(activity,
                "Login failed: wrong password!", Toast.LENGTH_SHORT).show()
        }

    }
    public fun createAccount(email : String, password : String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity)
            { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "createUserWithEmail:success")
                    val user = auth.currentUser
                    appendUserList(email)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(tag, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        activity.baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // ...
                }
                // ...
            }
        } catch (emailCollisionException : FirebaseAuthUserCollisionException) {
            Log.w(tag, "FirebaseAuthUserCollisionException has been thrown")
        } catch (weakPasswordException : FirebaseAuthWeakPasswordException) {
            Log.w(tag, "FirebaseAuthWeakPasswordException has been thrown")
        } catch (emailMalformedException : FirebaseAuthInvalidCredentialsException) {
            Log.w(tag, "FirebaseAuthInvalidCredentialsException has been thrown")
        }
    }

    public fun invalidEmailPasswordMessage(email: String, password: String) {
        val message = StringBuilder("")
        if (!validateEmail(email))
            message.append("Irrelevant email format!")
        if (!validatePassword(password)) {
            if (message.isNotEmpty())
                message.append("\n")
            message.append("Password must include minimum 5 characters!")
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }


    public fun saveUserInfo(email: String, password: String) {
        val sharedPref = activity.getSharedPreferences(
            activity.getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(activity.getString(R.string.preferences_key_email),
                email)
            putString(activity.getString(R.string.preferences_key_password),
                password)
            apply()
        }
    }
}