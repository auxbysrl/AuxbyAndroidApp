package com.fivedevs.auxby.screens.settings.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.LanguageModel
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class LanguagesAdapter(
    context: Context,
    languages: List<LanguageModel>,
    private val languageSelectedPublishSubject: PublishSubject<LanguageModel>
) : ArrayAdapter<LanguageModel>(
    context, R.layout.item_dropdown, languages
) {
    private val languages: MutableList<LanguageModel>
    private val filteredLanguages: List<LanguageModel>

    init {
        this.languages = ArrayList(languages)
        filteredLanguages = ArrayList(languages)
    }

    override fun getCount(): Int {
        return languages.size
    }

    override fun getItem(position: Int): LanguageModel {
        return languages[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        val item = getItem(position)

        val view = LayoutInflater.from(context).inflate(R.layout.item_dropdown, parent, false)
        view?.let { itemView ->

            val sd = itemView.findViewById<TextView>(R.id.tvItemLanguage)
            sd.text = item.codeEnum.getLanguageName(context)

            sd.setOnClickListener {
                languageSelectedPublishSubject.onNext(item)
            }
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any): String {
                return (resultValue as LanguageModel).codeEnum.getLanguageName(context)
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()
                val departmentsSuggestion: MutableList<LanguageModel> = ArrayList()
                for (department in filteredLanguages) {
                    departmentsSuggestion.add(department)
                }
                filterResults.values = departmentsSuggestion
                filterResults.count = departmentsSuggestion.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                languages.clear()
                if (results.count > 0) {
                    for (`object` in results.values as List<*>) {
                        if (`object` is LanguageModel) {
                            languages.add(`object`)
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }
}