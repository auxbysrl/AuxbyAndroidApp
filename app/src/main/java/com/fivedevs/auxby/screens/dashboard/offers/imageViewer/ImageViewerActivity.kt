package com.fivedevs.auxby.screens.dashboard.offers.imageViewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivityImageViewerBinding
import com.fivedevs.auxby.domain.models.OfferPhoto
import com.fivedevs.auxby.domain.utils.ZoomOutPageTransformer
import com.fivedevs.auxby.domain.utils.extensions.setDarkStatusBar
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.setTransparentStatusBar
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.dashboard.offers.adapters.ImageViewerPager2Adapter
import com.fivedevs.auxby.screens.dashboard.offers.details.OfferDetailsActivity.Companion.LAST_CAROUSEL_INT_EXTRA

class ImageViewerActivity : BaseActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkStatusBar()
        initBinding()
        initViewPager()
        initListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_viewer)
        binding.apply {
            lifecycleOwner = this@ImageViewerActivity
        }
    }

    private fun initViewPager() {
        val adapter = ImageViewerPager2Adapter(offerPhotos)
        val zoomOutPageTransformer = ZoomOutPageTransformer()

        binding.apply {
            vpOfferImages.adapter = adapter
            vpOfferImages.setPageTransformer { page, position ->
                zoomOutPageTransformer.transformPage(page, position)
            }
            vpDotsIndicator.attachTo(vpOfferImages)
            vpOfferImages.currentItem = selectedPosition
        }
    }

    private fun initListeners() {
        binding.btnClose.setOnClickListenerWithDelay {
            finish()
        }

        binding.vpOfferImages.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedPosition = position
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val resultValue = binding.vpOfferImages.currentItem
                val intent = Intent()
                intent.putExtra(LAST_CAROUSEL_INT_EXTRA, resultValue)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }

    companion object {
        var selectedPosition = 0
        var offerPhotos: MutableList<OfferPhoto> = mutableListOf()
    }
}