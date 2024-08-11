package com.fivedevs.auxby.screens.dashboard.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fivedevs.auxby.R
import com.fivedevs.auxby.data.database.entities.User
import com.fivedevs.auxby.databinding.FragmentChatBinding
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.dashboard.DashboardViewModel
import com.fivedevs.auxby.screens.dashboard.chat.adapters.ChatPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val viewModel by viewModels<ChatViewModel>()
    private val parentViewModel: DashboardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initTabLayout()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getChatRooms()
    }

    private fun initObservers() {
        parentViewModel.localUser.observe(viewLifecycleOwner) { user ->
            getChatsRooms(user)
        }

        parentViewModel.refreshChatFromNotificationEvent.observe(viewLifecycleOwner) {
            viewModel.getChatRooms()
        }
    }

    private fun getChatsRooms(user: User) {
        if (viewModel.isUserLoggedIn.value == true) {
            viewModel.onViewCreated(user)
        } else {
            showGuestChatView()
        }
    }

    private fun initTabLayout() {
        val chatTabNames = arrayOf(
            getString(R.string.buy),
            getString(R.string.sell)
        )
        val chatAdapter = ChatPagerAdapter(
            childFragmentManager,
            lifecycle,
        )
        binding.chatViewPager2.apply {
            adapter = chatAdapter
            isUserInputEnabled = false
            offscreenPageLimit = chatTabNames.size
        }

        TabLayoutMediator(binding.chatTabLayout, binding.chatViewPager2) { tab, position ->
            tab.text = chatTabNames[position]
        }.attach()
    }

    private fun showGuestChatView() {
        binding.inclGuestChatView.root.show()
    }

    private fun initListeners() {
        binding.chatTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.chatViewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.inclGuestChatView.btnGetStarted.setOnClickListenerWithDelay {
            requireActivity().launchActivityWithFinish<LoginActivity> { }
        }
    }

    companion object {
        fun newInstance() = ChatFragment()
    }
}