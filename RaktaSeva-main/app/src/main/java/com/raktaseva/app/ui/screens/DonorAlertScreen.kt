package com.raktaseva.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.model.BloodRequest
import com.raktaseva.app.ui.components.GlassCard
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel

@Composable
fun DonorAlertScreen(
    request: BloodRequest,
    viewModel: DonorViewModel,
    onBack: () -> Unit
) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val donor   = state.currentDonor

    // Track if this donor accepted
    var accepted  by remember { mutableStateOf(false) }
    var declined  by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(listOf(Color(0xFF1A0505), BgDeep), radius = 1200f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Blood Request Alert",
                        color = Blood, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Someone needs your help", color = TextSecondary, fontSize = 13.sp)
                }
            }

            // ── Blood group badge ─────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🚨", fontSize = 40.sp)
                    Text(
                        request.bloodGroup,
                        color      = Blood,
                        fontSize   = 52.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Blood Needed Urgently",
                        color      = TextPrimary,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Request details ───────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (request.patientName.isNotBlank()) {
                        DetailRow("Patient", request.patientName)
                    }
                    if (request.hospitalName.isNotBlank()) {
                        DetailRow("Hospital", request.hospitalName)
                    }
                    DetailRow("📍 Pincode", request.pincode)
                }
            }

            // ── Appeal message ────────────────────────────────────────────
            if (request.appealMessage.isNotBlank()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "\"${request.appealMessage}\"",
                        color     = TextSecondary,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── After accepting — show contact ────────────────────────────
            if (accepted && donor != null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✅ You accepted!", color = AccentGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "The requester can now see your contact.\nThank you for saving a life.",
                            color     = TextSecondary,
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        // Show requester pincode so donor knows where to go
                        Text(
                            "📍 Go to pincode: ${request.pincode}",
                            color      = TextPrimary,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Call button if hospital name available
                        if (request.hospitalName.isNotBlank()) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${request.pincode}")
                                    }
                                    context.startActivity(intent)
                                },
                                shape  = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Icon(Icons.Default.Call, null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Call Hospital Area", color = Color.White)
                            }
                        }
                    }
                }
            }

            if (declined) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "You declined this request.\nYou can still change your mind.",
                        color     = TextMuted,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Accept / Decline buttons ──────────────────────────────────
            if (!accepted && !declined) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Decline
                    OutlinedButton(
                        onClick = { declined = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape    = RoundedCornerShape(16.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text("❌  Decline", color = TextSecondary, fontSize = 15.sp)
                    }

                    // Accept
                    Button(
                        onClick = {
                            accepted = true
                            // Mark request fulfilled in Firestore
                            viewModel.fulfillRequest(request.id)
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Blood)
                    ) {
                        Text("✅  Accept", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Change mind after declining
            if (declined) {
                Button(
                    onClick  = { declined = false },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Blood)
                ) {
                    Text("I changed my mind — Accept", color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}