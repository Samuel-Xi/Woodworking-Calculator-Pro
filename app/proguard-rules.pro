# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Compose-friendly defaults (ProGuard 7+ understands)
-dontwarn org.jetbrains.annotations.**
