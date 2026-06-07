package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.Booking
import com.example.data.BookingStatus
import com.example.ui.components.CDLCard
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    navController: NavController,
    viewModel: BookingViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val allBookings by viewModel.allBookings.collectAsState()
    val activeBookings by viewModel.activeBookings.collectAsState()

    var showActiveOnly by remember { mutableStateOf(false) }
    val displayedBookings = if (showActiveOnly) activeBookings else allBookings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MY BOOKINGS",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showActiveOnly,
                    onClick = { showActiveOnly = false },
                    label = {
                        Text(
                            "All (${allBookings.size})",
                            fontWeight = if (!showActiveOnly) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                        selectedLabelColor = NeonCyan
                    ),
                    border = if (!showActiveOnly)
                        BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
                    else
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                )
                FilterChip(
                    selected = showActiveOnly,
                    onClick = { showActiveOnly = true },
                    label = {
                        Text(
                            "Active (${activeBookings.size})",
                            fontWeight = if (showActiveOnly) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldLive.copy(alpha = 0.15f),
                        selectedLabelColor = EmeraldLive
                    ),
                    border = if (showActiveOnly)
                        BorderStroke(1.5.dp, EmeraldLive.copy(alpha = 0.5f))
                    else
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                )
            }

            if (displayedBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (showActiveOnly) "No active bookings" else "No bookings yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (showActiveOnly) "Your upcoming reservations will appear here"
                            else "Book a private garage to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { navController.navigate("private") },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FIND GARAGES", fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedBookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancel = { viewModel.cancelBooking(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancel: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    val (statusLabel, statusColor) = when (booking.status) {
        BookingStatus.PENDING -> "PENDING" to AmberWarning
        BookingStatus.CONFIRMED -> "CONFIRMED" to EmeraldLive
        BookingStatus.COMPLETED -> "COMPLETED" to MaterialTheme.colorScheme.onSurfaceVariant
        BookingStatus.CANCELLED -> "CANCELLED" to CrimsonDanger
    }

    CDLCard {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    booking.garageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateFormat.format(Date(booking.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status badge
            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${timeFormat.format(Date(booking.startTime))} – ${timeFormat.format(Date(booking.endTime))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                "€${String.format("%.2f", booking.price)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
        }

        if (booking.licensePlate.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    booking.licensePlate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Cancel button for active bookings
        if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.PENDING) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CANCEL BOOKING",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonDanger,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
