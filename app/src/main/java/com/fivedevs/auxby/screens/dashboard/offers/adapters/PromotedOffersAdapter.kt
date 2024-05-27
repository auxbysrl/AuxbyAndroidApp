package com.fivedevs.auxby.screens.dashboard.offers.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemPromotedOffersBinding
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.CurrencyEnum
import com.fivedevs.auxby.domain.utils.Formatters
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import kotlin.reflect.KFunction1

class PromotedOffersAdapter(
    private val context: Context,
    private val dataList: MutableList<OfferModel>,
    var selectedOfferPublishSubject: KFunction1<Long, Unit>,
) : RecyclerView.Adapter<PromotedOffersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPromotedOffersBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adjustedPosition = position % dataList.size
        holder.bind(dataList[adjustedPosition])
    }

    override fun getItemCount(): Int {
        return if (dataList.size > 1) {
            Int.MAX_VALUE
        } else {
            dataList.size
        }
    }

    inner class ViewHolder(private val binding: ItemPromotedOffersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: OfferModel) {
            populateViews(binding, offer)

            itemView.setOnClickListenerWithDelay {
                selectedOfferPublishSubject(offer.id)
            }
        }
    }

    private fun getOfferPrice(offer: OfferModel): Float? {
        return if (offer.isOnAuction && offer.highestBid != 0f) {
            offer.highestBid
        } else {
            offer.price
        }
    }

    private fun populateViews(binding: ItemPromotedOffersBinding, offer: OfferModel) {
        binding.tvOfferTitle.text = offer.title
        binding.tvOfferDescription.text = offer.description
        binding.tvPriceTitle.text = if (offer.isOnAuction) context.getString(R.string.current_bid) else context.getString(R.string.price)
        binding.tvPriceValue.text = context.getString(
            R.string.offer_price_value,
            Formatters.priceDecimalFormat.format(getOfferPrice(offer)),
            offer.currencyType?.let { CurrencyEnum.valueOf(it).short() }
        )

        Glide.with(context)
            .load(offer.photos.firstOrNull()?.url.orEmpty())
            .error(context.getDrawableCompat(R.drawable.ic_placeholder_large))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(binding.ivOffer)
    }
}