package com.raktaseva.app.model

data class Donor(
    val id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val pincode: String = "",
    val phone: String = "",
    val lastDonationDate: Long = 0L,
    val isAvailable: Boolean = true,
    val isEligible: Boolean = true,
    val fcmToken: String = ""
)

data class BloodRequest(
    val id: String = "",
    val bloodGroup: String = "",
    val pincode: String = "",
    val patientName: String = "",
    val hospitalName: String = "",
    val appealMessage: String = "",
    val postedAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN",           // OPEN or FULFILLED
    val acceptedDonorName: String = "",    // filled when a donor accepts
    val acceptedDonorPhone: String = ""    // filled when a donor accepts
)

val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

fun Donor.isEligibleToday(): Boolean {
    if (lastDonationDate == 0L) return true
    val ninetyDays = 90L * 24 * 60 * 60 * 1000
    return (System.currentTimeMillis() - lastDonationDate) >= ninetyDays
}