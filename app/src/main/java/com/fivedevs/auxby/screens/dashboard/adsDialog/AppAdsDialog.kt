package com.fivedevs.auxby.screens.dashboard.adsDialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fivedevs.auxby.R
import com.fivedevs.auxby.databinding.DialogAppAdsBinding
import com.fivedevs.auxby.domain.models.AdsModel

class AppAdsDialog(
    private var appAddModel: AdsModel,
    private var confirmListener: (() -> Unit?)?
) : DialogFragment() {

    private lateinit var binding: DialogAppAdsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_app_ads, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setGravity(Gravity.CENTER_VERTICAL)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            isCancelable = false
        }
    }

    private fun initView() {
        binding.tvDescription.text = appAddModel.text

        Glide.with(requireContext())
            .load(appAddModel.image)
            .placeholder(R.drawable.ic_placeholder_large)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.ivImage)
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnAddOffer.setOnClickListener {
            confirmListener?.invoke()
            dialog?.dismiss()
        }
    }
}