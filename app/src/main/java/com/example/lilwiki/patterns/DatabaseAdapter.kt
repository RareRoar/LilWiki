package com.example.lilwiki.patterns

import android.util.Log
import com.example.lilwiki.ArticleActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.lang.NullPointerException


class DatabaseAdapter(userEmail: String?) {
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private var username : String
    private val tag = "DatabaseAdapter"

    companion object Keys {
        const val root = "users"
        const val subsectionOrderKey = "subsectionOrder"
        const val contentKey = "content"
        const val contentOrderKey = "contentOrder"
        const val textKey = "text"
        const val isLatexKey = "isLatex"
    }

    init {
        if (userEmail == null) {
            throw IllegalArgumentException("userEmail is null!")
        }
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            username = userEmail.replace('.', '\\')
        }
        else
            throw IllegalArgumentException("Irrelevant email format!")
    }

    private var disciplineTitle : String? = null
    private var branchTitle : String? = null
    private var articleTitle : String? = null
    private var subsectionTitle : String? = null
    private var subsectionOrder : Int? = null

    data class SubsectionContent(
        var text: String,
        var isLatex: Boolean
    )
    var subsectionContent : SubsectionContent? = null

    public fun setDiscipline(disciplineTitle: String) {
        if (validateString(disciplineTitle))
            this.disciplineTitle = disciplineTitle
        else
            throw IllegalArgumentException("Irrelevant discipline title!")
    }

    public fun setBranch(branchTitle: String) {
        if (validateString(branchTitle))
            this.branchTitle = branchTitle
        else
            throw IllegalArgumentException("Irrelevant branch title!")
    }

    public fun setArticle(articleTitle: String) {
        if (validateString(articleTitle))
            this.articleTitle = articleTitle
        else
            throw IllegalArgumentException("Irrelevant article title!")
    }

    public fun setSubsection(subsectionTitle: String, subsectionOrder: Int = 0) {
        if (validateString(subsectionTitle) && validateInt(subsectionOrder)) {
            this.subsectionTitle = subsectionTitle
            this.subsectionOrder = subsectionOrder
        }
        else
            throw IllegalArgumentException("Irrelevant subsection info!")
    }

    public fun setSubsectionContent(text: String, isLatex: Boolean) {
        subsectionContent = SubsectionContent(text, isLatex)
    }

    private fun validateInt(num: Int?) : Boolean {
        if (num == null)
            return false
        return num >= 0
    }

    private fun validateString(str: String?) : Boolean {
        if (str == null)
            return false
        val indexList = listOf<Int>(
            str.indexOf('.'),
            str.indexOf(','),
            str.indexOf('$'),
            str.indexOf('/'),
            str.indexOf('#')
        )
        return indexList.sum() != -6

    }

    public fun validateData() : Boolean {
        return validateString(disciplineTitle) &&
                validateString(branchTitle) &&
                validateString(articleTitle) &&
                validateString(subsectionTitle) &&
                validateInt(subsectionOrder)
    }

    public fun emptyAdapter() {
        disciplineTitle = null
        branchTitle = null
        articleTitle = null
        subsectionTitle = null
        subsectionOrder = null
        subsectionContent = null
    }

    private fun composeMapOfSubsectionContent(subContent: SubsectionContent) : Map<String, Any?> {
        return mapOf<String, Any?>(
            textKey to subContent.text,
            isLatexKey to subContent.isLatex
        )
    }

    private fun composeMapOfSubsection() : Map<String, Any?> {
        if (validateInt(subsectionOrder) && subsectionContent != null) {
            val mappedContent = composeMapOfSubsectionContent(subsectionContent!!)
            return mapOf<String, Any?>(
                subsectionOrderKey to subsectionOrder,
                contentKey to mappedContent
            )
        }
        else {
            Log.w(tag, "Cannot compose map of subsection.")
            throw IllegalArgumentException("Cannot compose map of subsection.")
        }
    }

    public fun removeSubsection(disciplineTitle: String, branchTitle: String,
                             articleTitle: String, subsectionTitle: String) {
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).child(articleTitle).
        child(subsectionTitle).removeValue()
    }

    public fun writeInfoToDB() {
        if (!validateData()) {
            Log.e(tag, "Attempt to write invalid data into Firebase RT DB")
            throw IllegalArgumentException("Attempt to write invalid data into Firebase RT DB")
        }
        val childUpdates = hashMapOf<String, Any>(
            "$root/${username}/${disciplineTitle}/${branchTitle}/" +
                    "${articleTitle}/${subsectionTitle}" to composeMapOfSubsection()
        )
        databaseRef.updateChildren(childUpdates).
        addOnSuccessListener {
            Log.i(tag, "A subsection has been written to Firebase RT DB successfully.")
        }.
        addOnFailureListener {
            Log.w(tag, "A subsection writing to Firebase RT DB has failed.")
        }
        Log.i(tag, "Writing info to Firebase RT DB completed.")
    }

    public fun getDisciplineTitleList(discTitleList : MutableList<String>,
                                      completionStatus : CompletionStatus
    ) {

        Log.i(tag, "Reading begins.")
        completionStatus.setFalse()
        discTitleList.clear()
        databaseRef.child("users").child(username).
        addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (discipline in snapshot.children) {
                        discTitleList.add(discipline.key.toString())
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get branch title list.")
                }
            })
    }

    public fun getBranchTitleList(branchTitleList : MutableList<String>, disciplineTitle: String,
                                  completionStatus : CompletionStatus
    ) {
        completionStatus.setFalse()
        branchTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).
        addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (branch in snapshot.children) {
                        branchTitleList.add(branch.key.toString())
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get branch title list.")
                }
            })
    }

    public fun getArticleTitleList(articleTitleList : MutableList<String>, disciplineTitle: String,
    branchTitle: String, completionStatus : CompletionStatus
    ) {
        completionStatus.setFalse()
        articleTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).
                addListenerForSingleValueEvent(
                    object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (article in snapshot.children) {
                            articleTitleList.add(article.key.toString())
                        }
                        completionStatus.setTrue()
                        Log.i(tag, "Reading complete.")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(tag, "Cannot get branch title list.")
                    }
                })
    }

    public fun getSubsectionTitleList(subsectionTitleList : MutableList<String>,
                                      subsectionList : MutableList<ArticleActivity.Subsection>,
                                      disciplineTitle: String, branchTitle: String,
                                      articleTitle: String, completionStatus : CompletionStatus
    ) {
        completionStatus.setFalse()
        subsectionTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).
        child(articleTitle).
        addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (subsection in snapshot.children) {
                        subsectionTitleList.add(subsection.key.toString())
                        subsectionList.add(ArticleActivity.Subsection(subsection.key.toString()))
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get subsection title list.")
                }
            }
        )
    }

    public fun getSubsectionContent(subsectionList : MutableList<ArticleActivity.Subsection>,
                                    disciplineTitle: String, branchTitle: String,
                                    articleTitle: String, subsectionTitle : String,
                                    completionStatus: CompletionStatus
    ) {
        completionStatus.setFalse()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).child(articleTitle).child(subsectionTitle).
                addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val currentSubsection = subsectionList.
                            find { it.title == subsectionTitle }
                            currentSubsection?.order =
                                snapshot.child("subsectionOrder")
                                    .getValue<Int>()
                            val dbText =
                                snapshot.child("content").child("text")
                                    .getValue<String>()
                            val dbIsLatex =
                                snapshot.child("content").child("isLatex")
                                    .getValue<Boolean>()
                            if (dbText != null &&
                                    dbIsLatex != null)
                                currentSubsection?.content = SubsectionContent(dbText, dbIsLatex)
                            else
                                throw NullPointerException("RT DB child is null.")
                            completionStatus.setTrue()
                            Log.i(tag, "Reading complete.")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(tag, "Cannot get subsection content list.")
                        }
                    }
                )
    }

    public fun getSelectedArticles(pathList : MutableList<FullArticlePath>,
                                   completionStatus : CompletionStatus, query : String) {

        Log.i(tag, "Reading begins.")
        completionStatus.setFalse()
        pathList.clear()
        databaseRef.child("users").
        addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(tag, "Reading continues.")
                    var user = ""
                    var discipline = ""
                    var branch = ""
                    var article = ""
                    for (userSnapshot in snapshot.children) {
                        user = userSnapshot.key.toString()
                        Log.d("user", user)
                        for (disciplineSnapshot in userSnapshot.children) {
                            discipline = disciplineSnapshot.key.toString()
                            for (branchSnapshot in disciplineSnapshot.children) {
                                branch = branchSnapshot.key.toString()
                                for (articleSnapshot in branchSnapshot.children) {
                                    if (articleSnapshot.key.toString()
                                            .contains(query, true)) {
                                        article = articleSnapshot.key.toString()
                                        val fullPath = FullArticlePath(
                                            user, discipline,
                                            branch, article
                                        )
                                        if (fullPath.validate()) {
                                            pathList.add(fullPath)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot select articles.")
                }
            }
        )
    }

    public fun removeArticle(disciplineTitle: String, branchTitle: String,
    articleTitle: String) {
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).child(articleTitle).removeValue()
    }
}

public class FullArticlePath(userParam : String, disciplineParam : String,
                             branchParam : String, articleParam : String) {

    var user : String? = null
    var discipline : String? = null
    var branch : String? = null
    var article : String? = null

    init {
        user = userParam
        discipline = disciplineParam
        branch = branchParam
        article = articleParam

    }

    public fun validate() : Boolean {
        return (user != null && user != "") &&
                (discipline != null && discipline != "") &&
                (branch != null && branch != "") &&
                (article != null && article != "")

    }

}

public class CompletionStatus {
    private var instance : Boolean = false

    public fun toBoolean() : Boolean {
        return instance
    }

    public fun setTrue() {
        instance = true
    }

    public fun setFalse() {
        instance = false
    }
}