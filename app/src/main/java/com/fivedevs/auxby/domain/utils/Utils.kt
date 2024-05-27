package com.fivedevs.auxby.domain.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.fivedevs.auxby.BuildConfig
import com.fivedevs.auxby.R
import com.fivedevs.auxby.domain.models.ErrorResponse
import com.fivedevs.auxby.domain.models.enums.ApiTypeEnum
import com.fivedevs.auxby.domain.utils.extensions.invisible
import com.fivedevs.auxby.domain.utils.extensions.show
import com.fivedevs.auxby.domain.utils.extensions.toast
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import retrofit2.HttpException

object Utils {

    fun showEmailChooser(context: Context) {
        // Email app list.
        val emailAppLauncherIntents: MutableList<Intent?> = ArrayList()

        // Create intent which can handle only by email apps.
        val emailAppIntent = Intent(Intent.ACTION_SENDTO)
        emailAppIntent.data = Uri.parse("mailto:")

        // Find from all installed apps that can handle email intent and check version.
        val emailApps = context.packageManager.queryIntentActivities(
            emailAppIntent,
            PackageManager.MATCH_ALL
        )

        // Collect email apps and put in intent list.
        for (resolveInfo in emailApps) {
            val packageName = resolveInfo.activityInfo.packageName
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            emailAppLauncherIntents.add(launchIntent)
        }

        // Create chooser with created intent list to show email apps of device.
        if (emailAppLauncherIntents.isEmpty()) {
            context.toast(context.getString(R.string.no_email_apps_installed))
        } else {
            val chooserIntent = Intent.createChooser(Intent(), context.getString(R.string.choose_email_app))
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailAppLauncherIntents.toTypedArray())
            context.startActivity(chooserIntent)
        }
    }

    fun handleApiError(t: Throwable): String? {
        return if (t is HttpException) {
            t.response()?.errorBody()?.let {
                Gson().fromJson(it.string(), ErrorResponse::class.java)?.message ?: t.message
            } ?: run {
                t.localizedMessage
            }
        } else {
            t.localizedMessage
        }
    }

    fun getFullImageUrl(partUrl: String): String {
        return partUrl.replace("./", ApiTypeEnum.AUXBY_PLATFORM.url)
    }

    fun setCollapsingToolbarTitle(appBarLayout: AppBarLayout, tvToolbar: TextView) {
        var isShow = true
        var scrollRange = -1
        appBarLayout.addOnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                tvToolbar.show()
                isShow = true
            } else if (isShow) {
                tvToolbar.invisible()
                isShow = false
            }
        }
    }

    fun redirectToBrowserLink(context: Context, link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        context.startActivity(browserIntent)
    }

    fun appIsInBackgroundOrKilled() = ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED

    fun showPhoneDialer(phoneNumber: String, context: Context) {
        if (phoneNumber.isEmpty()) return

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        context.startActivity(intent)
    }
}