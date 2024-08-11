# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Keep the names of classes/members in Kotlin
# Keep classes and members for Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep class okio.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# Keep classes and members for Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep classes and members for Dagger Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.**

# Keep classes and members for Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.Glide { *; }
-keep class com.bumptech.glide.load.** { *; }
-keep class com.bumptech.glide.load.resource.bitmap.** { *; }
-keep class com.bumptech.glide.request.target.** { *; }
-keep class com.bumptech.glide.request.transition.** { *; }
-keep class com.bumptech.glide.signature.** { *; }
-dontwarn com.bumptech.glide.**

-keep class okhttp3.** { *; }
-keep class com.fivedevs.auxby** { *; }
-keepclassmembers class **.R$* {public static <fields>;}
-keep class **.R$*
-keep class org.jsoup.**

-keep public class * extends com.fivedevs.auxby.screens.base.BaseBottomSheetDialog

-keepclassmembers class * extends com.fivedevs.auxby.screens.base.BaseBottomSheetDialog{
 public <init>(android.content.Context);
}
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

-keep class com.google.crypto.tink.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.huawei.hms.ads.** { *; }
-keep interface com.huawei.hms.ads.** { *; }