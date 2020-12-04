package com.example.lilwiki

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.agog.mathdisplay.MTMathView
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.template_subsection.view.*
import java.util.*

class ArticleActivity : AppCompatActivity() {

    companion object {
        var userName : String? = null
        var disciplineTitle : String? = null
        var branchTitle : String? = null
        var articleTitle : String? = null
    }

    class Subsection(paramTitle : String) {
        val title = paramTitle
        var order : Int? = null
        var content : DatabaseAdapter.SubsectionContent? = null

        fun setOrder(paramOrder : Int) : Boolean {
            return if (paramOrder >= 0) {
                order = paramOrder
                true
            } else
                false
        }

        fun setSubsectionContent(paramContent : DatabaseAdapter.SubsectionContent) {
            content = paramContent
        }


    }

    private val tag = "ArticleActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer
    private val subsectionTitleList = mutableListOf<String>()
    private val subsectionList = mutableListOf<Subsection>()
    private val subsTitleCompStatus = CompletionStatus()
    private val subsContentCompStatusList = mutableListOf<CompletionStatus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        userName = intent.getStringExtra("userName")
        disciplineTitle = intent.getStringExtra("disciplineTitle")
        branchTitle = intent.getStringExtra("branchTitle")
        articleTitle = intent.getStringExtra("articleTitle")

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))
        if (userName != null) {
            buttonRemove.isEnabled = false
            dbAdapter = DatabaseAdapter(userName)
        }
        dbAdapter.getSubsectionTitleList(subsectionTitleList, subsectionList,
            disciplineTitle.toString(), branchTitle.toString(), articleTitle.toString(),
            subsTitleCompStatus)
        buttonRemove.setOnClickListener { removeArticle() }
        buttonEdit.setOnClickListener { editArticle() }
    }

    private fun editArticle() {
        val intent = Intent(this, CreateArticleActivity::class.java)
        intent.putExtra("disciplineTitle", disciplineTitle)
        intent.putExtra("branchTitle", branchTitle)
        intent.putExtra("articleTitle", articleTitle)
        startActivity(intent)
    }

    private fun removeArticle() {
        dbAdapter.removeArticle(disciplineTitle.toString(), branchTitle.toString(),
            articleTitle.toString())
        val intent = Intent(this, DisciplineActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        var isContentReadingStarted = false
        autoUpdate = Timer()
        autoUpdate.schedule(object : TimerTask() {
            override fun run() {
                if (subsTitleCompStatus.toBoolean()) {
                    if (isContentReadingStarted) {
                        var conjugatedCompStatus = true
                        for (element in subsContentCompStatusList) {
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
                        for (subsectionTitle in subsectionTitleList) {
                            subsContentCompStatusList.add(CompletionStatus())
                            dbAdapter.getSubsectionContent(subsectionList,
                                disciplineTitle.toString(),
                                branchTitle.toString(),
                                articleTitle.toString(),
                                subsectionTitle,
                                subsContentCompStatusList.last())
                            }
                        isContentReadingStarted = true
                    }
                }
            }
        }, 0, 500)
    }

    private fun composeLayout() {
        if (userName != null) {
            buttonRemove.isEnabled = false
            buttonRemove.visibility = Button.INVISIBLE
            buttonEdit.isEnabled = false
            buttonEdit.visibility = Button.INVISIBLE
        }
        else {
            buttonRemove.isEnabled = true
            buttonRemove.visibility = Button.VISIBLE
            buttonEdit.isEnabled = true
            buttonEdit.visibility = Button.VISIBLE
        }

        articleProgressBar.visibility = ProgressBar.INVISIBLE

        titleView.text = articleTitle
        subsectionList.sortBy { it.order }
        var index = 0
        for (subsection in subsectionList) {
            val subTitleView = layoutInflater.inflate(R.layout.template_text,
                contentsView, false) as TextView
            val stringContent = "${index + 1}) " + subsection.title
            subTitleView.text = stringContent
            contentsView.addView(subTitleView)
            // TODO roots
            val subView = layoutInflater.inflate(R.layout.template_subsection,
                null) as LinearLayout
            subView.subsectionTitle.text = stringContent
            index += 1
            if (subsection.content?.isLatex!!) {
                val latexView = layoutInflater.inflate(R.layout.template_latex,
                    null) as MTMathView
                latexView.latex = subsection.content!!.text
                latexView.fontSize = MTMathView.convertDpToPixel(25.0F)
                subView.subsectionContent.addView(latexView)
            }
            else {
                val textView = layoutInflater.inflate(R.layout.template_text,
                    null) as TextView
                textView.text = subsection.content!!.text
                subView.subsectionContent.addView(textView)
            }
            subsectionsView.addView(subView)
        }
    }
}