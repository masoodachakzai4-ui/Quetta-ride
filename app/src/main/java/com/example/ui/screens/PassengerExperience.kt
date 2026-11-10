package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.QuettaPlatformUiState
import com.example.viewmodel.QuettaRideViewModel

@Composable
fun PassengerExperience(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.passengerScreen != PassengerScreen.HOME) {
        BackHandler {
            viewModel.navigatePassenger(PassengerScreen.HOME)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            PassengerBottomBar(
                currentScreen = uiState.passengerScreen,
                hasActiveRide = uiState.activeRide != null,
                onNavigate = { viewModel.navigatePassenger(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive 20-Screen Prototype Step Explorer Strip
            PassengerScreenExplorerBar(
                currentScreen = uiState.passengerScreen,
                onSelectScreen = { viewModel.navigatePassenger(it) }
            )

            when (uiState.passengerScreen) {
                PassengerScreen.SPLASH -> PassengerSplashScreen(
                    onContinue = { viewModel.navigatePassenger(PassengerScreen.WELCOME) }
                )
                PassengerScreen.WELCOME -> PassengerWelcomeScreen(
                    onLogin = { viewModel.navigatePassenger(PassengerScreen.LOGIN) },
                    onSignUp = { viewModel.navigatePassenger(PassengerScreen.SIGN_UP) },
                    onSkipToHome = { viewModel.navigatePassenger(PassengerScreen.HOME) }
                )
                PassengerScreen.LOGIN -> PassengerAuthScreen(
                    isSignUp = false,
                    onSendOtp = { viewModel.navigatePassenger(PassengerScreen.OTP_VERIFY) },
                    onSwitchMode = { viewModel.navigatePassenger(PassengerScreen.SIGN_UP) }
                )
                PassengerScreen.SIGN_UP -> PassengerAuthScreen(
                    isSignUp = true,
                    onSendOtp = { viewModel.navigatePassenger(PassengerScreen.OTP_VERIFY) },
                    onSwitchMode = { viewModel.navigatePassenger(PassengerScreen.LOGIN) }
                )
                PassengerScreen.OTP_VERIFY -> PassengerOtpScreen(
                    phone = uiState.pendingOtpPhone,
                    onVerified = { viewModel.navigatePassenger(PassengerScreen.HOME) }
                )
                PassengerScreen.HOME -> PassengerHomeDashboard(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.PICKUP_SELECT -> LocationPickerScreen(
                    title = "Select Pickup Location",
                    subtitle = "Choose your starting point in ${uiState.selectedCity}",
                    locations = uiState.locations,
                    selectedId = uiState.pickupLocation.id,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onSelectLocation = { viewModel.selectPickup(it) },
                    onBack = { viewModel.navigatePassenger(PassengerScreen.HOME) }
                )
                PassengerScreen.DESTINATION_SELECT -> LocationPickerScreen(
                    title = "Where to in ${uiState.selectedCity}?",
                    subtitle = "Select destination landmark or bazaar",
                    locations = uiState.locations,
                    selectedId = uiState.destinationLocation.id,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onSelectLocation = { viewModel.selectDestination(it) },
                    onBack = { viewModel.navigatePassenger(PassengerScreen.HOME) }
                )
                PassengerScreen.VEHICLE_SELECT -> VehicleSelectionScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.FARE_BREAKDOWN -> FareEstimationScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.NEARBY_DRIVERS -> NearbyDriversScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.BOOKING_CONFIRM -> BookingConfirmationScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.DRIVER_DETAILS -> DriverDetailsScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.LIVE_TRACKING,
                PassengerScreen.RIDE_IN_PROGRESS -> LiveRideTrackingScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.RIDE_COMPLETED -> RideCompletedScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.RATING_REVIEW -> RatingAndReviewScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                PassengerScreen.RIDE_HISTORY -> PassengerRideHistoryScreen(
                    bookings = uiState.bookings,
                    onBookAgain = { viewModel.navigatePassenger(PassengerScreen.HOME) }
                )
                PassengerScreen.PROFILE -> PassengerProfileScreen(
                    passenger = uiState.currentPassenger,
                    authenticatedEmail = uiState.authenticatedEmail,
                    cloudSyncActive = uiState.cloudSyncActive,
                    onSaveProfile = { name, phone, email, home, work ->
                        viewModel.updatePassengerProfile(name, phone, email, home, work)
                    },
                    onDeleteAccount = {
                        viewModel.deleteCurrentUserAccount {}
                    },
                    onOpenAuth = { viewModel.navigatePassenger(PassengerScreen.WELCOME) }
                )
                PassengerScreen.HELP_SUPPORT -> PassengerHelpSupportScreen(
                    onSubmitTicket = { subj, desc ->
                        viewModel.submitSupportComplaint(subj, desc, "Passenger")
                    }
                )
            }
        }
    }
}

@Composable
private fun PassengerScreenExplorerBar(
    currentScreen: PassengerScreen,
    onSelectScreen: (PassengerScreen) -> Unit
) {
    Surface(
        color = QuettaNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "20-Screen Flow:",
                style = MaterialTheme.typography.labelSmall,
                color = QuettaYellow,
                fontWeight = FontWeight.Bold
            )
            PassengerScreen.entries.forEachIndexed { idx, screen ->
                val isSelected = screen == currentScreen
                Surface(
                    color = if (isSelected) QuettaYellow else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clickable { onSelectScreen(screen) }
                        .testTag("passenger_step_${screen.name.lowercase()}")
                ) {
                    Text(
                        text = "${idx + 1}. ${screen.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) QuettaNavyDark else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PassengerBottomBar(
    currentScreen: PassengerScreen,
    hasActiveRide: Boolean,
    onNavigate: (PassengerScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == PassengerScreen.HOME || currentScreen == PassengerScreen.VEHICLE_SELECT,
            onClick = { onNavigate(PassengerScreen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            modifier = Modifier.testTag("passenger_nav_home")
        )
        NavigationBarItem(
            selected = currentScreen == PassengerScreen.LIVE_TRACKING || currentScreen == PassengerScreen.RIDE_IN_PROGRESS,
            onClick = {
                onNavigate(if (hasActiveRide) PassengerScreen.LIVE_TRACKING else PassengerScreen.VEHICLE_SELECT)
            },
            icon = {
                BadgedBox(
                    badge = {
                        if (hasActiveRide) {
                            Badge(containerColor = QuettaGreen) { Text("LIVE") }
                        }
                    }
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = "Book / Live Ride")
                }
            },
            label = { Text(if (hasActiveRide) "Live Ride" else "Book") },
            modifier = Modifier.testTag("passenger_nav_book")
        )
        NavigationBarItem(
            selected = currentScreen == PassengerScreen.RIDE_HISTORY,
            onClick = { onNavigate(PassengerScreen.RIDE_HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "Ride History") },
            label = { Text("History") },
            modifier = Modifier.testTag("passenger_nav_history")
        )
        NavigationBarItem(
            selected = currentScreen == PassengerScreen.PROFILE,
            onClick = { onNavigate(PassengerScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            modifier = Modifier.testTag("passenger_nav_profile")
        )
        NavigationBarItem(
            selected = currentScreen == PassengerScreen.HELP_SUPPORT,
            onClick = { onNavigate(PassengerScreen.HELP_SUPPORT) },
            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Support") },
            label = { Text("Support") },
            modifier = Modifier.testTag("passenger_nav_support")
        )
    }
}

@Composable
private fun PassengerSplashScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(QuettaNavyDark, QuettaNavy, Color(0xFF0F2942))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuettaLogoMark(size = 96.dp)
            Text(
                text = "QUETTA RIDE",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = QuettaYellow,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Book Smart. Ride Safe. Reach Anywhere.",
                    style = MaterialTheme.typography.labelLarge,
                    color = QuettaNavyDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Text(
                text = "Balochistan's Smart Multi-Vehicle Ride-Hailing Platform\nBike • Auto Rickshaw • Suzuki Alto",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuettaYellow,
                    contentColor = QuettaNavyDark
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp)
                    .testTag("splash_continue_btn")
            ) {
                Text("Launch Quetta Ride", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PassengerWelcomeScreen(
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onSkipToHome: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuettaHeroBannerIllustration()
        }
        item {
            Text(
                text = "Welcome to Quetta Ride",
                style = MaterialTheme.typography.headlineLarge,
                color = QuettaNavyDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Whether you are commuting solo on Jinnah Road, taking a Rickshaw with family to Kandahari Bazaar, or booking a comfortable Alto to Quetta Airport — we have your ride ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = QuettaTextSecondary
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VehicleCategoryId.entries.forEach { cat ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VehicleIllustrationCanvas(
                                category = cat,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.displayName.substringBefore(" /"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = QuettaNavyDark
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_login_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Login with Mobile Number", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_signup_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Create New Passenger Account", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onSkipToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue Instant Demo as Masood Khan Achakzai →", color = QuettaNavyLight)
            }
        }
    }
}

@Composable
private fun PassengerAuthScreen(
    isSignUp: Boolean,
    onSendOtp: () -> Unit,
    onSwitchMode: () -> Unit
) {
    var fullName by remember { mutableStateOf("Masood Khan Achakzai") }
    var phone by remember { mutableStateOf("+92 311 8004421") }
    var email by remember { mutableStateOf("masood@quettaride.pk") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuettaLogoMark(size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSignUp) "Create Passenger Account" else "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        color = QuettaNavyDark
                    )
                    Text(
                        text = "Instant SMS OTP verification for Quetta commuters",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuettaTextSecondary
                    )
                }
            }
        }
        if (isSignUp) {
            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Pakistani Mobile Number (+92)") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
        if (isSignUp) {
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        item {
            Button(
                onClick = onSendOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_send_otp_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuettaYellow,
                    contentColor = QuettaNavyDark
                )
            ) {
                Text(
                    text = if (isSignUp) "Register & Send 4-Digit OTP" else "Send Verification OTP",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            TextButton(onClick = onSwitchMode, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isSignUp) "Already have an account? Login" else "New to Quetta Ride? Create Account"
                )
            }
        }
    }
}

@Composable
private fun PassengerOtpScreen(
    phone: String,
    onVerified: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = "Account Verification",
            tint = QuettaNavyDark,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = "Mobile & Identity Verification",
            style = MaterialTheme.typography.headlineMedium,
            color = QuettaNavyDark
        )
        Text(
            text = "Your account is authenticated via Google Identity & Firebase Auth. Enter the SMS verification code sent to $phone to bind your mobile number.",
            style = MaterialTheme.typography.bodyMedium,
            color = QuettaTextSecondary,
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value = otpInput,
            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) otpInput = it },
            label = { Text("Enter 6-Digit SMS Code") },
            placeholder = { Text("••••••") },
            modifier = Modifier.fillMaxWidth(0.85f),
            singleLine = true
        )
        Button(
            onClick = onVerified,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_otp_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
        ) {
            Text("Confirm Verification & Open Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PassengerHomeDashboard(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Ride Banner if a booking is live
        uiState.activeRide?.let { activeRide ->
            item {
                Surface(
                    color = QuettaGreenDark,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigatePassenger(PassengerScreen.LIVE_TRACKING) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE RIDE • ${activeRide.status.label.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuettaYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeRide.pickupLocation.name} → ${activeRide.destinationLocation.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Captain ${activeRide.driverName} • ${activeRide.vehiclePlate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Button(
                            onClick = { viewModel.navigatePassenger(PassengerScreen.LIVE_TRACKING) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuettaYellow,
                                contentColor = QuettaNavyDark
                            )
                        ) {
                            Text("Track", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Welcome & Passenger Wallet Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = QuettaNavyDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CaptainAvatar(
                            name = uiState.currentPassenger.fullName,
                            seed = 1,
                            isOnline = true,
                            size = 48.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Salam, ${uiState.currentPassenger.fullName.substringBefore(" ")}!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Zone: ${uiState.pickupLocation.name}, ${uiState.selectedCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaYellow
                            )
                        }
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "QR WALLET",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "PKR ${uiState.currentPassenger.walletBalancePkr}",
                                style = MaterialTheme.typography.titleSmall,
                                color = QuettaYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Interactive Quetta Map Preview
        item {
            QuettaInteractiveMap(
                locations = uiState.locations,
                pickup = uiState.pickupLocation,
                destination = uiState.destinationLocation,
                nearbyDrivers = uiState.drivers,
                activeRide = uiState.activeRide,
                selectedCityName = uiState.selectedCity,
                onLocationTapped = { loc ->
                    viewModel.selectDestination(loc)
                },
                modifier = Modifier.height(235.dp)
            )
        }

        // Pickup & Destination Interactive Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plan Your Quetta Ride",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { viewModel.refreshLiveGpsLocation(silent = false) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("use_live_gps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "Live GPS",
                                    tint = QuettaGreenDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Live GPS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { viewModel.swapLocations() },
                                modifier = Modifier.testTag("swap_locations_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Pickup and Destination",
                                    tint = QuettaNavyDark
                                )
                            }
                        }
                    }

                    uiState.liveGpsCoordinate?.let { coord ->
                        Text(
                            text = "GPS Telemetry: ${"%.4f".format(coord.latitude)}° N, ${"%.4f".format(coord.longitude)}° E (±${coord.accuracyMeters.toInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaGreenDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Pickup Box
                    Surface(
                        color = QuettaSurfaceVariantLight,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigatePassenger(PassengerScreen.PICKUP_SELECT) }
                            .testTag("select_pickup_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Pickup",
                                tint = QuettaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PICKUP LOCATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuettaTextSecondary
                                )
                                Text(
                                    text = "${uiState.pickupLocation.name} • ${uiState.pickupLocation.area}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = QuettaNavyDark
                                )
                            }
                            Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = QuettaTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination Box
                    Surface(
                        color = QuettaSurfaceVariantLight,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigatePassenger(PassengerScreen.DESTINATION_SELECT) }
                            .testTag("select_destination_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Destination",
                                tint = QuettaRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WHERE TO?",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuettaTextSecondary
                                )
                                Text(
                                    text = "${uiState.destinationLocation.name} • ${uiState.destinationLocation.area}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = QuettaNavyDark
                                )
                            }
                            Icon(Icons.Default.Search, contentDescription = null, tint = QuettaNavyDark)
                        }
                    }
                }
            }
        }

        // Quick Vehicle Category Selector Cards (Bike, Rickshaw, Alto)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Vehicle Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = QuettaNavyDark
                )
                Text(
                    text = "Illustrative Estimates",
                    style = MaterialTheme.typography.labelSmall,
                    color = QuettaTextSecondary
                )
            }
        }

        items(VehicleCategoryId.entries) { category ->
            val breakdown = viewModel.getFareBreakdownFor(category)
            val cfg = uiState.vehicleConfigs.first { it.id == category }
            val isSelected = uiState.selectedVehicleCategory == category

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFEFCE8) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) QuettaNavyDark else QuettaBorderLight
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectVehicleCategory(category) }
                    .testTag("home_vehicle_card_${category.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleIllustrationCanvas(
                        category = category,
                        modifier = Modifier.size(width = 88.dp, height = 68.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = QuettaNavyDark
                            )
                        }
                        Text(
                            text = "${category.defaultCapacity} Pax • ~${cfg.avgEtaMinutes} mins away",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuettaTextSecondary
                        )
                        Text(
                            text = category.idealFor,
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaNavyLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PKR ${breakdown.totalEstimatedPkr}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Text(
                            text = "${breakdown.distanceKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                viewModel.selectVehicleCategory(category)
                                viewModel.navigatePassenger(PassengerScreen.VEHICLE_SELECT)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuettaNavyDark,
                                contentColor = QuettaYellow
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("book_now_${category.name.lowercase()}")
                        ) {
                            Text("Book Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Popular Locations in Quetta Chips
        item {
            Text(
                text = "Popular Quetta Destinations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuettaNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.locations) { loc ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, QuettaBorderLight),
                        modifier = Modifier.clickable { viewModel.selectDestination(loc) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = QuettaNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = loc.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = QuettaNavyDark
                                )
                                Text(
                                    text = loc.landmarkType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuettaTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPickerScreen(
    title: String,
    subtitle: String,
    locations: List<CityLocation>,
    selectedId: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectLocation: (CityLocation) -> Unit,
    onBack: () -> Unit
) {
    val filtered = remember(locations, searchQuery) {
        if (searchQuery.isBlank()) locations
        else locations.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.area.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = QuettaNavyDark)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = QuettaTextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search Jinnah Road, Sariab, Airport, Kuchlak...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("location_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { loc ->
                val isSelected = loc.id == selectedId
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFEFCE8) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) QuettaNavyDark else QuettaBorderLight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectLocation(loc) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = QuettaNavyDark.copy(alpha = 0.08f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = QuettaNavyDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = QuettaNavyDark
                            )
                            Text(
                                text = "${loc.area} • ${loc.landmarkType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaTextSecondary
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = QuettaGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectionScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            QuettaInteractiveMap(
                locations = uiState.locations,
                pickup = uiState.pickupLocation,
                destination = uiState.destinationLocation,
                nearbyDrivers = uiState.drivers,
                selectedCityName = uiState.selectedCity,
                modifier = Modifier.height(190.dp)
            )
        }
        item {
            Text(
                text = "Choose Your Quetta Ride Vehicle",
                style = MaterialTheme.typography.headlineMedium,
                color = QuettaNavyDark
            )
            Text(
                text = "All prices shown are demonstration estimates based on ${viewModel.getCurrentDistanceKm()} km distance.",
                style = MaterialTheme.typography.bodySmall,
                color = QuettaTextSecondary
            )
        }
        items(VehicleCategoryId.entries) { category ->
            val breakdown = viewModel.getFareBreakdownFor(category)
            val cfg = uiState.vehicleConfigs.first { it.id == category }
            val isSelected = uiState.selectedVehicleCategory == category

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFEFCE8) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) QuettaNavyDark else QuettaBorderLight
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectVehicleCategory(category) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VehicleIllustrationCanvas(
                            category = category,
                            modifier = Modifier.size(width = 100.dp, height = 74.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = QuettaNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Capacity: ${category.defaultCapacity} Passenger(s) • ETA ${cfg.avgEtaMinutes} mins",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cfg.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaTextSecondary
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED FARE (DEMO)",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuettaTextSecondary
                            )
                            Text(
                                text = "PKR ${breakdown.totalEstimatedPkr}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = QuettaNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectVehicleCategory(category)
                                    viewModel.navigatePassenger(PassengerScreen.FARE_BREAKDOWN)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Fare Details")
                            }
                            Button(
                                onClick = {
                                    viewModel.selectVehicleCategory(category)
                                    viewModel.navigatePassenger(PassengerScreen.BOOKING_CONFIRM)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = QuettaNavyDark,
                                    contentColor = QuettaYellow
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("select_book_now_${category.name.lowercase()}")
                            ) {
                                Text("Book Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FareEstimationScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val breakdown = viewModel.getFareBreakdownFor(uiState.selectedVehicleCategory)
    val cfg = uiState.vehicleConfigs.first { it.id == uiState.selectedVehicleCategory }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Transparent Fare Breakdown",
                style = MaterialTheme.typography.headlineMedium,
                color = QuettaNavyDark
            )
            Text(
                text = "Demonstration fare engine • Configurable live in Admin Dashboard",
                style = MaterialTheme.typography.bodySmall,
                color = QuettaTextSecondary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Selected Category", color = QuettaTextSecondary)
                        Text(uiState.selectedVehicleCategory.displayName, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Route Distance", color = QuettaTextSecondary)
                        Text("${breakdown.distanceKm} km", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Duration", color = QuettaTextSecondary)
                        Text("~${breakdown.durationMinutes} mins", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Flag-Fall Fare", color = QuettaTextSecondary)
                        Text("PKR ${breakdown.baseFarePkr}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance Charge (${breakdown.distanceKm} km × PKR ${cfg.perKmRatePkr}/km)", color = QuettaTextSecondary)
                        Text("PKR ${breakdown.distanceChargePkr}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Time Charge (${breakdown.durationMinutes} min × PKR ${cfg.perMinuteRatePkr}/min)", color = QuettaTextSecondary)
                        Text("PKR ${breakdown.timeChargePkr}")
                    }
                    if (breakdown.promoDiscountPkr > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Promo (${uiState.activePromoCode})", color = QuettaGreenDark, fontWeight = FontWeight.SemiBold)
                            Text("- PKR ${breakdown.promoDiscountPkr}", color = QuettaGreenDark, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Demonstrative Fare",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Text(
                            text = "PKR ${breakdown.totalEstimatedPkr}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigatePassenger(PassengerScreen.NEARBY_DRIVERS) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("View Nearby Captains")
                }
                Button(
                    onClick = { viewModel.navigatePassenger(PassengerScreen.BOOKING_CONFIRM) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
                ) {
                    Text("Proceed to Confirm", color = QuettaYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NearbyDriversScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val approvedDrivers = uiState.drivers.filter { it.approvalStatus == DriverApprovalStatus.APPROVED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Nearby Verified Captains in Quetta",
                style = MaterialTheme.typography.headlineMedium,
                color = QuettaNavyDark
            )
            Text(
                text = "Select your preferred captain or let Quetta Ride auto-assign the closest driver.",
                style = MaterialTheme.typography.bodySmall,
                color = QuettaTextSecondary
            )
        }
        items(approvedDrivers) { drv ->
            val isSelected = drv.id == uiState.selectedDriverId
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFEFCE8) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) QuettaNavyDark else QuettaBorderLight
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectDriverForBooking(drv.id)
                        viewModel.navigatePassenger(PassengerScreen.BOOKING_CONFIRM)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CaptainAvatar(name = drv.fullName, seed = drv.avatarSeed, isOnline = drv.isOnline, size = 54.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = drv.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Text(
                            text = "${drv.vehicleCategory.displayName} • ${drv.vehicleModel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuettaTextSecondary
                        )
                        Text(
                            text = "Plate: ${drv.vehiclePlate} • ★ ${drv.rating} (${drv.totalTrips} trips)",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.selectDriverForBooking(drv.id)
                            viewModel.navigatePassenger(PassengerScreen.BOOKING_CONFIRM)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Select", color = QuettaYellow)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingConfirmationScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val breakdown = viewModel.getFareBreakdownFor(uiState.selectedVehicleCategory)
    val chosenDriver = uiState.drivers.firstOrNull { it.id == uiState.selectedDriverId }
        ?: uiState.drivers.firstOrNull { it.vehicleCategory == uiState.selectedVehicleCategory }
        ?: uiState.drivers.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Confirm Your Booking",
                style = MaterialTheme.typography.headlineMedium,
                color = QuettaNavyDark
            )
            Text(
                text = "Review route, vehicle, captain, and demonstration fare before confirming.",
                style = MaterialTheme.typography.bodySmall,
                color = QuettaTextSecondary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VehicleIllustrationCanvas(
                            category = uiState.selectedVehicleCategory,
                            modifier = Modifier.size(width = 88.dp, height = 64.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedVehicleCategory.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = QuettaNavyDark
                            )
                            Text(
                                text = "${breakdown.distanceKm} km • ~${breakdown.durationMinutes} mins travel",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaTextSecondary
                            )
                        }
                        Text(
                            text = "PKR ${breakdown.totalEstimatedPkr}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = QuettaGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PICKUP", style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                            Text(uiState.pickupLocation.name, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = QuettaRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("DESTINATION", style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                            Text(uiState.destinationLocation.name, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CaptainAvatar(name = chosenDriver.fullName, seed = chosenDriver.avatarSeed, size = 42.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(chosenDriver.fullName, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                                Text("${chosenDriver.vehicleModel} • ${chosenDriver.vehiclePlate}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        TextButton(onClick = { viewModel.navigatePassenger(PassengerScreen.DRIVER_DETAILS) }) {
                            Text("Captain Info")
                        }
                    }
                    HorizontalDivider()
                    Text(
                        text = "PAYMENT METHOD",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuettaTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CASH" to "Cash (PKR)", "EASYPAISA" to "Easypaisa", "JAZZCASH" to "JazzCash").forEach { (code, label) ->
                            val selected = uiState.selectedPaymentMethod == code
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectPaymentMethod(code) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("payment_chip_${code.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = { viewModel.confirmPassengerBooking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_booking_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuettaYellow,
                    contentColor = QuettaNavyDark
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Ride • PKR ${breakdown.totalEstimatedPkr}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DriverDetailsScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val drv = uiState.drivers.firstOrNull { it.id == uiState.selectedDriverId } ?: uiState.drivers.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = QuettaNavyDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CaptainAvatar(name = drv.fullName, seed = drv.avatarSeed, size = 76.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = drv.fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${drv.vehicleModel} • ${drv.vehiclePlate} (${drv.vehicleColor})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuettaYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text("★ ${drv.rating} Rating", color = Color.White) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("${drv.totalTrips} Completed Trips", color = Color.White) }
                        )
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Verification & Local Credentials", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                    Text("• CNIC Verified: ${drv.cnic}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Balochistan Excise Vehicle Fitness Checked", style = MaterialTheme.typography.bodyMedium)
                    Text("• Languages Spoken: ${drv.languages}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Button(
                onClick = { viewModel.navigatePassenger(PassengerScreen.BOOKING_CONFIRM) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Back to Booking Confirmation", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LiveRideTrackingScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val ride = uiState.activeRide ?: uiState.bookings.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            QuettaInteractiveMap(
                locations = uiState.locations,
                pickup = ride.pickupLocation,
                destination = ride.destinationLocation,
                nearbyDrivers = uiState.drivers,
                activeRide = ride,
                selectedCityName = uiState.selectedCity,
                modifier = Modifier.height(250.dp)
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = QuettaGreen.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = ride.status.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = QuettaGreenDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Text(
                            text = "Booking ${ride.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = QuettaTextSecondary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { ride.rideProgressFraction.coerceIn(0.1f, 1.0f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = QuettaGreen,
                        trackColor = QuettaSurfaceVariantLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CaptainAvatar(name = ride.driverName ?: "Captain", seed = 1, size = 48.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = ride.driverName ?: "Mirwais Khan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = QuettaNavyDark
                                )
                                Text(
                                    text = "${ride.vehicleModel} • ${ride.vehiclePlate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QuettaTextSecondary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "PKR ${ride.farePkr}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = QuettaNavyDark
                            )
                            Text(
                                text = "${ride.distanceKm} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuettaTextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.advanceRideStepManually() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("advance_ride_step_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Stage")
                        }
                        Button(
                            onClick = { viewModel.completeActiveRide() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("complete_ride_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete Ride", fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.cancelActiveRide() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Booking", color = QuettaRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun RideCompletedScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val ride = uiState.activeRide ?: uiState.bookings.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = QuettaGreen.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Arrived",
                    tint = QuettaGreenDark,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Text(
            text = "You Have Reached ${ride.destinationLocation.name}!",
            style = MaterialTheme.typography.headlineMedium,
            color = QuettaNavyDark,
            textAlign = TextAlign.Center
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trip ID", color = QuettaTextSecondary)
                    Text(ride.id, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Captain", color = QuettaTextSecondary)
                    Text(ride.driverName ?: "Mirwais Khan", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Route", color = QuettaTextSecondary)
                    Text("${ride.pickupLocation.name} → ${ride.destinationLocation.name}", fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Fare Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "PKR ${ride.farePkr}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = QuettaGreenDark
                    )
                }
            }
        }
        Button(
            onClick = { viewModel.navigatePassenger(PassengerScreen.RATING_REVIEW) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("rate_captain_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = QuettaYellow,
                contentColor = QuettaNavyDark
            )
        ) {
            Text("Rate Your Captain & Leave Review", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RatingAndReviewScreen(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("Excellent Quetta Ride experience! Punctual and courteous captain.") }
    val ride = uiState.activeRide ?: uiState.bookings.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Rate Your Ride with ${ride.driverName ?: "Captain"}",
            style = MaterialTheme.typography.headlineMedium,
            color = QuettaNavyDark,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (star in 1..5) {
                IconButton(onClick = { selectedStars = star }) {
                    Icon(
                        imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$star Stars",
                        tint = QuettaAmber,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Share your feedback about vehicle cleanliness & safety") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(14.dp)
        )
        Button(
            onClick = { viewModel.submitPassengerRating(selectedStars, reviewText) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_rating_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
        ) {
            Text("Submit Review", color = QuettaYellow, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PassengerRideHistoryScreen(
    bookings: List<RideBooking>,
    onBookAgain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Quetta Ride History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = QuettaNavyDark
                )
                Button(onClick = onBookAgain, shape = RoundedCornerShape(10.dp)) {
                    Text("New Ride")
                }
            }
        }
        items(bookings) { b ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${b.id} • ${b.vehicleCategory.displayName}",
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Text(
                            text = "PKR ${b.farePkr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaGreenDark
                        )
                    }
                    Text(
                        text = "${b.pickupLocation.name} → ${b.destinationLocation.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuettaTextPrimary
                    )
                    Text(
                        text = "Captain: ${b.driverName ?: "Assigned"} (${b.vehiclePlate}) • ${b.createdAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuettaTextSecondary
                    )
                    b.passengerReview?.let { rev ->
                        Text(
                            text = "★ ${b.passengerRating ?: 5}/5 — \"$rev\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaNavyLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassengerProfileScreen(
    passenger: PassengerProfile,
    authenticatedEmail: String?,
    cloudSyncActive: Boolean,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenAuth: () -> Unit
) {
    var name by remember(passenger) { mutableStateOf(passenger.fullName) }
    var phone by remember(passenger) { mutableStateOf(passenger.phone) }
    var email by remember(passenger, authenticatedEmail) { mutableStateOf(authenticatedEmail ?: passenger.email) }
    var home by remember(passenger) { mutableStateOf(passenger.homeLocation) }
    var work by remember(passenger) { mutableStateOf(passenger.workLocation) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CaptainAvatar(name = passenger.fullName, seed = 1, size = 64.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(passenger.fullName, style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
                    Text("Member since ${passenger.memberSince} • ★ ${passenger.rating}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (cloudSyncActive) "Verified Cloud Account • ${authenticatedEmail ?: passenger.email}" else "Local Session",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuettaGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = home, onValueChange = { home = it }, label = { Text("Saved Home Address") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = work, onValueChange = { work = it }, label = { Text("Saved Work/University Address") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = { onSaveProfile(name, phone, email, home, work) },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_passenger_profile_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Save & Sync Profile to Cloud", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenAuth,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Mobile Verification / Welcome Flow")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth().testTag("delete_account_btn")
            ) {
                Text("Delete My Account & Cloud Data", color = QuettaRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Quetta Ride Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This permanently deletes your passenger profile and signs you out of Firebase Authentication.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuettaRed)
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PassengerHelpSupportScreen(
    onSubmitTicket: (String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Quetta Ride Help & Safety Center", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("24/7 Local Support Desk at Zarghoon Road HQ, Quetta • Helpline: 081-111-783-882", style = MaterialTheme.typography.bodySmall, color = QuettaTextSecondary)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Submit a Support Request or Complaint", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Issue Subject (e.g., Lost Item, Fare Query)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Describe your issue") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                onSubmitTicket(subject, details)
                                subject = ""
                                details = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_support_ticket_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
                    ) {
                        Text("Send to Admin Support Desk", color = QuettaYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
