package com.example.lilwiki

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.example.lilwiki.patterns.AuthFacade
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_discipline.*
import java.util.*


class DisciplineActivity : AppCompatActivity() {

    private val context = this
    private val tag = "DisciplineActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private val authFacade = AuthFacade(this, tag)
    private lateinit var autoUpdate : Timer
    private  var disciplineTitleList = mutableListOf<String>()
    val compStatus = CompletionStatus()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu!!.findItem(R.id.action_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    toSearchActivity(query)
                    return true
                }
            })
        return true
    }

    private fun toSearchActivity(query: String?) {
        val intent = Intent(context, SearchActivity::class.java)
        intent.putExtra("searchQuery", query)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (id == R.id.action_logout) {
            authFacade.signOutAccount()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun buildButton(title : String) : Button {
        val button = Button(this)
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.weight = 1F
        button.layoutParams = lp
        button.text = title
        button.setOnClickListener {
            if (title != "+")
                toBranchActivity(title)
            else
                toCreateArticleActivity()
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

    private fun toCreateArticleActivity() {
        val intent = Intent(this, SelectDisciplineActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discipline)
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
                    disciplineProgressBar.visibility = ProgressBar.INVISIBLE
                    autoUpdate.cancel()
                }
            }
        }, 0, 500)
    }

    private fun updateUI() {

        mainLinearLayout.removeAllViews()
        val n = disciplineTitleList.size - 1 - disciplineTitleList.size % 2
        for (i in 0..n step 2) {
            mainLinearLayout.addView(buildPairLayout(disciplineTitleList[i],
                disciplineTitleList[i + 1]))
        }

        if (n != disciplineTitleList.size - 1)
            mainLinearLayout.addView(buildPairLayout(disciplineTitleList[if (n == -1) 0 else n+1],
                "+"))
        else
            mainLinearLayout.addView(buildPairLayout("+", ""))
    }
}