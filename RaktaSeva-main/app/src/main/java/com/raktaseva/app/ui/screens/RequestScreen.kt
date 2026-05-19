package com.raktaseva.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.model.BLOOD_GROUPS
import com.raktaseva.app.ui.components.*
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequestScreen(viewModel: DonorViewModel, onBack: () -> Unit, onSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    var selectedGroup by remember { mutableStateOf("") }
    var pincode       by remember { mutableStateOf("") }
    var patientName   by remember { mutableStateOf("") }
    var hospitalName  by remember { mutableStateOf("") }
    var groupError    by remember { mutableStateOf(false) }
    var pincodeError  by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) { onSuccess(); viewModel.clearMessage() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1A0505), BgDeep), radius = 1200f))) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Emergency Request", color = Blood, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Donors will be notified instantly", color = TextSecondary, fontSize = 13.sp)
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🚨", fontSize = 20.sp)
                    Text("This sends an immediate alert to all eligible donors in your area.", color = TextSecondary, fontSize = 13.sp)
                }
            }

            SectionLabel("Blood Group Needed")
            if (groupError) Text("Please select a blood group", color = Blood, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BLOOD_GROUPS.forEach { group ->
                    BloodGroupChip(group = group, selected = selectedGroup == group) { selectedGroup = group; groupError = false }
                }
            }

            SectionLabel("Location")
            GlassTextField(value = pincode, onValueChange = { if (it.length <= 6) { pincode = it; pincodeError = false } }, label = "Hospital Pincode *", isError = pincodeError, keyboardType = KeyboardType.Number)
            if (pincodeError) Text("Enter a valid 6-digit pincode", color = Blood, fontSize = 12.sp)
            GlassTextField(value = hospitalName, onValueChange = { hospitalName = it }, label = "Hospital Name (Optional)")

            SectionLabel("Patient Details (Optional)")
            GlassTextField(value = patientName, onValueChange = { patientName = it }, label = "Patient Name")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨", fontSize = 14.sp)
                        Text("Claude AI will craft a personalised urgent appeal message.", color = TextSecondary, fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⏱️", fontSize = 14.sp)
                        Text("Donors will be notified within 5 seconds.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            if (state.errorMessage != null) Text(state.errorMessage!!, color = Blood, fontSize = 13.sp)

            Button(
                onClick = {
                    groupError   = selectedGroup.isEmpty()
                    pincodeError = pincode.length != 6
                    if (!groupError && !pincodeError) {
                        viewModel.postRequest(selectedGroup, pincode, patientName, hospitalName)
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blood)
            ) {
                if (state.isLoading) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Icon(Icons.Default.Send, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isLoading) "Alerting donors..." else "Send Emergency Request", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (state.isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Blood)
    }
}
