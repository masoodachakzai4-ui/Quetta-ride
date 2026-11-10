package com.example.model

enum class PlatformRole(val label: String, val badge: String) {
    PASSENGER("Passenger App", "Ride"),
    DRIVER("Driver App", "Captain"),
    ADMIN("Admin Panel", "HQ"),
    WEBSITE("Web Portal", "Startup")
}

enum class VehicleCategoryId(
    val displayName: String,
    val urduTag: String,
    val idealFor: String,
    val defaultCapacity: Int
) {
    BIKE("Motorcycle / Bike", "تیز اور سستا", "1 Passenger • Solo fast commute", 1),
    RICKSHAW("Auto Rickshaw", "روایتی اور کشادہ", "2–3 Passengers • Local Bazaar & neighborhood trips", 3),
    ALTO("Alto / Small Car", "آرام دہ فیملی سفر", "4 Passengers • Family & AC/Heater comfort", 4)
}

data class VehicleCategoryConfig(
    val id: VehicleCategoryId,
    val baseFarePkr: Int,
    val perKmRatePkr: Int,
    val perMinuteRatePkr: Int,
    val minimumFarePkr: Int,
    val avgEtaMinutes: Int,
    val isActive: Boolean = true,
    val description: String
)

data class CityLocation(
    val id: String,
    val name: String,
    val area: String,
    val city: String = "Quetta",
    val mapX: Float, // normalized 0.1f..0.9f on custom map canvas
    val mapY: Float, // normalized 0.1f..0.9f on custom map canvas
    val isPopular: Boolean = true,
    val landmarkType: String = "Hub"
)

data class ServiceCity(
    val name: String,
    val province: String,
    val isLive: Boolean,
    val launchPhase: String,
    val activeDriversCount: Int
)

enum class DriverApprovalStatus(val label: String) {
    APPROVED("Approved"),
    PENDING("Pending Review"),
    REJECTED("Rejected")
}

data class DriverProfile(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val cnic: String,
    val vehicleCategory: VehicleCategoryId,
    val vehicleModel: String,
    val vehiclePlate: String,
    val vehicleColor: String,
    val rating: Double,
    val totalTrips: Int,
    val isOnline: Boolean,
    val approvalStatus: DriverApprovalStatus,
    val mapX: Float,
    val mapY: Float,
    val todayEarningsPkr: Int,
    val todayRides: Int,
    val documentsVerified: Boolean,
    val avatarSeed: Int,
    val languages: String = "Pashto, Balochi, Brahui, Urdu"
)

data class PassengerProfile(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val homeLocation: String,
    val workLocation: String,
    val totalRides: Int,
    val rating: Double,
    val walletBalancePkr: Int,
    val memberSince: String,
    val emergencyContact: String
)

enum class RideStatus(val label: String, val stepIndex: Int) {
    SEARCHING_DRIVER("Searching Nearby Drivers", 1),
    DRIVER_ASSIGNED("Driver Assigned & En Route", 2),
    DRIVER_ARRIVING("Driver Arriving at Pickup", 3),
    IN_PROGRESS("Ride in Progress", 4),
    COMPLETED("Ride Completed", 5),
    CANCELLED("Cancelled", 0)
}

data class FareBreakdown(
    val vehicleCategory: VehicleCategoryId,
    val distanceKm: Double,
    val durationMinutes: Int,
    val baseFarePkr: Int,
    val distanceChargePkr: Int,
    val timeChargePkr: Int,
    val promoDiscountPkr: Int,
    val totalEstimatedPkr: Int
)

data class RideBooking(
    val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val driverId: String?,
    val driverName: String?,
    val vehicleCategory: VehicleCategoryId,
    val vehicleModel: String,
    val vehiclePlate: String,
    val pickupLocation: CityLocation,
    val destinationLocation: CityLocation,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val farePkr: Int,
    val status: RideStatus,
    val createdAt: String,
    val paymentMethod: String = "Cash / JazzCash / EasyPaisa",
    val passengerRating: Int? = null,
    val passengerReview: String? = null,
    val rideProgressFraction: Float = 0f
)

data class ComplaintItem(
    val id: String,
    val submittedBy: String,
    val role: String,
    val bookingId: String,
    val subject: String,
    val description: String,
    val status: String, // "Open", "In Review", "Resolved"
    val date: String
)

data class AppNotification(
    val id: String,
    val targetRole: PlatformRole,
    val title: String,
    val message: String,
    val timestamp: String,
    val isUnread: Boolean = true
)

data class PromoOffer(
    val code: String,
    val title: String,
    val discountPercent: Int,
    val maxDiscountPkr: Int,
    val validCity: String,
    val isActive: Boolean = true
)

enum class PassengerScreen(val title: String) {
    SPLASH("Splash Screen"),
    WELCOME("Welcome to Quetta Ride"),
    LOGIN("Login"),
    SIGN_UP("Passenger Sign Up"),
    OTP_VERIFY("OTP Verification"),
    HOME("Passenger Home"),
    PICKUP_SELECT("Select Pickup Location"),
    DESTINATION_SELECT("Select Destination"),
    VEHICLE_SELECT("Choose Vehicle & Fare"),
    FARE_BREAKDOWN("Fare Estimation Details"),
    NEARBY_DRIVERS("Nearby Drivers in Quetta"),
    BOOKING_CONFIRM("Booking Confirmation"),
    DRIVER_DETAILS("Driver Profile & Vehicle"),
    LIVE_TRACKING("Live Ride Tracking"),
    RIDE_IN_PROGRESS("Ride in Progress"),
    RIDE_COMPLETED("Ride Completed"),
    RATING_REVIEW("Rate Your Captain"),
    RIDE_HISTORY("Ride History"),
    PROFILE("My Profile"),
    HELP_SUPPORT("Help & Support")
}

enum class DriverScreen(val title: String) {
    SPLASH("Driver Splash"),
    LOGIN("Captain Login"),
    REGISTER_PERSONAL("1. Personal Info"),
    REGISTER_VEHICLE("2. Vehicle Details"),
    REGISTER_DOCS("3. Document Upload"),
    DASHBOARD("Captain Dashboard"),
    NEW_REQUEST("New Ride Request"),
    PICKUP_NAVIGATION("Navigate to Pickup"),
    RIDE_IN_PROGRESS("Active Trip"),
    COMPLETE_RIDE("Trip Summary"),
    DAILY_EARNINGS("Daily Earnings"),
    RIDE_HISTORY("Trip History"),
    PROFILE("Captain Profile"),
    RATINGS("Ratings & Reviews"),
    SETTINGS("Driver Settings")
}

enum class AdminSection(val title: String) {
    DASHBOARD("Dashboard"),
    PASSENGERS("Passengers"),
    DRIVERS("Drivers"),
    VEHICLES("Vehicle Management"),
    RIDES("Ride Management"),
    BOOKINGS("Bookings"),
    LOCATIONS("Locations & Cities"),
    FARES("Fare Management"),
    PAYMENTS("Payments"),
    COMPLAINTS("Complaints"),
    REVIEWS("Reviews"),
    REPORTS("Reports"),
    NOTIFICATIONS("Notifications"),
    SETTINGS("Settings")
}

enum class WebsitePage(val title: String) {
    HOME("Home"),
    ABOUT("About Us"),
    HOW_IT_WORKS("How It Works"),
    SERVICES("Our Services"),
    BECOME_DRIVER("Become a Driver"),
    SAFETY("Safety"),
    DOWNLOAD("Download App"),
    CONTACT("Contact Us"),
    LOGIN_REGISTER("Demo Access & Auth")
}
