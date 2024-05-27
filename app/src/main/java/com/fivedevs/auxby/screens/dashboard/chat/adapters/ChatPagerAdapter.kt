package com.fivedevs.auxby.screens.dashboard.chat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fivedevs.auxby.screens.dashboard.chat.fragments.ChatRoomsTabFragment

class ChatPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return CHAT_TABS_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatRoomsTabFragment.newInstance(BUY_TAB_POSITION)
            else -> ChatRoomsTabFragment.newInstance(SELL_TAB_POSITION)
        }
    }

    companion object {
        const val CHAT_TABS_COUNT = 2
        const val BUY_TAB_POSITION = 0
        const val SELL_TAB_POSITION = 1
    }
}