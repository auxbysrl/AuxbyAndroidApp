package com.fivedevs.auxby.screens.search.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemSearchSuggestionBinding
import com.fivedevs.auxby.domain.models.SearchSuggestion
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import io.reactivex.rxjava3.subjects.PublishSubject

class SearchSuggestionsAdapter(
    private val context: Context,
    private var suggestions: List<SearchSuggestion>,
    private val offerPublishSubject: PublishSubject<SearchSuggestion>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchSuggestionBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = suggestions.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(suggestions[position])
    }

    inner class ViewHolder(private val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: SearchSuggestion) {
            with(binding) {
                suggestion = item

                setResultsInCategory(item)

                container.setOnClickListenerWithDelay {
                    offerPublishSubject.onNext(item)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun ItemSearchSuggestionBinding.setResultsInCategory(item: SearchSuggestion) {
            val resultCount = item.noOffers
            val resultsText = context.getString(
                if (resultCount > 1) R.string.results_in else R.string.result_in
            )
            inCategory.text = "$resultCount $resultsText ${item.category.label.getName(context)}"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newSuggestions: List<SearchSuggestion>) {
        this.suggestions = newSuggestions.toMutableList()
        notifyDataSetChanged()
    }
}
