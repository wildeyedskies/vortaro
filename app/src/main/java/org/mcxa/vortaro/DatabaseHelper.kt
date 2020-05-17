package org.mcxa.vortaro

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap

const val SHARED_PREFS_NAME = "org.mcxa.vortaro"
const val DB_VERSION_KEY = "DB_VERSION"
const val DB_NAME = "data.db"
const val DB_VERSION = 2

class DatabaseHelper(private val context: Context) :
        SQLiteOpenHelper(context, context.filesDir.path + "/org.mcxa.vortaro/databases", null, 1) {
    //The Android's default system path of your application database.
    private val dbPath = context.filesDir.path + "/org.mcxa.vortaro/databases"
    // logging tag
    private val TAG = "DatabaseHelper"

    private val db: SQLiteDatabase = openOrCreate()

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    private fun openOrCreate(): SQLiteDatabase {
        // only execute if the database does not exist
        if (shouldCopyDB()) {
            copyDB()
        }
        return SQLiteDatabase.openDatabase(dbPath + DB_NAME, null, SQLiteDatabase.OPEN_READONLY)
    }

    /**
     * Note that we can't use the default upgrade logic for Sqlite because we are copying the file
     * instead of modifying the tables
     */
    private fun shouldCopyDB(): Boolean {
        // if there's no database currently loaded. copy
        if (!File(dbPath + DB_NAME).exists()) { return true }

        // if the version doesn't match our existing version, copy
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0)
        return prefs?.getInt(DB_VERSION_KEY, 0) != DB_VERSION
    }

    private fun copyDB() {
        Log.d(TAG, "Copying database")
        //By calling this method and empty database will be created into the default system path
        //of your application so we are gonna be able to overwrite that database with our database.
        this.readableDatabase

        //Open your local db as the input stream
        val inputStream = context.assets.open(DB_NAME)
        //Open the empty db as the output stream
        val outputStream = FileOutputStream(dbPath + DB_NAME)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // update the shared preferences
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0)
        prefs.edit().putInt(DB_VERSION_KEY, DB_VERSION).apply()
        Log.d(TAG, "Database version $DB_VERSION has been loaded")
    }

    override fun close() {
        db.close()
        super.close()
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
    override fun onCreate(p0: SQLiteDatabase?) {}

    /**
     * Search for a word. Note that checking if the string is null or empty should be done by the
     * calling function
<<<<<<< HEAD
     * @param exactTerm search query
     * @param w word adapter to push the results into
     */
    fun search(exactTerm: String, w: WordAdapter) {
        // iterate through dictionary and search for matches
        val normalizedTerm = exactTerm.xReplace().normalizeES()
        Log.d(TAG, "begining search for $exactTerm")

        GlobalScope.launch(Dispatchers.IO) {
            val wordmap = HashMap<Int, WordModel>()
            db.rawQuery("SELECT * FROM eo " +
                    "INNER JOIN en ON eo.rowid=en.eorow " +
                    "LEFT JOIN trans ON eo.eoword=trans.verb " +
                    "LEFT JOIN ety ON eo.eoword=ety.word " +
                    "WHERE eo.eoword=? OR eo.eoword=? OR eo.rowid IN " +
                    "(SELECT eo.rowid FROM eo INNER JOIN en ON eo.rowid=en.eorow WHERE en.enword=? OR en.enword=?)",
                    arrayOf(exactTerm, normalizedTerm, exactTerm, "to $exactTerm")
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val eorow = cursor.getInt(cursor.getColumnIndexOrThrow("eorow"))
                    // if the wordmap already has the Esperanto word, then just add the english def
                    if (wordmap.containsKey(eorow)) {
                        // grab the english values
                        val word = cursor.getString(cursor.getColumnIndexOrThrow("enword"))
                        val elaboration = cursor.getString(cursor.getColumnIndexOrThrow("el"))
                        val elbefore = cursor.getInt(cursor.getColumnIndexOrThrow("elbefore"))

                        wordmap.get(eorow)?.en?.add(EnModel(word, elaboration, when(elbefore) {
                            0 -> false
                            1 -> true
                            else -> null
                        }))
                        // add a new word model and english definition
                    } else {
                        val eoword = cursor.getString(cursor.getColumnIndexOrThrow("eoword"))
                        val enword = cursor.getString(cursor.getColumnIndexOrThrow("enword"))
                        val elaboration = cursor.getString(cursor.getColumnIndexOrThrow("el"))
                        val elbefore = cursor.getInt(cursor.getColumnIndexOrThrow("elbefore"))
                        val etymology = cursor.getString(cursor.getColumnIndexOrThrow("ety"))
                        val trans = cursor.getInt(cursor.getColumnIndexOrThrow("trans"))
                        Log.d(TAG, "found $eoword, $enword, $elaboration, $elbefore, $etymology, $trans")

                        val enmodels = LinkedList<EnModel>()
                        enmodels.add(EnModel(enword, elaboration, when(elbefore) {
                            2 -> true
                            1 -> false
                            else -> null
                        }))

                        wordmap.put(eorow, WordModel(eoword, enmodels, etymology ?: "",when(trans) {
                            2 -> true
                            1 -> false
                            else -> null
                        }))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                w.words.beginBatchedUpdates();

                // remove items at end, to avoid unnecessary array shifting
                while (w.words.size() > 0) {
                    w.words.removeItemAt(w.words.size() - 1);
                }

                wordmap.values.forEach { wordModel ->
                    w.words.add(wordModel)
                }

                w.words.endBatchedUpdates();
                Log.d(TAG, "search complete")
            }
        }
    }
}

// code ported from https://github.com/sstangl/tuja-vortaro/blob/master/vortaro.js

// replaces the x system characters
fun String.xReplace(): String {
    var mutWord = this

    val pairs = arrayOf(
            "cx" to "ĉ", "cX" to "ĉ", "Cx" to "Ĉ", "CX" to "Ĉ",
            "gx" to "ĝ", "gx" to "ĝ", "Gx" to "Ĝ", "Gx" to "Ĝ",
            "hx" to "ĥ", "hx" to "ĥ", "Hx" to "Ĥ", "Hx" to "Ĥ",
            "jx" to "ĵ", "jx" to "ĵ", "Jx" to "Ĵ", "Jx" to "Ĵ",
            "sx" to "ŝ", "sx" to "ŝ", "Sx" to "Ŝ", "Sx" to "Ŝ",
            "ux" to "ŭ", "ux" to "ŭ", "Ux" to "Ŭ", "Ux" to "Ŭ"
    )

    for ((target, replacement) in pairs) {
        mutWord = mutWord.replace(target, replacement)
    }

    Log.d("xReplace", "replaced " + this + " with " + mutWord)
    return mutWord
}

// function removes endings on an esperanto word
fun String.normalizeES(): String {
    val suffixes = arrayOf(
            // nomalize all verb endings to infinitive
            "as" to "i",
            "os" to "i",
            "is" to "i",
            "us" to "i",
            "u" to "i",

            // nomalize all noun endings to o
            "oj" to "o",
            "ojn" to "o",
            "on" to "o",

            // nomalize all adjective endings to o
            "aj" to "a",
            "ajn" to "a",
            "an" to "a",

            // normalize adverbs
            "en" to "e"
    )

    for ((target, replacement) in suffixes) {
        if (this.endsWith(target)) {
            //replace the ending
            val toRet = this.substring(0, this.length - target.length) + replacement
            Log.d("normalizeES", "replaced $this with $toRet")
            return toRet
        }
    }

    return this
}