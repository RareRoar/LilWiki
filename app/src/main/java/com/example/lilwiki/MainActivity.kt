package com.example.lilwiki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.app.AlertDialog
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mathview.latex = "x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a}"
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