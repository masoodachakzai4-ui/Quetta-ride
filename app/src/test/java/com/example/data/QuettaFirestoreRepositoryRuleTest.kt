package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [36])
class QuettaFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertAndGetUserProfile_authenticatedOwner_succeedsAndIsolatesCrossUser() = runBlocking {
        val passengerUid = signInTestUser("masood.passenger@quettaride.pk")
        val repository = QuettaFirestoreRepository(firestore)

        val upsertResult = withTimeout(5000L) {
            repository.upsertUserProfile(
                fullName = "Masood Khan Achakzai",
                phone = "+923118004421",
                email = "masood.passenger@quettaride.pk",
                homeLocation = "Satellite Town, Quetta"
            )
        }
        assertTrue("Upsert user profile should succeed", upsertResult.isSuccess)

        val profileResult = withTimeout(5000L) {
            repository.getUserProfile(passengerUid)
        }
        assertTrue(profileResult.isSuccess)
        assertEquals("Masood Khan Achakzai", profileResult.getOrNull()?.fullName)

        // Sign in as another user and verify PII read is blocked by security rules
        signInTestUser("stranger@quettaride.pk")
        val crossReadResult = withTimeout(5000L) {
            repository.getUserProfile(passengerUid)
        }
        assertTrue("Cross-user PII read must fail", crossReadResult.isFailure)
        val rootEx = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, rootEx?.code)
    }

    @Test
    fun bookingLifecycle_createAndTransactionalAccept_preventsDuplicateDriverAcceptance() = runBlocking {
        signInTestUser("passenger.booking@quettaride.pk")
        val repo = QuettaFirestoreRepository(firestore)

        val createRes = withTimeout(5000L) {
            repo.createRideBooking(
                passengerName = "Dr. Ayesha Durrani",
                passengerPhone = "+923342190843",
                vehicleCategory = "ALTO",
                pickupName = "Jinnah Road, Quetta",
                pickupLat = 30.1920,
                pickupLng = 67.0099,
                destName = "Bolan Medical Complex, Brewery Road",
                destLat = 30.1750,
                destLng = 66.9450,
                distanceKm = 4.8,
                estimatedMinutes = 13,
                farePkr = 390,
                paymentMethod = "CASH"
            )
        }
        assertTrue("Passenger createRideBooking must succeed", createRes.isSuccess)
        val bookingId = createRes.getOrThrow()

        // First driver accepts transactionally -> succeeds
        signInTestUser("captain.mirwais@quettaride.pk")
        val accept1 = withTimeout(5000L) {
            repo.acceptRideBookingTransaction(
                bookingId = bookingId,
                driverName = "Mirwais Khan Achakzai",
                vehicleModel = "Suzuki Alto VXR",
                vehiclePlate = "QTA-24-8812",
                driverLat = 30.1905,
                driverLng = 67.0060
            )
        }
        assertTrue("First driver accept transaction must succeed", accept1.isSuccess)

        // Second driver attempts to accept the same ride -> fails transactionally
        signInTestUser("captain.bilal@quettaride.pk")
        val accept2 = withTimeout(5000L) {
            repo.acceptRideBookingTransaction(
                bookingId = bookingId,
                driverName = "Bilal Ahmed Mengal",
                vehicleModel = "Sazgar Rickshaw",
                vehiclePlate = "QTA-22-4190",
                driverLat = 30.1880,
                driverLng = 67.0010
            )
        }
        assertTrue("Second driver duplicate acceptance must fail", accept2.isFailure)
    }

    @Test
    fun observePassengerBookings_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = QuettaFirestoreRepository(firestore)

        var caughtException: Throwable? = null
        try {
            withTimeout(3000L) {
                repo.observePassengerBookings().first()
            }
        } catch (e: Throwable) {
            caughtException = e
        }
        assertNotNull("Unauthenticated query must throw an exception", caughtException)
    }
}
