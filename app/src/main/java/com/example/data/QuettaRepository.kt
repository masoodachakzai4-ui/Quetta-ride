package com.example.data

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

class QuettaRepository(context: Context? = null) {

    private val prefs = context?.getSharedPreferences("quetta_ride_demo_prefs", Context.MODE_PRIVATE)

    fun defaultVehicleConfigs(): List<VehicleCategoryConfig> = listOf(
        VehicleCategoryConfig(
            id = VehicleCategoryId.BIKE,
            baseFarePkr = 45,
            perKmRatePkr = 22,
            perMinuteRatePkr = 3,
            minimumFarePkr = 90,
            avgEtaMinutes = 3,
            isActive = true,
            description = "Fastest solo ride through Jinnah Road & Liaquat Bazaar traffic. Helmet provided."
        ),
        VehicleCategoryConfig(
            id = VehicleCategoryId.RICKSHAW,
            baseFarePkr = 80,
            perKmRatePkr = 36,
            perMinuteRatePkr = 5,
            minimumFarePkr = 160,
            avgEtaMinutes = 4,
            isActive = true,
            description = "Spacious 3-wheeler ideal for 2–3 passengers and shopping bags across Quetta."
        ),
        VehicleCategoryConfig(
            id = VehicleCategoryId.ALTO,
            baseFarePkr = 140,
            perKmRatePkr = 52,
            perMinuteRatePkr = 7,
            minimumFarePkr = 280,
            avgEtaMinutes = 5,
            isActive = true,
            description = "Comfortable Suzuki Alto 660cc with AC/Heater for families, airport & weather protection."
        )
    )

    fun defaultQuettaLocations(): List<CityLocation> = listOf(
        CityLocation(
            id = "loc_jinnah",
            name = "Jinnah Road",
            area = "Commercial Center & Kandahari Bazaar",
            city = "Quetta",
            mapX = 0.50f,
            mapY = 0.48f,
            isPopular = true,
            landmarkType = "Commercial Hub"
        ),
        CityLocation(
            id = "loc_satellite",
            name = "Satellite Town",
            area = "Block 3 & Commercial Market",
            city = "Quetta",
            mapX = 0.64f,
            mapY = 0.68f,
            isPopular = true,
            landmarkType = "Residential & Education"
        ),
        CityLocation(
            id = "loc_sariab",
            name = "Sariab Road",
            area = "University of Balochistan Gate",
            city = "Quetta",
            mapX = 0.46f,
            mapY = 0.78f,
            isPopular = true,
            landmarkType = "Education Corridor"
        ),
        CityLocation(
            id = "loc_airport_rd",
            name = "Airport Road",
            area = "Alamdar Chowk & Shaikh Zayed Hospital",
            city = "Quetta",
            mapX = 0.34f,
            mapY = 0.28f,
            isPopular = true,
            landmarkType = "Main Arterial"
        ),
        CityLocation(
            id = "loc_brewery",
            name = "Brewery Road",
            area = "Bolan Medical Complex & BUITEMS",
            city = "Quetta",
            mapX = 0.24f,
            mapY = 0.56f,
            isPopular = true,
            landmarkType = "Medical & Campus"
        ),
        CityLocation(
            id = "loc_railway",
            name = "Quetta Railway Station",
            area = "Station Road Heritage Terminal",
            city = "Quetta",
            mapX = 0.44f,
            mapY = 0.54f,
            isPopular = true,
            landmarkType = "Transit Hub"
        ),
        CityLocation(
            id = "loc_airport",
            name = "Quetta International Airport",
            area = "Samungli Terminal",
            city = "Quetta",
            mapX = 0.20f,
            mapY = 0.20f,
            isPopular = true,
            landmarkType = "Airport"
        ),
        CityLocation(
            id = "loc_kuchlak",
            name = "Kuchlak",
            area = "N-25 Chaman Highway Northern Hub",
            city = "Quetta",
            mapX = 0.48f,
            mapY = 0.12f,
            isPopular = true,
            landmarkType = "Suburban Town"
        ),
        CityLocation(
            id = "loc_zarghoon",
            name = "Zarghoon Road",
            area = "Civil Secretariat & Serena Chowk",
            city = "Quetta",
            mapX = 0.62f,
            mapY = 0.40f,
            isPopular = true,
            landmarkType = "Civic Zone"
        ),
        CityLocation(
            id = "loc_hanna",
            name = "Hanna Urak Valley Gate",
            area = "Northeast Scenic Foothills",
            city = "Quetta",
            mapX = 0.84f,
            mapY = 0.24f,
            isPopular = true,
            landmarkType = "Recreation"
        )
    )

    fun defaultServiceCities(): List<ServiceCity> = listOf(
        ServiceCity("Quetta", "Balochistan", isLive = true, launchPhase = "Phase 1 • Active HQ", activeDriversCount = 418),
        ServiceCity("Karachi", "Sindh", isLive = false, launchPhase = "Phase 2 • Q2 Expansion", activeDriversCount = 0),
        ServiceCity("Lahore", "Punjab", isLive = false, launchPhase = "Phase 2 • Q3 Expansion", activeDriversCount = 0),
        ServiceCity("Islamabad", "ICT", isLive = false, launchPhase = "Phase 2 • Q3 Expansion", activeDriversCount = 0),
        ServiceCity("Peshawar", "KPK", isLive = false, launchPhase = "Phase 3 • National Rollout", activeDriversCount = 0),
        ServiceCity("Multan", "Punjab", isLive = false, launchPhase = "Phase 3 • National Rollout", activeDriversCount = 0),
        ServiceCity("Faisalabad", "Punjab", isLive = false, launchPhase = "Phase 3 • National Rollout", activeDriversCount = 0),
        ServiceCity("Gwadar", "Balochistan", isLive = false, launchPhase = "Phase 2 • Balochistan Coastal", activeDriversCount = 0)
    )

    fun defaultDrivers(): List<DriverProfile> = listOf(
        DriverProfile(
            id = "drv_1",
            fullName = "Mirwais Khan Achakzai",
            phone = "+92 313 8429910",
            email = "mirwais.captain@quettaride.pk",
            cnic = "54400-8392014-1",
            vehicleCategory = VehicleCategoryId.ALTO,
            vehicleModel = "Suzuki Alto VXR 2024",
            vehiclePlate = "QTA-24-8812",
            vehicleColor = "Pearl White",
            rating = 4.94,
            totalTrips = 642,
            isOnline = true,
            approvalStatus = DriverApprovalStatus.APPROVED,
            mapX = 0.54f,
            mapY = 0.45f,
            todayEarningsPkr = 3850,
            todayRides = 9,
            documentsVerified = true,
            avatarSeed = 1
        ),
        DriverProfile(
            id = "drv_2",
            fullName = "Bilal Ahmed Mengal",
            phone = "+92 333 7891204",
            email = "bilal.mengal@quettaride.pk",
            cnic = "54400-1928374-3",
            vehicleCategory = VehicleCategoryId.RICKSHAW,
            vehicleModel = "Sazgar 4-Stroke Deluxe",
            vehiclePlate = "QTA-22-4190",
            vehicleColor = "Navy & Golden Yellow",
            rating = 4.88,
            totalTrips = 915,
            isOnline = true,
            approvalStatus = DriverApprovalStatus.APPROVED,
            mapX = 0.47f,
            mapY = 0.52f,
            todayEarningsPkr = 2940,
            todayRides = 11,
            documentsVerified = true,
            avatarSeed = 2
        ),
        DriverProfile(
            id = "drv_3",
            fullName = "Zafarullah Kakar",
            phone = "+92 300 9821145",
            email = "zafar.kakar@quettaride.pk",
            cnic = "54400-5541298-7",
            vehicleCategory = VehicleCategoryId.BIKE,
            vehicleModel = "Honda CG 125 Special",
            vehiclePlate = "QTA-23-1109",
            vehicleColor = "Metallic Navy",
            rating = 4.91,
            totalTrips = 1120,
            isOnline = true,
            approvalStatus = DriverApprovalStatus.APPROVED,
            mapX = 0.58f,
            mapY = 0.60f,
            todayEarningsPkr = 2190,
            todayRides = 14,
            documentsVerified = true,
            avatarSeed = 3
        ),
        DriverProfile(
            id = "drv_4",
            fullName = "Saeedullah Baloch",
            phone = "+92 345 8112233",
            email = "saeed.baloch@quettaride.pk",
            cnic = "54400-7731902-5",
            vehicleCategory = VehicleCategoryId.ALTO,
            vehicleModel = "Suzuki Alto VXL AGS",
            vehiclePlate = "QTA-25-0392",
            vehicleColor = "Silver Metallic",
            rating = 4.85,
            totalTrips = 310,
            isOnline = true,
            approvalStatus = DriverApprovalStatus.APPROVED,
            mapX = 0.36f,
            mapY = 0.34f,
            todayEarningsPkr = 3120,
            todayRides = 7,
            documentsVerified = true,
            avatarSeed = 4
        ),
        DriverProfile(
            id = "drv_5",
            fullName = "Najeebullah Lehri",
            phone = "+92 312 4098871",
            email = "najeeb.lehri@quettaride.pk",
            cnic = "54400-9910234-9",
            vehicleCategory = VehicleCategoryId.RICKSHAW,
            vehicleModel = "New Asia Loader/Passenger",
            vehiclePlate = "QTA-21-7761",
            vehicleColor = "Emerald & Yellow",
            rating = 4.79,
            totalTrips = 490,
            isOnline = true,
            approvalStatus = DriverApprovalStatus.APPROVED,
            mapX = 0.30f,
            mapY = 0.59f,
            todayEarningsPkr = 2410,
            todayRides = 8,
            documentsVerified = true,
            avatarSeed = 5
        ),
        DriverProfile(
            id = "drv_6",
            fullName = "Hamza Khan Panezai",
            phone = "+92 321 6723190",
            email = "hamza.panezai@quettaride.pk",
            cnic = "54400-3321984-1",
            vehicleCategory = VehicleCategoryId.BIKE,
            vehicleModel = "Yamaha YBR 125G",
            vehiclePlate = "QTA-24-5501",
            vehicleColor = "Racing Blue",
            rating = 4.96,
            totalTrips = 180,
            isOnline = false,
            approvalStatus = DriverApprovalStatus.PENDING,
            mapX = 0.62f,
            mapY = 0.38f,
            todayEarningsPkr = 0,
            todayRides = 0,
            documentsVerified = true,
            avatarSeed = 6
        ),
        DriverProfile(
            id = "drv_7",
            fullName = "Kamran Shahwani",
            phone = "+92 331 5589012",
            email = "kamran.shahwani@quettaride.pk",
            cnic = "54400-6612098-3",
            vehicleCategory = VehicleCategoryId.ALTO,
            vehicleModel = "Suzuki Alto VX 2023",
            vehiclePlate = "QTA-23-9084",
            vehicleColor = "Graphite Gray",
            rating = 4.82,
            totalTrips = 45,
            isOnline = false,
            approvalStatus = DriverApprovalStatus.PENDING,
            mapX = 0.45f,
            mapY = 0.74f,
            todayEarningsPkr = 0,
            todayRides = 0,
            documentsVerified = true,
            avatarSeed = 7
        )
    )

    fun defaultPassengers(): List<PassengerProfile> = listOf(
        PassengerProfile(
            id = "pas_1",
            fullName = "Masood Khan Achakzai",
            phone = "+92 311 8004421",
            email = "masood@quettaride.pk",
            homeLocation = "Satellite Town, Block 3, Quetta",
            workLocation = "Zarghoon Road IT Park, Quetta",
            totalRides = 48,
            rating = 4.95,
            walletBalancePkr = 2450,
            memberSince = "Jan 2026",
            emergencyContact = "+92 300 8381122"
        ),
        PassengerProfile(
            id = "pas_2",
            fullName = "Dr. Ayesha Durrani",
            phone = "+92 334 2190843",
            email = "ayesha.durrani@bmc.edu.pk",
            homeLocation = "Model Town, Quetta",
            workLocation = "Bolan Medical Complex, Brewery Road",
            totalRides = 74,
            rating = 4.98,
            walletBalancePkr = 4100,
            memberSince = "Feb 2026",
            emergencyContact = "+92 333 7711229"
        ),
        PassengerProfile(
            id = "pas_3",
            fullName = "Usman Tareen",
            phone = "+92 301 5543218",
            email = "usman.tareen@buitems.edu.pk",
            homeLocation = "Jinnah Town, Samungli Road",
            workLocation = "Airport Road Campus, Quetta",
            totalRides = 31,
            rating = 4.86,
            walletBalancePkr = 920,
            memberSince = "Mar 2026",
            emergencyContact = "+92 321 9988112"
        ),
        PassengerProfile(
            id = "pas_4",
            fullName = "Fatima Raisani",
            phone = "+92 344 9081726",
            email = "fatima.raisani@uob.edu.pk",
            homeLocation = "Zarghoon Road Housing",
            workLocation = "Sariab Road University Gate",
            totalRides = 62,
            rating = 4.92,
            walletBalancePkr = 1780,
            memberSince = "Jan 2026",
            emergencyContact = "+92 345 1122334"
        )
    )

    fun defaultBookings(locations: List<CityLocation>): List<RideBooking> {
        val jinnah = locations.first { it.id == "loc_jinnah" }
        val satellite = locations.first { it.id == "loc_satellite" }
        val airport = locations.first { it.id == "loc_airport" }
        val zarghoon = locations.first { it.id == "loc_zarghoon" }
        val brewery = locations.first { it.id == "loc_brewery" }
        val sariab = locations.first { it.id == "loc_sariab" }
        val railway = locations.first { it.id == "loc_railway" }

        return listOf(
            RideBooking(
                id = "QR-9042",
                passengerId = "pas_1",
                passengerName = "Masood Khan Achakzai",
                passengerPhone = "+92 311 8004421",
                driverId = "drv_1",
                driverName = "Mirwais Khan Achakzai",
                vehicleCategory = VehicleCategoryId.ALTO,
                vehicleModel = "Suzuki Alto VXR 2024",
                vehiclePlate = "QTA-24-8812",
                pickupLocation = satellite,
                destinationLocation = zarghoon,
                distanceKm = 5.4,
                estimatedMinutes = 14,
                farePkr = 460,
                status = RideStatus.COMPLETED,
                createdAt = "Today, 09:15 AM",
                passengerRating = 5,
                passengerReview = "Very clean Alto car, polite captain, reached Zarghoon Road right on time!"
            ),
            RideBooking(
                id = "QR-9039",
                passengerId = "pas_2",
                passengerName = "Dr. Ayesha Durrani",
                passengerPhone = "+92 334 2190843",
                driverId = "drv_2",
                driverName = "Bilal Ahmed Mengal",
                vehicleCategory = VehicleCategoryId.RICKSHAW,
                vehicleModel = "Sazgar 4-Stroke Deluxe",
                vehiclePlate = "QTA-22-4190",
                pickupLocation = jinnah,
                destinationLocation = brewery,
                distanceKm = 4.8,
                estimatedMinutes = 13,
                farePkr = 285,
                status = RideStatus.COMPLETED,
                createdAt = "Today, 08:40 AM",
                passengerRating = 5,
                passengerReview = "Smooth rickshaw ride from Jinnah Road to BMC Hospital."
            ),
            RideBooking(
                id = "QR-9035",
                passengerId = "pas_3",
                passengerName = "Usman Tareen",
                passengerPhone = "+92 301 5543218",
                driverId = "drv_3",
                driverName = "Zafarullah Kakar",
                vehicleCategory = VehicleCategoryId.BIKE,
                vehicleModel = "Honda CG 125 Special",
                vehiclePlate = "QTA-23-1109",
                pickupLocation = railway,
                destinationLocation = sariab,
                distanceKm = 4.2,
                estimatedMinutes = 10,
                farePkr = 165,
                status = RideStatus.COMPLETED,
                createdAt = "Yesterday, 05:20 PM",
                passengerRating = 5,
                passengerReview = "Beat the evening rush easily on Bike. Extra helmet was clean."
            ),
            RideBooking(
                id = "QR-9031",
                passengerId = "pas_1",
                passengerName = "Masood Khan Achakzai",
                passengerPhone = "+92 311 8004421",
                driverId = "drv_4",
                driverName = "Saeedullah Baloch",
                vehicleCategory = VehicleCategoryId.ALTO,
                vehicleModel = "Suzuki Alto VXL AGS",
                vehiclePlate = "QTA-25-0392",
                pickupLocation = zarghoon,
                destinationLocation = airport,
                distanceKm = 11.2,
                estimatedMinutes = 22,
                farePkr = 820,
                status = RideStatus.COMPLETED,
                createdAt = "2 days ago, 04:00 PM",
                passengerRating = 4,
                passengerReview = "Great airport transfer across Samungli Road."
            )
        )
    }

    fun defaultComplaints(): List<ComplaintItem> = listOf(
        ComplaintItem(
            id = "CMP-204",
            submittedBy = "Usman Tareen",
            role = "Passenger",
            bookingId = "QR-9018",
            subject = "Pin location slightly off near Liaquat Bazaar gate",
            description = "Pickup pin snapped 80m away from main bazaar entrance due to narrow lane.",
            status = "In Review",
            date = "Today, 10:05 AM"
        ),
        ComplaintItem(
            id = "CMP-201",
            submittedBy = "Bilal Ahmed Mengal",
            role = "Driver",
            bookingId = "QR-8994",
            subject = "Waiting fee adjustment on Sariab Road railway crossing",
            description = "Train crossing delay added 9 minutes to the route.",
            status = "Resolved",
            date = "Yesterday"
        )
    )

    fun defaultPromos(): List<PromoOffer> = listOf(
        PromoOffer("QUETTA20", "Launch Celebration: 20% Off All Rides", 20, 120, "Quetta", true),
        PromoOffer("STUDENT15", "UOB & BUITEMS Student Commute Discount", 15, 80, "Quetta", true),
        PromoOffer("ALTOFAM", "Family Alto Airport Special", 10, 150, "Quetta", true)
    )

    fun defaultNotifications(): List<AppNotification> = listOf(
        AppNotification(
            id = "not_1",
            targetRole = PlatformRole.PASSENGER,
            title = "Welcome to Quetta Ride!",
            message = "Use promo code QUETTA20 for 20% off your next Bike, Rickshaw, or Alto ride.",
            timestamp = "Just now"
        ),
        AppNotification(
            id = "not_2",
            targetRole = PlatformRole.DRIVER,
            title = "Peak Demand Alert: Jinnah Road & Sariab Road",
            message = "High passenger demand right now. Stay online to earn 1.2x peak bonus.",
            timestamp = "12m ago"
        ),
        AppNotification(
            id = "not_3",
            targetRole = PlatformRole.ADMIN,
            title = "2 New Driver Registrations Pending",
            message = "Hamza Khan Panezai (Bike) and Kamran Shahwani (Alto) uploaded CNIC & vehicle docs.",
            timestamp = "25m ago"
        )
    )

    fun calculateDistanceKm(pickup: CityLocation, destination: CityLocation): Double {
        if (pickup.id == destination.id) return 1.8
        val dx = (pickup.mapX - destination.mapX) * 18.5f
        val dy = (pickup.mapY - destination.mapY) * 18.5f
        val raw = hypot(dx.toDouble(), dy.toDouble())
        return max(1.5, (raw * 10.0).roundToInt() / 10.0)
    }

    fun calculateFare(
        config: VehicleCategoryConfig,
        distanceKm: Double,
        applyPromoPercent: Int = 0
    ): FareBreakdown {
        val speedFactor = when (config.id) {
            VehicleCategoryId.BIKE -> 2.1
            VehicleCategoryId.RICKSHAW -> 2.6
            VehicleCategoryId.ALTO -> 2.3
        }
        val durationMin = max(5, (distanceKm * speedFactor).roundToInt())
        val distanceCharge = (distanceKm * config.perKmRatePkr).roundToInt()
        val timeCharge = durationMin * config.perMinuteRatePkr
        val gross = max(config.minimumFarePkr, config.baseFarePkr + distanceCharge + timeCharge)
        val discount = if (applyPromoPercent > 0) ((gross * applyPromoPercent) / 100.0).roundToInt() else 0
        val finalTotal = max(config.minimumFarePkr - discount, gross - discount)

        return FareBreakdown(
            vehicleCategory = config.id,
            distanceKm = distanceKm,
            durationMinutes = durationMin,
            baseFarePkr = config.baseFarePkr,
            distanceChargePkr = distanceCharge,
            timeChargePkr = timeCharge,
            promoDiscountPkr = discount,
            totalEstimatedPkr = finalTotal
        )
    }

    fun saveFareConfigs(configs: List<VehicleCategoryConfig>) {
        val p = prefs ?: return
        val arr = JSONArray()
        configs.forEach { cfg ->
            val obj = JSONObject().apply {
                put("id", cfg.id.name)
                put("baseFarePkr", cfg.baseFarePkr)
                put("perKmRatePkr", cfg.perKmRatePkr)
                put("perMinuteRatePkr", cfg.perMinuteRatePkr)
                put("minimumFarePkr", cfg.minimumFarePkr)
                put("avgEtaMinutes", cfg.avgEtaMinutes)
                put("isActive", cfg.isActive)
                put("description", cfg.description)
            }
            arr.put(obj)
        }
        p.edit().putString("vehicle_configs_json", arr.toString()).apply()
    }

    fun loadFareConfigs(): List<VehicleCategoryConfig> {
        val raw = prefs?.getString("vehicle_configs_json", null) ?: return defaultVehicleConfigs()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<VehicleCategoryConfig>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    VehicleCategoryConfig(
                        id = VehicleCategoryId.valueOf(obj.getString("id")),
                        baseFarePkr = obj.getInt("baseFarePkr"),
                        perKmRatePkr = obj.getInt("perKmRatePkr"),
                        perMinuteRatePkr = obj.getInt("perMinuteRatePkr"),
                        minimumFarePkr = obj.getInt("minimumFarePkr"),
                        avgEtaMinutes = obj.getInt("avgEtaMinutes"),
                        isActive = obj.optBoolean("isActive", true),
                        description = obj.getString("description")
                    )
                )
            }
            if (list.isEmpty()) defaultVehicleConfigs() else list
        } catch (_: Exception) {
            defaultVehicleConfigs()
        }
    }

    fun clearSavedDemoState() {
        prefs?.edit()?.clear()?.apply()
    }
}
