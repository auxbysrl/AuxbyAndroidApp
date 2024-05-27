package com.fivedevs.auxby.screens.addOffer.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemPromoteOfferBinding
import com.fivedevs.auxby.domain.models.PromoteOfferModel
import com.fivedevs.auxby.domain.utils.extensions.loadImageResource

class AdapterPromoteOffer(
    private val listOfPromotions: ArrayList<PromoteOfferModel>,
    private val context: Context,
    private val callback: (Int) -> Unit
) : RecyclerView.Adapter<AdapterPromoteOffer.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPromoteOfferBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(listOfPromotions[position])
    }

    override fun getItemCount() = listOfPromotions.size

    inner class ViewHolder(private val binding: ItemPromoteOfferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(promoteOffer: PromoteOfferModel) {
            selectItem(promoteOffer.isChecked)
            binding.apply {
                tvPromoteTitle.text = promoteOffer.title
                ivPromote.loadImageResource(promoteOffer.icon)
            }

            itemView.setOnClickListener {
                callback(adapterPosition)
                deselectItem()
                promoteOffer.isChecked = !promoteOffer.isChecked
                notifyItemChanged(adapterPosition)
            }
        }

        private fun selectItem(isChecked: Boolean) {
            if (isChecked) {
                binding.cvContainer.strokeColor = ContextCompat.getColor(context, R.color.colorAccent)
            } else {
                binding.cvContainer.strokeColor = ContextCompat.getColor(context, R.color.white)
            }
        }

        private fun deselectItem() {
            val selectedItem = listOfPromotions.indexOfFirst { it.isChecked }
            if (selectedItem > -1) {
                listOfPromotions[selectedItem].isChecked = false
                notifyItemChanged(selectedItem)
            }
        }
    }
}