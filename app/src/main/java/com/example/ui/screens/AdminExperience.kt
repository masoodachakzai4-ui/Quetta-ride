package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.CaptainAvatar
import com.example.ui.components.VehicleIllustrationCanvas
import com.example.ui.theme.*
import com.example.viewmodel.QuettaPlatformUiState
import com.example.viewmodel.QuettaRideViewModel
import kotlin.math.roundToInt

@Composable
fun AdminExperience(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.adminSection != AdminSection.DASHBOARD) {
        BackHandler {
            viewModel.navigateAdmin(AdminSection.DASHBOARD)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 700.dp
        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                AdminSidebarNavigation(
                    currentSection = uiState.adminSection,
                    onSelectSection = { viewModel.navigateAdmin(it) },
                    modifier = Modifier.width(230.dp).fillMaxHeight()
                )
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AdminMainContent(uiState = uiState, viewModel = viewModel)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AdminTopChipsBar(
                    currentSection = uiState.adminSection,
                    onSelectSection = { viewModel.navigateAdmin(it) }
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AdminMainContent(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun AdminSidebarNavigation(
    currentSection: AdminSection,
    onSelectSection: (AdminSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = QuettaNavyDark, modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "QUETTA RIDE HQ",
                    style = MaterialTheme.typography.titleMedium,
                    color = QuettaYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(AdminSection.entries) { section ->
                val isSelected = section == currentSection
                Surface(
                    color = if (isSelected) QuettaYellow else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSection(section) }
                        .testTag("admin_nav_${section.name.lowercase()}")
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) QuettaNavyDark else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminTopChipsBar(
    currentSection: AdminSection,
    onSelectSection: (AdminSection) -> Unit
) {
    Surface(color = QuettaNavyDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Admin Menu:",
                style = MaterialTheme.typography.labelSmall,
                color = QuettaYellow,
                fontWeight = FontWeight.Bold
            )
            AdminSection.entries.forEach { section ->
                val isSelected = section == currentSection
                Surface(
                    color = if (isSelected) QuettaYellow else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clickable { onSelectSection(section) }
                        .testTag("admin_chip_${section.name.lowercase()}")
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) QuettaNavyDark else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMainContent(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    when (uiState.adminSection) {
        AdminSection.DASHBOARD -> AdminOverviewSection(uiState, viewModel)
        AdminSection.PASSENGERS -> AdminPassengersSection(uiState.passengers)
        AdminSection.DRIVERS -> AdminDriversSection(uiState.drivers, viewModel)
        AdminSection.VEHICLES -> AdminVehicleCategoriesSection(uiState.vehicleConfigs, viewModel)
        AdminSection.RIDES,
        AdminSection.BOOKINGS -> AdminBookingsSection(uiState.bookings)
        AdminSection.LOCATIONS -> AdminLocationsSection(uiState, viewModel)
        AdminSection.FARES -> AdminFareConfigSection(uiState.vehicleConfigs, viewModel)
        AdminSection.PAYMENTS -> AdminPaymentsSection(uiState.bookings)
        AdminSection.COMPLAINTS -> AdminComplaintsSection(uiState.complaints, viewModel)
        AdminSection.REVIEWS -> AdminReviewsSection(uiState.bookings)
        AdminSection.REPORTS -> AdminReportsSection(uiState)
        AdminSection.NOTIFICATIONS -> AdminNotificationsSection(uiState.notifications)
        AdminSection.SETTINGS -> AdminSettingsSection(viewModel)
    }
}

@Composable
private fun AdminOverviewSection(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    val totalPassengers = uiState.passengers.size + 1480
    val totalDrivers = uiState.drivers.size + 410
    val activeDrivers = uiState.drivers.count { it.isOnline } + 312
    val totalRides = uiState.bookings.size + 6840
    val activeRides = if (uiState.activeRide != null) 28 else 27
    val completedRides = uiState.bookings.count { it.status == RideStatus.COMPLETED } + 6690
    val cancelledRides = uiState.bookings.count { it.status == RideStatus.CANCELLED } + 122
    val estimatedRevenue = uiState.bookings.sumOf { it.farePkr } + 1842500

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quetta Ride Executive Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        color = QuettaNavyDark
                    )
                    Text(
                        text = "Live Command Center • Quetta Launch Fleet & Operations",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuettaTextSecondary
                    )
                }
                Button(
                    onClick = { viewModel.navigateAdmin(AdminSection.FARES) },
                    colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
                ) {
                    Text("Configure Fares", color = QuettaYellow)
                }
            }
        }

        // 8 Required Analytics KPI Cards (4 rows of 2 on mobile)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Total Passengers", "$totalPassengers", "+18% this month", QuettaNavyDark, Modifier.weight(1f))
                    AdminKpiCard("Total Drivers", "$totalDrivers", "${uiState.drivers.count { it.approvalStatus == DriverApprovalStatus.PENDING }} pending approval", QuettaBlueAccent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Active Drivers", "$activeDrivers", "Online in Quetta grid", QuettaGreenDark, Modifier.weight(1f))
                    AdminKpiCard("Total Rides", "$totalRides", "Across 3 categories", QuettaNavyDark, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Active Rides", "$activeRides", "Live GPS tracked", QuettaAmber, Modifier.weight(1f))
                    AdminKpiCard("Completed Rides", "$completedRides", "97.8% completion rate", QuettaGreenDark, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Cancelled Rides", "$cancelledRides", "2.1% cancellation", QuettaRed, Modifier.weight(1f))
                    AdminKpiCard("Est. Revenue", "PKR $estimatedRevenue", "Gross Merchandise Value", QuettaGreenDark, Modifier.weight(1f))
                }
            }
        }

        // Custom Revenue & Fleet Split Analytics Chart Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Weekly Ride Demand Across Quetta Corridors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = QuettaNavyDark
                    )
                    Text(
                        text = "Bike (44%) • Rickshaw (33%) • Suzuki Alto (23%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuettaTextSecondary
                    )
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 8.dp)
                    ) {
                        val bars = listOf(0.58f, 0.72f, 0.65f, 0.81f, 0.94f, 0.88f, 1.0f)
                        val barColors = listOf(
                            QuettaNavy,
                            QuettaNavy,
                            QuettaBlueAccent,
                            QuettaGreen,
                            QuettaYellowDark,
                            QuettaNavyDark,
                            QuettaGreenDark
                        )
                        val barWidth = size.width / (bars.size * 2f)
                        bars.forEachIndexed { index, ratio ->
                            val barH = size.height * ratio
                            val left = index * (barWidth * 2f) + barWidth * 0.4f
                            drawRoundRect(
                                color = barColors[index % barColors.size],
                                topLeft = Offset(left, size.height - barH),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(day, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                        }
                    }
                }
            }
        }

        // Pending Driver Approvals Quick Action
        item {
            Text(
                text = "Pending Driver Approvals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuettaNavyDark
            )
        }
        items(uiState.drivers.filter { it.approvalStatus == DriverApprovalStatus.PENDING }) { drv ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(drv.fullName, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text("${drv.vehicleCategory.displayName} • ${drv.vehicleModel} (${drv.vehiclePlate})", style = MaterialTheme.typography.bodySmall)
                        Text("CNIC: ${drv.cnic}", style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.REJECTED) }
                        ) {
                            Text("Reject", color = QuettaRed)
                        }
                        Button(
                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.APPROVED) },
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark),
                            modifier = Modifier.testTag("approve_driver_${drv.id}")
                        ) {
                            Text("Approve")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
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
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
        }
    }
}

@Composable
private fun AdminPassengersSection(passengers: List<PassengerProfile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Registered Passengers", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(passengers) { p ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CaptainAvatar(name = p.fullName, seed = 2, size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.fullName, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text("${p.phone} • ${p.email}", style = MaterialTheme.typography.bodySmall)
                        Text("Home: ${p.homeLocation}", style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${p.totalRides} rides", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text("★ ${p.rating}", style = MaterialTheme.typography.labelSmall, color = QuettaAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDriversSection(
    drivers: List<DriverProfile>,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Registered Captains & Fleet Approval", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(drivers) { drv ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CaptainAvatar(name = drv.fullName, seed = drv.avatarSeed, isOnline = drv.isOnline, size = 46.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(drv.fullName, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                            Text("${drv.vehicleCategory.displayName} • ${drv.vehicleModel} (${drv.vehiclePlate})", style = MaterialTheme.typography.bodySmall)
                            Text("CNIC: ${drv.cnic} • ${drv.phone}", style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                        }
                        Surface(
                            color = when (drv.approvalStatus) {
                                DriverApprovalStatus.APPROVED -> QuettaGreen.copy(alpha = 0.16f)
                                DriverApprovalStatus.PENDING -> QuettaYellow.copy(alpha = 0.35f)
                                DriverApprovalStatus.REJECTED -> QuettaRed.copy(alpha = 0.16f)
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = drv.approvalStatus.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.APPROVED) },
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Approve", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.REJECTED) },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Reject", color = QuettaRed, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminVehicleCategoriesSection(
    configs: List<VehicleCategoryConfig>,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Vehicle Category Management", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Active fleet classes in Quetta & expandable national categories", style = MaterialTheme.typography.bodySmall)
        }
        items(configs) { cfg ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleIllustrationCanvas(category = cfg.id, modifier = Modifier.size(90.dp, 68.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cfg.id.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Capacity: ${cfg.id.defaultCapacity} Pax • Base PKR ${cfg.baseFarePkr} • PKR ${cfg.perKmRatePkr}/km", style = MaterialTheme.typography.bodySmall)
                        Text(cfg.description, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                    }
                    Switch(
                        checked = cfg.isActive,
                        onCheckedChange = { active ->
                            viewModel.updateVehicleFareConfig(
                                cfg.id,
                                cfg.baseFarePkr,
                                cfg.perKmRatePkr,
                                cfg.perMinuteRatePkr,
                                cfg.minimumFarePkr,
                                active
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminBookingsSection(bookings: List<RideBooking>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Live Bookings & Ride Status Log", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(bookings) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${b.id} • ${b.vehicleCategory.displayName}", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text("PKR ${b.farePkr}", fontWeight = FontWeight.Bold, color = QuettaGreenDark)
                    }
                    Text("Passenger: ${b.passengerName} → Captain: ${b.driverName ?: "Assigning"}")
                    Text("Route: ${b.pickupLocation.name} → ${b.destinationLocation.name} (${b.distanceKm} km)")
                    Text("Status: ${b.status.label} • ${b.createdAt}", style = MaterialTheme.typography.labelSmall, color = QuettaNavyLight)
                }
            }
        }
    }
}

@Composable
private fun AdminLocationsSection(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel
) {
    var newLocName by remember { mutableStateOf("") }
    var newLocArea by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Quetta Service Hubs & Pakistan Expansion", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add New Service Landmark", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newLocName,
                        onValueChange = { newLocName = it },
                        label = { Text("Landmark Name (e.g., Serena Hotel Chowk)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newLocArea,
                        onValueChange = { newLocArea = it },
                        label = { Text("Zone / Corridor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newLocName.isNotBlank()) {
                                viewModel.addQuettaLocation(newLocName, newLocArea, "Quetta")
                                newLocName = ""
                                newLocArea = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
                    ) {
                        Text("Add Quetta Location", color = QuettaYellow)
                    }
                }
            }
        }
        item {
            Text("Multi-City Expansion Roadmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(uiState.serviceCities) { city ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${city.name} (${city.province})", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text(city.launchPhase, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = city.isLive,
                        onCheckedChange = { viewModel.toggleCityLiveStatus(city.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminFareConfigSection(
    configs: List<VehicleCategoryConfig>,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Live Fare Engine Configurator", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text(
                "Adjust base fare & per-km rates below. Changes immediately update Passenger App fare estimates and persist locally.",
                style = MaterialTheme.typography.bodySmall,
                color = QuettaTextSecondary
            )
        }
        items(configs) { cfg ->
            var baseFare by remember(cfg) { mutableFloatStateOf(cfg.baseFarePkr.toFloat()) }
            var perKm by remember(cfg) { mutableFloatStateOf(cfg.perKmRatePkr.toFloat()) }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(cfg.id.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                    Text("Base Fare: PKR ${baseFare.roundToInt()}", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = baseFare,
                        onValueChange = { baseFare = it },
                        valueRange = 30f..300f
                    )
                    Text("Per-Kilometer Rate: PKR ${perKm.roundToInt()}/km", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = perKm,
                        onValueChange = { perKm = it },
                        valueRange = 15f..120f
                    )
                    Button(
                        onClick = {
                            viewModel.updateVehicleFareConfig(
                                categoryId = cfg.id,
                                baseFarePkr = baseFare.roundToInt(),
                                perKmRatePkr = perKm.roundToInt(),
                                perMinRatePkr = cfg.perMinuteRatePkr,
                                minFarePkr = cfg.minimumFarePkr,
                                isActive = cfg.isActive
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark),
                        modifier = Modifier.testTag("save_fare_${cfg.id.name.lowercase()}")
                    ) {
                        Text("Save ${cfg.id.displayName} Rates", color = QuettaYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPaymentsSection(bookings: List<RideBooking>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Payments & Captain Settlements", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Supports Cash, JazzCash, EasyPaisa, and RAAST QR settlements.", style = MaterialTheme.typography.bodySmall)
        }
        items(bookings) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Booking ${b.id} • ${b.passengerName}", fontWeight = FontWeight.Bold)
                        Text(b.paymentMethod, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("PKR ${b.farePkr}", fontWeight = FontWeight.Bold, color = QuettaGreenDark)
                }
            }
        }
    }
}

@Composable
private fun AdminComplaintsSection(
    complaints: List<ComplaintItem>,
    viewModel: QuettaRideViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Complaints & Dispute Resolution", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(complaints) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.id} • ${item.subject}", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text(item.status, fontWeight = FontWeight.Bold, color = if (item.status == "Resolved") QuettaGreenDark else QuettaAmber)
                    }
                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                    Text("By ${item.submittedBy} (${item.role}) • Booking ${item.bookingId}", style = MaterialTheme.typography.labelSmall)
                    if (item.status != "Resolved") {
                        Button(
                            onClick = { viewModel.resolveComplaint(item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaGreenDark)
                        ) {
                            Text("Mark Resolved")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReviewsSection(bookings: List<RideBooking>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Passenger Ratings & Quality Audit", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(bookings.filter { it.passengerReview != null }) { b ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${b.passengerName} rated ${b.driverName} ★ ${b.passengerRating ?: 5}/5", fontWeight = FontWeight.Bold)
                    Text("\"${b.passengerReview}\"")
                }
            }
        }
    }
}

@Composable
private fun AdminReportsSection(uiState: QuettaPlatformUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Operational & Investor Reports", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quetta Phase-1 Unit Economics Summary", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                Text("• Average Bike Ticket Size: PKR 175 (12% platform commission)")
                Text("• Average Rickshaw Ticket Size: PKR 290 (14% platform commission)")
                Text("• Average Suzuki Alto Ticket Size: PKR 495 (15% platform commission)")
                Text("• Peak Demand Corridors: Jinnah Road, Sariab Road (UOB), Brewery Road (BMC/BUITEMS), Satellite Town")
            }
        }
    }
}

@Composable
private fun AdminNotificationsSection(notifications: List<AppNotification>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("System & Dispatch Notifications", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        }
        items(notifications) { n ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("[${n.targetRole.badge}] ${n.title}", fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                    Text(n.message, style = MaterialTheme.typography.bodySmall)
                    Text(n.timestamp, style = MaterialTheme.typography.labelSmall, color = QuettaTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsSection(viewModel: QuettaRideViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Platform Settings & Demo Reset", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Reset Prototype State", fontWeight = FontWeight.Bold)
                Text("Restore all demonstration bookings, driver approvals, and fare configurations back to default Quetta launch settings.")
                Button(
                    onClick = { viewModel.resetDemoData() },
                    colors = ButtonDefaults.buttonColors(containerColor = QuettaRed),
                    modifier = Modifier.testTag("admin_reset_demo_btn")
                ) {
                    Text("Reset All Demo Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
