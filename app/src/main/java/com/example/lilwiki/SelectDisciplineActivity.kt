package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_select_discipline.*
import java.util.*

class SelectDisciplineActivity : AppCompatActivity() {

    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer
    private  var disciplineTitleList = mutableListOf<String>()
    private val compStatus = CompletionStatus()
    private val context = this
    private var chosenDiscipline : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_discipline)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))

        dbAdapter.getDisciplineTitleList(disciplineTitleList, compStatus)
    }

    override fun onResume() {
        super.onResume()
        autoUpdate = Timer()
        autoUpdate.schedule(object : TimerTask() {
            override fun run() {
                if (compStatus.toBoolean()) {
                    runOnUiThread { updateUI() }
                    selectDisciplineProgressBar.visibility = ProgressBar.INVISIBLE
                    autoUpdate.cancel()
                }
            }
        }, 0, 500)
    }

    private fun updateUI() {

        disciplineSpinner.visibility = Spinner.VISIBLE
        buttonDiscNext.visibility = Button.VISIBLE
        newDisciplineEdit.visibility = EditText.VISIBLE

        disciplineTitleList.add("#")
        val adapter: ArrayAdapter<String> = object: ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            disciplineTitleList
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
        disciplineSpinner.adapter = adapter
        disciplineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                newDisciplineEdit.isEnabled = false
                val selectedItemText = parent.getItemAtPosition(position).toString()
                if (selectedItemText != "#")
                    chosenDiscipline = selectedItemText
                else
                    newDisciplineEdit.isEnabled = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }

        }
        buttonDiscNext.setOnClickListener {
            if (newDisciplineEdit.isEnabled) {
                if (newDisciplineEdit.text.toString() != "") {
                    toSelectBranchActivity(newDisciplineEdit.text.toString())
                }
                else {
                    Toast.makeText(this,
                        "Fill a new discipline name.", Toast.LENGTH_SHORT).show()
                }
            }
            else
                toSelectBranchActivity(chosenDiscipline)
        }
    }

    private fun toSelectBranchActivity(disciplineTitle: String?) {
        val intent = Intent(this, SelectBranchActivity::class.java)
        intent.putExtra("disciplineTitle", disciplineTitle)
        startActivity(intent)
    }

    private fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
    ).toInt()
}