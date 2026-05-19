package com.raktaseva.app.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.raktaseva.app.api.AnthropicClient
import com.raktaseva.app.model.BloodRequest
import com.raktaseva.app.model.Donor
import com.raktaseva.app.repository.DonorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val currentDonor: Donor? = null,
    val openRequests: List<BloodRequest> = emptyList(),
    val selectedRequest: BloodRequest? = null      // for donor alert screen
)

class DonorViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DonorRepository()
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun registerDonor(donor: Donor, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repo.registerDonor(donor)
                    .onSuccess { id ->
                        _uiState.value = _uiState.value.copy(
                            isLoading      = false,
                            successMessage = "Registered successfully!",
                            currentDonor   = donor.copy(id = id)
                        )
                        onSuccess(id)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading    = false,
                            errorMessage = when {
                                error.message?.contains("timeout", ignoreCase = true) == true ->
                                    "Connection timed out. Check your internet and try again."
                                else -> "Registration failed: ${error.message}"
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = "Something went wrong: ${e.message}"
                )
            }
        }
    }

    fun loadDonor(id: String) {
        viewModelScope.launch {
            try {
                repo.getDonorById(id).onSuccess { donor ->
                    _uiState.value = _uiState.value.copy(currentDonor = donor)
                }
            } catch (e: Exception) { /* silent */ }
        }
    }

    fun toggleAvailability(donorId: String, available: Boolean) {
        viewModelScope.launch {
            try {
                repo.setAvailability(donorId, available).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        currentDonor = _uiState.value.currentDonor?.copy(isAvailable = available)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed: ${e.message}")
            }
        }
    }

    fun postRequest(
        bloodGroup: String, pincode: String,
        patientName: String, hospitalName: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val appeal = AnthropicClient.generateAppealMessage(
                    bloodGroup, pincode, patientName, hospitalName
                )
                val request = BloodRequest(
                    bloodGroup    = bloodGroup,
                    pincode       = pincode,
                    patientName   = patientName,
                    hospitalName  = hospitalName,
                    appealMessage = appeal
                )
                repo.postRequest(request)
                    .onSuccess {
                        sendLocalNotification(bloodGroup, appeal)
                        _uiState.value = _uiState.value.copy(
                            isLoading      = false,
                            successMessage = "Request sent! Donors are being notified."
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading    = false,
                            errorMessage = "Failed: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = "Something went wrong: ${e.message}"
                )
            }
        }
    }

    // ── Select request (for donor alert screen) ───────────────────────────
    fun selectRequest(request: BloodRequest) {
        _uiState.value = _uiState.value.copy(selectedRequest = request)
    }

    // ── Fulfill request (donor accepted) ─────────────────────────────────
    fun fulfillRequest(requestId: String) {
        viewModelScope.launch {
            try {
                repo.fulfillRequest(
                    requestId = requestId,
                    donorName  = _uiState.value.currentDonor?.name ?: "",
                    donorPhone = _uiState.value.currentDonor?.phone ?: ""
                )
                val updated = _uiState.value.openRequests.filter { it.id != requestId }
                _uiState.value = _uiState.value.copy(openRequests = updated)
            } catch (e: Exception) { /* silent */ }
        }
    }

    private fun sendLocalNotification(bloodGroup: String, message: String) {
        val ctx       = getApplication<Application>()
        val manager   = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "rakta_alerts"
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Blood Alerts", NotificationManager.IMPORTANCE_HIGH)
                .apply { enableVibration(true) }
        )
        viewModelScope.launch {
            delay(500)
            val notif = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🩸 Emergency: $bloodGroup Blood Needed")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()
            manager.notify(System.currentTimeMillis().toInt(), notif)
        }
    }

    fun loadOpenRequests() {
        viewModelScope.launch {
            try {
                repo.getOpenRequests().onSuccess { list ->
                    _uiState.value = _uiState.value.copy(openRequests = list)
                }
            } catch (e: Exception) { /* silent */ }
        }
    }

    fun recordDonation(donorId: String) {
        viewModelScope.launch {
            try {
                repo.updateLastDonationDate(donorId, System.currentTimeMillis()).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        currentDonor = _uiState.value.currentDonor?.copy(
                            lastDonationDate = System.currentTimeMillis(),
                            isEligible       = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}