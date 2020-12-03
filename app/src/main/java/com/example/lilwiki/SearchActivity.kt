package com.example.lilwiki

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_branch.*
import kotlinx.android.synthetic.main.activity_discipline.*
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*

class SearchActivity : AppCompatActivity() {

    private lateinit var dbAdapter : DatabaseAdapter
    private val context = this
    private lateinit var autoUpdate : Timer
    private  var pathList = mutableListOf<FullArticlePath>()
    val compStatus = CompletionStatus()

    companion object {
        var searchQuery : String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))
        searchQuery = intent.getStringExtra("searchQuery")

        dbAdapter.getSelectedArticles(pathList, compStatus, searchQuery.toString())

        setContentView(R.layout.activity_search)
    }

    override fun onResume() {
        super.onResume()
        autoUpdate = Timer()
        autoUpdate.schedule(object : TimerTask() {
            override fun run() {
                if (compStatus.toBoolean()) {
                    runOnUiThread { updateUI() }
                    searchProgressBar.visibility = ProgressBar.INVISIBLE
                    autoUpdate.cancel()
                }
            }
        }, 0, 500)
    }

    private fun updateUI() {
        val listViewContent = ArrayList<String>()
        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)
        val searchMode = sharedPrefs
            .getString(getString(R.string.preferences_search_mode),
                "NOT FOUND").toString()
        val currentUserName = sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND").toString()
        for (path in pathList) {
            val user = path.user!!.replace('\\', '.')
            if (searchMode != "private" || user == currentUserName) {
                listViewContent.add("${user}->${path.discipline}->${path.branch}->${path.article}")
                Log.d("path", "${user}->${path.discipline}->${path.branch}->${path.article}")
            }
        }

        Log.d("path",listViewContent.size.toString())
        //listViewContent = arrayListOf("a", "b")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listViewContent
        )
        val listView = findViewById<ListView>(R.id.selectedArticles)
        listView.adapter = adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val user = pathList[position].user!!.replace('\\', '.')
                toArticleActivity(user,
                    pathList[position].discipline,
                    pathList[position].branch,
                    pathList[position].article)
            }
        /*listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _
                toArticleActivity(pathList[position].user,
                    pathList[position].discipline,
                    pathList[position].branch,
                    pathList[position].article)
            }*/
    }
    private fun toArticleActivity(userName : String?, disciplineTitle : String?,
                                  branchTitle : String?, articleTitle : String?) {
        val intent = Intent(this, ArticleActivity::class.java)
        intent.putExtra("userName", userName)
        intent.putExtra("disciplineTitle", disciplineTitle)
        intent.putExtra("branchTitle", branchTitle)
        intent.putExtra("articleTitle", articleTitle)
        startActivity(intent)
    }
}