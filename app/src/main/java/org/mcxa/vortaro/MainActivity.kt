package org.mcxa.vortaro

import android.content.Intent
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

data class Definition(val es: String, val en: String)

class MainActivity : AppCompatActivity() {
    // tag for log methods
    val TAG = "Main_Activity"
    val ESPDIC_FILENAME = "espdic.bin"
    val ETYMOLOGY_FILENAME = "etymology.bin"
    val TRANSITIVE_FILENAME = "transitiveco.bin"

    val dictionary = ArrayList<Definition>()
    val etymology = HashMap<String, String>()
    val transitive = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set up the action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //load the words
        //readWords(baseList, filteredList)
        val wordAdapter = WordAdapter(this)

        parseFiles()

        word_view.apply{
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        search_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                wordAdapter.words.beginBatchedUpdates();
                // remove items at end, to avoid unnecessary array shifting
                while (wordAdapter.words.size() > 0) {
                    wordAdapter.words.removeItemAt(wordAdapter.words.size() - 1);
                }

                // iterate through dictionary and search for matches
                val normalizedTerm = s.toString().xReplace().normalizeES()
                Log.d(TAG, "begining search for " + s)
                for (def in dictionary) {
                    if (def.es == normalizedTerm) {
                        val ety = etymology.get(def.es.normalizeES())
                        wordAdapter.words.add(Word(def.es, def.en, if(ety != null) ety else ""))
                    }
                }
                wordAdapter.words.endBatchedUpdates();
                Log.d(TAG, "search complete")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // parse the dictionary files
    fun parseFiles() {
        Log.d(TAG, "Parsing espdic")
        val espdicScanner = Scanner(GZIPInputStream(this.assets.open(ESPDIC_FILENAME)))
        while (espdicScanner.hasNextLine()) {
            val data = espdicScanner.nextLine().split(':')
            //parse the data format
            // es:en
            val definition = Definition(data[0], data[1])
            dictionary.add(definition)
        }
        espdicScanner.close()
        Log.d(TAG, "parsing complete")

        Log.d(TAG, "Parsing etymology")
        val etymologyScanner = Scanner(GZIPInputStream(this.assets.open(ETYMOLOGY_FILENAME)))
        while (etymologyScanner.hasNextLine()) {
            val data = etymologyScanner.nextLine().split(':')
            //parse the data format
            // word:ety
            etymology.put(data[0], data[1])
        }
        etymologyScanner.close()
        Log.d(TAG, "parsing complete")

        Log.d(TAG, "Parsing transitiveco")
        val transScanner = Scanner(GZIPInputStream(this.assets.open(TRANSITIVE_FILENAME)))
        while (transScanner.hasNextLine()) {
            val data = transScanner.nextLine().split(':')
            //parse the data format
            // verb:transitive (t/f)
            transitive.put(data[0], data[1] == "t")
        }
        transScanner.close()
        Log.d(TAG, "parsing complete")
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