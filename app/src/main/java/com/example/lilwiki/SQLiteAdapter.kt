package com.example.lilwiki

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity


class SQLiteAdapter(activityParam: AppCompatActivity, emailParam : String) {

    private val context = activityParam
    private val username = emailParam
    private var db : SQLiteDatabase

    init {
        db = context.baseContext.openOrCreateDatabase("lilwikiapp.db", MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Settings (user TEXT NOT NULL UNIQUE, id_theme INTEGER NOT NULL, id_mode INTEGER NOT NULL, PRIMARY KEY(user));")
        var query = db.rawQuery("SELECT COUNT(*) FROM Settings WHERE user='${username}';", null)
        if (query.moveToFirst()) {
            if (query.getInt(0) == 0) {
                db.execSQL("INSERT INTO Settings(user, id_theme, id_mode) VALUES ('${username}', 1, 1);")
            }
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Theme (id INT NOT NULL UNIQUE, theme_name TEXT NOT NULL, PRIMARY KEY(id));")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS SearchMode (id INT NOT NULL UNIQUE, mode_name TEXT NOT NULL, PRIMARY KEY(id));")
        query = db.rawQuery("SELECT COUNT(*) FROM Theme", null)
        if (query.moveToFirst()) {
            if (query.getInt(0) == 0) {
                db.execSQL("INSERT INTO Theme(id, theme_name) VALUES (1, 'light'), (2, 'dark');")
            }
        }
        query = db.rawQuery("SELECT COUNT(*) FROM SearchMode", null)
        if (query.moveToFirst()) {
            if (query.getInt(0) == 0) {
                db.execSQL("INSERT INTO SearchMode(id, mode_name) VALUES (1, 'private'), (2, 'public');")
            }
        }
        query.close()
        db.close()
    }

    public fun getSettings() : Map<String, String> {
        db = context.baseContext.openOrCreateDatabase("lilwikiapp.db", MODE_PRIVATE, null)
        val query = db.rawQuery("SELECT id_theme, id_mode FROM Settings WHERE user='${username}';", null)
        if (query.moveToFirst()) {
            val idTheme = query.getInt(0)
            val idMode = query.getInt(1)
            return mapOf("theme" to if (idTheme == 1) "light" else "dark",
                "mode" to if (idMode == 1) "private" else "public")
        }
        query.close()
        db.close()
        throw SQLiteException("No tuple.")
    }

    public fun saveSettings(theme : String, mode : String) {
        db = context.baseContext.openOrCreateDatabase("lilwikiapp.db", MODE_PRIVATE, null)
        val idTheme = if (theme == "light") 1 else 2
        val idMode = if (mode == "private") 1 else 2
        //db.execSQL("INSERT INTO Settings(id, id_theme, id_language) VALUES ('${username}', ${idTheme}, ${idLanguage});")
        db.execSQL("UPDATE Settings SET id_theme=${idTheme}, id_mode=${idMode} WHERE user='${username}'")
        db.close()
    }
}