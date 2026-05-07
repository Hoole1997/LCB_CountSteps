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

# Keep metadata used by Gson TypeToken, Room annotations, and Kotlin generated
# constructors. Third-party SDK rules are intentionally left to their own
# consumer ProGuard files.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Manifest entry points.
-keep class com.example.lcb.app.LcbApp { *; }
-keep class com.example.lcb.app.MainActivity { *; }

# Classes marked with @Keep should remain stable if added later.
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Legacy DataStore JSON migration uses Gson field names for these app models.
-keep class com.example.lcb.app.data.StepDailyRecord { *; }
-keep class com.example.lcb.app.data.HydrationRecord { *; }

# Room loads the generated database implementation by class name and maps the
# app-owned entities/DAO contracts during generated code execution.
-keep class com.example.lcb.app.data.LcbDatabase { *; }
-keep class com.example.lcb.app.data.LcbDatabase_Impl { *; }
-keep class com.example.lcb.app.data.DailyStepEntity { *; }
-keep class com.example.lcb.app.data.HydrationRecordEntity { *; }
-keep interface com.example.lcb.app.data.*Dao { *; }
-keep class com.example.lcb.app.data.*Dao_Impl { *; }

# App-owned renderer callbacks are passed into the ad SDK through interfaces.
# SDK packages themselves rely on their bundled consumer rules.
-keep class com.example.lcb.app.ad.LcbAdInitializer { *; }
-keep class com.example.lcb.app.ad.renderer.** { *; }
