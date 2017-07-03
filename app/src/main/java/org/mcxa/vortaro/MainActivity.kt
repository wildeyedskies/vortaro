package org.mcxa.vortaro

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.collections.ArrayList

// data model for a espdic definition
data class EspdicModel(val es: String, val en: Array<EspdicEn>)

enum class Elaboration {
    BEFORE, AFTER, NONE
}

// data class that represents an english definition in ESPDIC, most entries have many of these
data class EspdicEn(val word: String, val elaboration: String?, val eltype: Elaboration) {
    override fun equals(other: Any?): Boolean {
        if (other !is EspdicEn) return false
        else return this.word == other.word && this.elaboration == other.elaboration
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

// makes it easy to say espdicModel.en.toString() and get a nice rendered output
fun Array<EspdicEn>.display(): String {
    var string = ""
    this.forEach{
        if (!string.isEmpty()) string += ", "

        if (it.eltype == Elaboration.AFTER) {
            string += it.word
            if (it.elaboration != null) string += " (" + it.elaboration + ')'
        } else if (it.eltype == Elaboration.BEFORE) {
            if (it.elaboration != null) string += '(' + it.elaboration + ") "
            string += it.word
        } else {
            string += it.word
        }
    }
    return string
}

// quick way to determine if there is an exact english match to an english word
fun Array<EspdicEn>.matches(query: String): Boolean {
    this.forEach {
        if (it.word == query) return true
        if (it.word == "to " + query) return true
    }
    return false
}

// I cannot figure out how to make a constructor that processes data
// and builds an object
fun buildEspdicModel(espdicLine: String): EspdicModel {
    val data = espdicLine.split(":")

    // this is the english definition, it may be multiple terms seperated by ,
    val enData = data[1].split(", ")
    // we can build an array of these english terms
    val en = Array<EspdicEn>(enData.size, { i ->
        // whether the elaboration comes before the word
        if (enData[i].startsWith('(')) {
            val eltype = Elaboration.BEFORE
            val elaboration = enData[i].substringAfter('(').substringBefore(')')
            val word = enData[i].substringAfter(')').trim()

            EspdicEn(word, elaboration, eltype)
        } else if (enData[i].endsWith(')')) {
            val eltype = Elaboration.AFTER
            val elaboration = enData[i].substringAfter('(').substringBefore(')')
            val word = enData[i].substringBefore('(').trim()

            EspdicEn(word, elaboration, eltype)
        } else {
            val eltype = Elaboration.NONE
            val elaboration = null
            val word = enData[i]

            EspdicEn(word, elaboration, eltype)
        }
    })

    return EspdicModel(data[0], en)
}

class MainActivity : AppCompatActivity() {
    // tag for log methods
    val TAG = "Main_Activity"
    val ESPDIC_FILENAME = "espdic.bin"
    val ETYMOLOGY_FILENAME = "etymology.bin"
    val TRANSITIVE_FILENAME = "transitiveco.bin"

    val dictionary = ArrayList<EspdicModel>()
    val etymology = HashMap<String, String>()
    val transitive = HashMap<String, Boolean>()
    // do not search unless 0
    var parsingLock = 3

    // parse and load the dictionary files
    fun parseFiles() {
        object: AsyncTask<Void,Void,Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                Log.d(TAG, "Parsing espdic")
                val espdicScanner = Scanner(GZIPInputStream(this@MainActivity.assets.open(ESPDIC_FILENAME)))
                while (espdicScanner.hasNextLine()) {
                    val data = espdicScanner.nextLine()
                    //parse the data format
                    // es:en,en (elaboration)
                    val definition = buildEspdicModel(data)
                    dictionary.add(definition)
                }
                espdicScanner.close()
                Log.d(TAG, "parsing complete")

                return null
            }

            override fun onPostExecute(result: Void?) {
                if(--parsingLock == 0) executeSearch(search_text.text)
            }
        }.execute()

        object: AsyncTask<Void,Void,Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                Log.d(TAG, "Parsing etymology")
                val etymologyScanner = Scanner(GZIPInputStream(this@MainActivity.assets.open(ETYMOLOGY_FILENAME)))
                while (etymologyScanner.hasNextLine()) {
                    val data = etymologyScanner.nextLine().split(':')
                    //parse the data format
                    // word:ety
                    etymology.put(data[0], data[1])
                }
                etymologyScanner.close()
                Log.d(TAG, "parsing complete")

                return null
            }

            override fun onPostExecute(result: Void?) {
                if(--parsingLock == 0) executeSearch(search_text.text)
            }
        }.execute()

        object: AsyncTask<Void,Void,Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                Log.d(TAG, "Parsing transitiveco")
                val transScanner = Scanner(GZIPInputStream(this@MainActivity.assets.open(TRANSITIVE_FILENAME)))
                while (transScanner.hasNextLine()) {
                    val data = transScanner.nextLine().split(':')
                    //parse the data format
                    // verb:transitive (t/f)
                    transitive.put(data[0], data[1] == "t")
                }
                transScanner.close()
                Log.d(TAG, "parsing complete")

                return null
            }

            override fun onPostExecute(result: Void?) {
                if(--parsingLock == 0) executeSearch(search_text.text)
            }
        }.execute()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set up the action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val wordAdapter = WordAdapter(this)

        // load the word files
        parseFiles()

        word_view.apply{
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        search_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // do not search if parsing is complete
                if (parsingLock > 0) return
                executeSearch(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun executeSearch(s: CharSequence?) {
        val wordAdapter = word_view.adapter as WordAdapter

        wordAdapter.words.beginBatchedUpdates();

        // remove items at end, to avoid unnecessary array shifting
        while (wordAdapter.words.size() > 0) {
            wordAdapter.words.removeItemAt(wordAdapter.words.size() - 1);
        }

        // don't search for anything if we have just the empty string
        if (s!!.isNotEmpty()) {
            // iterate through dictionary and search for matches
            val normalizedTerm = s.toString().xReplace().normalizeES()
            val exactTerm = s.toString()
            Log.d(TAG, "begining search for " + s)
            for (def in dictionary) {
                if (def.es == normalizedTerm || def.en.matches(exactTerm)) {
                    val ety = etymology.get(def.es.normalizeES())

                    //check for transitive
                    val tr = if (def.es.endsWith('i') && transitive.containsKey(def.es)) {
                        if (transitive.get(def.es)!!) " (tr)"
                        else " (itr)"
                    } else ""

                    wordAdapter.words.add(WordModel(def.es + tr, def.en.display(), if (ety != null) ety else ""))
                }
            }
            Log.d(TAG, "search complete")
        }

        wordAdapter.words.endBatchedUpdates();

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.about -> {
                val i = Intent(this, AboutActivity::class.java)
                startActivity(i) // brings up the second activity
                return true
            }
        }

        return false
    }
}

// code ported from https://github.com/sstangl/tuja-vortaro/blob/master/vortaro.js

// replaces the x system characters
fun String.xReplace(): String {
    var mutWord = this

    // I like Kotlin a lot more than java, but what is with this array syntax??
    val pairs = arrayOf(
        arrayOf("cx", "ĉ"), arrayOf("cX", "ĉ"), arrayOf("Cx", "Ĉ"), arrayOf("CX", "Ĉ"),
        arrayOf("gx", "ĝ"), arrayOf("gx", "ĝ"), arrayOf("Gx", "Ĝ"), arrayOf("Gx", "Ĝ"),
        arrayOf("hx", "ĥ"), arrayOf("hx", "ĥ"), arrayOf("Hx", "Ĥ"), arrayOf("Hx", "Ĥ"),
        arrayOf("jx", "ĵ"), arrayOf("jx", "ĵ"), arrayOf("Jx", "Ĵ"), arrayOf("Jx", "Ĵ"),
        arrayOf("sx", "ŝ"), arrayOf("sx", "ŝ"), arrayOf("Sx", "Ŝ"), arrayOf("Sx", "Ŝ"),
        arrayOf("ux", "ŭ"), arrayOf("ux", "ŭ"), arrayOf("Ux", "Ŭ"), arrayOf("Ux", "Ŭ")
    )
    for (replacement in pairs) {
        mutWord = mutWord.replace(replacement[0], replacement[1])
    }

    Log.d("xReplace", "replaced " + this + " with " + mutWord)
    return mutWord
}

// function removes endings on an esperanto word
fun String.normalizeES(): String {
    val suffixes = arrayOf(
            // nomalize all verb endings to infinitive
            arrayOf("as", "i"),
            arrayOf("os", "i"),
            arrayOf("is", "i"),
            arrayOf("us", "i"),
            arrayOf("u", "i"),

            // nomalize all noun endings to o
            arrayOf("oj", "o"),
            arrayOf("ojn", "o"),
            arrayOf("on", "o"),

            // nomalize all adjective endings to o
            arrayOf("aj", "a"),
            arrayOf("ajn", "a"),
            arrayOf("an", "a"),

            // normalize adverbs
            arrayOf("en", "e")
    )

    for (replacement in suffixes) {
        if (this.endsWith(replacement[0])) {
            //replace the ending
            val toRet = this.substring(0, this.length - replacement[0].length) + replacement[1]
            Log.d("xReplace", "replaced " + this + " with " + toRet)
            return toRet
        }
    }

    return this
}