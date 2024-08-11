package com.fivedevs.auxby.screens.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.fivedevs.auxby.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog(
    private val dialogLayout: Int,
    private val isFullScreen: Boolean,
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    @SuppressLint("RestrictedApi", "CheckResult")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            dialogLayout,
            null,
            false
        )
        dialog.setContentView(binding.root)
        val layoutParams =
            (binding.root.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as BottomSheetBehavior<View>
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = true
        behavior.peekHeight = 0
        setupDragDownDialog(dialog)
        setFullScreenDialog(binding)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        getContentView(binding)
    }

    private fun setFullScreenDialog(binding: ViewDataBinding) {
        if (isFullScreen) {
            binding.root.layoutParams.height = resources.displayMetrics.heightPixels
        }
    }

    private fun setupDragDownDialog(dialog: Dialog) {
        val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> return
                    BottomSheetBehavior.STATE_SETTLING -> return
                    BottomSheetBehavior.STATE_DRAGGING -> return
                    BottomSheetBehavior.STATE_COLLAPSED -> dismiss()
                    BottomSheetBehavior.STATE_EXPANDED -> return
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> return
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    abstract fun getContentView(binding: ViewDataBinding)
}