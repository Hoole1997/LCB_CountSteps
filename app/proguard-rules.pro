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
-keep class com.example.lcb.app.HydrateActivity { *; }
-keep class com.example.lcb.app.ReportDetailActivity { *; }
-keep class com.example.lcb.app.HydrateReportActivity { *; }
-keep class com.example.lcb.app.AchievementActivity { *; }

# AndroidX can restore Fragments/DialogFragments from saved class names after
# process death. Keep app-owned fragment names and default constructors stable.
-keep class com.example.lcb.app.HomeFragment { *; }
-keep class com.example.lcb.app.TrendsFragment { *; }
-keep class com.example.lcb.app.MeFragment { *; }
-keep class com.example.lcb.app.ui.sheets.** extends androidx.fragment.app.Fragment { *; }
-keep class com.example.lcb.app.ui.sheets.** extends androidx.fragment.app.DialogFragment { *; }
-keep class com.example.lcb.app.ui.sheets.** extends com.google.android.material.bottomsheet.BottomSheetDialogFragment { *; }

# ViewModelProvider creates AndroidViewModel instances through constructors.
-keep class com.example.lcb.app.LcbAppViewModel { *; }

# XML inflation and launcher custom view provider use class names / public
# constructors. Keep app custom views stable for release resource inflation.
-keep class com.example.lcb.app.ui.hydrate.HydrateHeaderBackgroundView { *; }
-keep class com.example.lcb.app.ui.hydrate.WaterDropWaveView { *; }
-keep class com.example.lcb.app.ui.hydrate.AddWaterIconView { *; }
-keep class com.example.lcb.app.ui.sheets.WeightWheelPickerView { *; }
-keep class com.example.lcb.app.launcher.StepLauncherWidgetView { *; }
-keep class com.example.lcb.app.launcher.StepLauncherWidgetManager { *; }

# Keep enum names used across Intent extras, saved UI state, or string valueOf.
-keep enum com.example.lcb.app.ui.components.TabDestination { *; }
-keep enum com.example.lcb.app.ui.report.ReportMetricType { *; }

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
-keep class com.example.lcb.app.data.WeightRecordEntity { *; }
-keep class com.example.lcb.app.data.HourlyStepEntity { *; }
-keep interface com.example.lcb.app.data.*Dao { *; }
-keep class com.example.lcb.app.data.*Dao_Impl { *; }

# App-owned renderer callbacks are passed into the ad SDK through interfaces.
# SDK packages themselves rely on their bundled consumer rules.
-keep class com.example.lcb.app.ad.LcbAdInitializer { *; }
-keep class com.example.lcb.app.ad.renderer.** { *; }
