package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.QuettaPlatformUiState
import com.example.viewmodel.QuettaRideViewModel

@Composable
fun WebsiteExperience(
    uiState: QuettaPlatformUiState,
    viewModel: QuettaRideViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.websitePage != WebsitePage.HOME) {
        BackHandler {
            viewModel.navigateWebsite(WebsitePage.HOME)
        }
    }

    var legalModalTitle by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Website Navigation Bar (All 9 Pages)
        Surface(color = QuettaNavyDark, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WebsitePage.entries.forEach { page ->
                    val isSelected = page == uiState.websitePage
                    Surface(
                        color = if (isSelected) QuettaYellow else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clickable { viewModel.navigateWebsite(page) }
                            .testTag("website_nav_${page.name.lowercase()}")
                    ) {
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) QuettaNavyDark else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when (uiState.websitePage) {
                WebsitePage.HOME -> {
                    item {
                        WebsiteHeroSection(
                            onBookRide = {
                                viewModel.switchRole(PlatformRole.PASSENGER)
                                viewModel.navigatePassenger(PassengerScreen.HOME)
                            },
                            onBecomeDriver = {
                                viewModel.switchRole(PlatformRole.DRIVER)
                                viewModel.navigateDriver(DriverScreen.REGISTER_PERSONAL)
                            }
                        )
                    }
                    item {
                        WebsiteFleetShowcaseSection(
                            configs = uiState.vehicleConfigs,
                            onBookCategory = { cat ->
                                viewModel.selectVehicleCategory(cat)
                                viewModel.switchRole(PlatformRole.PASSENGER)
                                viewModel.navigatePassenger(PassengerScreen.VEHICLE_SELECT)
                            }
                        )
                    }
                    item { WebsiteHowItWorksSection() }
                    item { WebsitePopularQuettaLocationsSection(uiState.locations, viewModel) }
                    item { WebsiteTestimonialsAndFaqSection() }
                }

                WebsitePage.ABOUT -> {
                    item { WebsiteAboutUsPage() }
                }

                WebsitePage.HOW_IT_WORKS -> {
                    item { WebsiteHowItWorksSection() }
                }

                WebsitePage.SERVICES -> {
                    item {
                        WebsiteFleetShowcaseSection(
                            configs = uiState.vehicleConfigs,
                            onBookCategory = { cat ->
                                viewModel.selectVehicleCategory(cat)
                                viewModel.switchRole(PlatformRole.PASSENGER)
                                viewModel.navigatePassenger(PassengerScreen.VEHICLE_SELECT)
                            }
                        )
                    }
                }

                WebsitePage.BECOME_DRIVER -> {
                    item {
                        WebsiteBecomeDriverPage(
                            onOpenDriverRegistration = {
                                viewModel.switchRole(PlatformRole.DRIVER)
                                viewModel.navigateDriver(DriverScreen.REGISTER_PERSONAL)
                            }
                        )
                    }
                }

                WebsitePage.SAFETY -> {
                    item { WebsiteSafetyPage() }
                }

                WebsitePage.DOWNLOAD -> {
                    item {
                        WebsiteDownloadPage(
                            onOpenPassenger = { viewModel.switchRole(PlatformRole.PASSENGER) },
                            onOpenDriver = { viewModel.switchRole(PlatformRole.DRIVER) }
                        )
                    }
                }

                WebsitePage.CONTACT -> {
                    item {
                        WebsiteContactUsPage(
                            onSubmitMessage = { subj, msg ->
                                viewModel.submitSupportComplaint(subj, msg, "Website Visitor")
                            }
                        )
                    }
                }

                WebsitePage.LOGIN_REGISTER -> {
                    item {
                        WebsiteDemoLoginInstructionsPage(
                            onLaunchPassenger = { viewModel.switchRole(PlatformRole.PASSENGER) },
                            onLaunchDriver = { viewModel.switchRole(PlatformRole.DRIVER) },
                            onLaunchAdmin = { viewModel.switchRole(PlatformRole.ADMIN) }
                        )
                    }
                }
            }

            // Professional Website Footer
            item {
                WebsiteFooter(
                    onNavigatePage = { viewModel.navigateWebsite(it) },
                    onOpenLegal = { legalModalTitle = it }
                )
            }
        }
    }

    if (legalModalTitle != null) {
        AlertDialog(
            onDismissRequest = { legalModalTitle = null },
            title = { Text(legalModalTitle ?: "Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Quetta Ride Technologies (Pvt.) Ltd. operates in accordance with Balochistan Provincial Transport Authority regulations. All passenger & captain data is protected, and all fares shown in this Phase-1 Prototype are illustrative demonstration estimates."
                )
            },
            confirmButton = {
                TextButton(onClick = { legalModalTitle = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun WebsiteHeroSection(
    onBookRide: () -> Unit,
    onBecomeDriver: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = QuettaNavyDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuettaHeroBannerIllustration()
            Text(
                text = "Your City. Your Ride. Your Way.",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Book bikes, rickshaws and comfortable cars across Quetta with just a few taps.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBookRide,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuettaYellow,
                        contentColor = QuettaNavyDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("website_hero_book_btn")
                ) {
                    Text("Book a Ride", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBecomeDriver,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QuettaYellow),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, QuettaYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("website_hero_driver_btn")
                ) {
                    Text("Become a Driver", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WebsiteFleetShowcaseSection(
    configs: List<VehicleCategoryConfig>,
    onBookCategory: (VehicleCategoryId) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Three Smart Vehicle Categories Built for Quetta",
            style = MaterialTheme.typography.headlineMedium,
            color = QuettaNavyDark
        )
        configs.forEach { cfg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleIllustrationCanvas(
                        category = cfg.id,
                        modifier = Modifier.size(100.dp, 74.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cfg.id.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                        Text(cfg.id.idealFor, style = MaterialTheme.typography.labelMedium, color = QuettaGreenDark)
                        Text(cfg.description, style = MaterialTheme.typography.bodySmall, color = QuettaTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onBookCategory(cfg.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Book ${cfg.id.displayName.substringBefore(" /")}", color = QuettaYellow)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteHowItWorksSection() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("How Quetta Ride Works", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("1. Choose Pickup & Destination — Select Jinnah Road, Sariab Road, Satellite Town, Airport, or any Quetta landmark.")
            Text("2. Pick Your Vehicle — Choose Bike (1 Pax), Rickshaw (2–3 Pax), or Suzuki Alto (4 Pax Family).")
            Text("3. Track Your Verified Captain — View upfront PKR fare estimate, captain CNIC verification, and live route tracking.")
            Text("4. Pay Easily & Rate — Pay via Cash, JazzCash, or EasyPaisa and rate your captain.")
        }
    }
}

@Composable
private fun WebsitePopularQuettaLocationsSection(
    locations: List<CityLocation>,
    viewModel: QuettaRideViewModel
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Popular Service Corridors in Quetta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
            locations.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { loc ->
                        OutlinedButton(
                            onClick = {
                                viewModel.selectDestination(loc)
                                viewModel.switchRole(PlatformRole.PASSENGER)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(loc.name, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteTestimonialsAndFaqSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What Quetta Commuters & Captains Say", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                Text("★ ★ ★ ★ ★ \"Booking an Alto from Satellite Town to Bolan Medical Complex for my parents has never been easier. Upfront fare with no haggling!\" — Dr. Ayesha Durrani")
                HorizontalDivider()
                Text("★ ★ ★ ★ ★ \"My daily Rickshaw earnings increased by 35% because I spend less time waiting empty at Alamdar Chowk.\" — Captain Bilal Ahmed Mengal")
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Frequently Asked Questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QuettaNavyDark)
                Text("Q: Are the fares fixed before booking?\nA: Yes! You see the complete base + per-km fare breakdown before confirming your Bike, Rickshaw, or Alto.")
                Text("Q: Which cities are next after Quetta?\nA: Our modular platform is architected for rapid expansion to Gwadar, Karachi, Lahore, Islamabad, Peshawar, Multan, and Faisalabad.")
            }
        }
    }
}

@Composable
private fun WebsiteAboutUsPage() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("About Quetta Ride", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Quetta Ride is Balochistan's homegrown smart mobility startup dedicated to connecting commuters, students, families, and local captains through dignified, safe, and affordable technology.")
            Text("Our Mission: Empower 10,000+ local motorcycle, rickshaw, and small car owners in Quetta with steady digital livelihoods while giving citizens reliable point-to-point transport.")
        }
    }
}

@Composable
private fun WebsiteBecomeDriverPage(onOpenDriverRegistration: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Earn With Pride as a Quetta Ride Captain", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("• Flexible online/offline hours — drive full-time or part-time\n• Low commission rates tailored for Quetta captains\n• Instant daily cash & mobile wallet payouts")
            Button(
                onClick = onOpenDriverRegistration,
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Open Captain Registration Form", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WebsiteSafetyPage() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Safety & Trust Standards", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("1. NADRA CNIC & Excise Document Verification for every Captain\n2. Live Route Sharing with family emergency contacts\n3. 24/7 Quetta Local Control Room & SOS Assistance\n4. Mandatory helmets for Bike rides & inspected 4-stroke Rickshaws/Altos")
        }
    }
}

@Composable
private fun WebsiteDownloadPage(
    onOpenPassenger: () -> Unit,
    onOpenDriver: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Experience Quetta Ride on Mobile", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Launch the interactive Passenger or Captain mobile experiences directly inside this prototype:")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenPassenger, colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)) {
                    Text("Open Passenger App", color = QuettaYellow)
                }
                OutlinedButton(onClick = onOpenDriver) {
                    Text("Open Captain App")
                }
            }
        }
    }
}

@Composable
private fun WebsiteContactUsPage(onSubmitMessage: (String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Contact Quetta Ride HQ", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("Office: Zarghoon Road Software & Innovation Park, Quetta, Balochistan\nPhone: +92 (081) 284-9900 • Email: salam@quettaride.pk")
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Your Message") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onSubmitMessage(subject, message)
                        subject = ""
                        message = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = QuettaNavyDark)
            ) {
                Text("Send Message", color = QuettaYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WebsiteDemoLoginInstructionsPage(
    onLaunchPassenger: () -> Unit,
    onLaunchDriver: () -> Unit,
    onLaunchAdmin: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Demo Evaluator & Investor Guide", style = MaterialTheme.typography.headlineMedium, color = QuettaNavyDark)
            Text("This prototype provides instant one-tap access to all three operational roles plus the public startup portal:")
            Text("• Passenger Demo Account: Masood Khan Achakzai (+92 311 8004421) • Demo OTP: 4829")
            Text("• Captain Demo Account: Mirwais Khan Achakzai (Suzuki Alto • QTA-24-8812)")
            Text("• Administrator HQ: Full access to 14 management modules, driver approvals & live fare engine")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onLaunchPassenger, modifier = Modifier.weight(1f)) { Text("Passenger") }
                Button(onClick = onLaunchDriver, modifier = Modifier.weight(1f)) { Text("Driver") }
                Button(onClick = onLaunchAdmin, modifier = Modifier.weight(1f)) { Text("Admin") }
            }
        }
    }
}

@Composable
private fun WebsiteFooter(
    onNavigatePage: (WebsitePage) -> Unit,
    onOpenLegal: (String) -> Unit
) {
    Surface(
        color = QuettaNavyDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuettaLogoMark(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("QUETTA RIDE", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Book Smart. Ride Safe. Reach Anywhere.", style = MaterialTheme.typography.labelSmall, color = QuettaYellow)
                }
            }
            Text(
                "HQ: Zarghoon Road, Quetta, Balochistan, Pakistan • Helpline: 081-111-783-882",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Privacy Policy",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuettaYellow,
                    modifier = Modifier.clickable { onOpenLegal("Privacy Policy") }
                )
                Text(
                    "Terms & Conditions",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuettaYellow,
                    modifier = Modifier.clickable { onOpenLegal("Terms & Conditions") }
                )
                Text(
                    "Demo Login Guide",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuettaYellow,
                    modifier = Modifier.clickable { onNavigatePage(WebsitePage.LOGIN_REGISTER) }
                )
            }
        }
    }
}
