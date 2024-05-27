package com.fivedevs.auxby.screens.dashboard.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.ChatRoom
import com.fivedevs.auxby.databinding.BuySellFragmentBinding
import com.fivedevs.auxby.domain.utils.extensions.hide
import com.fivedevs.auxby.domain.utils.extensions.launchActivity
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.dashboard.chat.ChatViewModel
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatRoomsAdapter
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatPagerAdapter.Companion.BUY_TAB_POSITION
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatPagerAdapter.Companion.SELL_TAB_POSITION
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity.Companion.CHAT_ROOM_DETAILS
import com.fivedevs.auxby.screens.dashboard.chat.chatMessages.ChatMessagesActivity.Companion.CHAT_ROOM_TYPE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomsTabFragment(private val tabPosition: Int) : Fragment() {

    private lateinit var binding: BuySellFragmentBinding
    private val sharedViewModel: ChatViewModel by viewModels({ requireParentFragment() })
    private var chatRoomsAdapter: ChatRoomsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.buy_sell_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOffersAdapter()
        initObservers()
    }

    private fun initObservers() {
        when (tabPosition) {
            BUY_TAB_POSITION -> {
                observeBuyOffers()
            }
            SELL_TAB_POSITION -> {
                observeSellOffers()
            }
        }
    }

    private fun initOffersAdapter() {
        chatRoomsAdapter = ChatRoomsAdapter(
            requireContext(),
            arrayListOf(),
            this::onChatSelected,
            tabPosition
        )
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatRoomsAdapter
        }
    }

    private fun observeBuyOffers() {
        sharedViewModel.buyerChatRooms.observe(viewLifecycleOwner) { rooms ->
            updateAdapterList(rooms)
        }
    }

    private fun observeSellOffers() {
        sharedViewModel.sellerChatRooms.observe(viewLifecycleOwner) { rooms ->
            updateAdapterList(rooms)
        }
    }

    private fun updateAdapterList(offersList: List<ChatRoom>) {
        if (offersList.isEmpty()) {
            showEmptyStateView()
        } else {
            hideEmptyStateView()
            chatRoomsAdapter?.updateChatRoomsList(offersList.sortedByDescending { it.lastMessageTime }, sharedViewModel.user)
        }
    }

    private fun hideEmptyStateView() {
        binding.rvChat.show()
        binding.inclEmptyListView.root.hide()
    }

    private fun showEmptyStateView() {
        binding.rvChat.hide()
        binding.inclEmptyListView.root.show()
    }

    private fun onChatSelected(chatRoom: ChatRoom) {
        requireContext().launchActivity<ChatMessagesActivity> {
            putExtra(CHAT_ROOM_DETAILS, chatRoom)
            putExtra(CHAT_ROOM_TYPE, tabPosition)
        }
    }

    companion object {
        fun newInstance(tabPosition: Int) =
            ChatRoomsTabFragment(tabPosition)
    }
}