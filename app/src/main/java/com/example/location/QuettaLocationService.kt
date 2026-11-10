package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.*

data class RealGpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5.0f,
    val speedMps: Float = 0.0f,
    val bearing: Float = 0.0f,
    val isLiveHardwareFix: Boolean = true
)

object QuettaGpsCoordinates {
    // Authentic Quetta, Balochistan WGS84 Coordinates
    val JINNAH_ROAD = RealGpsCoordinate(30.1920, 67.0099, isLiveHardwareFix = false)
    val SATELLITE_TOWN = RealGpsCoordinate(30.1620, 67.0220, isLiveHardwareFix = false)
    val SARIAB_ROAD = RealGpsCoordinate(30.1450, 66.9880, isLiveHardwareFix = false)
    val AIRPORT_ROAD = RealGpsCoordinate(30.2250, 66.9650, isLiveHardwareFix = false)
    val BREWERY_ROAD = RealGpsCoordinate(30.1750, 66.9450, isLiveHardwareFix = false)
    val RAILWAY_STATION = RealGpsCoordinate(30.1835, 67.0010, isLiveHardwareFix = false)
    val QUETTA_AIRPORT = RealGpsCoordinate(30.2514, 66.9378, isLiveHardwareFix = false)
    val KUCHLAK = RealGpsCoordinate(30.3180, 66.9550, isLiveHardwareFix = false)
    val ZARGHOON_ROAD = RealGpsCoordinate(30.2015, 67.0260, isLiveHardwareFix = false)
    val HANNA_VALLEY = RealGpsCoordinate(30.2580, 67.1050, isLiveHardwareFix = false)

    fun forLandmarkId(id: String): RealGpsCoordinate = when (id) {
        "loc_jinnah" -> JINNAH_ROAD
        "loc_satellite" -> SATELLITE_TOWN
        "loc_sariab" -> SARIAB_ROAD
        "loc_airport_rd" -> AIRPORT_ROAD
        "loc_brewery" -> BREWERY_ROAD
        "loc_railway" -> RAILWAY_STATION
        "loc_airport" -> QUETTA_AIRPORT
        "loc_kuchlak" -> KUCHLAK
        "loc_zarghoon" -> ZARGHOON_ROAD
        "loc_hanna" -> HANNA_VALLEY
        else -> JINNAH_ROAD
    }

    /**
     * Calculates exact Haversine great-circle road distance (with 1.25x urban road curvature factor)
     * in kilometers between two GPS coordinates.
     */
    fun calculateRoadDistanceKm(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val straightLineKm = earthRadiusKm * c
        val roadEstimatedKm = straightLineKm * 1.25
        return max(1.2, (roadEstimatedKm * 10.0).roundToInt() / 10.0)
    }
}

class QuettaLocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    fun hasLocationPermission(): Boolean {
        return try {
            val fine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            fine || coarse
        } catch (_: Throwable) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationFix(): RealGpsCoordinate? {
        if (!hasLocationPermission()) return null
        return try {
            val loc: Location? = withTimeoutOrNull(3000L) {
                fusedClient.lastLocation.await()
                    ?: fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            }
            loc?.let {
                RealGpsCoordinate(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyMeters = it.accuracy,
                    speedMps = it.speed,
                    bearing = it.bearing,
                    isLiveHardwareFix = true
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun observeLiveLocationUpdates(intervalMs: Long = 5000L): Flow<RealGpsCoordinate> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(2500L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                trySend(
                    RealGpsCoordinate(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        speedMps = loc.speed,
                        bearing = loc.bearing,
                        isLiveHardwareFix = true
                    )
                )
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (t: Throwable) {
            close()
            return@callbackFlow
        }

        awaitClose {
            try {
                fusedClient.removeLocationUpdates(callback)
            } catch (_: Throwable) {
            }
        }
    }
}
