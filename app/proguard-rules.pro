# Project specific ProGuard rules

# Keep Room Database Entities and DAOs
-keep class com.example.data.db.entities.** { *; }
-keep class com.example.data.db.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Retrofit, Moshi, and API Network Models
-keep class com.example.data.network.** { *; }
-keep class com.example.domain.model.** { *; }

# Keep annotations used by Retrofit, Moshi, Room, and Jackson/Gson
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Retrofit & OkHttp ProGuard Rules
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Moshi rules
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-dontwarn com.squareup.moshi.**

# Preserve Kotlin Serialization / Metadata if used
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

