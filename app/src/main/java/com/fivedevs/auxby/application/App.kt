package com.fivedevs.auxby.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.fivedevs.auxby.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import io.branch.referral.Branch
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initFirebase()
        initBranchDeeplink()
        registerActivityLifecycle()
    }

    private fun registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                // No need to clear the currentActivity here if you always want to know the current foreground activity
            }

            override fun onActivityStopped(activity: Activity) {
                // No need to clear the currentActivity here if you always want to know the current foreground activity
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Do nothing
            }

            override fun onActivityDestroyed(activity: Activity) {
                // Clear the reference to the destroyed activity
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }
        })
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.ENABLE_CRASHLITICS)
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.e("onCreate: subscribeToTopic")
                } else {
                    Timber.e("onCreate: subscribeToTopic failed")
                }
            }
    }

    private fun initBranchDeeplink(){
        // Branch logging for debugging
        Branch.enableLogging()
        // Branch object initialization
        Branch.getAutoInstance(this)
    }
}