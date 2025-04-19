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

# Keep all public classes and methods in your package
#-keep class com.appversal.appstorys.AppStorys { *; }

# Remove all logging (hides debug info)
#-assumenosideeffects class android.util.Log { *; }

# Obfuscate everything else
#-dontwarn com.appversal.appstorys

#-dontwarn java.lang.invoke.StringConcatFactory


# Keep the public API
-keep class com.appversal.appstorys.AppStorysAPI { *; }
-keep class com.appversal.appstorys.AppStorysAPI$* { *; }

# Keep composable functions annotations
-keep class androidx.compose.runtime.* { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Obfuscate all internal implementation
-keep class !com.appversal.appstorys.AppStorysAPI,com.appversal.appstorys.** { *; }
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses,PermittedSubclasses,EnclosingMethod

# For Kotlin specific features
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}