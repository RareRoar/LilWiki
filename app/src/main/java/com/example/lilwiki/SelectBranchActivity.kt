package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_select_branch.*
import java.util.*


class SelectBranchActivity : AppCompatActivity() {

    companion object {
        var disciplineTitle : String? = null
    }

    private val tag = "SelectBranchActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer
    private  var branchTitleList = mutableListOf<String>()
    val compStatus = CompletionStatus()
    val context = this

    private var chosenBranch : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_branch)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))

        disciplineTitle = intent.getStringExtra("disciplineTitle")

        dbAdapter.getBranchTitleList(branchTitleList, disciplineTitle!!, compStatus)

        chosenDisciplineText.text = disciplineTitle
    }

    override fun onResume() {
        super.onResume()
        autoUpdate = Timer()
        autoUpdate.schedule(object : TimerTask() {
            override fun run() {
                if (compStatus.toBoolean()) {
                    runOnUiThread { updateUI() }
                    selectBranchProgressBar.visibility = ProgressBar.INVISIBLE
                    autoUpdate.cancel()
                }
            }
        }, 0, 500)
    }

    private fun updateUI() {

        chosenDisciplineText.visibility = TextView.VISIBLE
        branchSpinner.visibility = Spinner.VISIBLE
        buttonBranchNext.visibility = Button.VISIBLE
        newBranchEdit.visibility = EditText.VISIBLE

        branchTitleList.add("#")
        val adapter: ArrayAdapter<String> = object: ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            branchTitleList
        ){
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(
                    position,
                    convertView,
                    parent
                ) as TextView
                view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                view.setPadding(
                    25.toDp(context), // left
                    10.toDp(context), // top
                    50.toDp(context), // right
                    10.toDp(context) // bottom
                )
                return view
            }
        }
        branchSpinner.adapter = adapter

        branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                newBranchEdit.isEnabled = false
                val selectedItemText = parent.getItemAtPosition(position).toString()
                if (selectedItemText != "#")
                    chosenBranch = selectedItemText
                else
                    newBranchEdit.isEnabled = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }

        }
        buttonBranchNext.setOnClickListener {
            if (newBranchEdit.isEnabled) {
                if (newBranchEdit.text.toString() != "") {
                    Log.i(tag, "Writing " + newBranchEdit.text.toString())
                    toCreateArticleActivity(disciplineTitle, newBranchEdit.text.toString())
                }
                else {
                    Toast.makeText(this,
                        "Fill a new branch name.", Toast.LENGTH_SHORT).show()
                }
            }
            else
                toCreateArticleActivity(disciplineTitle, chosenBranch)
        }
    }

    private fun toCreateArticleActivity(disciplineTitle: String?, branchTitle : String?) {
        val intent = Intent(this, CreateArticleActivity::class.java)
        intent.putExtra("disciplineTitle", disciplineTitle)
        intent.putExtra("branchTitle", branchTitle)
        startActivity(intent)
    }

    private fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
    ).toInt()
}