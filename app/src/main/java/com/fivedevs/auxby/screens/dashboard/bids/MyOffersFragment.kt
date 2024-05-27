package com.fivedevs.auxby.screens.dashboard.bids

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.FragmentOffersListBinding
import com.fivedevs.auxby.domain.utils.Constants.MY_BIDS_TYPE
import com.fivedevs.auxby.domain.utils.Constants.MY_OFFERS_TYPE
import com.fivedevs.auxby.domain.utils.extensions.launchActivityWithFinish
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.dashboard.bids.adapters.MyBidsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MyOffersFragment(private val fragmentType: Int) : Fragment() {

    private lateinit var binding: FragmentOffersListBinding
    private val viewModel by viewModels<MyOffersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offers_list, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabLayout()
        initObservers()
        initListeners()
        getMyBids()
    }

    private fun initObservers() {
        when (fragmentType) {
            MY_OFFERS_TYPE -> {
                viewModel.myOffers.observe(viewLifecycleOwner) { offers ->
                    Timber.i("initObservers MY_OFFERS_TYPE")
                    viewModel.filterOffers(offers)
                }
            }
            else -> {
                Timber.i("initObservers MY_BIDS_TYPE")
                viewModel.myBidOffers.observe(viewLifecycleOwner) { offers ->
                    viewModel.filterOffers(offers)
                }
            }
        }
    }

    private fun getMyBids() {
        if (viewModel.isUserLoggedIn.value == true) {
            viewModel.onViewCreated(fragmentType)
        } else {
            showGuestBidsView()
        }
    }

    private fun showGuestBidsView() {
        binding.inclGuestMyBidsView.root.show()
    }

    private fun initListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.inclGuestMyBidsView.btnGetStarted.setOnClickListenerWithDelay {
            requireActivity().launchActivityWithFinish<LoginActivity> { }
        }
    }

    private fun initTabLayout() {
        val servicesTabNames = arrayOf(
            getString(R.string.active_offers),
            getString(R.string.innactive_offers)
        )
        val adapter = MyBidsPagerAdapter(
            childFragmentManager,
            lifecycle,
        )
        binding.viewPager2.adapter = adapter
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.offscreenPageLimit = servicesTabNames.size

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = servicesTabNames[position]
        }.attach()
    }

    companion object {
        fun newInstance(type: Int = MY_BIDS_TYPE) = MyOffersFragment(type)
    }
}