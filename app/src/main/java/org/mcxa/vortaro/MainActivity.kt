package org.mcxa.vortaro

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
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
        val wordAdapter = WordAdapter(this)

        // load the words in the background
        object : AsyncTask<Void,List<Word>,Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val input = Scanner(GZIPInputStream(this@MainActivity.assets.open(DICTIONARY_FILENAME)))
                var counter = 0; var lastUpdate = 0
                while (input.hasNextLine()) {
                    val data = input.nextLine().split(':')
                    //parse the data format
                    // es:en:ety
                    val word = Word(data[0], data[1], data[2])
                    wordList.add(word)

                    if (++counter % 20 == 0) {
                        publishProgress(wordList.subList(lastUpdate, counter - 1))
                        lastUpdate = counter - 1
                    }
                }
                return null
            }

            override fun onProgressUpdate(vararg values: List<Word>) {
                wordAdapter.words.addAll(values[0])
            }
        }.execute()

        word_view.apply{
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        search_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // if our filter is becoming more specific we can just filter
                // the current results
                if (before < count) {
                    wordAdapter.words.beginBatchedUpdates()
                    // iterate through the list back to front and remove all non-matching words
                    for (i in wordAdapter.words.size() - 1 downTo 0) {
                        val word = wordAdapter.words.get(i)
                        // check if our search query exists in english or esperanto, if not, remove
                        // it
                        if (!word.en.contains(s.toString()) && !word.es.contains(s.toString()))
                            wordAdapter.words.removeItemAt(i)
                    }
                    wordAdapter.words.endBatchedUpdates()
                } else {
                    // in this case we need to filter through the original list
                    //TODO background this
                    val filteredList = wordList.filter {
                        it.en.contains(s.toString()) || it.es.contains(s.toString())
                    }
                    wordAdapter.words.beginBatchedUpdates();
                    //remove items at end, to avoid unnecessary array shifting
                    while (wordAdapter.words.size() > 0) {
                        wordAdapter.words.removeItemAt(wordAdapter.words.size() - 1);
                    }
                    wordAdapter.words.addAll(filteredList)
                    wordAdapter.words.endBatchedUpdates();

                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}