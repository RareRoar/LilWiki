package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_select_branch.*
import kotlinx.android.synthetic.main.activity_select_discipline.*
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
                    branchProgressBar.visibility = ProgressBar.INVISIBLE
                    autoUpdate.cancel()
                }
            }
        }, 0, 500)
    }

    private fun updateUI() {
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

                // set item text style and font
                view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)

                // set spinner item padding
                view.setPadding(
                    25.toDp(context), // left
                    10.toDp(context), // top
                    50.toDp(context), // right
                    10.toDp(context) // bottom
                )

                // alternate item style
                if (position %2 == 1){
                    view.background = ColorDrawable(Color.parseColor("#F0FFF0"))
                }else{
                    view.background = ColorDrawable(Color.parseColor("#FFFFF0"))
                }

                return view
            }
        }

        // finally, data bind spinner with adapter
        branchSpinner.adapter = adapter


        // spinner on item selected listener
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

    fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
    ).toInt()
}