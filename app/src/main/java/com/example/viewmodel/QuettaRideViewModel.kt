package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudRideBooking
import com.example.data.QuettaFirestoreRepository
import com.example.data.QuettaRepository
import com.example.location.QuettaGpsCoordinates
import com.example.location.QuettaLocationService
import com.example.location.RealGpsCoordinate
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class QuettaPlatformUiState(
    val activeRole: PlatformRole = PlatformRole.PASSENGER,
    val selectedCity: String = "Quetta",
    val passengerScreen: PassengerScreen = PassengerScreen.HOME,
    val driverScreen: DriverScreen = DriverScreen.DASHBOARD,
    val adminSection: AdminSection = AdminSection.DASHBOARD,
    val websitePage: WebsitePage = WebsitePage.HOME,

    // Real Cloud Auth & GPS Telemetry State
    val authenticatedUid: String? = null,
    val authenticatedEmail: String? = null,
    val cloudSyncActive: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val liveGpsCoordinate: RealGpsCoordinate? = null,
    val usingLiveGpsPickup: Boolean = false,
    val selectedPaymentMethod: String = "CASH",

    // Passenger State
    val currentPassenger: PassengerProfile,
    val isPassengerAuthenticated: Boolean = true,
    val pendingOtpPhone: String = "+92 311 8004421",
    val pickupLocation: CityLocation,
    val destinationLocation: CityLocation,
    val selectedVehicleCategory: VehicleCategoryId = VehicleCategoryId.ALTO,
    val selectedDriverId: String? = "drv_1",
    val activePromoCode: String? = "QUETTA20",
    val activeRide: RideBooking? = null,
    val searchQuery: String = "",

    // Driver State
    val activeDriver: DriverProfile,
    val incomingDriverRequest: RideBooking? = null,
    val regFullName: String = "",
    val regMobile: String = "+92 3",
    val regEmail: String = "",
    val regCnic: String = "54400-",
    val regVehicleCat: VehicleCategoryId = VehicleCategoryId.RICKSHAW,
    val regVehiclePlate: String = "QTA-26-",
    val regVehicleModel: String = "Sazgar 4-Stroke",
    val regVehicleColor: String = "Navy Blue & Yellow",
    val regPhotoUploaded: Boolean = true,
    val regCnicUploaded: Boolean = true,
    val regVehicleDocsUploaded: Boolean = true,

    // Platform Collections
    val vehicleConfigs: List<VehicleCategoryConfig>,
    val locations: List<CityLocation>,
    val serviceCities: List<ServiceCity>,
    val drivers: List<DriverProfile>,
    val passengers: List<PassengerProfile>,
    val bookings: List<RideBooking>,
    val complaints: List<ComplaintItem>,
    val promos: List<PromoOffer>,
    val notifications: List<AppNotification>,
    val statusBannerMessage: String? = null
)

class QuettaRideViewModel(
    context: Context? = null,
    private val currentUserId: String? = null,
    private val currentUserName: String? = null,
    private val currentUserEmail: String? = null
) : ViewModel() {

    private val repository = QuettaRepository(context)
    private val firestoreRepository: QuettaFirestoreRepository? = rememberFirestoreRepo(context)
    private val locationService: QuettaLocationService? = context?.let { QuettaLocationService(it.applicationContext) }

    private var rideSimulationJob: Job? = null
    private var gpsStreamJob: Job? = null

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<QuettaPlatformUiState> = _uiState.asStateFlow()

    init {
        checkInitialLocationPermissions()
        if (currentUserId != null && firestoreRepository != null) {
            initializeCloudSynchronization(currentUserId, currentUserName, currentUserEmail)
        }
    }

    private fun rememberFirestoreRepo(context: Context?): QuettaFirestoreRepository? {
        if (context == null || currentUserId == null) return null
        return try {
            QuettaFirestoreRepository(context.applicationContext)
        } catch (t: Throwable) {
            null
        }
    }

    private fun createInitialState(): QuettaPlatformUiState {
        val locs = repository.defaultQuettaLocations()
        val drivers = repository.defaultDrivers()
        val passengers = repository.defaultPassengers()
        val configs = repository.loadFareConfigs()
        val bookings = repository.defaultBookings(locs)

        val defaultPickup = locs.first { it.id == "loc_jinnah" }
        val defaultDest = locs.first { it.id == "loc_satellite" }

        val initialPassenger = passengers.first().let { base ->
            if (!currentUserName.isNullOrBlank() || !currentUserEmail.isNullOrBlank()) {
                base.copy(
                    id = currentUserId ?: base.id,
                    fullName = currentUserName?.ifBlank { base.fullName } ?: base.fullName,
                    email = currentUserEmail?.ifBlank { base.email } ?: base.email
                )
            } else base
        }

        val sampleIncoming = RideBooking(
            id = "QR-9048",
            passengerId = "pas_2",
            passengerName = "Dr. Ayesha Durrani",
            passengerPhone = "+92 334 2190843",
            driverId = drivers.first().id,
            driverName = drivers.first().fullName,
            vehicleCategory = VehicleCategoryId.ALTO,
            vehicleModel = drivers.first().vehicleModel,
            vehiclePlate = drivers.first().vehiclePlate,
            pickupLocation = locs.first { it.id == "loc_jinnah" },
            destinationLocation = locs.first { it.id == "loc_brewery" },
            distanceKm = 4.8,
            estimatedMinutes = 13,
            farePkr = 390,
            status = RideStatus.SEARCHING_DRIVER,
            createdAt = "Live Request • Just Now"
        )

        return QuettaPlatformUiState(
            authenticatedUid = currentUserId,
            authenticatedEmail = currentUserEmail,
            cloudSyncActive = currentUserId != null && firestoreRepository != null,
            liveGpsCoordinate = QuettaGpsCoordinates.JINNAH_ROAD,
            currentPassenger = initialPassenger,
            pickupLocation = defaultPickup,
            destinationLocation = defaultDest,
            activeDriver = drivers.first(),
            incomingDriverRequest = sampleIncoming,
            vehicleConfigs = configs,
            locations = locs,
            serviceCities = repository.defaultServiceCities(),
            drivers = drivers,
            passengers = passengers,
            bookings = bookings,
            complaints = repository.defaultComplaints(),
            promos = repository.defaultPromos(),
            notifications = repository.defaultNotifications()
        )
    }

    private fun checkInitialLocationPermissions() {
        val granted = locationService?.hasLocationPermission() == true
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                statusBannerMessage = if (granted) {
                    "Live GPS enabled! Acquiring real satellite coordinates for Quetta..."
                } else {
                    "Location permission declined. Using Quetta landmark coordinates."
                }
            )
        }
        if (granted) {
            refreshLiveGpsLocation(silent = false)
            startLiveGpsTelemetryStream()
        }
    }

    fun refreshLiveGpsLocation(silent: Boolean = false) {
        val svc = locationService ?: return
        if (!svc.hasLocationPermission()) return
        viewModelScope.launch {
            val coord = svc.getCurrentLocationFix() ?: QuettaGpsCoordinates.JINNAH_ROAD
            val nearest = findNearestQuettaLandmark(coord, _uiState.value.locations)
            _uiState.update { state ->
                state.copy(
                    liveGpsCoordinate = coord,
                    usingLiveGpsPickup = true,
                    pickupLocation = nearest,
                    statusBannerMessage = if (silent) state.statusBannerMessage else {
                        "Live GPS Lock: ${"%.4f".format(coord.latitude)}, ${"%.4f".format(coord.longitude)} (${nearest.name})"
                    }
                )
            }
            val activeRide = _uiState.value.activeRide
            if (activeRide != null && _uiState.value.cloudSyncActive) {
                firestoreRepository?.recordRideLocationTelemetry(
                    bookingId = activeRide.id,
                    role = _uiState.value.activeRole.name,
                    latitude = coord.latitude,
                    longitude = coord.longitude,
                    accuracyMeters = coord.accuracyMeters.toDouble(),
                    speedMps = coord.speedMps.toDouble()
                )
            }
        }
    }

    private fun findNearestQuettaLandmark(coord: RealGpsCoordinate, locations: List<CityLocation>): CityLocation {
        return locations.minByOrNull { loc ->
            val target = QuettaGpsCoordinates.forLandmarkId(loc.id)
            QuettaGpsCoordinates.calculateRoadDistanceKm(
                coord.latitude,
                coord.longitude,
                target.latitude,
                target.longitude
            )
        } ?: locations.first()
    }

    private fun startLiveGpsTelemetryStream() {
        val svc = locationService ?: return
        if (!svc.hasLocationPermission()) return
        gpsStreamJob?.cancel()
        gpsStreamJob = viewModelScope.launch {
            svc.observeLiveLocationUpdates(intervalMs = 8000L)
                .catch { /* keep existing coordinate on transient GPS provider error */ }
                .collect { coord ->
                    _uiState.update { it.copy(liveGpsCoordinate = coord) }
                    val state = _uiState.value
                    if (state.cloudSyncActive && state.activeRole == PlatformRole.DRIVER && state.activeDriver.isOnline) {
                        firestoreRepository?.updateDriverAvailabilityAndLocation(
                            isOnline = true,
                            latitude = coord.latitude,
                            longitude = coord.longitude
                        )
                    }
                }
        }
    }

    private fun initializeCloudSynchronization(uid: String, displayName: String?, email: String?) {
        val cloudRepo = firestoreRepository ?: return
        viewModelScope.launch {
            val p = _uiState.value.currentPassenger
            cloudRepo.upsertUserProfile(
                fullName = displayName?.ifBlank { p.fullName } ?: p.fullName,
                phone = p.phone.replace(" ", ""),
                email = email?.ifBlank { p.email } ?: p.email,
                homeLocation = p.homeLocation,
                workLocation = p.workLocation
            )
            cloudRepo.getUserProfile(uid).getOrNull()?.let { cloudProfile ->
                _uiState.update { state ->
                    state.copy(
                        currentPassenger = state.currentPassenger.copy(
                            id = uid,
                            fullName = cloudProfile.fullName.ifBlank { state.currentPassenger.fullName },
                            phone = cloudProfile.phone.ifBlank { state.currentPassenger.phone },
                            email = cloudProfile.email.ifBlank { state.currentPassenger.email },
                            walletBalancePkr = cloudProfile.walletBalancePkr.toInt(),
                            totalRides = cloudProfile.totalRides.toInt(),
                            rating = cloudProfile.rating,
                            homeLocation = cloudProfile.homeLocation.ifBlank { state.currentPassenger.homeLocation },
                            workLocation = cloudProfile.workLocation.ifBlank { state.currentPassenger.workLocation }
                        )
                    )
                }
            }
        }

        // Real-time Passenger Bookings listener
        viewModelScope.launch {
            cloudRepo.observePassengerBookings()
                .catch { /* ignore transient listener error */ }
                .collect { cloudBookings ->
                    if (cloudBookings.isNotEmpty()) {
                        _uiState.update { state ->
                            val mapped = cloudBookings.map { cb -> mapCloudBookingToUi(cb, state.locations) }
                            val merged = (mapped + state.bookings.filter { local -> mapped.none { it.id == local.id } })
                            state.copy(bookings = merged)
                        }
                    }
                }
        }

        // Real-time Searching Ride Requests listener for Driver mode
        viewModelScope.launch {
            cloudRepo.observeOpenRideRequests()
                .catch { /* ignore transient listener error */ }
                .collect { searchingBookings ->
                    val latest = searchingBookings.firstOrNull() ?: return@collect
                    _uiState.update { state ->
                        val mapped = mapCloudBookingToUi(latest, state.locations)
                        state.copy(incomingDriverRequest = mapped)
                    }
                }
        }
    }

    private fun mapCloudBookingToUi(cb: CloudRideBooking, locations: List<CityLocation>): RideBooking {
        val pickup = locations.find { it.name.equals(cb.pickupName, ignoreCase = true) }
            ?: locations.first()
        val dest = locations.find { it.name.equals(cb.destName, ignoreCase = true) }
            ?: locations.last()
        val cat = runCatching { VehicleCategoryId.valueOf(cb.vehicleCategory) }.getOrDefault(VehicleCategoryId.ALTO)
        val status = runCatching { RideStatus.valueOf(cb.status) }.getOrDefault(RideStatus.DRIVER_ASSIGNED)
        return RideBooking(
            id = cb.id.ifBlank { "QR-LIVE" },
            passengerId = cb.passengerId,
            passengerName = cb.passengerName,
            passengerPhone = cb.passengerPhone,
            driverId = cb.driverId,
            driverName = cb.driverName,
            vehicleCategory = cat,
            vehicleModel = cb.vehicleModel.ifBlank { "Suzuki Alto" },
            vehiclePlate = cb.vehiclePlate.ifBlank { "QTA-24-8812" },
            pickupLocation = pickup,
            destinationLocation = dest,
            distanceKm = cb.distanceKm,
            estimatedMinutes = cb.estimatedMinutes.toInt(),
            farePkr = cb.farePkr.toInt(),
            status = status,
            createdAt = "Synced • Cloud Firestore",
            passengerRating = cb.passengerRating.toInt(),
            passengerReview = cb.passengerReview.ifBlank { null },
            rideProgressFraction = when (status) {
                RideStatus.SEARCHING_DRIVER -> 0.08f
                RideStatus.DRIVER_ASSIGNED -> 0.20f
                RideStatus.DRIVER_ARRIVING -> 0.35f
                RideStatus.IN_PROGRESS -> 0.70f
                RideStatus.COMPLETED -> 1.0f
                RideStatus.CANCELLED -> 0f
            }
        )
    }

    // Navigation & Role Switching
    fun switchRole(role: PlatformRole) {
        _uiState.update { it.copy(activeRole = role, statusBannerMessage = null) }
    }

    fun navigatePassenger(screen: PassengerScreen) {
        _uiState.update { it.copy(passengerScreen = screen, statusBannerMessage = null) }
    }

    fun navigateDriver(screen: DriverScreen) {
        _uiState.update { it.copy(driverScreen = screen, statusBannerMessage = null) }
    }

    fun navigateAdmin(section: AdminSection) {
        _uiState.update { it.copy(adminSection = section, statusBannerMessage = null) }
    }

    fun navigateWebsite(page: WebsitePage) {
        _uiState.update { it.copy(websitePage = page, statusBannerMessage = null) }
    }

    fun selectCity(cityName: String) {
        _uiState.update {
            it.copy(
                selectedCity = cityName,
                statusBannerMessage = "Switched service preview to $cityName"
            )
        }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update {
            it.copy(
                selectedPaymentMethod = method,
                statusBannerMessage = "Payment method set to $method"
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Passenger Location & Vehicle Selection
    fun selectPickup(location: CityLocation) {
        _uiState.update { state ->
            val dest = if (state.destinationLocation.id == location.id) {
                state.locations.firstOrNull { it.id != location.id } ?: state.destinationLocation
            } else state.destinationLocation
            state.copy(
                pickupLocation = location,
                usingLiveGpsPickup = false,
                destinationLocation = dest,
                passengerScreen = PassengerScreen.HOME,
                statusBannerMessage = "Pickup set to ${location.name}"
            )
        }
    }

    fun selectDestination(location: CityLocation) {
        _uiState.update { state ->
            val pickup = if (state.pickupLocation.id == location.id) {
                state.locations.firstOrNull { it.id != location.id } ?: state.pickupLocation
            } else state.pickupLocation
            state.copy(
                pickupLocation = pickup,
                destinationLocation = location,
                passengerScreen = PassengerScreen.VEHICLE_SELECT,
                statusBannerMessage = "Destination set to ${location.name}"
            )
        }
    }

    fun swapLocations() {
        _uiState.update { state ->
            state.copy(
                pickupLocation = state.destinationLocation,
                destinationLocation = state.pickupLocation,
                usingLiveGpsPickup = false
            )
        }
    }

    fun selectVehicleCategory(category: VehicleCategoryId) {
        _uiState.update { state ->
            val matchingDriver = state.drivers.firstOrNull {
                it.vehicleCategory == category && it.approvalStatus == DriverApprovalStatus.APPROVED && it.isOnline
            } ?: state.drivers.firstOrNull { it.vehicleCategory == category }
            state.copy(
                selectedVehicleCategory = category,
                selectedDriverId = matchingDriver?.id ?: state.selectedDriverId
            )
        }
    }

    fun selectDriverForBooking(driverId: String) {
        _uiState.update { state ->
            val drv = state.drivers.find { it.id == driverId }
            state.copy(
                selectedDriverId = driverId,
                selectedVehicleCategory = drv?.vehicleCategory ?: state.selectedVehicleCategory
            )
        }
    }

    fun applyPromoCode(code: String?) {
        _uiState.update { it.copy(activePromoCode = code) }
    }

    fun getCurrentDistanceKm(): Double {
        val s = _uiState.value
        val pickupCoord = if (s.usingLiveGpsPickup && s.liveGpsCoordinate != null) {
            s.liveGpsCoordinate
        } else {
            QuettaGpsCoordinates.forLandmarkId(s.pickupLocation.id)
        }
        val destCoord = QuettaGpsCoordinates.forLandmarkId(s.destinationLocation.id)
        return QuettaGpsCoordinates.calculateRoadDistanceKm(
            pickupCoord.latitude,
            pickupCoord.longitude,
            destCoord.latitude,
            destCoord.longitude
        )
    }

    fun getFareBreakdownFor(category: VehicleCategoryId): FareBreakdown {
        val s = _uiState.value
        val cfg = s.vehicleConfigs.firstOrNull { it.id == category }
            ?: repository.defaultVehicleConfigs().first { it.id == category }
        val distanceKm = getCurrentDistanceKm()
        val promo = s.promos.firstOrNull { it.code.equals(s.activePromoCode, ignoreCase = true) && it.isActive }
        return repository.calculateFare(cfg, distanceKm, promo?.discountPercent ?: 0)
    }

    // Confirm Booking & Persist to Cloud Firestore + Start Live Tracking
    fun confirmPassengerBooking() {
        val s = _uiState.value
        val breakdown = getFareBreakdownFor(s.selectedVehicleCategory)
        val chosenDriver = s.drivers.firstOrNull { it.id == s.selectedDriverId }
            ?: s.drivers.firstOrNull { it.vehicleCategory == s.selectedVehicleCategory }
            ?: s.drivers.first()

        val bookingId = "QR-${(9050..9999).random()}"
        val newBooking = RideBooking(
            id = bookingId,
            passengerId = s.currentPassenger.id,
            passengerName = s.currentPassenger.fullName,
            passengerPhone = s.currentPassenger.phone,
            driverId = chosenDriver.id,
            driverName = chosenDriver.fullName,
            vehicleCategory = s.selectedVehicleCategory,
            vehicleModel = chosenDriver.vehicleModel,
            vehiclePlate = chosenDriver.vehiclePlate,
            pickupLocation = s.pickupLocation,
            destinationLocation = s.destinationLocation,
            distanceKm = breakdown.distanceKm,
            estimatedMinutes = breakdown.durationMinutes,
            farePkr = breakdown.totalEstimatedPkr,
            status = RideStatus.DRIVER_ASSIGNED,
            createdAt = "Just now",
            rideProgressFraction = 0.08f
        )

        val passengerNotif = AppNotification(
            id = "not_${System.currentTimeMillis()}",
            targetRole = PlatformRole.PASSENGER,
            title = "Booking Confirmed ($bookingId)",
            message = "${chosenDriver.fullName} (${chosenDriver.vehicleModel} • ${chosenDriver.vehiclePlate}) is assigned.",
            timestamp = "Just now"
        )
        val driverNotif = AppNotification(
            id = "not_drv_${System.currentTimeMillis()}",
            targetRole = PlatformRole.DRIVER,
            title = "New Ride Assigned ($bookingId)",
            message = "Pickup ${s.currentPassenger.fullName} at ${s.pickupLocation.name} -> ${s.destinationLocation.name}.",
            timestamp = "Just now"
        )
        val adminNotif = AppNotification(
            id = "not_adm_${System.currentTimeMillis()}",
            targetRole = PlatformRole.ADMIN,
            title = "New Live Booking ($bookingId)",
            message = "${s.selectedVehicleCategory.displayName} booked from ${s.pickupLocation.name} (PKR ${breakdown.totalEstimatedPkr}).",
            timestamp = "Just now"
        )

        _uiState.update { state ->
            state.copy(
                activeRide = newBooking,
                incomingDriverRequest = newBooking,
                bookings = listOf(newBooking) + state.bookings,
                notifications = listOf(passengerNotif, driverNotif, adminNotif) + state.notifications,
                passengerScreen = PassengerScreen.LIVE_TRACKING,
                statusBannerMessage = "Booking $bookingId confirmed & synced to Cloud Firestore! Captain ${chosenDriver.fullName} is en route."
            )
        }

        // Persist to Cloud Firestore
        val pickupCoord = if (s.usingLiveGpsPickup && s.liveGpsCoordinate != null) {
            s.liveGpsCoordinate
        } else {
            QuettaGpsCoordinates.forLandmarkId(s.pickupLocation.id)
        }
        val destCoord = QuettaGpsCoordinates.forLandmarkId(s.destinationLocation.id)

        viewModelScope.launch {
            firestoreRepository?.createRideBooking(
                passengerName = s.currentPassenger.fullName,
                passengerPhone = s.currentPassenger.phone.replace(" ", ""),
                vehicleCategory = s.selectedVehicleCategory.name,
                pickupName = s.pickupLocation.name,
                pickupLat = pickupCoord.latitude,
                pickupLng = pickupCoord.longitude,
                destName = s.destinationLocation.name,
                destLat = destCoord.latitude,
                destLng = destCoord.longitude,
                distanceKm = breakdown.distanceKm,
                estimatedMinutes = breakdown.durationMinutes,
                farePkr = breakdown.totalEstimatedPkr,
                paymentMethod = s.selectedPaymentMethod
            )
        }

        startSimulatedRideProgress()
    }

    private fun startSimulatedRideProgress() {
        rideSimulationJob?.cancel()
        rideSimulationJob = viewModelScope.launch {
            delay(2200)
            _uiState.update { state ->
                val ride = state.activeRide ?: return@update state
                if (ride.status == RideStatus.CANCELLED || ride.status == RideStatus.COMPLETED) return@update state
                val updated = ride.copy(status = RideStatus.DRIVER_ARRIVING, rideProgressFraction = 0.25f)
                state.copy(
                    activeRide = updated,
                    bookings = state.bookings.map { if (it.id == updated.id) updated else it }
                )
            }
            delay(2500)
            _uiState.update { state ->
                val ride = state.activeRide ?: return@update state
                if (ride.status == RideStatus.CANCELLED || ride.status == RideStatus.COMPLETED) return@update state
                val updated = ride.copy(status = RideStatus.IN_PROGRESS, rideProgressFraction = 0.55f)
                state.copy(
                    activeRide = updated,
                    bookings = state.bookings.map { if (it.id == updated.id) updated else it }
                )
            }
            delay(2500)
            _uiState.update { state ->
                val ride = state.activeRide ?: return@update state
                if (ride.status == RideStatus.CANCELLED || ride.status == RideStatus.COMPLETED) return@update state
                val updated = ride.copy(status = RideStatus.IN_PROGRESS, rideProgressFraction = 0.82f)
                state.copy(
                    activeRide = updated,
                    bookings = state.bookings.map { if (it.id == updated.id) updated else it }
                )
            }
        }
    }

    fun advanceRideStepManually() {
        val s = _uiState.value
        val ride = s.activeRide ?: return
        val nextStatus = when (ride.status) {
            RideStatus.SEARCHING_DRIVER -> RideStatus.DRIVER_ASSIGNED
            RideStatus.DRIVER_ASSIGNED -> RideStatus.DRIVER_ARRIVING
            RideStatus.DRIVER_ARRIVING -> RideStatus.IN_PROGRESS
            RideStatus.IN_PROGRESS -> RideStatus.COMPLETED
            RideStatus.COMPLETED -> RideStatus.COMPLETED
            RideStatus.CANCELLED -> RideStatus.CANCELLED
        }
        val nextProgress = when (nextStatus) {
            RideStatus.DRIVER_ASSIGNED -> 0.15f
            RideStatus.DRIVER_ARRIVING -> 0.30f
            RideStatus.IN_PROGRESS -> 0.75f
            RideStatus.COMPLETED -> 1.0f
            else -> ride.rideProgressFraction
        }
        if (nextStatus == RideStatus.COMPLETED) {
            completeActiveRide()
        } else {
            val updated = ride.copy(status = nextStatus, rideProgressFraction = nextProgress)
            _uiState.update { state ->
                state.copy(
                    activeRide = updated,
                    bookings = state.bookings.map { if (it.id == updated.id) updated else it },
                    passengerScreen = if (nextStatus == RideStatus.IN_PROGRESS) PassengerScreen.RIDE_IN_PROGRESS else state.passengerScreen
                )
            }
            viewModelScope.launch {
                val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
                firestoreRepository?.updateRideStatusAndGps(
                    bookingId = ride.id,
                    newStatus = nextStatus.name,
                    driverLat = coord.latitude,
                    driverLng = coord.longitude
                )
            }
        }
    }

    fun completeActiveRide() {
        rideSimulationJob?.cancel()
        val rideToComplete = _uiState.value.activeRide
        _uiState.update { state ->
            val ride = state.activeRide ?: return@update state
            val completedRide = ride.copy(status = RideStatus.COMPLETED, rideProgressFraction = 1.0f)
            val updatedDrivers = state.drivers.map { drv ->
                if (drv.id == ride.driverId) {
                    drv.copy(
                        todayEarningsPkr = drv.todayEarningsPkr + ride.farePkr,
                        todayRides = drv.todayRides + 1,
                        totalTrips = drv.totalTrips + 1
                    )
                } else drv
            }
            val updatedActiveDriver = updatedDrivers.find { it.id == state.activeDriver.id } ?: state.activeDriver
            val updatedPassenger = state.currentPassenger.copy(
                totalRides = state.currentPassenger.totalRides + 1
            )
            val doneNotif = AppNotification(
                id = "not_done_${System.currentTimeMillis()}",
                targetRole = PlatformRole.PASSENGER,
                title = "Ride Completed (${ride.id})",
                message = "Arrived safely at ${ride.destinationLocation.name}. Total Fare: PKR ${ride.farePkr} (${state.selectedPaymentMethod}).",
                timestamp = "Just now"
            )
            state.copy(
                activeRide = completedRide,
                bookings = state.bookings.map { if (it.id == completedRide.id) completedRide else it },
                drivers = updatedDrivers,
                activeDriver = updatedActiveDriver,
                currentPassenger = updatedPassenger,
                notifications = listOf(doneNotif) + state.notifications,
                passengerScreen = PassengerScreen.RIDE_COMPLETED,
                statusBannerMessage = "Ride ${ride.id} completed! Please rate your captain."
            )
        }
        if (rideToComplete != null) {
            viewModelScope.launch {
                val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
                firestoreRepository?.updateRideStatusAndGps(
                    bookingId = rideToComplete.id,
                    newStatus = "COMPLETED",
                    driverLat = coord.latitude,
                    driverLng = coord.longitude,
                    paymentStatus = "PAID"
                )
            }
        }
    }

    fun cancelActiveRide() {
        rideSimulationJob?.cancel()
        val rideToCancel = _uiState.value.activeRide
        _uiState.update { state ->
            val ride = state.activeRide ?: return@update state
            val cancelled = ride.copy(status = RideStatus.CANCELLED)
            state.copy(
                activeRide = null,
                bookings = state.bookings.map { if (it.id == cancelled.id) cancelled else it },
                passengerScreen = PassengerScreen.HOME,
                statusBannerMessage = "Booking ${ride.id} was cancelled."
            )
        }
        if (rideToCancel != null) {
            viewModelScope.launch {
                val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
                firestoreRepository?.updateRideStatusAndGps(
                    bookingId = rideToCancel.id,
                    newStatus = "CANCELLED",
                    driverLat = coord.latitude,
                    driverLng = coord.longitude
                )
            }
        }
    }

    fun submitPassengerRating(stars: Int, reviewText: String) {
        val ride = _uiState.value.activeRide ?: _uiState.value.bookings.firstOrNull()
        val finalReview = reviewText.ifBlank { "Smooth and reliable Quetta Ride trip!" }
        _uiState.update { state ->
            val updatedBookings = if (ride != null) {
                state.bookings.map {
                    if (it.id == ride.id) it.copy(passengerRating = stars, passengerReview = finalReview) else it
                }
            } else state.bookings
            state.copy(
                activeRide = null,
                bookings = updatedBookings,
                passengerScreen = PassengerScreen.RIDE_HISTORY,
                statusBannerMessage = "Thank you for your $stars-star feedback!"
            )
        }
        if (ride != null) {
            viewModelScope.launch {
                firestoreRepository?.submitPassengerRating(ride.id, stars, finalReview)
            }
        }
    }

    fun updatePassengerProfile(name: String, phone: String, email: String, home: String, work: String) {
        _uiState.update { state ->
            val updated = state.currentPassenger.copy(
                fullName = name.ifBlank { state.currentPassenger.fullName },
                phone = phone.ifBlank { state.currentPassenger.phone },
                email = email.ifBlank { state.currentPassenger.email },
                homeLocation = home.ifBlank { state.currentPassenger.homeLocation },
                workLocation = work.ifBlank { state.currentPassenger.workLocation }
            )
            state.copy(
                currentPassenger = updated,
                passengers = state.passengers.map { if (it.id == updated.id) updated else it },
                statusBannerMessage = "Passenger profile saved and synced to Cloud Firestore."
            )
        }
        viewModelScope.launch {
            val p = _uiState.value.currentPassenger
            firestoreRepository?.upsertUserProfile(
                fullName = p.fullName,
                phone = p.phone.replace(" ", ""),
                email = p.email,
                homeLocation = p.homeLocation,
                workLocation = p.workLocation
            )
        }
    }

    fun deleteCurrentUserAccount(onAccountDeleted: () -> Unit) {
        viewModelScope.launch {
            firestoreRepository?.deleteUserAccountData()
            onAccountDeleted()
        }
    }

    // Driver App Actions
    fun toggleDriverOnline() {
        val nextOnline = !_uiState.value.activeDriver.isOnline
        _uiState.update { state ->
            val updatedDriver = state.activeDriver.copy(isOnline = nextOnline)
            state.copy(
                activeDriver = updatedDriver,
                drivers = state.drivers.map { if (it.id == updatedDriver.id) updatedDriver else it },
                statusBannerMessage = if (nextOnline) "You are now ONLINE and broadcasting live GPS in Quetta." else "You are now OFFLINE."
            )
        }
        viewModelScope.launch {
            val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
            firestoreRepository?.updateDriverAvailabilityAndLocation(
                isOnline = nextOnline,
                latitude = coord.latitude,
                longitude = coord.longitude
            )
        }
    }

    fun triggerSampleDriverRequest() {
        val s = _uiState.value
        val locs = s.locations
        val pickup = locs.random()
        val dest = locs.filter { it.id != pickup.id }.random()
        val pCoord = QuettaGpsCoordinates.forLandmarkId(pickup.id)
        val dCoord = QuettaGpsCoordinates.forLandmarkId(dest.id)
        val dist = QuettaGpsCoordinates.calculateRoadDistanceKm(
            pCoord.latitude,
            pCoord.longitude,
            dCoord.latitude,
            dCoord.longitude
        )
        val cfg = s.vehicleConfigs.first { it.id == s.activeDriver.vehicleCategory }
        val fare = repository.calculateFare(cfg, dist).totalEstimatedPkr
        val req = RideBooking(
            id = "QR-${(9100..9999).random()}",
            passengerId = "pas_2",
            passengerName = s.passengers.random().fullName,
            passengerPhone = "+92 334 2190843",
            driverId = s.activeDriver.id,
            driverName = s.activeDriver.fullName,
            vehicleCategory = s.activeDriver.vehicleCategory,
            vehicleModel = s.activeDriver.vehicleModel,
            vehiclePlate = s.activeDriver.vehiclePlate,
            pickupLocation = pickup,
            destinationLocation = dest,
            distanceKm = dist,
            estimatedMinutes = (dist * 2.4).roundToInt().coerceAtLeast(6),
            farePkr = fare,
            status = RideStatus.SEARCHING_DRIVER,
            createdAt = "Live Request • Just Now"
        )
        _uiState.update {
            it.copy(
                incomingDriverRequest = req,
                driverScreen = DriverScreen.NEW_REQUEST,
                statusBannerMessage = "New ride request from ${req.passengerName}!"
            )
        }
    }

    fun acceptDriverRequest() {
        val req = _uiState.value.incomingDriverRequest ?: return
        val drv = _uiState.value.activeDriver
        val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD

        _uiState.update { state ->
            val accepted = req.copy(
                status = RideStatus.DRIVER_ASSIGNED,
                driverId = state.activeDriver.id,
                driverName = state.activeDriver.fullName,
                rideProgressFraction = 0.2f
            )
            val exists = state.bookings.any { it.id == accepted.id }
            val updatedBookings = if (exists) {
                state.bookings.map { if (it.id == accepted.id) accepted else it }
            } else {
                listOf(accepted) + state.bookings
            }
            state.copy(
                incomingDriverRequest = accepted,
                activeRide = accepted,
                bookings = updatedBookings,
                driverScreen = DriverScreen.PICKUP_NAVIGATION,
                statusBannerMessage = "Ride ${accepted.id} accepted atomically! Navigating to ${accepted.pickupLocation.name}."
            )
        }

        viewModelScope.launch {
            firestoreRepository?.acceptRideBookingTransaction(
                bookingId = req.id,
                driverName = drv.fullName,
                vehicleModel = drv.vehicleModel,
                vehiclePlate = drv.vehiclePlate,
                driverLat = coord.latitude,
                driverLng = coord.longitude
            )
        }
    }

    fun rejectDriverRequest() {
        _uiState.update { state ->
            state.copy(
                incomingDriverRequest = null,
                driverScreen = DriverScreen.DASHBOARD,
                statusBannerMessage = "Ride request declined. Searching next nearby request..."
            )
        }
    }

    fun startDriverTripFromPickup() {
        val req = _uiState.value.incomingDriverRequest ?: _uiState.value.activeRide ?: return
        _uiState.update { state ->
            val inProgress = req.copy(status = RideStatus.IN_PROGRESS, rideProgressFraction = 0.65f)
            state.copy(
                incomingDriverRequest = inProgress,
                activeRide = inProgress,
                bookings = state.bookings.map { if (it.id == inProgress.id) inProgress else it },
                driverScreen = DriverScreen.RIDE_IN_PROGRESS,
                statusBannerMessage = "Passenger picked up! En route to ${inProgress.destinationLocation.name}."
            )
        }
        viewModelScope.launch {
            val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
            firestoreRepository?.updateRideStatusAndGps(
                bookingId = req.id,
                newStatus = "IN_PROGRESS",
                driverLat = coord.latitude,
                driverLng = coord.longitude
            )
        }
    }

    fun completeDriverTrip() {
        val req = _uiState.value.incomingDriverRequest ?: _uiState.value.activeRide ?: return
        _uiState.update { state ->
            val completed = req.copy(status = RideStatus.COMPLETED, rideProgressFraction = 1.0f)
            val updatedDriver = state.activeDriver.copy(
                todayEarningsPkr = state.activeDriver.todayEarningsPkr + completed.farePkr,
                todayRides = state.activeDriver.todayRides + 1,
                totalTrips = state.activeDriver.totalTrips + 1
            )
            val earnNotif = AppNotification(
                id = "not_earn_${System.currentTimeMillis()}",
                targetRole = PlatformRole.DRIVER,
                title = "Earnings Updated (+PKR ${completed.farePkr})",
                message = "Completed trip ${completed.id} to ${completed.destinationLocation.name}.",
                timestamp = "Just now"
            )
            state.copy(
                incomingDriverRequest = completed,
                activeRide = null,
                activeDriver = updatedDriver,
                drivers = state.drivers.map { if (it.id == updatedDriver.id) updatedDriver else it },
                bookings = state.bookings.map { if (it.id == completed.id) completed else it },
                notifications = listOf(earnNotif) + state.notifications,
                driverScreen = DriverScreen.COMPLETE_RIDE,
                statusBannerMessage = "Trip completed! PKR ${completed.farePkr} added to Today's Earnings."
            )
        }
        viewModelScope.launch {
            val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
            firestoreRepository?.updateRideStatusAndGps(
                bookingId = req.id,
                newStatus = "COMPLETED",
                driverLat = coord.latitude,
                driverLng = coord.longitude,
                paymentStatus = "PAID"
            )
        }
    }

    // Driver Registration Form Updates
    fun updateDriverRegPersonal(name: String, mobile: String, email: String, cnic: String) {
        _uiState.update {
            it.copy(
                regFullName = name,
                regMobile = mobile,
                regEmail = email,
                regCnic = cnic
            )
        }
    }

    fun updateDriverRegVehicle(cat: VehicleCategoryId, plate: String, model: String, color: String) {
        _uiState.update {
            it.copy(
                regVehicleCat = cat,
                regVehiclePlate = plate,
                regVehicleModel = model,
                regVehicleColor = color
            )
        }
    }

    fun submitDriverRegistration() {
        val stateSnap = _uiState.value
        val newDriver = DriverProfile(
            id = currentUserId ?: "drv_${stateSnap.drivers.size + 1}",
            fullName = stateSnap.regFullName.ifBlank { "Captain Tariq Jamali" },
            phone = stateSnap.regMobile.ifBlank { "+92 315 8901122" },
            email = stateSnap.regEmail.ifBlank { currentUserEmail ?: "tariq.jamali@quettaride.pk" },
            cnic = stateSnap.regCnic.ifBlank { "54400-8821903-5" },
            vehicleCategory = stateSnap.regVehicleCat,
            vehicleModel = stateSnap.regVehicleModel.ifBlank { "Honda CG 125" },
            vehiclePlate = stateSnap.regVehiclePlate.ifBlank { "QTA-26-4901" },
            vehicleColor = stateSnap.regVehicleColor.ifBlank { "Navy Blue" },
            rating = 5.0,
            totalTrips = 0,
            isOnline = false,
            approvalStatus = DriverApprovalStatus.PENDING,
            mapX = 0.52f,
            mapY = 0.50f,
            todayEarningsPkr = 0,
            todayRides = 0,
            documentsVerified = true,
            avatarSeed = stateSnap.drivers.size + 1
        )
        _uiState.update { state ->
            val admNotif = AppNotification(
                id = "not_reg_${System.currentTimeMillis()}",
                targetRole = PlatformRole.ADMIN,
                title = "New Driver Registration",
                message = "${newDriver.fullName} applied for ${newDriver.vehicleCategory.displayName} (${newDriver.vehiclePlate}).",
                timestamp = "Just now"
            )
            state.copy(
                drivers = listOf(newDriver) + state.drivers,
                activeDriver = newDriver,
                notifications = listOf(admNotif) + state.notifications,
                driverScreen = DriverScreen.DASHBOARD,
                statusBannerMessage = "Captain application submitted to Cloud Firestore (PENDING verification)!"
            )
        }
        viewModelScope.launch {
            val coord = _uiState.value.liveGpsCoordinate ?: QuettaGpsCoordinates.JINNAH_ROAD
            firestoreRepository?.registerDriverProfile(
                fullName = newDriver.fullName,
                phone = newDriver.phone.replace(" ", ""),
                email = newDriver.email,
                cnic = newDriver.cnic,
                licenseNumber = "QTA-LTV-${(1000..9999).random()}",
                vehicleCategory = newDriver.vehicleCategory.name,
                vehicleModel = newDriver.vehicleModel,
                vehiclePlate = newDriver.vehiclePlate,
                vehicleColor = newDriver.vehicleColor,
                latitude = coord.latitude,
                longitude = coord.longitude
            )
            firestoreRepository?.uploadDriverDocument("CNIC", newDriver.cnic)
        }
    }

    // Admin Actions
    fun updateDriverApproval(driverId: String, status: DriverApprovalStatus) {
        _uiState.update { state ->
            val updatedDrivers = state.drivers.map {
                if (it.id == driverId) it.copy(approvalStatus = status, isOnline = status == DriverApprovalStatus.APPROVED) else it
            }
            val drvName = updatedDrivers.find { it.id == driverId }?.fullName ?: "Driver"
            state.copy(
                drivers = updatedDrivers,
                activeDriver = if (state.activeDriver.id == driverId) {
                    state.activeDriver.copy(approvalStatus = status, isOnline = status == DriverApprovalStatus.APPROVED)
                } else state.activeDriver,
                statusBannerMessage = "$drvName status updated to ${status.label} & logged in Audit Logs."
            )
        }
        viewModelScope.launch {
            firestoreRepository?.writeAuditLog(
                action = "DRIVER_APPROVAL_${status.name}",
                targetId = driverId,
                details = "Updated driver approval status to ${status.name}"
            )
        }
    }

    fun updateVehicleFareConfig(
        categoryId: VehicleCategoryId,
        baseFarePkr: Int,
        perKmRatePkr: Int,
        perMinRatePkr: Int,
        minFarePkr: Int,
        isActive: Boolean
    ) {
        val cleanBase = baseFarePkr.coerceAtLeast(20)
        val cleanKm = perKmRatePkr.coerceAtLeast(10)
        val cleanMin = perMinRatePkr.coerceAtLeast(1)
        val cleanMinimum = minFarePkr.coerceAtLeast(50)

        _uiState.update { state ->
            val updated = state.vehicleConfigs.map { cfg ->
                if (cfg.id == categoryId) {
                    cfg.copy(
                        baseFarePkr = cleanBase,
                        perKmRatePkr = cleanKm,
                        perMinuteRatePkr = cleanMin,
                        minimumFarePkr = cleanMinimum,
                        isActive = isActive
                    )
                } else cfg
            }
            repository.saveFareConfigs(updated)
            state.copy(
                vehicleConfigs = updated,
                statusBannerMessage = "${categoryId.displayName} fare rates saved & audited in Cloud Firestore!"
            )
        }
        viewModelScope.launch {
            firestoreRepository?.writeAuditLog(
                action = "UPDATE_FARE_${categoryId.name}",
                targetId = categoryId.name,
                details = "Base=$cleanBase, PerKm=$cleanKm, PerMin=$cleanMin, Min=$cleanMinimum"
            )
        }
    }

    fun addQuettaLocation(name: String, area: String, city: String) {
        if (name.isBlank()) return
        _uiState.update { state ->
            val newLoc = CityLocation(
                id = "loc_${System.currentTimeMillis()}",
                name = name.trim(),
                area = area.ifBlank { "Quetta Metropolitan Area" },
                city = city.ifBlank { "Quetta" },
                mapX = (25..78).random() / 100f,
                mapY = (20..78).random() / 100f,
                isPopular = true,
                landmarkType = "Service Zone"
            )
            state.copy(
                locations = state.locations + newLoc,
                statusBannerMessage = "Added $name ($city) to service locations."
            )
        }
    }

    fun toggleCityLiveStatus(cityName: String) {
        _uiState.update { state ->
            val updated = state.serviceCities.map { city ->
                if (city.name == cityName) {
                    val nowLive = !city.isLive
                    city.copy(
                        isLive = nowLive,
                        activeDriversCount = if (nowLive && city.activeDriversCount == 0) 125 else city.activeDriversCount
                    )
                } else city
            }
            state.copy(
                serviceCities = updated,
                statusBannerMessage = "Updated $cityName expansion status."
            )
        }
    }

    fun resolveComplaint(complaintId: String) {
        _uiState.update { state ->
            state.copy(
                complaints = state.complaints.map {
                    if (it.id == complaintId) it.copy(status = "Resolved") else it
                },
                statusBannerMessage = "Complaint $complaintId marked as Resolved."
            )
        }
        viewModelScope.launch {
            firestoreRepository?.writeAuditLog(
                action = "RESOLVE_COMPLAINT",
                targetId = complaintId,
                details = "Marked complaint $complaintId as Resolved"
            )
        }
    }

    fun submitSupportComplaint(subject: String, description: String, role: String) {
        if (subject.isBlank()) return
        val bookingId = _uiState.value.bookings.firstOrNull()?.id ?: "QR-9042"
        val submittedName = if (role == "Passenger") _uiState.value.currentPassenger.fullName else _uiState.value.activeDriver.fullName
        val cleanDesc = description.ifBlank { "Submitted via in-app support desk." }

        _uiState.update { state ->
            val newItem = ComplaintItem(
                id = "CMP-${(210..999).random()}",
                submittedBy = submittedName,
                role = role,
                bookingId = bookingId,
                subject = subject,
                description = cleanDesc,
                status = "Open",
                date = "Just now"
            )
            val admNotif = AppNotification(
                id = "not_cmp_${System.currentTimeMillis()}",
                targetRole = PlatformRole.ADMIN,
                title = "New Support Ticket (${newItem.id})",
                message = "$subject — by ${newItem.submittedBy}",
                timestamp = "Just now"
            )
            state.copy(
                complaints = listOf(newItem) + state.complaints,
                notifications = listOf(admNotif) + state.notifications,
                statusBannerMessage = "Support ticket ${newItem.id} submitted to Quetta Ride HQ."
            )
        }
        viewModelScope.launch {
            firestoreRepository?.createSupportComplaint(
                submittedBy = submittedName,
                role = role.uppercase(),
                bookingId = bookingId,
                subject = subject,
                description = cleanDesc
            )
        }
    }

    fun resetDemoData() {
        rideSimulationJob?.cancel()
        repository.clearSavedDemoState()
        _uiState.value = createInitialState().copy(
            statusBannerMessage = "All local state reset to factory defaults!"
        )
    }
}
