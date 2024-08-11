package com.fivedevs.auxby.screens.dashboard.bids.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fivedevs.auxby.screens.dashboard.bids.fragments.MyOffersTabFragment

class MyBidsPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return MY_BIDS_TAB_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyOffersTabFragment.newInstance(ACTIVE_OFFERS_TAB_POS)
            else -> MyOffersTabFragment.newInstance(INACTIVE_OFFERS_TAB_POS)
        }
    }

    companion object {
        const val MY_BIDS_TAB_COUNT = 2
        const val ACTIVE_OFFERS_TAB_POS = 0
        const val INACTIVE_OFFERS_TAB_POS = 1
    }
}