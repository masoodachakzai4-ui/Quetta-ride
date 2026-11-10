package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.hypot

@Composable
fun QuettaLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(QuettaNavyDark, QuettaNavyLight)
                )
            )
            .border(1.5.dp, QuettaYellow.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val w = this.size.width
            val h = this.size.height

            // Subtle perspective road swoop at bottom
            val roadPath = Path().apply {
                moveTo(w * 0.15f, h * 0.92f)
                quadraticTo(w * 0.5f, h * 0.65f, w * 0.85f, h * 0.92f)
                lineTo(w * 0.68f, h * 0.96f)
                quadraticTo(w * 0.5f, h * 0.76f, w * 0.32f, h * 0.96f)
                close()
            }
            drawPath(roadPath, color = Color.White.copy(alpha = 0.35f))

            // Bold Location Pin silhouette
            val pinPath = Path().apply {
                moveTo(w * 0.5f, h * 0.82f)
                cubicTo(w * 0.20f, h * 0.54f, w * 0.18f, h * 0.12f, w * 0.5f, h * 0.10f)
                cubicTo(w * 0.82f, h * 0.12f, w * 0.80f, h * 0.54f, w * 0.5f, h * 0.82f)
                close()
            }
            drawPath(pinPath, color = QuettaYellow)

            // Inner navy circle with stylized vehicle front
            drawCircle(
                color = QuettaNavyDark,
                radius = w * 0.19f,
                center = Offset(w * 0.5f, h * 0.38f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.38f, h * 0.33f),
                size = Size(w * 0.24f, h * 0.11f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
fun QuettaHeroBannerIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071326),
                        Color(0xFF132A4A),
                        Color(0xFF1E3A5F)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Golden sunset glow behind Chiltan & Koh-e-Murdar peaks
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        QuettaYellow.copy(alpha = 0.55f),
                        QuettaAmber.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.76f, h * 0.32f),
                    radius = w * 0.42f
                ),
                center = Offset(w * 0.76f, h * 0.32f),
                radius = w * 0.42f
            )

            // Sun disc
            drawCircle(
                color = QuettaYellow.copy(alpha = 0.88f),
                radius = h * 0.13f,
                center = Offset(w * 0.76f, h * 0.30f)
            )

            // Distant Takatu & Zarghoon mountain range
            val backMountains = Path().apply {
                moveTo(0f, h * 0.72f)
                lineTo(w * 0.14f, h * 0.38f)
                lineTo(w * 0.29f, h * 0.55f)
                lineTo(w * 0.48f, h * 0.26f)
                lineTo(w * 0.66f, h * 0.50f)
                lineTo(w * 0.85f, h * 0.31f)
                lineTo(w, h * 0.48f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = backMountains,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF243B61), Color(0xFF0F213C))
                )
            )

            // Foreground Chiltan rugged ridge
            val frontMountains = Path().apply {
                moveTo(0f, h * 0.78f)
                lineTo(w * 0.22f, h * 0.48f)
                lineTo(w * 0.38f, h * 0.64f)
                lineTo(w * 0.58f, h * 0.42f)
                lineTo(w * 0.77f, h * 0.62f)
                lineTo(w * 0.92f, h * 0.49f)
                lineTo(w, h * 0.64f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = frontMountains,
                color = Color(0xFF0D1B33)
            )

            // Quetta highway / Jinnah Road deck
            drawRect(
                color = Color(0xFF091224),
                topLeft = Offset(0f, h * 0.74f),
                size = Size(w, h * 0.26f)
            )

            // Golden road divider dashes
            val dashY = h * 0.86f
            for (i in 0..8) {
                drawRoundRect(
                    color = QuettaYellow.copy(alpha = 0.75f),
                    topLeft = Offset(w * (i * 0.12f + 0.02f), dashY),
                    size = Size(w * 0.06f, 4.dp.toPx()),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Overlay text & fleet badges
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = QuettaYellow,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Quetta",
                            tint = QuettaNavyDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "QUETTA • BALOCHISTAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Bike • Rickshaw • Alto",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Your City. Your Ride. Your Way.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Book Smart. Ride Safe. Reach Anywhere across Jinnah Road, Satellite Town, Sariab & Airport Road.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VehicleIllustrationCanvas(
    category: VehicleCategoryId,
    modifier: Modifier = Modifier
) {
    val bgBrush = when (category) {
        VehicleCategoryId.BIKE -> Brush.linearGradient(
            listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
        )
        VehicleCategoryId.RICKSHAW -> Brush.linearGradient(
            listOf(Color(0xFFECFDF5), Color(0xFFFEF9C3))
        )
        VehicleCategoryId.ALTO -> Brush.linearGradient(
            listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(1.dp, QuettaBorderLight, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val w = size.width
            val h = size.height

            // Ground shadow
            drawOval(
                color = QuettaNavyDark.copy(alpha = 0.12f),
                topLeft = Offset(w * 0.12f, h * 0.78f),
                size = Size(w * 0.76f, h * 0.12f)
            )

            when (category) {
                VehicleCategoryId.BIKE -> {
                    val rearWheel = Offset(w * 0.26f, h * 0.68f)
                    val frontWheel = Offset(w * 0.74f, h * 0.68f)
                    val wheelRadius = h * 0.17f

                    // Wheels
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = rearWheel, style = Stroke(width = 6f))
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = frontWheel, style = Stroke(width = 6f))
                    drawCircle(QuettaYellowDark, radius = wheelRadius * 0.35f, center = rearWheel)
                    drawCircle(QuettaYellowDark, radius = wheelRadius * 0.35f, center = frontWheel)

                    // Frame & CG-125 Tank
                    drawLine(QuettaNavy, start = rearWheel, end = Offset(w * 0.48f, h * 0.44f), strokeWidth = 6f)
                    drawLine(QuettaNavy, start = Offset(w * 0.48f, h * 0.44f), end = Offset(w * 0.64f, h * 0.34f), strokeWidth = 6f)
                    drawLine(QuettaNavy, start = Offset(w * 0.62f, h * 0.28f), end = frontWheel, strokeWidth = 6f)

                    // Fuel tank (Bright Yellow & Navy stripe)
                    drawRoundRect(
                        color = QuettaNavyDark,
                        topLeft = Offset(w * 0.44f, h * 0.34f),
                        size = Size(w * 0.22f, h * 0.14f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = QuettaYellow,
                        topLeft = Offset(w * 0.46f, h * 0.38f),
                        size = Size(w * 0.18f, h * 0.05f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Seat
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(w * 0.24f, h * 0.38f),
                        size = Size(w * 0.22f, h * 0.09f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Headlight
                    drawCircle(
                        color = QuettaYellow,
                        radius = h * 0.06f,
                        center = Offset(w * 0.70f, h * 0.36f)
                    )
                }

                VehicleCategoryId.RICKSHAW -> {
                    val rearWheel = Offset(w * 0.28f, h * 0.72f)
                    val frontWheel = Offset(w * 0.76f, h * 0.72f)
                    val wheelRadius = h * 0.14f

                    // Canopy (Bright Yellow top + Emerald & Navy body)
                    val bodyPath = Path().apply {
                        moveTo(w * 0.16f, h * 0.68f)
                        lineTo(w * 0.18f, h * 0.26f)
                        lineTo(w * 0.64f, h * 0.26f)
                        lineTo(w * 0.76f, h * 0.52f)
                        lineTo(w * 0.76f, h * 0.68f)
                        close()
                    }
                    drawPath(bodyPath, color = QuettaGreenDark)

                    // Yellow Rickshaw Roof Hood
                    drawRoundRect(
                        color = QuettaYellow,
                        topLeft = Offset(w * 0.16f, h * 0.20f),
                        size = Size(w * 0.52f, h * 0.11f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Navy lower skirt with Pakistani truck-art geometric dots
                    drawRoundRect(
                        color = QuettaNavyDark,
                        topLeft = Offset(w * 0.16f, h * 0.52f),
                        size = Size(w * 0.60f, h * 0.16f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    for (i in 0..3) {
                        drawCircle(
                            color = QuettaYellow,
                            radius = 3.5f,
                            center = Offset(w * (0.24f + i * 0.11f), h * 0.60f)
                        )
                    }

                    // Windshield & Passenger Opening
                    drawRoundRect(
                        color = Color(0xFFBAE6FD),
                        topLeft = Offset(w * 0.54f, h * 0.32f),
                        size = Size(w * 0.15f, h * 0.16f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(w * 0.24f, h * 0.32f),
                        size = Size(w * 0.24f, h * 0.18f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Wheels
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = rearWheel)
                    drawCircle(Color.White, radius = wheelRadius * 0.45f, center = rearWheel)
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = frontWheel)
                    drawCircle(Color.White, radius = wheelRadius * 0.45f, center = frontWheel)
                }

                VehicleCategoryId.ALTO -> {
                    val rearWheel = Offset(w * 0.27f, h * 0.70f)
                    val frontWheel = Offset(w * 0.73f, h * 0.70f)
                    val wheelRadius = h * 0.15f

                    // Car Lower Body (Crisp Pearl White with Navy outline)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.12f, h * 0.44f),
                        size = Size(w * 0.76f, h * 0.24f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = QuettaNavyDark,
                        topLeft = Offset(w * 0.12f, h * 0.44f),
                        size = Size(w * 0.76f, h * 0.24f),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 3.5f)
                    )

                    // Cabin Roof
                    val cabinPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.45f)
                        lineTo(w * 0.32f, h * 0.24f)
                        lineTo(w * 0.64f, h * 0.24f)
                        lineTo(w * 0.76f, h * 0.45f)
                        close()
                    }
                    drawPath(cabinPath, color = QuettaNavy)

                    // Windows
                    drawRoundRect(
                        color = Color(0xFFBAE6FD),
                        topLeft = Offset(w * 0.33f, h * 0.28f),
                        size = Size(w * 0.30f, h * 0.14f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Quetta Ride Yellow Door Stripe
                    drawRoundRect(
                        color = QuettaYellow,
                        topLeft = Offset(w * 0.36f, h * 0.53f),
                        size = Size(w * 0.28f, h * 0.06f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Wheels
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = rearWheel)
                    drawCircle(Color(0xFFCBD5E1), radius = wheelRadius * 0.5f, center = rearWheel)
                    drawCircle(QuettaNavyDark, radius = wheelRadius, center = frontWheel)
                    drawCircle(Color(0xFFCBD5E1), radius = wheelRadius * 0.5f, center = frontWheel)
                }
            }
        }
    }
}

@Composable
fun QuettaInteractiveMap(
    locations: List<CityLocation>,
    pickup: CityLocation,
    destination: CityLocation,
    nearbyDrivers: List<DriverProfile>,
    activeRide: RideBooking? = null,
    selectedCityName: String = "Quetta",
    onLocationTapped: ((CityLocation) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val distanceKm = activeRide?.distanceKm ?: run {
        val dx = (pickup.mapX - destination.mapX) * 18.5f
        val dy = (pickup.mapY - destination.mapY) * 18.5f
        kotlin.math.max(1.5, (hypot(dx.toDouble(), dy.toDouble()) * 10.0).toInt() / 10.0)
    }
    val estMin = activeRide?.estimatedMinutes ?: (distanceKm * 2.3).toInt().coerceAtLeast(5)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFEAF1F8))
            .border(1.5.dp, QuettaBorderLight, RoundedCornerShape(22.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(locations) {
                    if (onLocationTapped != null) {
                        detectTapGestures { tapOffset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val closest = locations.minByOrNull { loc ->
                                hypot(loc.mapX * w - tapOffset.x, loc.mapY * h - tapOffset.y)
                            }
                            if (closest != null) {
                                val distPx = hypot(closest.mapX * w - tapOffset.x, closest.mapY * h - tapOffset.y)
                                if (distPx < 90f) {
                                    onLocationTapped(closest)
                                }
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // Topographic mountain shading on East (Koh-e-Murdar) & West (Chiltan)
            val westMountain = Path().apply {
                moveTo(0f, h * 0.25f)
                quadraticTo(w * 0.16f, h * 0.50f, 0f, h * 0.88f)
                close()
            }
            drawPath(westMountain, color = Color(0xFFD8E4EF))

            val eastMountain = Path().apply {
                moveTo(w, h * 0.10f)
                quadraticTo(w * 0.78f, h * 0.45f, w, h * 0.85f)
                close()
            }
            drawPath(eastMountain, color = Color(0xFFD8E4EF))

            // Subtle urban blocks grid
            val gridStepX = w / 8f
            val gridStepY = h / 7f
            for (i in 1..7) {
                drawLine(
                    color = Color(0xFFDCE6F2),
                    start = Offset(i * gridStepX, 0f),
                    end = Offset(i * gridStepX, h),
                    strokeWidth = 1.5f
                )
            }
            for (j in 1..6) {
                drawLine(
                    color = Color(0xFFDCE6F2),
                    start = Offset(0f, j * gridStepY),
                    end = Offset(w, j * gridStepY),
                    strokeWidth = 1.5f
                )
            }

            // Major Quetta Arterial Roads (Sariab-Jinnah-Kuchlak N-25 Spine & Samungli-Zarghoon Axis)
            val mainSpine = Path().apply {
                moveTo(w * 0.48f, h * 0.05f) // Kuchlak
                quadraticTo(w * 0.50f, h * 0.48f, w * 0.45f, h * 0.94f) // Jinnah Rd -> Sariab Rd
            }
            drawPath(mainSpine, color = Color.White, style = Stroke(width = 14f, cap = StrokeCap.Round))
            drawPath(mainSpine, color = Color(0xFFCBD5E1), style = Stroke(width = 2f))

            val eastWestAxis = Path().apply {
                moveTo(w * 0.08f, h * 0.15f) // Airport / Samungli
                quadraticTo(w * 0.48f, h * 0.48f, w * 0.90f, h * 0.25f) // Zarghoon -> Hanna
            }
            drawPath(eastWestAxis, color = Color.White, style = Stroke(width = 11f, cap = StrokeCap.Round))

            val brewerySatelliteAxis = Path().apply {
                moveTo(w * 0.14f, h * 0.62f) // Brewery Rd
                quadraticTo(w * 0.50f, h * 0.54f, w * 0.75f, h * 0.74f) // Railway -> Satellite Town
            }
            drawPath(brewerySatelliteAxis, color = Color.White, style = Stroke(width = 11f, cap = StrokeCap.Round))

            // Active Route Line between Pickup and Destination
            val startPt = Offset(pickup.mapX * w, pickup.mapY * h)
            val endPt = Offset(destination.mapX * w, destination.mapY * h)
            val ctrlPt = Offset(
                (startPt.x + endPt.x) / 2f,
                kotlin.math.min(startPt.y, endPt.y) - h * 0.08f
            )

            val routePath = Path().apply {
                moveTo(startPt.x, startPt.y)
                quadraticTo(ctrlPt.x, ctrlPt.y, endPt.x, endPt.y)
            }

            // Route shadow glow
            drawPath(
                path = routePath,
                color = QuettaNavyDark.copy(alpha = 0.20f),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            // Route main navy stroke
            drawPath(
                path = routePath,
                color = QuettaNavyDark,
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
            // Route yellow dashed highlight
            drawPath(
                path = routePath,
                color = QuettaYellow,
                style = Stroke(
                    width = 3.5f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            )

            // Draw all Quetta Landmark nodes
            locations.forEach { loc ->
                val pt = Offset(loc.mapX * w, loc.mapY * h)
                val isPickup = loc.id == pickup.id
                val isDest = loc.id == destination.id
                if (!isPickup && !isDest) {
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = pt
                    )
                    drawCircle(
                        color = QuettaNavy.copy(alpha = 0.65f),
                        radius = 5f,
                        center = pt
                    )
                }
            }

            // Nearby Online Drivers markers
            nearbyDrivers.filter { it.isOnline }.forEach { drv ->
                val dPt = Offset(drv.mapX * w, drv.mapY * h)
                val badgeColor = when (drv.vehicleCategory) {
                    VehicleCategoryId.BIKE -> QuettaBlueAccent
                    VehicleCategoryId.RICKSHAW -> QuettaGreen
                    VehicleCategoryId.ALTO -> QuettaNavyDark
                }
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = dPt
                )
                drawCircle(
                    color = badgeColor,
                    radius = 9f,
                    center = dPt
                )
            }

            // Pickup Marker (Emerald Green with pulse)
            drawCircle(
                color = QuettaGreen.copy(alpha = 0.25f),
                radius = pulseRadius,
                center = startPt
            )
            drawCircle(color = Color.White, radius = 14f, center = startPt)
            drawCircle(color = QuettaGreen, radius = 10f, center = startPt)

            // Destination Marker (Navy + Bright Yellow Target)
            drawCircle(
                color = QuettaYellow.copy(alpha = 0.35f),
                radius = pulseRadius,
                center = endPt
            )
            drawCircle(color = QuettaNavyDark, radius = 15f, center = endPt)
            drawCircle(color = QuettaYellow, radius = 8f, center = endPt)

            // If Active Ride is in progress, draw animated moving vehicle along the quadratic Bezier curve
            if (activeRide != null && activeRide.status != RideStatus.CANCELLED) {
                val t = activeRide.rideProgressFraction.coerceIn(0.05f, 0.98f)
                val invT = 1f - t
                val curX = invT * invT * startPt.x + 2 * invT * t * ctrlPt.x + t * t * endPt.x
                val curY = invT * invT * startPt.y + 2 * invT * t * ctrlPt.y + t * t * endPt.y
                val carPt = Offset(curX, curY)

                drawCircle(
                    color = QuettaYellow,
                    radius = 19f,
                    center = carPt
                )
                drawCircle(
                    color = QuettaNavyDark,
                    radius = 14f,
                    center = carPt
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = carPt
                )
            }
        }

        // Top HUD Overlay: Simulated GPS Badge & Distance/ETA Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = QuettaNavyDark.copy(alpha = 0.90f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(QuettaGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$selectedCityName Grid • Simulated GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Surface(
                color = QuettaYellow,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Route Distance",
                        tint = QuettaNavyDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$distanceKm km • ~$estMin mins",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = QuettaNavyDark
                    )
                }
            }
        }

        // Bottom Route Legend Pill
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(QuettaGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "PICKUP",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaTextSecondary,
                            fontSize = 9.sp
                        )
                        Text(
                            text = pickup.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "To",
                    tint = QuettaTextSecondary,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(16.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(QuettaYellowDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "DESTINATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuettaTextSecondary,
                            fontSize = 9.sp
                        )
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuettaNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptainAvatar(
    name: String,
    seed: Int = 1,
    isOnline: Boolean = true,
    size: Dp = 48.dp
) {
    val bgColors = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF065F46),
        Color(0xFF7C2D12),
        Color(0xFF312E81),
        Color(0xFF0F172A)
    )
    val chosenBg = bgColors[seed % bgColors.size]
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "QR" }

    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(chosenBg)
                .border(2.dp, QuettaYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(QuettaGreen)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}
