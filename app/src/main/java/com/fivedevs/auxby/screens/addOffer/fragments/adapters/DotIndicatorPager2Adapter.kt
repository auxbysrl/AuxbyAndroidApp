package com.fivedevs.auxby.screens.addOffer.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemOfferImageBinding
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.utils.extensions.loadImage
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay

class DotIndicatorPager2Adapter(
    var offerPhotos: MutableList<OfferPhoto>,
    private val imageCallback: ((Int) -> Unit?)? = null
) :
    RecyclerView.Adapter<DotIndicatorPager2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemOfferImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(offerPhotos[position])
    }

    override fun getItemCount() = offerPhotos.size

    inner class ViewHolder(private val binding: ItemOfferImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OfferPhoto) {
            binding.apply {
                ivOffer.loadImage(offer.url, R.drawable.ic_placeholder_large)
            }
            itemView.setOnClickListenerWithDelay {
                imageCallback?.let { it(adapterPosition) }
            }
        }
    }

    inner class OfferImagesDiffCallback(
        private var oldList: MutableList<OfferPhoto>,
        private var newList: MutableList<OfferPhoto>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].url == newList[newItemPosition].url
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun updateOfferImagesList(photos: List<OfferPhoto>) {
        val oldList = this.offerPhotos.toMutableList()
        val diffResult = DiffUtil.calculateDiff(
            OfferImagesDiffCallback(
                oldList,
                photos.toMutableList()
            )
        )
        this.offerPhotos = photos.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }
}
