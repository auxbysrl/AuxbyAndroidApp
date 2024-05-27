package com.fivedevs.auxby.screens.dashboard.offers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemOfferImageFullscreenBinding
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.utils.extensions.loadImage

class ImageViewerPager2Adapter(
    private val offerPhotos: MutableList<OfferPhoto>
) :
    RecyclerView.Adapter<ImageViewerPager2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemOfferImageFullscreenBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(offerPhotos[position])
    }

    override fun getItemCount() = offerPhotos.size

    inner class ViewHolder(private val binding: ItemOfferImageFullscreenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: OfferPhoto) {
            binding.apply {
                ivOffer.loadImage(offer.url, R.drawable.ic_placeholder_large)
            }
        }
    }
}