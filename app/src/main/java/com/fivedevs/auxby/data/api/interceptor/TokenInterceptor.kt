package com.fivedevs.auxby.data.api.interceptor

import com.fivedevs.auxby.application.App
import com.fivedevs.auxby.data.prefs.PreferencesService
import com.fivedevs.auxby.domain.utils.Api
import com.fivedevs.auxby.domain.utils.Constants
import com.fivedevs.auxby.domain.utils.Constants.RESPONSE_CODE_401
import com.fivedevs.auxby.domain.utils.EnvironmentManager
import com.fivedevs.auxby.domain.utils.extensions.isTokenNeed
import com.fivedevs.auxby.domain.utils.extensions.launchActivityNewTask
import com.fivedevs.auxby.screens.authentification.login.LoginActivity
import com.fivedevs.auxby.screens.authentification.register.RegisterActivity
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.io.IOException
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val preferencesService: PreferencesService,
    private val application: App
) : Interceptor {

    // Initialize the flag to false to indicate that no logout action is in progress.
    private var isLogoutInProgress = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferencesService.getUserToken()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        token?.let {
            if (it.isNotEmpty() && originalRequest.isTokenNeed()) {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }

        val apiType = originalRequest
            .tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(Api::class.java)?.value
            ?: throw IOException("You must add ApiType to your request method in interface.")

        val baseUrl = EnvironmentManager.getBaseUrl(apiType).toHttpUrl()

        val newUrl = originalRequest.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        val newRequest = requestBuilder
            .url(newUrl)
            .build()

        return chain.proceed(newRequest).apply {
            if (invalidTokenCondition(code, token)) {
                // Check if a logout action is already in progress
                if (!isLogoutInProgress && (application.currentActivity?.localClassName?.contains(LoginActivity::class.java.simpleName) == false &&
                            application.currentActivity?.localClassName?.contains(RegisterActivity::class.java.simpleName) == false)) {
                    // Set the flag to indicate that a logout action is in progress
                    isLogoutInProgress = true
                    logoutCurrentUser()
                }
            }
        }
    }

    private fun invalidTokenCondition(code: Int, token: String?): Boolean {
        return (code == RESPONSE_CODE_401 || (token.isNullOrEmpty() && code !in (200..299)))
    }

    private fun logoutCurrentUser() {
        // Ensure the flag is reset after the logout process is complete
        preferencesService.clearUserDetails()
        if (application.currentActivity !is LoginActivity) {
            application.launchActivityNewTask<LoginActivity>()
        }
    }
}