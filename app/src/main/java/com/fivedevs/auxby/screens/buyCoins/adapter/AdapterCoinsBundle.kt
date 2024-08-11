package com.fivedevs.auxby.screens.buyCoins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemCoinCardBinding
import com.fivedevs.auxby.domain.models.CoinBundle
import com.fivedevs.auxby.domain.models.enums.BundleEnum
import com.fivedevs.auxby.domain.models.enums.ProductTypeEnum
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import io.reactivex.rxjava3.subjects.PublishSubject

class AdapterCoinsBundle(
    var context: Context,
    var bundles: MutableList<CoinBundle>,
    var bundlePublishSubject: PublishSubject<CoinBundle>
) : RecyclerView.Adapter<AdapterCoinsBundle.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCoinCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(bundles[position], position)
    }

    override fun getItemCount() = bundles.size

    inner class ViewHolder(private val binding: ItemCoinCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(coinBundle: CoinBundle, position: Int) {
            binding.setVariable(BR.bundleModel, coinBundle)
            selectItem(coinBundle.isChecked)
            setCardDetails(coinBundle)
            setCardClickListener(coinBundle)
        }

        private fun setCardClickListener(coinBundle: CoinBundle) {
            itemView.setOnClickListenerWithDelay {
                bundlePublishSubject.onNext(coinBundle)
                deselectItem()
                coinBundle.isChecked = !coinBundle.isChecked
                notifyItemChanged(position)
            }
        }

        private fun setCardDetails(coinBundle: CoinBundle) {
            when (coinBundle.id) {
                ProductTypeEnum.SILVER.value -> {
                    coinBundle.coins = "50"
                    setBundleImage(BundleEnum.SILVER.getBackground())
                    binding.tvCoinsAmount.text = context.getString(R.string.coins_amount, "50")
                }
                ProductTypeEnum.GOLD.value -> {
                    coinBundle.coins = "100"
                    setBundleImage(BundleEnum.GOLD.getBackground())
                    binding.tvCoinsAmount.text = context.getString(R.string.coins_amount, "100")
                }
                ProductTypeEnum.DIAMOND.value -> {
                    coinBundle.coins = "150"
                    setBundleImage(BundleEnum.DIAMOND.getBackground())
                    binding.tvCoinsAmount.text = context.getString(R.string.coins_amount, "150")
                }
            }
        }

        private fun setBundleImage(imageId: Int) {
            binding.ivBundleImage.setImageDrawable(context.getDrawableCompat(imageId))
        }

        private fun selectItem(isChecked: Boolean) {
            if (isChecked) {
                binding.bundleCheckBox.setImageDrawable(context.getDrawableCompat(R.drawable.ic_check))
            } else {
                binding.bundleCheckBox.setImageDrawable(context.getDrawableCompat(R.drawable.ic_uncheck))
            }
        }

        private fun deselectItem() {
            val selectedItem = bundles.indexOfFirst { it.isChecked }
            if (selectedItem > -1) {
                bundles[selectedItem].isChecked = false
                notifyItemChanged(selectedItem)
            }
        }
    }
}