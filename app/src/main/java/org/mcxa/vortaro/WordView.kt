package org.mcxa.vortaro

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.word_item.view.*
import java.util.*

// model class for a single WordModel
data class WordModel(val es : String, val en : LinkedList<EnModel>, val ety : String,
                     val trans: Boolean?) {
    fun displayDef(): String {
        // format es into esf
        val esf = when(trans) {
            true -> "$es (tr)"
            false -> "$es (ntr)"
            null -> es
        }

        // format en into enf
        var enf = ""
        en.forEach { element ->
            if (enf.isNotEmpty()) enf += ", "
            enf += element.toString()
        }

        return "$esf : $enf"
    }
}

data class EnModel(val word: String, val elaboration: String?, val elbefore: Boolean?) {
    override fun toString(): String {
        // handle the special case where we only have an elaboration
        // due to me being lazy in my parse-DB code the elaboration already has () in the case where
        // we just have an elaboration and no definitions
        if (word.isEmpty() && elaboration != null) return elaboration
        if (elaboration.isNullOrEmpty()) return word

        return when(elbefore) {
            null -> word
            true -> "($elaboration) $word"
            false -> "$word ($elaboration)"
        }
    }
}

class WordAdapter: RecyclerView.Adapter<WordAdapter.ViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val wordView = inflater.inflate(R.layout.word_item, parent, false)
        return ViewHolder(wordView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = words.get(position)

        holder.word?.text = word.displayDef()
        holder.etymology?.text = word.ety
    }

    override fun getItemCount(): Int {
        return words.size()
    }


}