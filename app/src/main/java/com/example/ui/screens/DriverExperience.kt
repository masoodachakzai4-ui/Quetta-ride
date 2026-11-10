package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.QuettaPlatformUiState
import com.example.viewmodel.QuettaRideViewModel

@Composable
fun DriverExperience(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.driverScreen != DriverScreen.DASHBOARD) {
        BackHandler {
            viewModel.navigateDriver(DriverScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            DriverBottomBar(
                currentScreen = uiState.driverScreen,
                hasIncomingRequest = uiState.incomingDriverRequest != null,
                onNavigate = { viewModel.navigateDriver(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DriverScreenExplorerBar(
                currentScreen = uiState.driverScreen,
                onSelectScreen = { viewModel.navigateDriver(it) }
            )

            when (uiState.driverScreen) {
                DriverScreen.SPLASH -> DriverSplashView(
                    onContinue = { viewModel.navigateDriver(DriverScreen.LOGIN) }
                )
                DriverScreen.LOGIN -> DriverLoginView(
                    onLoginSuccess = { viewModel.navigateDriver(DriverScreen.DASHBOARD) },
                    onStartRegistration = { viewModel.navigateDriver(DriverScreen.REGISTER_PERSONAL) }
                )
                DriverScreen.REGISTER_PERSONAL -> DriverRegisterPersonalView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.REGISTER_VEHICLE -> DriverRegisterVehicleView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.REGISTER_DOCS -> DriverRegisterDocsView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.DASHBOARD -> DriverDashboardView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.NEW_REQUEST -> DriverNewRequestView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.PICKUP_NAVIGATION,
                DriverScreen.RIDE_IN_PROGRESS -> DriverActiveTripNavigationView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.COMPLETE_RIDE -> DriverTripCompletedView(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DriverScreen.DAILY_EARNINGS -> DriverEarningsView(
                    driver = uiState.activeDriver,
                    bookings = uiState.bookings
                )
                DriverScreen.RIDE_HISTORY -> DriverHistoryView(
                    bookings = uiState.bookings
                )
                DriverScreen.PROFILE -> DriverProfileView(
                    driver = uiState.activeDriver,
                    onRegisterNew = { viewModel.navigateDriver(DriverScreen.REGISTER_PERSONAL) }
                )
                DriverScreen.RATINGS -> DriverRatingsView(
                    driver = uiState.activeDriver,
                    bookings = uiState.bookings
                )
                DriverScreen.SETTINGS -> DriverSettingsView(
                    driver = uiState.activeDriver,
                    onToggleOnline = { viewModel.toggleDriverOnline() },
                    onResetDemo = { viewModel.resetDemoData() }
                )
            }
        }
    }
}

@Composable
private fun DriverScreenExplorerBar(
    currentScreen: DriverScreen,
    onSelectScreen: (DriverScreen) -> Unit
) {
    Surface(
        color = Color(0xFF064E3B),
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
                text = "Captain Screens:",
                style = MaterialTheme.typography.labelSmall,
                color = QuettaYellow,
                fontWeight = FontWeight.Bold
            )
            DriverScreen.entries.forEachIndexed { idx, screen ->
                val isSelected = screen == currentScreen
                Surface(
                    color = if (isSelected) QuettaYellow else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clickable { onSelectScreen(screen) }
                        .testTag("driver_step_${screen.name.lowercase()}")
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
private fun DriverBottomBar(
    currentScreen: DriverScreen,
    hasIncomingRequest: Boolean,
    onNavigate: (DriverScreen) -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentScreen == DriverScreen.DASHBOARD,
            onClick = { onNavigate(DriverScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            modifier = Modifier.testTag("driver_nav_dashboard")
        )
        NavigationBarItem(
            selected = currentScreen == DriverScreen.NEW_REQUEST || currentScreen == DriverScreen.PICKUP_NAVIGATION || currentScreen == DriverScreen.RIDE_IN_PROGRESS,
            onClick = { onNavigate(DriverScreen.NEW_REQUEST) },
            icon = {
                BadgedBox(
                    badge = {
                        if (hasIncomingRequest) {
                            Badge(containerColor = QuettaRed) { Text("1") }
                        }
                    }
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = "Requests")
                }
            },
            label = { Text("Requests") },
            modifier = Modifier.testTag("driver_nav_requests")
        )
        NavigationBarItem(
            selected = currentScreen == DriverScreen.DAILY_EARNINGS,
            onClick = { onNavigate(DriverScreen.DAILY_EARNINGS) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
            label = { Text("Earnings") },
            modifier = Modifier.testTag("driver_nav_earnings")
        )
        NavigationBarItem(
            selected = currentScreen == DriverScreen.REGISTER_PERSONAL || currentScreen == DriverScreen.REGISTER_VEHICLE || currentScreen == DriverScreen.REGISTER_DOCS,
            onClick = { onNavigate(DriverScreen.REGISTER_PERSONAL) },
            icon = { Icon(Icons.Default.AppRegistration, contentDescription = "Register") },
            label = { Text("Register") },
            modifier = Modifier.testTag("driver_nav_register")
        )
        NavigationBarItem(
            selected = currentScreen == DriverScreen.PROFILE || currentScreen == DriverScreen.SETTINGS,
            onClick = { onNavigate(DriverScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Captain") },
            label = { Text("Profile") },
            modifier = Modifier.testTag("driver_nav_profile")
        )
    }
}

@Composable
private fun DriverSplashView(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuettaNavyDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        QuettaLogoMark(size = 88.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "QUETTA RIDE CAPTAIN",
            style = MaterialTheme.typography.displayMedium,
            color = QuettaYellow,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Drive Your Bike, Rickshaw, or Alto in Quetta & Earn on Your Schedule.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = QuettaYellow, contentColor = QuettaNavyDark),
            modifier = Modifier.fillMaxWidth(0.75f).height(52.dp)
        ) {
            Text("Continue to Captain Portal", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DriverLoginView(
    onLoginSuccess: () -> Unit,
    onStartRegistration: () -> Unit
) {
    var phone by remember { mutableStateOf("+92 313 8429910") }
    var cnic by remember { mutableStateOf("54400-8392014-1") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Captain Login", style = MaterialTheme.typography.headlineLarge, color = QuettaNavyDark)
            Text("Sign in with your registered Pakistani mobile number & CNIC", style = MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Registered Mobile Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = cnic,
                onValueChange = { cnic = it },
                label = { Text("CNIC Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Login as Captain Mirwais Khan", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onStartRegistration,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("New Driver? Register Bike / Rickshaw / Alto")
            }
        }
    }
}

@Composable
private fun DriverRegisterPersonalView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    var name by remember(uiState.regFullName) { mutableStateOf(uiState.regFullName.ifBlank { "Tariq Mahmood Jamali" }) }
    var mobile by remember(uiState.regMobile) { mutableStateOf(uiState.regMobile.ifBlank { "+92 315 8901122" }) }
    var email by remember(uiState.regEmail) { mutableStateOf(uiState.regEmail.ifBlank { "tariq.jamali@quettaride.pk" }) }
    var cnic by remember(uiState.regCnic) { mutableStateOf(uiState.regCnic.ifBlank { "54400-8821903-5" }) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Driver Registration • Step 1 of 3", style = MaterialTheme.typography.labelLarge, color = QuettaGreenDark)
            Text("Personal Information", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        item {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name (as on CNIC)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("NADRA CNIC Number (54400-XXXXXXX-X)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = {
                    viewModel.updateDriverRegPersonal(name, mobile, email, cnic)
                    viewModel.navigateDriver(DriverScreen.REGISTER_VEHICLE)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("driver_reg_next_vehicle_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Next: Vehicle Information →", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DriverRegisterVehicleView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    var selectedCat by remember { mutableStateOf(uiState.regVehicleCat) }
    var plate by remember { mutableStateOf(uiState.regVehiclePlate.ifBlank { "QTA-26-4901" }) }
    var model by remember { mutableStateOf(uiState.regVehicleModel.ifBlank { "Sazgar 4-Stroke Rickshaw" }) }
    var color by remember { mutableStateOf(uiState.regVehicleColor.ifBlank { "Navy Blue & Bright Yellow" }) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Driver Registration • Step 2 of 3", style = MaterialTheme.typography.labelLarge, color = QuettaGreenDark)
            Text("Vehicle Information", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        item {
            Text("Select Vehicle Category", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VehicleCategoryId.entries.forEach { cat ->
                    val isSelected = selectedCat == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCat = cat
                            model = when (cat) {
                                VehicleCategoryId.BIKE -> "Honda CG 125"
                                VehicleCategoryId.RICKSHAW -> "Sazgar 4-Stroke Deluxe"
                                VehicleCategoryId.ALTO -> "Suzuki Alto VXR 660cc"
                            }
                        },
                        label = { Text(cat.displayName.substringBefore(" /")) }
                    )
                }
            }
        }
        item {
            OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Registration Plate (e.g., QTA-25-1102)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Vehicle Make & Model") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Vehicle Color") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = {
                    viewModel.updateDriverRegVehicle(selectedCat, plate, model, color)
                    viewModel.navigateDriver(DriverScreen.REGISTER_DOCS)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("driver_reg_next_docs_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Next: Upload Documents →", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DriverRegisterDocsView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    var photoChecked by remember { mutableStateOf(true) }
    var cnicChecked by remember { mutableStateOf(true) }
    var exciseChecked by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Driver Registration • Step 3 of 3", style = MaterialTheme.typography.labelLarge, color = QuettaGreenDark)
            Text("Document Upload & Verification", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Upload clear photos of your documents for Admin approval.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            DocUploadCard(
                title = "Captain Profile Photo",
                subtitle = "Clear front-facing portrait photo",
                uploaded = photoChecked,
                onToggle = { photoChecked = !photoChecked }
            )
        }
        item {
            DocUploadCard(
                title = "NADRA CNIC (Front & Back)",
                subtitle = "Valid Pakistani National Identity Card",
                uploaded = cnicChecked,
                onToggle = { cnicChecked = !cnicChecked }
            )
        }
        item {
            DocUploadCard(
                title = "Vehicle Registration & Driving License",
                subtitle = "Balochistan Excise Smart Card & LTV/M-Cycle License",
                uploaded = exciseChecked,
                onToggle = { exciseChecked = !exciseChecked }
            )
        }
        item {
            Button(
                onClick = { viewModel.submitDriverRegistration() },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("submit_driver_registration_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = QuettaYellow, contentColor = QuettaNavyDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CloudDone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Application to Quetta Ride Admin", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DocUploadCard(
    title: String,
    subtitle: String,
    uploaded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = QuettaTextSecondary)
            }
            Surface(
                color = if (uploaded) QuettaGreen.copy(alpha = 0.15f) else QuettaSurfaceVariantLight,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (uploaded) QuettaGreenDark else QuettaNavyDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uploaded) "Uploaded" else "Tap to Attach",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uploaded) QuettaGreenDark else QuettaNavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverDashboardView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val drv = uiState.activeDriver

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Captain Status & Online/Offline Toggle Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (drv.isOnline) QuettaNavyDark else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        CaptainAvatar(name = drv.fullName, seed = drv.avatarSeed, isOnline = drv.isOnline, size = 54.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = drv.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${drv.vehicleModel} • ${drv.vehiclePlate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuettaYellow
                            )
                            Text(
                                text = "Status: ${if (drv.isOnline) "ONLINE (Receiving Rides)" else "OFFLINE"} • ${drv.approvalStatus.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Switch(
                        checked = drv.isOnline,
                        onCheckedChange = { viewModel.toggleDriverOnline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = QuettaNavyDark,
                            checkedTrackColor = QuettaYellow
                        ),
                        modifier = Modifier.testTag("driver_online_switch")
                    )
                }
            }
        }

        // KPI Grid: Today's Earnings, Today's Rides, Rating, Total Trips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DriverMetricCard(
                    title = "Today's Earnings",
                    value = "PKR ${drv.todayEarningsPkr}",
                    subtitle = "+12% vs yesterday",
                    accent = QuettaGreenDark,
                    modifier = Modifier.weight(1f)
                )
                DriverMetricCard(
                    title = "Today's Rides",
                    value = "${drv.todayRides} Trips",
                    subtitle = "${drv.totalTrips} lifetime",
                    accent = QuettaNavyDark,
                    modifier = Modifier.weight(1f)
                )
                DriverMetricCard(
                    title = "Captain Rating",
                    value = "★ ${drv.rating}",
                    subtitle = "Top 5% in Quetta",
                    accent = QuettaAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Incoming Ride Request Alert Card
        item {
            val req = uiState.incomingDriverRequest
            if (req != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, QuettaNavyDark),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = QuettaNavyDark, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "NEW RIDE REQUEST • ${req.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuettaYellow,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "PKR ${req.farePkr}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = QuettaGreenDark
                            )
                        }
                        Text(
                            text = "Passenger: ${req.passengerName} (${req.distanceKm} km • ~${req.estimatedMinutes} mins)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                        Text(
                            text = "Pickup: ${req.pickupLocation.name} → Dropoff: ${req.destinationLocation.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = QuettaTextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rejectDriverRequest() },
                                modifier = Modifier.weight(1f).testTag("driver_reject_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = QuettaRed)
                            ) {
                                Text("Reject Ride", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.acceptDriverRequest() },
                                modifier = Modifier.weight(1f).testTag("driver_accept_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark)
                            ) {
                                Text("Accept Ride", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Waiting for Next Quetta Ride Request", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                            Text("Tap 'Simulate Request' to test incoming passenger booking", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { viewModel.triggerSampleDriverRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark),
                            modifier = Modifier.testTag("simulate_driver_request_btn")
                        ) {
                            Text("Simulate Request", color = QuettaYellow)
                        }
                    }
                }
            }
        }

        // Quetta Live Demand Map for Driver
        item {
            QuettaInteractiveMap(
                locations = uiState.locations,
                pickup = uiState.incomingDriverRequest?.pickupLocation ?: uiState.pickupLocation,
                destination = uiState.incomingDriverRequest?.destinationLocation ?: uiState.destinationLocation,
                nearbyDrivers = listOf(drv),
                activeRide = uiState.incomingDriverRequest,
                selectedCityName = uiState.selectedCity,
                modifier = Modifier.height(210.dp)
            )
        }
    }
}

@Composable
private fun DriverMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
        }
    }
}

@Composable
private fun DriverNewRequestView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val req = uiState.incomingDriverRequest

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Incoming Booking Dispatch", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        if (req == null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active ride dispatch at the moment.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { viewModel.triggerSampleDriverRequest() }) {
                        Text("Trigger Live Dispatch Request")
                    }
                }
            }
        } else {
            QuettaInteractiveMap(
                locations = uiState.locations,
                pickup = req.pickupLocation,
                destination = req.destinationLocation,
                nearbyDrivers = listOf(uiState.activeDriver),
                activeRide = req,
                modifier = Modifier.height(220.dp)
            )
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Passenger: ${req.passengerName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Pickup: ${req.pickupLocation.name} (${req.pickupLocation.area})")
                    Text("Destination: ${req.destinationLocation.name} (${req.destinationLocation.area})")
                    Text("Distance: ${req.distanceKm} km • Est. Fare: PKR ${req.farePkr}", fontWeight = FontWeight.Bold, color = QuettaGreenDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.rejectDriverRequest() }, modifier = Modifier.weight(1f)) {
                            Text("Reject Ride", color = QuettaRed)
                        }
                        Button(
                            onClick = { viewModel.acceptDriverRequest() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark)
                        ) {
                            Text("Accept Ride", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverActiveTripNavigationView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val req = uiState.incomingDriverRequest ?: uiState.activeRide ?: uiState.bookings.first()
    val isAtPickupStage = uiState.driverScreen == DriverScreen.PICKUP_NAVIGATION

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (isAtPickupStage) "Pickup Navigation: ${req.pickupLocation.name}" else "Ride in Progress → ${req.destinationLocation.name}",
            style = MaterialTheme.typography.headlineMedium,
            color = QuettaNavyDark
        )
        QuettaInteractiveMap(
            locations = uiState.locations,
            pickup = req.pickupLocation,
            destination = req.destinationLocation,
            nearbyDrivers = listOf(uiState.activeDriver),
            activeRide = req,
            modifier = Modifier.height(250.dp)
        )
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Passenger: ${req.passengerName} • ${req.passengerPhone}", fontWeight = FontWeight.Bold)
                Text("Fare to Collect: PKR ${req.farePkr} (${req.paymentMethod})", color = QuettaGreenDark, fontWeight = FontWeight.Bold)
                if (isAtPickupStage) {
                    Button(
                        onClick = { viewModel.startDriverTripFromPickup() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("driver_start_trip_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
                    ) {
                        Text("Passenger Picked Up • Start Trip", color = QuettaYellow, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeDriverTrip() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("driver_complete_trip_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark)
                    ) {
                        Text("Arrived at Destination • Complete Ride", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverTripCompletedView(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val req = uiState.incomingDriverRequest ?: uiState.bookings.first()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.TaskAlt, contentDescription = null, tint = QuettaGreenDark, modifier = Modifier.size(64.dp))
        Text("Trip Completed Successfully!", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        Text("Cash / Wallet Collected: PKR ${req.farePkr}", style = MaterialTheme.typography.titleLarge, color = QuettaGreenDark, fontWeight = FontWeight.Bold)
        Text("Today's Total Captain Earnings: PKR ${uiState.activeDriver.todayEarningsPkr}")
        Button(
            onClick = { viewModel.navigateDriver(DriverScreen.DASHBOARD) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
        ) {
            Text("Return to Captain Dashboard", color = QuettaYellow, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DriverEarningsView(
    driver: DriverProfile,
    bookings: List<RideBooking>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = QuettaNavyDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TODAY'S CAPTAIN EARNINGS", style = MaterialTheme.typography.labelSmall, color = QuettaYellow)
                    Text("PKR ${driver.todayEarningsPkr}", style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Completed Rides Today: ${driver.todayRides} • Weekly Bonus Eligible", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
        items(bookings) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${b.pickupLocation.name} → ${b.destinationLocation.name}", fontWeight = FontWeight.Bold)
                        Text("${b.id} • ${b.distanceKm} km • ${b.createdAt}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("+ PKR ${b.farePkr}", fontWeight = FontWeight.Bold, color = QuettaGreenDark)
                }
            }
        }
    }
}

@Composable
private fun DriverHistoryView(bookings: List<RideBooking>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Captain Ride History", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(bookings) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${b.id} • ${b.passengerName}", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                    Text("${b.pickupLocation.name} → ${b.destinationLocation.name} (${b.distanceKm} km)")
                    Text("Fare: PKR ${b.farePkr} • ${b.status.label}", color = QuettaGreenDark, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DriverProfileView(
    driver: DriverProfile,
    onRegisterNew: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CaptainAvatar(name = driver.fullName, seed = driver.avatarSeed, size = 60.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(driver.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${driver.vehicleCategory.displayName} • ${driver.vehiclePlate}")
                    }
                }
                HorizontalDivider()
                Text("Mobile: ${driver.phone}")
                Text("CNIC: ${driver.cnic}")
                Text("Vehicle: ${driver.vehicleModel} (${driver.vehicleColor})")
                Text("Approval Status: ${driver.approvalStatus.label}", fontWeight = FontWeight.Bold, color = QuettaGreenDark)
            }
        }
        OutlinedButton(onClick = onRegisterNew, modifier = Modifier.fillMaxWidth()) {
            Text("Open Driver Registration Form (Demo)")
        }
    }
}

@Composable
private fun DriverRatingsView(
    driver: DriverProfile,
    bookings: List<RideBooking>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Captain Ratings & Passenger Feedback", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Overall Rating: ★ ${driver.rating} across ${driver.totalTrips} trips", color = QuettaAmber, fontWeight = FontWeight.Bold)
        }
        items(bookings.filter { it.passengerReview != null }) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${b.passengerName} • ★ ${b.passengerRating ?: 5}/5", fontWeight = FontWeight.Bold)
                    Text("\"${b.passengerReview}\"", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun DriverSettingsView(
    driver: DriverProfile,
    onToggleOnline: () -> Unit,
    onResetDemo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Captain App Settings", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Availability Status (${if (driver.isOnline) "Online" else "Offline"})", fontWeight = FontWeight.Bold)
                Switch(checked = driver.isOnline, onCheckedChange = { onToggleOnline() })
            }
        }
        OutlinedButton(onClick = onResetDemo, modifier = Modifier.fillMaxWidth()) {
            Text("Reset Demo Data", color = QuettaRed)
        }
    }
}
