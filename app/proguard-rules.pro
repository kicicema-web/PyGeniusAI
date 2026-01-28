# ProGuard rules for PyGenius AI

# Keep Python runtime
-keep class com.chaquo.python.** { *; }
-keep class com.pygeniusai.python.** { *; }

# Keep data classes
-keep class com.pygeniusai.data.** { *; }

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Compose
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# Gson
-keep class com.google.gson.** { *; }
-keep class * { @com.google.gson.annotations.SerializedName <fields>; }
