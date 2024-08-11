package com.fivedevs.auxby.screens.addOffer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemAddPhotoBinding
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.utils.Constants.OFFER_MAXIMUM_PHOTOS
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import io.reactivex.rxjava3.subjects.PublishSubject

class OfferPhotoAdapter(
    var context: Context,
    private var offerPhotos: MutableList<OfferPhoto>,
    private val onPhotoSelected: PublishSubject<OfferPhoto>
) : RecyclerView.Adapter<OfferPhotoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddPhotoBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(offerPhotos[position])
    }

    override fun getItemCount() = offerPhotos.size

    inner class ViewHolder(private val binding: ItemAddPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offerPhoto: OfferPhoto) {
            binding.apply {

                populateViews(offerPhoto)

                ivPhoto.setOnClickListenerWithDelay {
                    onPhotoSelected.onNext(offerPhoto)
                }
            }
        }

        private fun ItemAddPhotoBinding.populateViews(offerPhoto: OfferPhoto) {
            if (offerPhoto.url.isEmpty()) {
                binding.ivPlus.show()
                binding.ivPhoto.setImageResource(0)
                return
            }
            binding.ivPlus.hide()
            binding.root.setStrokeColor(
                ColorStateList.valueOf(
                    if (offerPhoto.isCover) {
                        context.getColor(R.color.colorAccent)
                    } else {
                        context.getColor(R.color.white)
                    }
                )
            )

            Glide
                .with(context)
                .load(offerPhoto.url)
                .apply(RequestOptions().override(100, 100))
                .error(context.getDrawableCompat(R.drawable.ic_placeholder))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivPhoto)
        }
    }

    inner class OffersDiffCallback(
        private var oldList: MutableList<OfferPhoto>,
        private var newList: MutableList<OfferPhoto>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].url == newList[newItemPosition].url
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePhotosList(photos: List<OfferPhoto>) {

        val oldList = this.offerPhotos.toMutableList()
        val newList = getNewOfferPhotos(photos)
        val diffResult = DiffUtil.calculateDiff(
            OffersDiffCallback(
                oldList,
                newList
            )
        )
        this.offerPhotos = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getNewOfferPhotos(photos: List<OfferPhoto>): MutableList<OfferPhoto> {
        val newList = photos.toMutableList().apply {
            if (photos.size < OFFER_MAXIMUM_PHOTOS) {
                add(0, OfferPhoto())
            }

            if (photos.firstOrNull()?.url.orEmpty().isNotEmpty()) {
                photos.firstOrNull { it.isCover }?.isCover = false
                photos.first().isCover = true
            }
        }
        return newList
    }

    fun getListOfPhotos() = this.offerPhotos.filter { it.url.isNotEmpty() }
}