package com.fivedevs.auxby.domain.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                return !(ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> true
        }
    }

    fun hasStoragePermission(activity: Activity): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                checkStoragePermissionUnderAPI33(activity)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                checkStoragePermissionOrHigherAPI33(activity)
            }
            else -> true
        }
    }

    private fun checkStoragePermissionUnderAPI33(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                Constants.PERMISSION_STORAGE
            )
            false
        } else
            true
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    fun checkStoragePermissionOrHigherAPI33(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                Constants.PERMISSION_STORAGE
            )
            false
        } else
            true
    }

    fun getMediaRequiredPermissions(): List<String> {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else ->
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

}