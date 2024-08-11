package com.fivedevs.auxby.screens.dashboard.offers.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ItemCategoryBinding
import com.fivedevs.auxby.domain.models.CategoryModel
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.getName
import com.fivedevs.auxby.domain.utils.extensions.orZero
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import io.reactivex.rxjava3.subjects.PublishSubject

class CategoryAdapter(
    var context: Context,
    var categories: MutableList<CategoryModel>,
    val onCategorySelected: PublishSubject<CategoryModel>,
    val shouldHighlight: Boolean = false
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryModel) {
            binding.apply {
                if (shouldHighlight){
                    binding.clCategoryContainer.strokeColor = context.resources.getColor(R.color.colorAccent)
                }
                handleCategorySelection()
                populateViews(category)

                root.setOnClickListenerWithDelay {
                    onCategorySelected.onNext(category)
                    notifyItemChanged(selectedPosition)
                    notifyItemChanged(adapterPosition)
                    selectedPosition = adapterPosition
                }

            }
        }

        private fun ItemCategoryBinding.populateViews(category: CategoryModel) {
            Glide
                .with(context)
                .load(category.getIconUrl())
                .error(context.getDrawableCompat(R.drawable.ic_placeholder))
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivCategory)
            tvCategoryTitle.text = category.label.getName(context)
            tvNumberOfCategories.text = getNumberOfOffers(category.noOffers.orZero())

            if (category.noOffers != null) {
                tvNumberOfCategories.show()
                tvNumberOfCategories.text = getNumberOfOffers(category.noOffers.orZero())
            }
        }

        private fun handleCategorySelection() {
            if (!categories.any { it.noOffers != null }) {
                binding.root.setStrokeColor(
                    ColorStateList.valueOf(
                        if (selectedPosition == adapterPosition) {
                            context.getColor(R.color.colorAccent)
                        } else {
                            context.getColor(R.color.white)
                        }
                    )
                )
            }
        }

        private fun getNumberOfOffers(noOffers: Int): String {
            return when (noOffers) {
                0 -> {
                    context.getString(R.string.no_offers)
                }
                1 -> {
                    context.getString(R.string.one_offer)
                }
                else -> {
                    context.getString(R.string.number_of_offers, noOffers)
                }
            }
        }
    }

    inner class CategoriesDiffCallback(
        private var oldList: MutableList<CategoryModel>,
        private var newList: MutableList<CategoryModel>
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

    fun updateCategoriesList(categories: List<CategoryModel>) {
        val oldList = this.categories.toMutableList()
        val diffResult = DiffUtil.calculateDiff(
            CategoriesDiffCallback(
                oldList,
                categories.toMutableList()
            )
        )
        this.categories = categories.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }
}