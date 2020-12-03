package com.example.lilwiki

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.lang.NullPointerException
import java.util.concurrent.Semaphore


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

    /*
    private fun validateContenList() : Boolean {
        var contentListValidation = true
        if (subsectionContentList != null) {
            for (element in subsectionContentList!!) {
                contentListValidation = contentListValidation && validateInt(element.order)
            }
        } else {
            return false
        }
        return contentListValidation
    } */

    public fun validateData() : Boolean {
        return validateString(disciplineTitle) &&
                validateString(branchTitle) &&
                validateString(articleTitle) &&
                validateString(subsectionTitle) &&
                validateInt(subsectionOrder) /*&&
                validateContenList()*/
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
            //Keys.contentOrderKey to subContent.order,
            Keys.textKey to subContent.text,
            Keys.isLatexKey to subContent.isLatex
        )
    }

    private fun composeMapOfSubsection() : Map<String, Any?> {
        if (validateInt(subsectionOrder) && subsectionContent != null) {
            val mappedContent = composeMapOfSubsectionContent(subsectionContent!!)
            return mapOf<String, Any?>(
                Keys.subsectionOrderKey to subsectionOrder,
                Keys.contentKey to mappedContent
            )
        }
        else {
            Log.w(tag, "Cannot compose map of subsection.")
            throw IllegalArgumentException("Cannot compose map of subsection.")
        }
    }

    private fun appendDisciplineList() {
        databaseRef.child("users").child(username).child("disciplineList").orderByKey().
        addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var index = 0
                var isUpdate = false
                var bufferString: String? = dataSnapshot.child(index.toString()).getValue<String>()
                while (bufferString != null) {
                    if (bufferString == disciplineTitle) {
                        isUpdate = true
                        break
                    }
                    index += 1
                    bufferString = dataSnapshot.child(index.toString()).getValue<String>()
                }
                databaseRef.child("users").child(username).child("disciplineList")
                    .child(index.toString()).setValue(disciplineTitle)
                Log.i(tag, "Successfully written discipline title.")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(tag, "disciplineList.append failed.", databaseError.toException())
            }
        })
    }

    private fun appendBranchList() {
        databaseRef.child("users").child(username).child(disciplineTitle!!).
        child("branchList").orderByKey().
        addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var index = 0
                var isUpdate = false
                var bufferString: String? = dataSnapshot.child(index.toString()).getValue<String>()
                while (bufferString != null) {
                    if (bufferString == branchTitle) {
                        isUpdate = true
                        break
                    }
                    index += 1
                    bufferString = dataSnapshot.child(index.toString()).getValue<String>()
                }
                databaseRef.child("users").child(username).child(disciplineTitle!!).
                child("branchList").child(index.toString()).setValue(branchTitle)
                Log.i(tag, "Successfully written branch title.")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(tag, "branchList.append failed.", databaseError.toException())
            }
        })
    }

    private fun appendArticleList() {
        databaseRef.child("users").child(username).child(disciplineTitle!!).
        child(branchTitle!!).child("articleList").orderByKey().
        addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var index = 0
                var isUpdate = false
                var bufferString: String? = dataSnapshot.child(index.toString()).getValue<String>()
                while (bufferString != null) {
                    if (bufferString == articleTitle) {
                        isUpdate = true
                        break
                    }
                    index += 1
                    bufferString = dataSnapshot.child(index.toString()).getValue<String>()
                }
                databaseRef.child("users").child(username).child(disciplineTitle!!)
                    .child(branchTitle!!).child("articleList")
                    .child(index.toString()).setValue(articleTitle)
                Log.i(tag, "Successfully written article title.")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(tag, "articleList.append failed.", databaseError.toException())
            }
        })
    }

    public fun writeInfoToDB() {
        if (!validateData()) {
            Log.e(tag, "Attempt to write invalid data into Firebase RT DB")
            throw IllegalArgumentException("Attempt to write invalid data into Firebase RT DB")
        }
        val childUpdates = hashMapOf<String, Any>(
            "${Keys.root}/${username}/${disciplineTitle}/${branchTitle}/" +
                    "${articleTitle}/${subsectionTitle}" to composeMapOfSubsection()
        )
        databaseRef.updateChildren(childUpdates).
        addOnSuccessListener {
            Log.i(tag, "A subsection has been written to Firebase RT DB successfully.")
        }.
        addOnFailureListener {
            Log.w(tag, "A subsection writing to Firebase RT DB has failed.")
        }

        databaseRef.child("users").child(username).child(disciplineTitle!!).
        child(branchTitle!!).child(articleTitle!!).child("subsectionList").orderByKey().
        addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                databaseRef.child("users").child(username).child(disciplineTitle!!)
                    .child(branchTitle!!).child(articleTitle!!).child("subsectionList").
                    child(dataSnapshot.childrenCount.toString()).setValue(subsectionTitle)
                Log.i(tag, "Successfully written subsection title.")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(tag, "subsectionList.append failed.", databaseError.toException())
            }
        })
        appendDisciplineList()
        appendBranchList()
        appendArticleList()
        Log.i(tag, "Writing info to Firebase RT DB completed.")
    }

    public fun getDisciplineTitleList(discTitleList : MutableList<String>,
                                      completionStatus : CompletionStatus) {

        Log.i(tag, "Reading begins.")
        completionStatus.setFalse()
        discTitleList.clear()
        databaseRef.child("users").child(username).
        child("disciplineList").
        addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(tag, "Reading continues.")
                    var index = 0
                    var bufferString: String? = snapshot.child(index.toString()).getValue<String>()
                    while (bufferString != null) {
                        discTitleList.add(bufferString.toString())
                        index += 1
                        bufferString = snapshot.child(index.toString()).getValue<String>()
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get discipline title list.")
                }
            }
        )
    }

    public fun getBranchTitleList(branchTitleList : MutableList<String>, disciplineTitle: String,
                                  completionStatus : CompletionStatus) {
        completionStatus.setFalse()
        branchTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child("branchList").
        addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var index = 0
                    var bufferString: String? = snapshot.child(index.toString()).getValue<String>()
                    while (bufferString != null) {
                        branchTitleList.add(bufferString.toString())
                        index += 1
                        bufferString = snapshot.child(index.toString()).getValue<String>()
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get branch title list.")
                }
            }
        )
    }

    public fun getArticleTitleList(articleTitleList : MutableList<String>, disciplineTitle: String,
    branchTitle: String, completionStatus : CompletionStatus) {
        completionStatus.setFalse()
        articleTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).
        child("articleList").
        addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var index = 0
                    var bufferString: String? = snapshot.child(index.toString()).getValue<String>()
                    while (bufferString != null) {
                        articleTitleList.add(bufferString.toString())
                        index += 1
                        bufferString = snapshot.child(index.toString()).getValue<String>()
                    }
                    completionStatus.setTrue()
                    Log.i(tag, "Reading complete.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(tag, "Cannot get branch title list.")
                }
            }
        )
    }

    public fun getSubsectionTitleList(subsectionTitleList : MutableList<String>,
                                      subsectionList : MutableList<ArticleActivity.Subsection>,
                                      disciplineTitle: String, branchTitle: String,
                                      articleTitle: String, completionStatus : CompletionStatus) {
        completionStatus.setFalse()
        subsectionTitleList.clear()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).
        child(articleTitle).child("subsectionList").
        addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var index = 0
                    var bufferString: String? = snapshot.child(index.toString()).getValue<String>()
                    while (bufferString != null) {
                        subsectionTitleList.add(bufferString.toString())
                        subsectionList.add(ArticleActivity.Subsection(bufferString.toString()))
                        index += 1
                        bufferString = snapshot.child(index.toString()).getValue<String>()
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
                                    completionStatus: CompletionStatus) {
        completionStatus.setFalse()
        databaseRef.child("users").child(username).
        child(disciplineTitle).child(branchTitle).child(articleTitle).child(subsectionTitle).
                addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val currentSubsection = subsectionList.
                            find { it.title == subsectionTitle }
                            currentSubsection?.order =
                                snapshot.child("subsectionOrder").getValue<Int>()
                            //val dbOrder =
                            //    snapshot.child("content").child("order").getValue<Int>()
                            val dbText =
                                snapshot.child("content").child("text").getValue<String>()
                            val dbIsLatex =
                                snapshot.child("content").child("isLatex").getValue<Boolean>()
                            if (//dbOrder != null &&
                                    dbText != null &&
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
                            if (disciplineSnapshot.key != "disciplineList") {
                                discipline = disciplineSnapshot.key.toString()
                                for (branchSnapshot in disciplineSnapshot.children) {
                                    if (branchSnapshot.key != "branchList") {
                                        branch = branchSnapshot.key.toString()
                                        for (articleSnapshot in branchSnapshot.children) {
                                            if (articleSnapshot.key != "articleList" &&
                                                    articleSnapshot.key.toString()
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

    //TODO update
    //TODO delete

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