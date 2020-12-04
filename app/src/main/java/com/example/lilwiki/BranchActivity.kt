package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_branch.*
import java.util.*
import kotlin.collections.ArrayList

class BranchActivity : AppCompatActivity() {

    private val tag = "BranchActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer

    val branchCompStatus = CompletionStatus()
    val articleCompStatusList = arrayListOf<CompletionStatus>()

    companion object {
        var disciplineTitle : String? = null
    }

    private fun toArticleActivity(disciplineTitle : String?, branchTitle : String?, articleTitle : String?) {
        val intent = Intent(this, ArticleActivity::class.java)
        intent.putExtra("disciplineTitle", disciplineTitle)
        intent.putExtra("branchTitle", branchTitle)
        intent.putExtra("articleTitle", articleTitle)
        startActivity(intent)
    }

    private lateinit var branchTitleList : MutableList<String>
    private lateinit var articleTitleMap : MutableMap<String, MutableList<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branch)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))

        branchTitleList = mutableListOf()
        articleTitleMap = mutableMapOf()
        disciplineTitle = intent.getStringExtra("disciplineTitle")

        dbAdapter.getBranchTitleList(branchTitleList, disciplineTitle.toString(), branchCompStatus)

    }

    override fun onResume() {
        super.onResume()
        var isArticleReadingStarted = false
        autoUpdate = Timer()
        autoUpdate.schedule(object : TimerTask() {
            override fun run() {
                if (branchCompStatus.toBoolean()) {
                    if (isArticleReadingStarted) {
                        var conjugatedCompStatus = true
                        for (element in articleCompStatusList) {
                            conjugatedCompStatus = conjugatedCompStatus && element.toBoolean()
                            if (!conjugatedCompStatus)
                                break
                        }
                        if (conjugatedCompStatus) {
                            runOnUiThread { composeLayout() }
                            autoUpdate.cancel()
                        }
                    }
                    else {
                        for (branchTitle in branchTitleList) {
                            articleCompStatusList.add(CompletionStatus())
                            articleTitleMap[branchTitle] = mutableListOf()
                            articleTitleMap[branchTitle]?.let {
                                dbAdapter.getArticleTitleList(
                                    it, disciplineTitle.toString(),
                                    branchTitle, articleCompStatusList.last())
                            }
                        }
                        isArticleReadingStarted = true
                    }
                }
            }
        }, 0, 500)
    }

    private fun composeLayout() {
        branchProgressBar.visibility = ProgressBar.INVISIBLE
        branchTextView.text = disciplineTitle
        val expListViewContent = mutableListOf<MutableList<MutableMap<String, String>>>()
        val groupDataList: ArrayList<Map<String, String>> = ArrayList()
        for (branchTitle in branchTitleList) {
            groupDataList.add(mapOf("branchTitle" to branchTitle))
            val articleList = mutableListOf<MutableMap<String, String>>()
            for (articleTitle in articleTitleMap[branchTitle]!!)
                articleList.add(mutableMapOf("articleTitle" to articleTitle))
            expListViewContent.add(articleList)
        }
        val adapter = SimpleExpandableListAdapter(
            this,
            groupDataList,
            android.R.layout.simple_expandable_list_item_1,
            arrayOf("branchTitle"),
            intArrayOf(android.R.id.text1),
            expListViewContent,
            android.R.layout.simple_list_item_1,
            arrayOf("articleTitle"),
            intArrayOf(android.R.id.text1)
        )
        expendableLV.setAdapter(adapter)
        expendableLV.setOnChildClickListener { _: ExpandableListView,
                                               _: View,
                                               groupPosition: Int,
                                               childPosition: Int,
                                               _: Long ->
            toArticleActivity(disciplineTitle, groupDataList[groupPosition]["branchTitle"],
                expListViewContent[groupPosition][childPosition]["articleTitle"])
            false
        }
    }
}
