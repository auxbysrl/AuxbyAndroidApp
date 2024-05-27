package com.fivedevs.auxby.screens.addOffer.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.databinding.ItemOfferTagBinding
import com.fivedevs.auxby.domain.models.CategoryFieldsValue
import com.fivedevs.auxby.domain.utils.extensions.capitalizeFirst

class AdapterOfferTags(
    private val listOfTags: MutableList<CategoryFieldsValue>
) : RecyclerView.Adapter<AdapterOfferTags.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOfferTagBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(listOfTags[position])
    }

    override fun getItemCount() = listOfTags.size

    inner class ViewHolder(private val binding: ItemOfferTagBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offerTag: CategoryFieldsValue) {
            binding.apply {
                tvTagTitle.text = offerTag.key.capitalizeFirst()
                tvTagValue.text = offerTag.value
            }
        }
    }
}