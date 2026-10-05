# Base rules
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod
-keep class kotlin.Metadata { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Moshi codegen
-keep class com.squareup.moshi.** { *; }
-keep class *JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.JsonClass *;
}
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Firebase & Google Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# App-specific data models & components (using detected namespace com.example)
-keep class com.example.data.** { *; }
-keep class com.example.**$Companion { *; }
-keepclassmembers class com.example.data.** { *; }

-keep class com.example.data.remote.CurrencyApiResponse { *; }
-keep class com.example.data.local.CalculationEntity { *; }
-keep class com.example.data.local.AppDatabase { *; }
-keep class com.example.data.local.CalculationDao { *; }
-keep class com.example.data.remote.CurrencyApiService { *; }
