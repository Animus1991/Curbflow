package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.SubscriptionTier
import com.example.ui.components.CDLCard
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val profile by viewModel.profile.collectAsState()
    val currentTier = profile?.subscriptionTier ?: SubscriptionTier.FREE

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CHOOSE YOUR PLAN",
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Unlock smarter parking",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Upgrade for real-time data, smart routing, and priority features",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Free Tier ---
            SubscriptionTierCard(
                tier = SubscriptionTier.FREE,
                title = "Free",
                price = "€0",
                period = "forever",
                icon = Icons.Default.Explore,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                features = listOf(
                    "Basic parking heatmap",
                    "5-minute data delay",
                    "3 searches per day",
                    "Community reports"
                ),
                isCurrentPlan = currentTier == SubscriptionTier.FREE,
                onSelect = { viewModel.updateSubscription(SubscriptionTier.FREE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Commuter Tier ---
            SubscriptionTierCard(
                tier = SubscriptionTier.COMMUTER,
                title = "Commuter",
                price = "€6.99",
                period = "/month",
                icon = Icons.Default.DirectionsCar,
                accentColor = EmeraldLive,
                features = listOf(
                    "Real-time parking data",
                    "Smart route suggestions",
                    "Unlimited searches",
                    "Favorite zone alerts",
                    "Booking discounts (5%)"
                ),
                isCurrentPlan = currentTier == SubscriptionTier.COMMUTER,
                isRecommended = true,
                onSelect = { viewModel.updateSubscription(SubscriptionTier.COMMUTER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Professional Tier ---
            SubscriptionTierCard(
                tier = SubscriptionTier.PROFESSIONAL,
                title = "Professional",
                price = "€9.99",
                period = "/month",
                icon = Icons.Default.WorkspacePremium,
                accentColor = NeonCyan,
                features = listOf(
                    "Everything in Commuter",
                    "Fleet API access",
                    "Priority smart routing",
                    "Advanced analytics dashboard",
                    "Booking discounts (15%)",
                    "Ad-free experience"
                ),
                isCurrentPlan = currentTier == SubscriptionTier.PROFESSIONAL,
                onSelect = { viewModel.updateSubscription(SubscriptionTier.PROFESSIONAL) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Legal disclaimer ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Plans do not guarantee parking availability. CurbFlow AI provides probability-based guidance only. You can change or cancel your plan at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SubscriptionTierCard(
    tier: SubscriptionTier,
    title: String,
    price: String,
    period: String,
    icon: ImageVector,
    accentColor: Color,
    features: List<String>,
    isCurrentPlan: Boolean,
    isRecommended: Boolean = false,
    onSelect: () -> Unit
) {
    val borderColor = if (isCurrentPlan) accentColor else MaterialTheme.colorScheme.outline
    val borderWidth = if (isCurrentPlan) 2.dp else 1.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isCurrentPlan) 12.dp else 4.dp,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isRecommended) {
                            Text(
                                "MOST POPULAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    }
                }

                // Current plan badge
                if (isCurrentPlan) {
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "CURRENT",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // Features
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action button
            if (isCurrentPlan) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.3f)),
                    enabled = false
                ) {
                    Text(
                        "ACTIVE PLAN",
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
            } else {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "SELECT PLAN",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
