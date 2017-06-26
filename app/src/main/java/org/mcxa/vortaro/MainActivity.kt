package org.mcxa.vortaro

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

class MainActivity : AppCompatActivity() {
    val DICTIONARY_FILENAME = "vortaro.bin"

    val wordList = Collections.synchronizedList(ArrayList<Word>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set up the action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //load the words
        //readWords(baseList, filteredList)
        val wordAapter = WordAdapter()

        val input = Scanner(GZIPInputStream(this@MainActivity.assets.open(DICTIONARY_FILENAME)))

        //TODO run this is the background
        while (input.hasNextLine()) {
            val data = input.nextLine().split(':')
            //parse the data format
            // es:en:ety
            val word = Word(data[0], data[1], data[2])
            wordList.add(word)
        }
        wordAapter.words.addAll(wordList)

        word_view.apply{
            adapter = wordAapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    // load the words into the wordlist in the background
    /*fun readWords() {
        object : AsyncTask<Void,Void,Void>() {
            override fun doInBackground(vararg params: Void?): Void {
                val input = Scanner(GZIPInputStream(this@MainActivity.assets.open(DICTIONARY_FILENAME)))

                while (input.hasNextLine()) {
                    val data = input.nextLine().split(':')
                    //parse the data format
                    // es:en:ety
                    val word = Word(data[0], data[1], data[2])
                    wordList.add(word)
                }
                return null
            }

            override fun onPostExecute(result: Void?) {

            }
        }.execute()
    }*/
}