# PhotoField ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends dagger.hilt.android.HiltApplication

# Keep Room entities and DAOs
-keep class com.photofieldapp.data.local.entity.** { *; }
-keep class com.photofieldapp.data.local.dao.** { *; }
-keep class com.photofieldapp.data.local.AppDatabase { *; }

# Keep Hilt ViewModels
-keep class com.photofieldapp.presentation.viewmodel.** { *; }

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep CameraX
-keep class androidx.camera.** { *; }

# Keep osmdroid (OpenStreetMap)
-keep class org.osmdroid.** { *; }
-keep class org.slf4j.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.datetime.** { *; }

# Keep Coil
-keep class coil3.** { *; }
-keep class io.coil.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Prevent obfuscation of enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}