package com.example.lilwiki

import android.os.Bundle
import android.widget.SimpleExpandableListAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_branch.*

class BranchActivity : AppCompatActivity() {

    companion object {
        var disciplineTitle : String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branch)
        disciplineTitle = intent.getStringExtra("disciplineTitle")
        val expListViewContent = mutableListOf<MutableList<MutableMap<String, String>>>()
        var index = 0
        for (discipline in TempRep.disciplines) {
            if (discipline.title == disciplineTitle)
                break
            index += 1
        }
        val groupDataList: ArrayList<Map<String, String>> = ArrayList()
        for (branch in TempRep.disciplines[index].branches) {
            groupDataList.add(mapOf<String, String>("branchTitle" to branch.title))
            val articleList = mutableListOf<MutableMap<String, String>>()
            for (article in branch.articles)
                articleList.add(mutableMapOf("articleTitle" to article.title))
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
   }
}
