# =======================================================
# Consumer ProGuard Rules for AppStorys SDK
# =======================================================
#
# These rules are for the app that consumes this SDK.
# They ensure that all public APIs and third-party dependencies
# used by this SDK are not removed or obfuscated.

# -------------------------------------------------------
# Keep the public API of the SDK.
# This prevents the app from removing classes and methods
# that developers need to call directly.
# -------------------------------------------------------
-keep class com.appversal.appstorys.AppStorysAPI { *; }
-keep class com.appversal.appstorys.AppStorysAPI$* { *; }

# Keep utility classes that are meant to be public.
# Remove this if the utils package is not part of the public API.
-keep class com.appversal.appstorys.utils.** { *; }

# -------------------------------------------------------
# Keep third-party libraries used by the SDK.
# This ensures that the app doesn't remove necessary parts
# of the MQTT library.
# -------------------------------------------------------
-keep class org.eclipse.paho.client.mqttv3.** {*;}
-keep class org.eclipse.paho.client.mqttv3.*$* { *; }
-keep class org.eclipse.paho.client.mqttv3.logging.JSR47Logger { *; }

# -------------------------------------------------------
# Keep rules for Android platform features used by the SDK,
# like Compose and Kotlin reflection/serialization.
# These are crucial to prevent crashes in the consuming app.
# -------------------------------------------------------
# Keep composable functions annotations
-keep class androidx.compose.runtime.* { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Kotlin-specific features that R8 might otherwise remove.
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses,PermittedSubclasses,EnclosingMethod

# -------------------------------------------------------
# Recommended rules to avoid warnings when consuming the SDK.
# -------------------------------------------------------
-dontwarn java.lang.invoke.StringConcatFactory