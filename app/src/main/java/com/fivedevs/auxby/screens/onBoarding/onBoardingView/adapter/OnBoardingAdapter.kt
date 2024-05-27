package com.fivedevs.auxby.screens.onBoarding.onBoardingView.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fivedevs.auxby.databinding.ItemOnboardingBinding
import com.fivedevs.auxby.screens.onBoarding.onBoardingView.model.OnBoardingElement

class OnBoardingAdapter(
    private val dataList: MutableList<OnBoardingElement>
) : RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val biding: ItemOnboardingBinding) : RecyclerView.ViewHolder(biding.root) {
        fun bind(item: OnBoardingElement) {
            biding.item = item
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size
}