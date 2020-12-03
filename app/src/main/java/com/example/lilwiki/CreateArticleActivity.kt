package com.example.lilwiki

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_article.*
import kotlinx.android.synthetic.main.activity_create_article.view.*
import kotlinx.android.synthetic.main.template_subsection_form.view.*
import java.util.*

class CreateArticleActivity : AppCompatActivity() {

    companion object {
        var disciplineTitle : String? = null
        var branchTitle : String? = null
    }

    private val tag = "CreateArticleActivity"
    private lateinit var dbAdapter : DatabaseAdapter
    private lateinit var autoUpdate : Timer
    private val context = this
    private var subsectionFormList = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_storage_name),
            Context.MODE_PRIVATE)

        dbAdapter = DatabaseAdapter(sharedPrefs.getString(getString(R.string.preferences_key_email),
            "NOT FOUND"))


        disciplineTitle = intent.getStringExtra("disciplineTitle")
        branchTitle = intent.getStringExtra("branchTitle")

        val subsForm = layoutInflater.inflate(R.layout.template_subsection_form, createArticleScrollView, false)
        subsectionFormList.add(subsForm)
        createArticleScrollView.createArticleScrollContainer.addView(subsForm)

        floatingActionButton.setOnClickListener { appendSubsForm() }
        createArticleButton.setOnClickListener { saveArticle() }
    }

    private fun appendSubsForm() {
        val subsForm = layoutInflater.inflate(R.layout.template_subsection_form, createArticleScrollView, false)
        subsectionFormList.add(subsForm)
        createArticleScrollView.createArticleScrollContainer.addView(subsForm)
    }

    private fun toDisciplineActivity() {
        val intent = Intent(this, DisciplineActivity::class.java)
        startActivity(intent)
    }

    private fun saveArticle() {

        //Toast.makeText(this, disciplineTitle.toString() + branchTitle.toString() + createArticleEditText.text.toString(), Toast.LENGTH_SHORT).show()
        dbAdapter.setDiscipline(disciplineTitle!!)
        Log.i(tag, disciplineTitle!!);
        dbAdapter.setBranch(branchTitle!!)
        Log.i(tag, branchTitle!!);
        dbAdapter.setArticle(createArticleEditText.text.toString())
        Log.i(tag, createArticleEditText.text.toString());
        var index = 0
        for (form in subsectionFormList) {
            dbAdapter.setSubsection(form.createArticleSubsTitleEdit.text.toString(), index)
            dbAdapter.setSubsectionContent(form.createArticleSubsText.text.toString(),
                form.createArticleIsLatex.isChecked)
            dbAdapter.writeInfoToDB()
            index += 1
        }
    }

}