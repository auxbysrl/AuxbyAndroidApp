package com.fivedevs.auxby.screens.dashboard.offers.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.databinding.ItemOfferSmallBinding
import com.fivedevs.auxby.databinding.ViewBiddersAndMoreBinding
import com.fivedevs.auxby.domain.models.OfferBid
import com.fivedevs.auxby.domain.models.OfferModel
import com.fivedevs.auxby.domain.models.enums.*
import com.fivedevs.auxby.domain.utils.Currencies
import com.fivedevs.auxby.domain.utils.Formatters.priceDecimalFormat
import com.fivedevs.auxby.domain.utils.OfferUtils.getFavoriteIconByState
import com.fivedevs.auxby.domain.utils.OfferUtils.getOfferDate
import com.fivedevs.auxby.domain.utils.extensions.*
import com.fivedevs.auxby.domain.utils.pagination.PaginationConstants
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.reflect.KFunction1


// TODO duplicate code - merge with OfferAdapter
class SmallOfferAdapter(
    var context: Context,
    var offers: MutableList<OfferModel>,
    var selectedOfferPublishSubject: KFunction1<Long, Unit>,
    private val saveOfferPublishSubject: PublishSubject<OfferModel>,
    private val userLoggedIn: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var user = User()
    var isInactiveTab = false
    var isMyOffersActivity = false

    private var isLoadingAdded = false

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PaginationConstants.ITEM -> {
                val binding = ItemOfferSmallBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                ViewHolder(binding)
            }
            else -> {
                val inflater = LayoutInflater.from(context)
                val viewLoading = inflater.inflate(R.layout.item_progress, viewGroup, false)
                LoadingViewHolder(viewLoading)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            PaginationConstants.ITEM -> {
                (holder as ViewHolder).bind(offers[position])
            }
            PaginationConstants.LOADING -> {
                (holder as LoadingViewHolder).progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == offers.size.dec() && isLoadingAdded) {
            PaginationConstants.LOADING
        } else {
            PaginationConstants.ITEM
        }
    }

    override fun getItemCount() = offers.size
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById<View>(R.id.loadProgress) as ProgressBar
    }

    inner class ViewHolder(private val binding: ItemOfferSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isFavorite = false

        fun bind(offer: OfferModel) {
            binding.apply {
                isFavorite = offer.isUserFavorite
                populateViews(offer)
                setOfferDescription(offer)
                initListeners(offer)
                handleDisplayBidders(offer)
                setOfferStateBadge(offer)
            }
        }

        private fun ItemOfferSmallBinding.setOfferStateBadge(offer: OfferModel) {
            if (isInactiveTab && isMyOffersActivity) {
                offer.status?.let { status ->
                    inclOfferStateBadge.root.show()
                    inclOfferStateBadge.cvBadgeContainer.setCardBackgroundColor(
                        context.getColorCompat(
                            getStateColorByStatus(status)
                        )
                    )
                    inclOfferStateBadge.ivBadgeIcon.loadImageResource(
                        getStateIconByStatus(
                            status
                        )
                    )
                    inclOfferStateBadge.tvBadgeTitle.text = getStateTitleByStatus(status, context)
                }
            }
        }

        private fun ItemOfferSmallBinding.populateViews(offer: OfferModel) {
            setOfferImage(offer)
            setPriceColor(offer)
            setPromotedIcon(offer)

            tvOfferTitle.text = offer.title
            tvLocation.text = offer.location.toString().ifEmpty { "-" }
            tvPrice.text = getPriceBidTitle(offer)
            tvPriceValue.text = getPriceValue(offer)
            tvDate.text = getOfferDate(offer, context)
            tvDate.setCompoundDrawablesWithIntrinsicBounds(
                if (offer.isOnAuction) R.drawable.ic_clock_thin
                else R.drawable.ic_calendar, 0, 0, 0
            )
            setFavoriteIcon()
        }

        private fun ItemOfferSmallBinding.setPromotedIcon(offer: OfferModel) {
            if (offer.isPromoted && offer.status.equals(OfferStateEnum.ACTIVE.name, true)) {
                ivRibbon.show()
            } else {
                ivRibbon.hide()
            }
        }

        private fun ItemOfferSmallBinding.setPriceColor(offer: OfferModel) {
            if (offer.isOnAuction && !offer.bids.isNullOrEmpty()) {
                val filteredBidsByValue = offer.bids?.filterNotNull()?.sortedByDescending { it.bidValue }
                if (filteredBidsByValue?.first()?.email.equals(user.email, true)) {
                    tvPriceValue.setTextColorRes(R.color.green)
                } else if (filteredBidsByValue?.any { it.email == user.email } == true) {
                    tvPriceValue.setTextColorRes(R.color.red)
                } else {
                    tvPriceValue.setTextColorRes(R.color.colorPrimary)
                }
            }
        }

        private fun ItemOfferSmallBinding.setOfferImage(offer: OfferModel) {
            ivOffer.setBackgroundResource(R.drawable.ic_placeholder_large)
            Glide.with(context)
                .load(offer.photos.firstOrNull()?.url.orEmpty())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .dontAnimate()
                .into(ivOffer)
        }

        private fun getPriceValue(offer: OfferModel) = context.getString(
            R.string.offer_price_value,
            if (offer.highestBid.orElse(0f) > 0) priceDecimalFormat.format(offer.highestBid) else priceDecimalFormat.format(offer.price),
            offer.currencyType?.let { type ->
                Currencies.currenciesList.firstOrNull { it.name.equals(type, true) }?.symbol ?: CurrencyEnum.RON.symbol()
            }
        )

        private fun ItemOfferSmallBinding.initListeners(offer: OfferModel) {
            itemView.setOnClickListenerWithDelay {
                selectedOfferPublishSubject(offer.id)
            }

            ivFavorite.setOnClickListenerWithDelay {
                handleFavouriteClick(offer)
            }
        }

        private fun handleFavouriteClick(offer: OfferModel) {
            if (!userLoggedIn) {
                saveOfferPublishSubject.onNext(offer)
                return
            }

            isFavorite = !isFavorite
            offer.apply { isUserFavorite = isFavorite }
            saveOfferPublishSubject.onNext(offer)
            setFavoriteIcon()
        }

        private fun setFavoriteIcon() {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(context)
                .load(getFavoriteIconByState(isFavorite))
                .apply(requestOptions)
                .into(binding.ivFavorite)
        }

        private fun ItemOfferSmallBinding.handleDisplayBidders(offer: OfferModel) {
            if (!offer.isOnAuction) {
                return
            }
            defaultBidsState()

            val layoutParams: ViewGroup.LayoutParams = inclBiddersAndMore.icBid.layoutParams
            layoutParams.width = 40
            layoutParams.height = 40
            inclBiddersAndMore.icBid.layoutParams = layoutParams

            val bids = offer.bids.orEmpty().sortedByDescending { it?.bidValue.orElse(0f) }
                .distinctBy { it?.email }
            showBidders(bids, inclBiddersAndMore)

            if (bids.size > 3) {
                inclBiddersAndMore.tvDescriptionAuction.show()
                inclBiddersAndMore.tvDescriptionAuction.text =
                    context.resources.getString(R.string.text_n_others, "${bids.size - 3}")
            } else if (bids.isEmpty()) {
                inclBiddersAndMore.tvNoBids.show()
            }
        }

        private fun ItemOfferSmallBinding.defaultBidsState() {
            inclBiddersAndMore.tvNoBids.hide()
            inclBiddersAndMore.ivBidder1.hide()
            inclBiddersAndMore.ivBidder2.hide()
            inclBiddersAndMore.ivBidder3.hide()
            inclBiddersAndMore.tvDescriptionAuction.hide()
        }

        private fun showBidders(
            bids: List<OfferBid?>,
            inclBiddersAndMore: ViewBiddersAndMoreBinding
        ) {
            bids.take(3).forEachIndexed { index, bid ->
                when (index) {
                    0 -> {
                        inclBiddersAndMore.ivBidder1.show()
                        populateBidderAvatar(bid, inclBiddersAndMore.ivBidder1)
                    }
                    1 -> {
                        inclBiddersAndMore.ivBidder2.show()
                        populateBidderAvatar(bid, inclBiddersAndMore.ivBidder2)
                    }
                    2 -> {
                        inclBiddersAndMore.ivBidder3.show()
                        populateBidderAvatar(bid, inclBiddersAndMore.ivBidder3)
                    }
                }
            }
        }

        private fun populateBidderAvatar(
            bid: OfferBid?,
            bidView: AppCompatImageView
        ) {
            Glide.with(context)
                .load(bid?.userAvatar)
                .error(context.getDrawableCompat(R.drawable.ic_profile_placeholder))
                .circleCrop()
                .placeholder(context.getDrawableCompat(R.drawable.ic_profile_placeholder))
                .override(300, 300)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(bidView)
        }

        private fun ItemOfferSmallBinding.setOfferDescription(offer: OfferModel) {
            if (offer.isOnAuction) {
                tvDescription.hide()
                inclBiddersAndMore.llAuctionOthersContainer.show()
                inclBiddersAndMore.tvDescriptionAuction.text =
                    context.getString(R.string.bidders_and_others, (8 - 3).toString())
            } else {
                inclBiddersAndMore.llAuctionOthersContainer.hide()
                tvDescription.show()
                tvDescription.text = offer.description
            }
        }

        private fun getPriceBidTitle(offer: OfferModel): String {
            return if (offer.isOnAuction) {
                if (offer.status?.equals(OfferStateEnum.FINISHED.getStatusName()) == true
                    || offer.status?.equals(OfferStateEnum.INTERRUPTED.getStatusName()) == true
                ) {
                    context.getString(R.string.final_bid)
                } else {
                    context.getString(R.string.current_bid)
                }
            } else {
                context.getString(R.string.price)
            }
        }
    }

    fun addNewOffers(offers: List<OfferModel>, isUpdate: Boolean = false) {
        if (isUpdate) {
            updateOffers(offers)
        } else {
            offers.forEach { newOffer ->
                val indexOfOffer = this.offers.indexOfFirst { it.id == newOffer.id }
                if (indexOfOffer != -1) {
                    updateOffer(indexOfOffer, newOffer)
                } else {
                    add(newOffer)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateOffers(offers: List<OfferModel>) {
        this.offers = offers.toMutableList()
        notifyDataSetChanged()
        return
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeOffers() {
        offers.clear()
        notifyDataSetChanged()
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(OfferModel())
    }

    private fun add(offer: OfferModel) {
        val position = offers.size
        offers.add(position, offer)
        notifyItemInserted(position)
    }

    private fun getPositionOrder(newOfferDate: String, oldOfferDate: String): Int {
        return if (newOfferDate.isEmpty() && oldOfferDate.isNotEmpty()) offers.size
        else if (newOfferDate.isNotEmpty() && oldOfferDate.isEmpty()) offers.size -1
        else offers.size
    }

    private fun updateOffer(indexOfOffer: Int, newOffer: OfferModel) {
        this.offers[indexOfOffer] = newOffer
        notifyItemChanged(indexOfOffer)
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position: Int = offers.size.dec()
        offers.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class SavedOffersDiffCallback(
        private var oldList: MutableList<OfferModel>,
        private var newList: MutableList<OfferModel>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun updateOffersList(savedOffers: List<OfferModel>) {
        val oldList = this.offers.toMutableList()
        val diffResult = DiffUtil.calculateDiff(
            SavedOffersDiffCallback(
                oldList,
                savedOffers.toMutableList()
            )
        )
        this.offers = savedOffers.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }
}