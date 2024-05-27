package com.fivedevs.auxby.screens.yourOffers

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityYourOffersBinding
import com.fivedevs.auxby.domain.utils.Constants.MY_OFFERS_TYPE
import com.fivedevs.auxby.domain.utils.extensions.replace
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.bids.MyOffersFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourOffersActivity : BaseActivity() {

    private lateinit var binding: ActivityYourOffersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initListeners()
        showOffersFragment()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_your_offers)
        binding.apply {
            lifecycleOwner = this@YourOffersActivity
        }
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun showOffersFragment() {
        replace(MyOffersFragment.newInstance(MY_OFFERS_TYPE), binding.flYourOffersContainer.id)
    }
}
