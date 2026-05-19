package com.raktaseva.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.model.Donor
import com.raktaseva.app.model.BLOOD_GROUPS
import com.raktaseva.app.ui.components.*
import com.raktaseva.app.ui.theme.*
import com.raktaseva.app.viewmodel.DonorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonorRegistrationScreen(
    viewModel: DonorViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name             by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var pincode          by remember { mutableStateOf("") }
    var selectedGroup    by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableLongStateOf(0L) }
    var dateLabel        by remember { mutableStateOf("Never donated / Select date") }

    var nameError    by remember { mutableStateOf(false) }
    var phoneError   by remember { mutableStateOf(false) }
    var pincodeError by remember { mutableStateOf(false) }
    var groupError   by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            state.currentDonor?.id?.let { onSuccess(it) }
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(listOf(Color(0xFF0D1A0D), BgDeep), radius = 1200f)
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
                    Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Donor Registration",
                        color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Join the life-saving network",
                        color = TextSecondary, fontSize = 13.sp
                    )
                }
            }

            // ── Personal info ─────────────────────────────────────────────
            SectionLabel("Personal Information")

            GlassTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = "Full Name *",
                isError       = nameError
            )
            if (nameError) Text("Name is required", color = Blood, fontSize = 12.sp)

            GlassTextField(
                value         = phone,
                onValueChange = { phone = it; phoneError = false },
                label         = "Phone Number *",
                isError       = phoneError,
                keyboardType  = KeyboardType.Phone
            )
            if (phoneError) Text("Enter a valid 10-digit phone number", color = Blood, fontSize = 12.sp)

            GlassTextField(
                value         = pincode,
                onValueChange = { if (it.length <= 6) { pincode = it; pincodeError = false } },
                label         = "Pincode *",
                isError       = pincodeError,
                keyboardType  = KeyboardType.Number
            )
            if (pincodeError) Text("Enter a valid 6-digit pincode", color = Blood, fontSize = 12.sp)

            // ── Blood group ───────────────────────────────────────────────
            SectionLabel("Blood Group")
            if (groupError) Text("Please select your blood group", color = Blood, fontSize = 12.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement   = Arrangement.spacedBy(10.dp)
            ) {
                BLOOD_GROUPS.forEach { group ->
                    BloodGroupChip(
                        group    = group,
                        selected = selectedGroup == group,
                        onClick  = { selectedGroup = group; groupError = false }
                    )
                }
            }

            // ── Last donation date ─────────────────────────────────────────
            SectionLabel("Last Donation Date")
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick  = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            cal.set(y, m, d)
                            lastDonationDate = cal.timeInMillis
                            dateLabel = SimpleDateFormat(
                                "dd MMM yyyy", Locale.getDefault()
                            ).format(cal.time)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Blood)
                    Text(
                        dateLabel,
                        color    = if (lastDonationDate == 0L) TextMuted else TextPrimary,
                        fontSize = 15.sp
                    )
                }
            }

            // ── Info ──────────────────────────────────────────────────────
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ℹ️", fontSize = 14.sp)
                    Text(
                        "Donors who gave blood within 90 days are automatically " +
                                "marked ineligible until the cooldown ends.",
                        color = TextSecondary, fontSize = 13.sp
                    )
                }
            }

            if (state.errorMessage != null) {
                Text(state.errorMessage!!, color = Blood, fontSize = 13.sp)
            }

            // ── Submit ────────────────────────────────────────────────────
            RedButton(
                text     = if (state.isLoading) "Registering..." else "Register as Donor",
                modifier = Modifier.fillMaxWidth(),
                enabled  = !state.isLoading
            ) {
                nameError    = name.isBlank()
                phoneError   = phone.length < 10
                pincodeError = pincode.length != 6
                groupError   = selectedGroup.isEmpty()

                if (!nameError && !phoneError && !pincodeError && !groupError) {
                    viewModel.registerDonor(
                        Donor(
                            name             = name.trim(),
                            bloodGroup       = selectedGroup,
                            pincode          = pincode.trim(),
                            phone            = phone.trim(),
                            lastDonationDate = lastDonationDate
                        )
                    ) { id -> onSuccess(id) }
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = Blood
            )
        }
    }
}