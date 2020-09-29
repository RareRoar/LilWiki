package com.example.lilwiki

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_discipline.*


class DisciplineActivity : AppCompatActivity() {

    private fun buildButton(title : String) : Button {
        val button = Button(this)
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.weight = 1F
        button.layoutParams = lp
        button.text = title
        button.setOnClickListener {
            toBranchActivity(if (title != "+") title else null)
        }
        return button
    }

    private fun buildPairLayout(title1 : String, title2 : String) : LinearLayout {
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.addView(buildButton(title1))
        if (title2 != "")
            linearLayout.addView(buildButton(title2))
        return linearLayout
    }

    private fun toBranchActivity(disciplineTitle : String?) {
        val intent = Intent(this, BranchActivity::class.java)
        intent.putExtra("disciplineTitle", disciplineTitle)
        startActivity(intent)
    }

    private fun addDiscipline() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discipline)

        val n = TempRep.disciplines.size - 1 - TempRep.disciplines.size % 2
        for (i in 0..n step 2) {
            mainLinearLayout.addView(buildPairLayout(TempRep.disciplines[i].title, TempRep.disciplines[i + 1].title))
        }
        if (n != TempRep.disciplines.size - 1)
            mainLinearLayout.addView(buildPairLayout(TempRep.disciplines[n].title, "+"))
        else
            mainLinearLayout.addView(buildPairLayout("+", ""))

    }
}