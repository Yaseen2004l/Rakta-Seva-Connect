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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.ui.components.*
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel

@Composable
fun HomeScreen(
    viewModel: DonorViewModel,
    onNeedBlood: () -> Unit,
    onDonate: () -> Unit,
    onProfile: () -> Unit,
    onRequests: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOpenRequests() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1A0A0A), BgDeep), radius = 1200f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🩸 Rakta-Seva", color = Blood, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Connect", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Every second counts", color = TextSecondary, fontSize = 13.sp)
                }
                IconButton(onClick = onProfile) {
                    Icon(Icons.Default.Person, contentDescription = "Profile",
                        tint = TextSecondary, modifier = Modifier.size(28.dp))
                }
            }

            // Stat row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.Favorite, "Success Rate", "100%", modifier = Modifier.weight(1f))
                StatCard(Icons.Default.People, "Donors Ready", "Active", modifier = Modifier.weight(1f))
            }

            SectionLabel("Emergency Actions")

            // I Need Blood
            GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onNeedBlood) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            Brush.linearGradient(listOf(Blood, BloodDark)),
                            androidx.compose.foundation.shape.CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) { Text("🆘", fontSize = 22.sp) }
                    Column {
                        Text("I Need Blood", color = TextPrimary, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Post an emergency request", color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null,
                        tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // I Want to Donate — goes to Profile if registered, Register if not
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.currentDonor != null) onProfile() else onDonate()
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            Brush.linearGradient(listOf(Color(0xFF27AE60), Color(0xFF1E8449))),
                            androidx.compose.foundation.shape.CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) { Text("💉", fontSize = 22.sp) }
                    Column {
                        Text(
                            if (state.currentDonor != null) "My Donor Profile"
                            else "I Want to Donate",
                            color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (state.currentDonor != null)
                                "Registered as ${state.currentDonor!!.bloodGroup} donor"
                            else "Register as a blood donor",
                            color = TextSecondary, fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null,
                        tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // Recent requests
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Recent Requests")
                TextButton(onClick = onRequests) {
                    Text("View All", color = Blood, fontSize = 12.sp)
                }
            }

            if (state.openRequests.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🩸", fontSize = 28.sp)
                        Text("No active requests right now",
                            color = TextMuted, fontSize = 14.sp,
                            textAlign = TextAlign.Center)
                        Text("Pull to refresh or check back later",
                            color = TextMuted, fontSize = 12.sp,
                            textAlign = TextAlign.Center)
                    }
                }
            } else {
                state.openRequests.take(3).forEachIndexed { index, req ->
                    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onRequests) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp)
                                    .background(Color(0x14FFFFFF),
                                        androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(String.format("%02d", index + 1),
                                    color = TextSecondary, fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("${req.bloodGroup} Blood Needed",
                                    color = TextPrimary, fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold)
                                Text(req.hospitalName.ifBlank { "Pincode: ${req.pincode}" },
                                    color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.OpenInNew, contentDescription = null,
                                tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "\"Every second counts.\nEvery donor matters.\"",
                    color = TextSecondary, fontSize = 13.sp,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Light,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}