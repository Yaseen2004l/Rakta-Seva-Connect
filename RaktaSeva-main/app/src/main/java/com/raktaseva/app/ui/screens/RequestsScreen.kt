package com.raktaseva.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.model.BloodRequest
import com.raktaseva.app.ui.components.GlassCard
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RequestsScreen(
    viewModel: DonorViewModel,
    onBack: () -> Unit,
    onViewRequest: (BloodRequest) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOpenRequests() }

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
                .padding(top = 56.dp, bottom = 16.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            "Blood Requests",
                            color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (state.openRequests.isEmpty()) "No requests yet"
                            else "${state.openRequests.size} total",
                            color = TextSecondary, fontSize = 13.sp
                        )
                    }
                }
                IconButton(onClick = { viewModel.loadOpenRequests() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Content ───────────────────────────────────────────────────
            if (state.openRequests.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🩸", fontSize = 48.sp)
                        Text(
                            "No requests yet",
                            color      = TextSecondary,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "When someone posts a blood request\nit will appear here.",
                            color     = TextMuted,
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadOpenRequests() },
                            shape   = RoundedCornerShape(12.dp),
                            colors  = ButtonDefaults.buttonColors(containerColor = Blood)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint     = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Refresh", color = Color.White)
                        }
                    }
                }
            } else {
                val uniqueRequests = state.openRequests.distinctBy { it.id }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = uniqueRequests, key = { it.id }) { req ->
                        RequestCard(
                            req           = req,
                            onViewRequest = {
                                // Only allow tapping into OPEN requests
                                if (req.status == "OPEN") onViewRequest(req)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    req: BloodRequest,
    onViewRequest: () -> Unit = {}
) {
    val isFulfilled = req.status == "FULFILLED"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick  = if (isFulfilled) null else onViewRequest
    ) {

        // ── Top row ───────────────────────────────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Blood group badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isFulfilled)
                            Brush.linearGradient(listOf(AccentGreen, Color(0xFF1E8449)))
                        else
                            Brush.linearGradient(listOf(Blood, BloodDark)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    req.bloodGroup,
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (req.patientName.isNotBlank()) req.patientName else "Patient",
                    color      = TextPrimary,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (req.hospitalName.isNotBlank()) {
                    Text(req.hospitalName, color = TextSecondary, fontSize = 13.sp)
                }
                Text("📍 Pincode: ${req.pincode}", color = TextMuted, fontSize = 12.sp)
            }

            // Status + time
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isFulfilled) AccentGreen.copy(0.15f)
                            else Blood.copy(0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        req.status,
                        color      = if (isFulfilled) AccentGreen else Blood,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(req.postedAt)),
                    color = TextMuted, fontSize = 11.sp
                )
                Text(
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(req.postedAt)),
                    color = TextMuted, fontSize = 11.sp
                )
            }
        }

        // ── Fulfilled — show donor info ────────────────────────────────────
        if (isFulfilled && req.acceptedDonorName.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0x22FFFFFF), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("✅", fontSize = 14.sp)
                Column {
                    Text(
                        "Accepted by ${req.acceptedDonorName}",
                        color      = AccentGreen,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "📞 ${req.acceptedDonorPhone}",
                        color    = AccentGreen,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── Appeal message for OPEN requests ──────────────────────────────
        if (!isFulfilled && req.appealMessage.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0x22FFFFFF), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("✨", fontSize = 12.sp)
                Text(req.appealMessage, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap to Accept or Decline →",
                color     = Blood.copy(alpha = 0.7f),
                fontSize  = 11.sp,
                textAlign = TextAlign.End,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}