package com.photofieldapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationUtil private constructor(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val priority: Int = Priority.PRIORITY_HIGH_ACCURACY
) {

    companion object {
        @Volatile
        private var INSTANCE: LocationUtil? = null

        fun getInstance(
            context: Context,
            fusedLocationClient: FusedLocationProviderClient,
            priority: Int = Priority.PRIORITY_HIGH_ACCURACY
        ): LocationUtil {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationUtil(context.applicationContext, fusedLocationClient, priority).also { INSTANCE = it }
            }
        }
    }

    suspend fun getCurrentLocation(
        owner: LifecycleOwner? = null,
        timeoutMs: Long = 10000
    ): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val deferred = CompletableDeferred<Location?>()
        val cts = CancellationTokenSource()
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    deferred.complete(location)
                }
            }
        }

        val request = LocationRequest.Builder(priority, 0L)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(request, callback, cts.token)

            // Also try to get last known location immediately
            val lastLocation = fusedLocationClient.getLastLocation().await()
            if (lastLocation != null && !deferred.isCompleted) {
                deferred.complete(lastLocation)
            }

            // Wait with timeout
            val result = kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
                deferred.await()
            }

            cts.cancel()
            fusedLocationClient.removeLocationUpdates(callback)
            result

        } catch (e: Exception) {
            cts.cancel()
            fusedLocationClient.removeLocationUpdates(callback)
            null
        }
    }

    fun getCurrentLocationCallback(
        onResult: (Location?) -> Unit,
        timeoutMs: Long = 10000
    ) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                onResult(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        val request = LocationRequest.Builder(priority, 0L)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(request, callback, context.mainExecutor)

        // Timeout fallback
        android.os.Handler(context.mainLooper).postDelayed({
            fusedLocationClient.removeLocationUpdates(callback)
            // Try last known location as fallback
            fusedLocationClient.getLastLocation().addOnSuccessListener { location ->
                onResult(location)
            }.addOnFailureListener {
                onResult(null)
            }
        }, timeoutMs)
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            requestCode
        )
    }
}