package com.example.lilwiki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.app.AlertDialog
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun buttonLeftHandler(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("left")
        alertDialogBuilder.setMessage("message")
        alertDialogBuilder.show()
    }
    
    fun buttonRightHandler(view: View) {
        val testIntent = Intent(this, SecondActivity::class.java)
        testIntent.putExtra(SecondActivity.testProperty, "prev")
        startActivity(testIntent)
    }
}