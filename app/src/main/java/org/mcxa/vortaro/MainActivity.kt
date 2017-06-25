package org.mcxa.vortaro

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Scanner
import java.util.zip.GZIPInputStream

class MainActivity : AppCompatActivity() {
    val DICTIONARY_FILENAME = "vortaro.bin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set up the action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // load the wordlist
        val words = readWords()
        val wordAapter = WordAdapter(this, words)

        word_view.apply{
            adapter = wordAapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    fun readWords(): List<Word> {
        val words = ArrayList<Word>()

        val input = Scanner(GZIPInputStream(this.assets.open(DICTIONARY_FILENAME)))

        while (input.hasNextLine()) {
            val data = input.nextLine().split(':')
            //parse the data format
            // es:en:ety
            words.add(Word(data[0], data[1], data[2]))
        }

        return words
    }
}