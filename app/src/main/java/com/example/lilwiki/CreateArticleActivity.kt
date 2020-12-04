package com.example.lilwiki

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.lilwiki.patterns.CompletionStatus
import com.example.lilwiki.patterns.DatabaseAdapter
import kotlinx.android.synthetic.main.activity_create_article.*
import kotlinx.android.synthetic.main.activity_create_article.view.*
import kotlinx.android.synthetic.main.template_subsection_form.view.*
import java.util.*

class CreateArticleActivity : AppCompatActivity() {

    companion object {
        var disciplineTitle : String? = null
        var branchTitle : String? = null
        var articleTitle : String? = null
    }

    private val tag = "CreateArticleActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer
    private val context = this
    private var subsectionFormList = mutableListOf<View>()

    private var subsectionTitleListToRemove = mutableListOf<String>()
    private val subsectionTitleList = mutableListOf<String>()
    private val subsectionList = mutableListOf<ArticleActivity.Subsection>()
    private val subsTitleCompStatus = CompletionStatus()
    private val subsContentCompStatusList = mutableListOf<CompletionStatus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            getString(R.string.not_found)))

        disciplineTitle = intent.getStringExtra("disciplineTitle")
        branchTitle = intent.getStringExtra("branchTitle")
        articleTitle = intent.getStringExtra("articleTitle")

        if (articleTitle != null) {
            createArticleEditText.setText(articleTitle)

            dbAdapter.getSubsectionTitleList(subsectionTitleList, subsectionList,
                ArticleActivity.disciplineTitle.toString(), ArticleActivity.branchTitle.toString(),
                ArticleActivity.articleTitle.toString(), subsTitleCompStatus)
        }
        else {
            val subsForm = layoutInflater.inflate(
                R.layout.template_subsection_form,
                createArticleScrollView, false
            )
            subsectionFormList.add(subsForm)
            createArticleScrollView.createArticleScrollContainer.addView(subsForm)
        }
        floatingActionButton.setOnClickListener { appendSubsForm() }
        createArticleButton.setOnClickListener { saveArticle() }
    }

    private fun appendSubsForm() {
        val subsForm = layoutInflater.inflate(R.layout.template_subsection_form,
            createArticleScrollView, false)
        subsForm.buttonRemoveSubsForm.setOnClickListener {
            createArticleScrollView.createArticleScrollContainer.removeView(subsForm)
            subsectionFormList.remove(subsForm)
        }
        subsectionFormList.add(subsForm)
        createArticleScrollView.createArticleScrollContainer.addView(subsForm)
    }

    private fun toDisciplineActivity() {
        val intent = Intent(this, DisciplineActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (articleTitle != null) {
            createArticleProgressBar.visibility = ProgressBar.VISIBLE
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
                                runOnUiThread { fillContent() }
                                autoUpdate.cancel()
                            }
                        } else {
                            for (subsectionTitle in subsectionTitleList) {
                                subsContentCompStatusList.add(CompletionStatus())
                                dbAdapter.getSubsectionContent(
                                    subsectionList,
                                    ArticleActivity.disciplineTitle.toString(),
                                    ArticleActivity.branchTitle.toString(),
                                    ArticleActivity.articleTitle.toString(),
                                    subsectionTitle,
                                    subsContentCompStatusList.last()
                                )
                            }
                            isContentReadingStarted = true
                        }
                    }
                }
            }, 0, 500)
        }
    }

    private fun fillContent() {
        createArticleProgressBar.visibility = ProgressBar.INVISIBLE
        subsectionList.sortBy { it.order }
        for (subsection in subsectionList) {
            val subsForm = layoutInflater.inflate(R.layout.template_subsection_form,
                createArticleScrollView, false)
            subsForm.createArticleSubsTitleEdit.setText(subsection.title)
            subsForm.createArticleIsLatex.isChecked = subsection.content!!.isLatex
            subsForm.createArticleSubsText.setText(subsection.content!!.text)
            subsForm.buttonRemoveSubsForm.setOnClickListener {
                createArticleScrollView.createArticleScrollContainer.removeView(subsForm)
                subsectionFormList.remove(subsForm)
                subsectionTitleListToRemove.add(subsection.title)
            }

            subsectionFormList.add(subsForm)
            createArticleScrollView.createArticleScrollContainer.addView(subsForm)
        }
    }

    private fun saveArticle() {
        dbAdapter.setDiscipline(disciplineTitle!!)
        Log.i(tag, disciplineTitle!!);
        dbAdapter.setBranch(branchTitle!!)
        Log.i(tag, branchTitle!!);
        dbAdapter.setArticle(createArticleEditText.text.toString())
        Log.i(tag, createArticleEditText.text.toString());
        var index = 0
        val newSubsTitles = mutableListOf<String>()
        for (form in subsectionFormList) {
            newSubsTitles.add(form.createArticleSubsTitleEdit.text.toString())
            dbAdapter.setSubsection(form.createArticleSubsTitleEdit.text.toString(), index)
            dbAdapter.setSubsectionContent(form.createArticleSubsText.text.toString(),
                form.createArticleIsLatex.isChecked)
            dbAdapter.writeInfoToDB()
            index += 1
        }

        for (subsectionTitle in subsectionTitleList) {
            if (newSubsTitles.indexOf(subsectionTitle) == -1) {
                subsectionTitleListToRemove.add(subsectionTitle)
            }
        }
        for (subsectionToRemove in subsectionTitleListToRemove) {
            dbAdapter.removeSubsection(disciplineTitle!!, branchTitle!!,
                articleTitle!!, subsectionToRemove)
        }
        toDisciplineActivity()
    }

}