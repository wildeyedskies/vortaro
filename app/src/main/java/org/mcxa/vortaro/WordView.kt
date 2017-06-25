package org.mcxa.vortaro

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.word_item.view.*

// model class for a single Word
data class Word(val es : String, val en : String, val ety : String)

class WordAdapter(val context: Context, val words: List<Word>) :
        RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val word = itemView.item_word
        val etymology = itemView.item_etymology
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val wordView = inflater.inflate(R.layout.word_item, parent, false)
        return ViewHolder(wordView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val word = words.get(position)

        holder?.word?.setText(word.es + " : " + word.en)
        holder?.etymology?.setText(word.ety)
    }

    override fun getItemCount(): Int {
        return words.size
    }
}