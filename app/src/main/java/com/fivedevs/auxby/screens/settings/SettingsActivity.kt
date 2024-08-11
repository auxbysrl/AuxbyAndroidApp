package com.fivedevs.auxby.screens.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.ActivitySettingsBinding
import com.fivedevs.auxby.domain.models.LanguageModel
import com.fivedevs.auxby.domain.utils.extensions.getDrawableCompat
import com.fivedevs.auxby.domain.utils.extensions.setOnClickListenerWithDelay
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.views.AlerterUtils
import com.fivedevs.auxby.screens.base.BaseActivity
import com.fivedevs.auxby.screens.settings.adapters.LanguagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initListeners()
        setAppLanguage()
        initObservers()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.apply {
            viewModel = this@SettingsActivity.viewModel
            lifecycleOwner = this@SettingsActivity
        }
    }

    private fun initObservers() {
        viewModel.languagesList.observe(this) {
            initLanguagesList(it)
        }

        viewModel.languageApp.observe(this) {
            viewModel.createLanguagesList(it)
        }

        viewModel.languageChanged.observe(this) {
            recreateActivity()
        }

        viewModel.user.observe(this) { user ->
            user.avatar?.let { it -> populateUserAvatar(it) }
        }

        viewModel.errorUpdateNewsLetter.observe(this) {
            AlerterUtils.showErrorAlert(this, resources.getString(R.string.something_went_wrong))
        }
    }

    private fun initListeners() {
        binding.inclToolbar.materialToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.inclLanguage.btnEditLanguage.setOnClickListenerWithDelay {
            viewModel.changeEditLanguageVisibility()
            //binding.inclNotifications.btnEditNotifications.isEnabled = false
        }

        binding.inclLanguage.btnCancel.setOnClickListenerWithDelay {
            viewModel.changeEditLanguageVisibility()
            //binding.inclNotifications.btnEditNotifications.isEnabled = true
        }

//        binding.inclNotifications.btnEditNotifications.setOnClickListenerWithDelay {
//            viewModel.changeEditNotificationsVisibility()
//            binding.inclLanguage.btnEditLanguage.isEnabled = false
//        }
//
//        binding.inclNotifications.btnCancel.setOnClickListenerWithDelay {
//            viewModel.changeEditNotificationsVisibility()
//            binding.inclLanguage.btnEditLanguage.isEnabled = true
//        }

        binding.inclNewsletter.viewNewsletter.setOnClickListenerWithDelay {
            viewModel.changeNewsletterStatus()
        }
    }

    private fun populateUserAvatar(avatar: String) {
        binding.inclToolbar.ivAvatar.show()
        Glide.with(this@SettingsActivity).load(avatar).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
            .error(getDrawableCompat(R.drawable.ic_profile_placeholder)).circleCrop().into(binding.inclToolbar.ivAvatar)
    }

    private fun initLanguagesList(languages: List<LanguageModel>) {
        val adapter = LanguagesAdapter(this, languages, viewModel.languageSelectedPublishSubject)
        binding.inclLanguage.actvEditLanguage.setAdapter(adapter)
    }

    private fun setAppLanguage() {
        viewModel.setAppLanguage(Locale.getDefault().language)
    }
}