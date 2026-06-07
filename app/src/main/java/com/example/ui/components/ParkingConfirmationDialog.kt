package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Enhanced Parking Confirmation Dialog.
 * 
 * Shown after the user has arrived at the zone to collect feedback
 * for model training and community accuracy improvement.
 * 
 * Features:
 * - "I Parked Here" confirmation (1-tap with location lock)
 * - "Spot Was Taken" negative feedback
 * - Optional comment field for detailed feedback
 * - Duration tracking (how long did it take to find)
 * - Privacy notice (data is anonymized to H3 cell)
 */
@Composable
fun ParkingConfirmationDialog(
    zoneName: String,
    onConfirmParked: (feedback: String) -> Unit,
    onSpotTaken: (feedback: String) -> Unit,
    onDismiss: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableIntStateOf(-1) }
    val durationOptions = listOf("< 1 min", "1-3 min", "3-5 min", "5+ min")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Did you find a spot?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Near $zoneName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Duration question
                Text(
                    "HOW LONG DID IT TAKE?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durationOptions.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedDuration == idx,
                            onClick = { selectedDuration = idx },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Optional feedback
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Optional feedback") },
                    placeholder = { Text("e.g. 'Found spot on side street'") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(16.dp)
                )

                // Privacy notice
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldLive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your feedback is anonymized to H3 cell level. No exact location stored.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = if (selectedDuration >= 0) durationOptions[selectedDuration] else ""
                    onConfirmParked("$duration|$feedbackText")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLive)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("I PARKED HERE", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val duration = if (selectedDuration >= 0) durationOptions[selectedDuration] else ""
                    onSpotTaken("$duration|$feedbackText")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonDanger.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonDanger, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SPOT WAS TAKEN", fontWeight = FontWeight.Black, color = CrimsonDanger)
            }
        }
    )
}
