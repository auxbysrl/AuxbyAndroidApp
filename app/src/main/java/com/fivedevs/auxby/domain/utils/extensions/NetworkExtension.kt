package com.fivedevs.auxby.domain.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.fivedevs.auxby.domain.utils.NoTokenNeed
import okhttp3.Request
import retrofit2.Invocation

@SuppressLint("MissingPermission")
fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw =
        connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

fun <T : Annotation> Request.getAnnotation(annotationClass: Class<T>): T? =
    this.tag(Invocation::class.java)?.method()?.getAnnotation(annotationClass)

fun Request.isTokenNeed(): Boolean {
    return this.getAnnotation(NoTokenNeed::class.java) == null
}