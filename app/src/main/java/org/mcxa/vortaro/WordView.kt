package org.mcxa.vortaro

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.util.SortedList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.word_item.view.*

// model class for a single WordModel
data class WordModel(val es : String, val en : String, val ety : String)

class WordAdapter(val context: Context) :
        RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    val words = SortedList<WordModel>(WordModel::class.java, object: SortedList.Callback<WordModel>() {
        override fun areContentsTheSame(oldItem: WordModel?, newItem: WordModel?): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(item1: WordModel?, item2: WordModel?): Boolean {
            return item1 == item2
        }

        override fun compare(o1: WordModel?, o2: WordModel?): Int {
            return o1!!.es.compareTo(o2!!.es)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }
    })

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

        holder?.word?.setText(
                String.format(context.resources.getString(R.string.definition), word.es, word.en))
        holder?.etymology?.setText(word.ety)
    }

    override fun getItemCount(): Int {
        return words.size()
    }


}