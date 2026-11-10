package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class QuettaFirestoreRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. USER PROFILE & ACCOUNT OPERATIONS ---

    suspend fun upsertUserProfile(
        fullName: String,
        phone: String,
        email: String,
        homeLocation: String = "Satellite Town, Quetta",
        workLocation: String = "Zarghoon Road, Quetta",
        emergencyContact: String = "+923008381122"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updatePayload = mapOf(
                    "fullName" to fullName.trim().take(100),
                    "phone" to phone.trim().take(24),
                    "email" to email.trim().take(120),
                    "homeLocation" to homeLocation.trim().take(200),
                    "workLocation" to workLocation.trim().take(200),
                    "emergencyContact" to emergencyContact.trim().take(30),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "fullName" to fullName.trim().ifBlank { "Quetta Commuter" }.take(100),
                    "phone" to phone.trim().ifBlank { "+923118004421" }.take(24),
                    "email" to email.trim().ifBlank { "user@quettaride.pk" }.take(120),
                    "homeLocation" to homeLocation.trim().take(200),
                    "workLocation" to workLocation.trim().take(200),
                    "emergencyContact" to emergencyContact.trim().take(30),
                    "totalRides" to 0L,
                    "rating" to 5.0,
                    "walletBalancePkr" to 1500L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun getUserProfile(userId: String): Result<CloudUserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            snap.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeUserProfile(userId: String): Flow<CloudUserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    snap.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun deleteUserAccountData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 2. DRIVER ONBOARDING, DOCUMENTS & AVAILABILITY ---

    suspend fun registerDriverProfile(
        fullName: String,
        phone: String,
        email: String,
        cnic: String,
        licenseNumber: String,
        vehicleCategory: String,
        vehicleModel: String,
        vehiclePlate: String,
        vehicleColor: String,
        latitude: Double = 30.1920,
        longitude: Double = 67.0099
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("drivers").document(uid)
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().ifBlank { "Captain Quetta" }.take(100),
            "phone" to phone.trim().ifBlank { "+923138429910" }.take(24),
            "email" to email.trim().ifBlank { "captain@quettaride.pk" }.take(120),
            "cnic" to cnic.trim().ifBlank { "54400-8392014-1" }.take(20),
            "licenseNumber" to licenseNumber.trim().ifBlank { "QTA-LTV-8821" }.take(40),
            "vehicleCategory" to vehicleCategory,
            "vehicleModel" to vehicleModel.trim().ifBlank { "Suzuki Alto VXR" }.take(80),
            "vehiclePlate" to vehiclePlate.trim().ifBlank { "QTA-25-1102" }.take(24),
            "vehicleColor" to vehicleColor.trim().ifBlank { "Pearl White" }.take(40),
            "approvalStatus" to "PENDING",
            "isOnline" to false,
            "latitude" to latitude,
            "longitude" to longitude,
            "rating" to 5.0,
            "totalTrips" to 0L,
            "todayEarningsPkr" to 0L,
            "todayRides" to 0L,
            "documentsVerified" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun uploadDriverDocument(
        docType: String,
        docNumber: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("driver_documents").document()
        val payload = mapOf(
            "userId" to uid,
            "docType" to docType,
            "docNumber" to docNumber.trim().ifBlank { "54400-8392014-1" }.take(50),
            "verificationStatus" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateDriverAvailabilityAndLocation(
        isOnline: Boolean,
        latitude: Double,
        longitude: Double
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("drivers").document(uid)
        val updateMap = mapOf(
            "isOnline" to isOnline,
            "latitude" to latitude,
            "longitude" to longitude,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updateMap).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeApprovedDrivers(): Flow<List<CloudDriverProfile>> = flow {
        val uid = requireUserId()
        val path = "drivers"
        emitAll(
            db.collection(path)
                .whereEqualTo("approvalStatus", "APPROVED")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CloudDriverProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // --- 3. REAL-TIME BOOKING ENGINE & TRANSACTIONAL DRIVER MATCHING ---

    suspend fun createRideBooking(
        passengerName: String,
        passengerPhone: String,
        vehicleCategory: String,
        pickupName: String,
        pickupLat: Double,
        pickupLng: Double,
        destName: String,
        destLat: Double,
        destLng: Double,
        distanceKm: Double,
        estimatedMinutes: Int,
        farePkr: Int,
        paymentMethod: String = "CASH"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("bookings").document()
        val safeFare = farePkr.coerceIn(50, 100000)
        val commission = ((safeFare * 15) / 100).coerceAtLeast(10)
        val netDriver = (safeFare - commission).coerceAtLeast(40)

        val payload = mapOf(
            "passengerId" to uid,
            "passengerName" to passengerName.trim().ifBlank { "Passenger" }.take(100),
            "passengerPhone" to passengerPhone.trim().ifBlank { "+923118004421" }.take(24),
            "vehicleCategory" to vehicleCategory,
            "pickupName" to pickupName.trim().take(150),
            "pickupLat" to pickupLat,
            "pickupLng" to pickupLng,
            "destName" to destName.trim().take(150),
            "destLat" to destLat,
            "destLng" to destLng,
            "distanceKm" to distanceKm.coerceIn(0.5, 500.0),
            "estimatedMinutes" to estimatedMinutes.coerceIn(2, 600),
            "farePkr" to safeFare,
            "commissionPkr" to commission,
            "driverNetPkr" to netDriver,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to "PENDING",
            "status" to "SEARCHING_DRIVER",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    /**
     * Uses a Firestore atomic transaction to ensure only ONE driver can accept an open ride.
     * Prevents duplicate driver assignment race conditions.
     */
    suspend fun acceptRideBookingTransaction(
        bookingId: String,
        driverName: String,
        vehicleModel: String,
        vehiclePlate: String,
        driverLat: Double,
        driverLng: Double
    ): Result<Unit> = runCatching {
        val driverUid = requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStatus = snapshot.getString("status")
                if (currentStatus != "SEARCHING_DRIVER") {
                    throw IllegalStateException("Ride $bookingId is no longer available (current status: $currentStatus)")
                }
                transaction.update(
                    docRef,
                    mapOf(
                        "driverId" to driverUid,
                        "driverName" to driverName.trim().take(100),
                        "vehicleModel" to vehicleModel.trim().take(80),
                        "vehiclePlate" to vehiclePlate.trim().take(24),
                        "status" to "DRIVER_ASSIGNED",
                        "driverLat" to driverLat,
                        "driverLng" to driverLng,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRideStatusAndGps(
        bookingId: String,
        newStatus: String,
        driverLat: Double,
        driverLng: Double,
        paymentStatus: String = "PENDING"
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "paymentStatus" to paymentStatus,
                    "driverLat" to driverLat,
                    "driverLng" to driverLng,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun submitPassengerRating(
        bookingId: String,
        ratingStars: Int,
        reviewText: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("bookings").document(bookingId)
        try {
            docRef.update(
                mapOf(
                    "passengerRating" to ratingStars.coerceIn(1, 5),
                    "passengerReview" to reviewText.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observePassengerBookings(): Flow<List<CloudRideBooking>> = flow {
        val uid = requireUserId()
        val path = "bookings"
        emitAll(
            db.collection(path)
                .whereEqualTo("passengerId", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CloudRideBooking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeOpenRideRequests(): Flow<List<CloudRideBooking>> = flow {
        val uid = requireUserId()
        val path = "bookings"
        emitAll(
            db.collection(path)
                .whereEqualTo("status", "SEARCHING_DRIVER")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CloudRideBooking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // --- 4. LIVE GPS TELEMETRY STREAM ---

    suspend fun recordRideLocationTelemetry(
        bookingId: String,
        role: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Double,
        speedMps: Double
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("ride_locations").document()
        val payload = mapOf(
            "bookingId" to bookingId,
            "userId" to uid,
            "role" to role,
            "latitude" to latitude,
            "longitude" to longitude,
            "accuracyMeters" to accuracyMeters.coerceIn(0.0, 5000.0),
            "speedMps" to speedMps.coerceIn(0.0, 100.0),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- 5. SUPPORT COMPLAINTS & AUDIT LOGS ---

    suspend fun createSupportComplaint(
        submittedBy: String,
        role: String,
        bookingId: String,
        subject: String,
        description: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("complaints").document()
        val payload = mapOf(
            "userId" to uid,
            "submittedBy" to submittedBy.trim().ifBlank { "Quetta User" }.take(100),
            "role" to if (role == "DRIVER") "DRIVER" else "PASSENGER",
            "bookingId" to bookingId.trim().ifBlank { "QR-GENERAL" }.take(128),
            "subject" to subject.trim().ifBlank { "Support Inquiry" }.take(150),
            "description" to description.trim().ifBlank { "Submitted via mobile app" }.take(1000),
            "status" to "OPEN",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun writeAuditLog(
        action: String,
        targetId: String,
        details: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("audit_logs").document()
        val payload = mapOf(
            "actorUid" to uid,
            "action" to action.trim().take(100),
            "targetId" to targetId.trim().take(128),
            "details" to details.trim().take(500),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
