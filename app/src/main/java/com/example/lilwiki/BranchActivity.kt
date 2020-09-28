package com.example.lilwiki

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class BranchActivity : AppCompatActivity() {

    companion object {
        const val testProperty = "none"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branch)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("title")
        alertDialogBuilder.setMessage(testProperty)
        alertDialogBuilder.show()
    }
}