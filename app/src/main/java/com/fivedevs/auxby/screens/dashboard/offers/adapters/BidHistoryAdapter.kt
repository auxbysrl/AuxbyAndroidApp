package com.fivedevs.auxby.screens.dashboard.offers.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemBidHistoryBinding
import com.fivedevs.auxby.domain.models.OfferBid
import com.fivedevs.auxby.domain.utils.DateUtils
import com.fivedevs.auxby.domain.utils.OfferUtils.getFormattedAmountWithCurrency
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat

class BidHistoryAdapter(
    var context: Context,
    var bids: List<OfferBid?>,
    val currencyType: String?
) : RecyclerView.Adapter<BidHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBidHistoryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(bids[position])
    }

    override fun getItemCount() = bids.size

    inner class ViewHolder(private val binding: ItemBidHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offerBid: OfferBid?) {
            binding.apply {
                populateViews(offerBid)
            }
        }

        private fun ItemBidHistoryBinding.populateViews(offerBid: OfferBid?) {
            offerBid?.let { offer ->
                Glide.with(context)
                    .load(offer.userAvatar)
                    .error(context.getDrawableCompat(R.drawable.ic_profile_placeholder))
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivUserAvatar)
                tvUsername.text = offer.userName
                tvBidTime.text = getBidTime(offer.bidDate)
                tvBidAmount.text =
                    getFormattedAmountWithCurrency(context, offer.bidValue, currencyType)
            }
        }

        private fun getBidTime(bidDate: String): String {
            return if (DateUtils().isToday(DateUtils().getFormattedDateYearMonthDayFromServer(bidDate))) {
                context.getString(R.string.today_at, DateUtils().getFormattedDateForUserActive(bidDate))
            } else {
                DateUtils().getFormattedDateForBidHistory(bidDate)
            }
        }
    }
}