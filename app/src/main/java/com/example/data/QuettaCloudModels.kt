package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val homeLocation: String = "",
    val workLocation: String = "",
    val emergencyContact: String = "",
    val totalRides: Long = 0L,
    val rating: Double = 5.0,
    val walletBalancePkr: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudDriverProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val cnic: String = "",
    val licenseNumber: String = "",
    val vehicleCategory: String = "ALTO",
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val vehicleColor: String = "",
    val approvalStatus: String = "PENDING",
    val isOnline: Boolean = false,
    val latitude: Double = 30.1920,
    val longitude: Double = 67.0099,
    val rating: Double = 5.0,
    val totalTrips: Long = 0L,
    val todayEarningsPkr: Long = 0L,
    val todayRides: Long = 0L,
    val documentsVerified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudDriverDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val docType: String = "CNIC",
    val docNumber: String = "",
    val verificationStatus: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudRideBooking(
    @DocumentId val id: String = "",
    val passengerId: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicleCategory: String = "ALTO",
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val pickupName: String = "",
    val pickupLat: Double = 30.1920,
    val pickupLng: Double = 67.0099,
    val destName: String = "",
    val destLat: Double = 30.1620,
    val destLng: Double = 67.0220,
    val distanceKm: Double = 5.0,
    val estimatedMinutes: Long = 12L,
    val farePkr: Long = 350L,
    val commissionPkr: Long = 52L,
    val driverNetPkr: Long = 298L,
    val paymentMethod: String = "CASH",
    val paymentStatus: String = "PENDING",
    val status: String = "SEARCHING_DRIVER",
    val driverLat: Double = 30.1920,
    val driverLng: Double = 67.0099,
    val passengerRating: Long = 5L,
    val passengerReview: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudRideLocationTelemetry(
    @DocumentId val id: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val role: String = "DRIVER",
    val latitude: Double = 30.1920,
    val longitude: Double = 67.0099,
    val accuracyMeters: Double = 5.0,
    val speedMps: Double = 8.5,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudFareConfiguration(
    @DocumentId val id: String = "",
    val categoryId: String = "ALTO",
    val baseFarePkr: Long = 140L,
    val perKmRatePkr: Long = 52L,
    val perMinuteRatePkr: Long = 7L,
    val minimumFarePkr: Long = 280L,
    val waitingChargePerMinPkr: Long = 5L,
    val cancellationFeePkr: Long = 100L,
    val commissionPercent: Long = 15L,
    val isActive: Boolean = true,
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudSupportComplaint(
    @DocumentId val id: String = "",
    val userId: String = "",
    val submittedBy: String = "",
    val role: String = "PASSENGER",
    val bookingId: String = "",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudAuditLog(
    @DocumentId val id: String = "",
    val actorUid: String = "",
    val action: String = "",
    val targetId: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
