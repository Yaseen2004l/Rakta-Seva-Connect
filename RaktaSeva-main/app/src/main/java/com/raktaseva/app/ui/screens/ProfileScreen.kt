package com.raktaseva.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.model.isEligibleToday
import com.raktaseva.app.ui.components.*
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: DonorViewModel,
    onBack: () -> Unit,
    onRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val donor = state.currentDonor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF0A0D1A), BgDeep),
                    radius = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "My Profile",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Not registered ────────────────────────────────────────────
            if (donor == null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💉", fontSize = 48.sp)
                        Text(
                            "You are not registered as a donor yet.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        RedButton(
                            text = "Register Now",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onRegister
                        )
                    }
                }

            } else {

                // ── State for dialogs ─────────────────────────────────────
                var showDonationDialog  by remember { mutableStateOf(false) }
                var showDonationSuccess by remember { mutableStateOf(false) }

                // ── Donation confirmation dialog ───────────────────────────
                if (showDonationDialog) {
                    AlertDialog(
                        onDismissRequest = { showDonationDialog = false },
                        containerColor   = BgCard,
                        title = {
                            Text(
                                "Record Donation",
                                color      = TextPrimary,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                "This will record today as your last donation date. " +
                                        "You will be automatically marked ineligible for 90 days.",
                                color    = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.recordDonation(donor.id)
                                    showDonationDialog  = false
                                    showDonationSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blood)
                            ) {
                                Text("Yes, I Donated", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDonationDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                        }
                    )
                }

                // ── Avatar + name card ─────────────────────────────────────
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    Brush.linearGradient(listOf(Blood, BloodDark)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                donor.name.firstOrNull()?.uppercaseChar()?.toString() ?: "D",
                                color      = Color.White,
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                donor.name,
                                color      = TextPrimary,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(donor.phone, color = TextSecondary, fontSize = 14.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Blood.copy(alpha = 0.2f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        donor.bloodGroup,
                                        color      = Blood,
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("• ${donor.pincode}", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // ── Status cards row ──────────────────────────────────────
                val eligible = donor.isEligibleToday()
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text(
                            "ELIGIBILITY",
                            color         = TextMuted,
                            fontSize      = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (eligible) "✅ Eligible" else "⏳ Cooling Down",
                            color      = if (eligible) AccentGreen else AccentOrange,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text(
                            "STATUS",
                            color         = TextMuted,
                            fontSize      = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (donor.isAvailable) "🟢 Available" else "🔴 Unavailable",
                            color      = if (donor.isAvailable) AccentGreen else Blood,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── Last donation card ────────────────────────────────────
                if (donor.lastDonationDate > 0L) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint     = Blood,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    "LAST DONATION",
                                    color         = TextMuted,
                                    fontSize      = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(Date(donor.lastDonationDate)),
                                    color      = TextPrimary,
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!donor.isEligibleToday()) {
                                    val daysLeft = 90 - (
                                            (System.currentTimeMillis() - donor.lastDonationDate)
                                                    / (1000 * 60 * 60 * 24)
                                            )
                                    Text(
                                        "Eligible again in ~$daysLeft days",
                                        color    = AccentOrange,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Availability toggle ───────────────────────────────────
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Availability",
                                color      = TextPrimary,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (donor.isAvailable)
                                    "You will receive emergency alerts"
                                else
                                    "You won't receive any alerts",
                                color    = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked         = donor.isAvailable,
                            onCheckedChange = { viewModel.toggleAvailability(donor.id, it) },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = Color.White,
                                checkedTrackColor   = AccentGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Blood.copy(0.4f)
                            )
                        )
                    }
                }

                // ── Donation success banner ───────────────────────────────
                if (showDonationSuccess) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("✅", fontSize = 20.sp)
                            Text(
                                "Donation recorded! You will be eligible again in 90 days.",
                                color    = AccentGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Record donation button ────────────────────────────────
                GhostButton(
                    text     = "Record New Donation",
                    modifier = Modifier.fillMaxWidth(),
                    onClick  = { showDonationDialog = true }
                )
            }
        }
    }
}