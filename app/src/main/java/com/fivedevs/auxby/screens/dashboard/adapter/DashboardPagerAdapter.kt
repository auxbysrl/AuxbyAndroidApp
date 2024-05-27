package com.fivedevs.auxby.screens.dashboard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fivedevs.auxby.screens.dashboard.bids.MyOffersFragment
import com.fivedevs.auxby.screens.dashboard.chat.ChatFragment
import com.fivedevs.auxby.screens.dashboard.favourite.FavouriteFragment
import com.fivedevs.auxby.screens.dashboard.offers.OffersFragment

class DashboardPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUMBER_OF_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TAB_OFFERS_POSITION -> OffersFragment.newInstance()
            TAB_FAVOURITE_POSITION -> FavouriteFragment.newInstance()
            TAB_CHAT_POSITION -> ChatFragment.newInstance()
            TAB_BIDS_POSITION -> MyOffersFragment.newInstance()
            else -> Fragment()
        }
    }

    companion object {
        const val TAB_OFFERS_POSITION = 0
        const val TAB_FAVOURITE_POSITION = 1
        const val TAB_PLACEHOLDER_POSITION = 2
        const val TAB_CHAT_POSITION = 3
        const val TAB_BIDS_POSITION = 4
        const val NUMBER_OF_TABS = 5
    }
}