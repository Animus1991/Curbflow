package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.data.PrivateParking
import com.example.ui.components.CDLCard
import com.example.ui.components.GlassSurface
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    garageId: String,
    bookingViewModel: BookingViewModel = viewModel(factory = LocalViewModelFactory.current),
    userViewModel: UserViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val garage = remember { MockData.privateList.find { it.id == garageId } }
    val profile by userViewModel.profile.collectAsState()

    var durationHours by remember { mutableIntStateOf(2) }
    var licensePlate by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var bookingConfirmed by remember { mutableStateOf(false) }

    val now = remember { System.currentTimeMillis() }
    val startTime = now + 15 * 60_000 // 15 min from now
    val endTime = startTime + durationHours * 3_600_000L
    val totalPrice = (garage?.pricePerHour ?: 0.0) * durationHours

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "BOOK PARKING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (garage == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Garage not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (bookingConfirmed) {
            // --- Confirmation View ---
            BookingConfirmationView(
                garage = garage,
                startTime = startTime,
                endTime = endTime,
                totalPrice = totalPrice,
                licensePlate = licensePlate,
                timeFormat = timeFormat,
                dateFormat = dateFormat,
                onDone = { navController.popBackStack() },
                onViewBookings = {
                    navController.navigate("bookings") {
                        popUpTo("private") { inclusive = false }
                    }
                }
            )
        } else {
            // --- Booking Form ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Garage header
                GarageInfoHeader(garage)

                Spacer(modifier = Modifier.height(24.dp))

                // Date & time
                CDLCard {
                    Text(
                        "BOOKING DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    BookingInfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = dateFormat.format(Date(startTime))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BookingInfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Start time",
                        value = timeFormat.format(Date(startTime))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BookingInfoRow(
                        icon = Icons.Default.TimerOff,
                        label = "End time",
                        value = timeFormat.format(Date(endTime))
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration selector
                    Text(
                        "DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 4, 8).forEach { hours ->
                            val isSelected = durationHours == hours
                            FilterChip(
                                selected = isSelected,
                                onClick = { durationHours = hours },
                                label = {
                                    Text(
                                        "${hours}h",
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                                    selectedLabelColor = NeonCyan
                                ),
                                border = if (isSelected)
                                    BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
                                else
                                    FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = false,
                                        borderColor = MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle info
                CDLCard {
                    Text(
                        "VEHICLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it.uppercase() },
                        label = { Text("License plate (optional)") },
                        placeholder = { Text("e.g. ABC-1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            cursorColor = NeonCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price summary
                CDLCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "TOTAL COST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$durationHours hours × €${String.format("%.2f", garage.pricePerHour)}/hr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "€${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm button
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "CONFIRM BOOKING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Confirm dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        "Confirm Booking",
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column {
                        Text("${garage?.name ?: ""}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${dateFormat.format(Date(startTime))} • ${timeFormat.format(Date(startTime))} – ${timeFormat.format(Date(endTime))}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Total: €${String.format("%.2f", totalPrice)}",
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            bookingViewModel.createBooking(
                                userId = profile?.id ?: "user_demo",
                                garageId = garageId,
                                garageName = garage?.name ?: "",
                                startTime = startTime,
                                endTime = endTime,
                                price = totalPrice,
                                licensePlate = licensePlate
                            )
                            bookingConfirmed = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("CONFIRM", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun GarageInfoHeader(garage: PrivateParking) {
    CDLCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(NeonBlue.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    garage.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    garage.area,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        " ${garage.rating}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "${garage.availableSlots}/${garage.totalSlots} spots",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (garage.availableSlots > 5) EmeraldLive else CrimsonDanger,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "€${String.format("%.2f", garage.pricePerHour)}/hr",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = label,
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookingConfirmationView(
    garage: PrivateParking,
    startTime: Long,
    endTime: Long,
    totalPrice: Double,
    licensePlate: String,
    timeFormat: SimpleDateFormat,
    dateFormat: SimpleDateFormat,
    onDone: () -> Unit,
    onViewBookings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(EmeraldLive.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Booking confirmed",
                tint = EmeraldLive,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "BOOKING CONFIRMED",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = EmeraldLive
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your parking spot is reserved",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Booking card
        CDLCard {
            Column {
                Text(
                    garage.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    garage.area,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))

                BookingInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = dateFormat.format(Date(startTime))
                )
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Time",
                    value = "${timeFormat.format(Date(startTime))} – ${timeFormat.format(Date(endTime))}"
                )
                if (licensePlate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BookingInfoRow(
                        icon = Icons.Default.DirectionsCar,
                        label = "Vehicle",
                        value = licensePlate
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total paid",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "€${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QR placeholder
        CDLCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "Booking QR code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Show this at the garage entrance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Button(
            onClick = onViewBookings,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("VIEW MY BOOKINGS", fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                "BACK TO GARAGES",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
