package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.authStateFlow
import com.example.auth.signInWithGoogleInteractive
import com.example.auth.signOutQuettaUser
import com.example.model.PlatformRole
import com.example.ui.components.QuettaLogoMark
import com.example.ui.screens.AdminExperience
import com.example.ui.screens.DriverExperience
import com.example.ui.screens.PassengerExperience
import com.example.ui.screens.WebsiteExperience
import com.example.ui.theme.*
import com.example.viewmodel.QuettaRideViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (_: Throwable) {
        }
        enableEdgeToEdge()
        setContent {
            QuettaRideTheme(darkTheme = false) {
                QuettaRideAuthenticatedRoot()
            }
        }
    }
}

@Composable
fun QuettaRideAuthenticatedRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember {
        runCatching { CredentialManager.create(context) }.getOrNull()
    }
    val auth = remember {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
    val authFlow = remember(auth) {
        auth?.authStateFlow() ?: flowOf<FirebaseUser?>(null)
    }
    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = auth?.currentUser)

    val viewModel = remember(currentUser?.uid) {
        QuettaRideViewModel(
            context = context.applicationContext,
            currentUserId = currentUser?.uid,
            currentUserName = currentUser?.displayName,
            currentUserEmail = currentUser?.email
        )
    }

    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    QuettaRidePlatformApp(
        viewModel = viewModel,
        isSignedInWithGoogle = currentUser != null,
        onOpenGoogleSignIn = { showGoogleSignInDialog = true },
        onSignOut = {
            if (credentialManager != null) {
                signOutQuettaUser(
                    credentialManager = credentialManager,
                    scope = scope
                )
            } else {
                runCatching { auth?.signOut() }
            }
        }
    )

    if (showGoogleSignInDialog && credentialManager != null) {
        QuettaGoogleAuthDialog(
            credentialManager = credentialManager,
            onDismiss = { showGoogleSignInDialog = false }
        )
    }
}

@Composable
fun QuettaGoogleAuthDialog(
    credentialManager: CredentialManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuettaLogoMark(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Quetta Ride Cloud Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = QuettaNavyDark
                    )
                    Text(
                        text = "Google Identity & Cloud Firestore",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuettaTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign in with your Google Account to synchronize live Quetta bookings, Captain dispatch, and ratings across devices via Cloud Firestore.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuettaTextSecondary
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuettaRed,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(color = QuettaNavyDark)
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            signInWithGoogleInteractive(
                                context = context,
                                credentialManager = credentialManager,
                                scope = scope,
                                onAuthSuccess = {
                                    isLoading = false
                                    onDismiss()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QuettaNavyDark,
                            contentColor = QuettaYellow
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dialog_google_sign_in_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = QuettaYellow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue in App")
            }
        }
    )
}

@Composable
fun QuettaRidePlatformApp(
    viewModel: QuettaRideViewModel,
    isSignedInWithGoogle: Boolean = false,
    onOpenGoogleSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showCitySelectorDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(granted)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuettaNavyDark)
                    .statusBarsPadding()
            ) {
                // Brand Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        QuettaLogoMark(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "QUETTA RIDE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = QuettaYellow.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clickable { showCitySelectorDialog = true }
                                        .testTag("city_selector_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.selectedCity,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = QuettaYellow,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select City",
                                            tint = QuettaYellow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Book Smart. Ride Safe. Reach Anywhere.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isSignedInWithGoogle) {
                            Surface(
                                color = QuettaYellow,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable { onOpenGoogleSignIn() }
                                    .testTag("google_sign_in_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Sign in with Google",
                                        tint = QuettaNavyDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sign In",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = QuettaNavyDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("top_signout_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = QuettaGreen
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (uiState.hasLocationPermission) {
                                    viewModel.refreshLiveGpsLocation(silent = false)
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.testTag("top_gps_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.hasLocationPermission) Icons.Default.GpsFixed else Icons.Default.LocationSearching,
                                contentDescription = "Live GPS Location",
                                tint = if (uiState.hasLocationPermission) QuettaGreen else QuettaYellow
                            )
                        }

                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier.testTag("top_notifications_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = QuettaYellow, contentColor = QuettaNavyDark) {
                                        Text("${uiState.notifications.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.resetDemoData() },
                            modifier = Modifier.testTag("top_reset_demo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset State",
                                tint = QuettaYellow
                            )
                        }
                    }
                }

                // 4-Experience Role Switcher Bar (Passenger | Driver | Admin | Website)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlatformRole.entries.forEach { role ->
                        val isSelected = uiState.activeRole == role
                        Surface(
                            color = if (isSelected) QuettaYellow else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.switchRole(role) }
                                .testTag("role_tab_${role.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = role.badge.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) QuettaNavyDark.copy(alpha = 0.75f) else QuettaYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = role.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) QuettaNavyDark else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Status Feedback Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                uiState.statusBannerMessage?.let { msg ->
                    Surface(
                        color = Color(0xFFECFDF5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = QuettaGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = QuettaNavyDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = QuettaNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Active Platform Experience Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState.activeRole) {
                    PlatformRole.PASSENGER -> PassengerExperience(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    PlatformRole.DRIVER -> DriverExperience(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    PlatformRole.ADMIN -> AdminExperience(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    PlatformRole.WEBSITE -> WebsiteExperience(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Text("Quetta Ride Notifications", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications) { notif ->
                        Surface(
                            color = QuettaSurfaceVariantLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "[${notif.targetRole.badge}] ${notif.title}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = QuettaNavyDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QuettaTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCitySelectorDialog) {
        AlertDialog(
            onDismissRequest = { showCitySelectorDialog = false },
            title = {
                Text("Service City & Pakistan Expansion", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.serviceCities) { city ->
                        Surface(
                            color = if (uiState.selectedCity == city.name) Color(0xFFFEFCE8) else QuettaSurfaceVariantLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCity(city.name)
                                    showCitySelectorDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${city.name}, ${city.province}",
                                        fontWeight = FontWeight.Bold,
                                        color = QuettaNavyDark
                                    )
                                    Text(
                                        text = city.launchPhase,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = QuettaTextSecondary
                                    )
                                }
                                if (city.isLive) {
                                    Badge(containerColor = QuettaGreen) { Text("LIVE") }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCitySelectorDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
