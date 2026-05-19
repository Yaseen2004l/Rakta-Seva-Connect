package com.raktaseva.app.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.app.model.BloodRequest
import com.raktaseva.app.model.Donor
import com.raktaseva.app.model.isEligibleToday
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class DonorRepository {
    private val db          = FirebaseFirestore.getInstance()
    private val donorsCol   = db.collection("donors")
    private val requestsCol = db.collection("requests")

    suspend fun registerDonor(donor: Donor): Result<String> = runCatching {
        withTimeout(30_000) {
            val doc = donorsCol.add(donor.copy(isEligible = donor.isEligibleToday())).await()
            Log.d("RaktaSeva", "Donor registered: ${doc.id}")
            doc.id
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "registerDonor failed: ${e.message}", e) } }

    suspend fun getDonorById(id: String): Result<Donor> = runCatching {
        withTimeout(30_000) {
            val snap = donorsCol.document(id).get().await()
            snap.toObject(Donor::class.java)!!.copy(id = snap.id)
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "getDonorById failed: ${e.message}", e) } }

    suspend fun setAvailability(donorId: String, available: Boolean): Result<Unit> = runCatching {
        withTimeout(30_000) {
            donorsCol.document(donorId).update("isAvailable", available).await()
            Unit
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "setAvailability failed: ${e.message}", e) } }

    suspend fun updateLastDonationDate(donorId: String, date: Long): Result<Unit> = runCatching {
        withTimeout(30_000) {
            donorsCol.document(donorId).update(
                mapOf("lastDonationDate" to date, "isEligible" to false)
            ).await()
            Unit
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "updateDonation failed: ${e.message}", e) } }

    suspend fun postRequest(request: BloodRequest): Result<String> = runCatching {
        withTimeout(30_000) {
            val doc = requestsCol.add(request).await()
            Log.d("RaktaSeva", "Request posted: ${doc.id}")
            doc.id
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "postRequest failed: ${e.message}", e) } }

    // Fetch ALL requests (open and fulfilled) so requester can see who accepted
    suspend fun getOpenRequests(): Result<List<BloodRequest>> = runCatching {
        withTimeout(30_000) {
            val results = requestsCol
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(BloodRequest::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.postedAt }
            Log.d("RaktaSeva", "Fetched ${results.size} requests")
            results
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "getOpenRequests failed: ${e.message}", e) } }

    suspend fun fulfillRequest(
        requestId: String,
        donorName: String,
        donorPhone: String
    ): Result<Unit> = runCatching {
        withTimeout(30_000) {
            requestsCol.document(requestId).update(
                mapOf(
                    "status"             to "FULFILLED",
                    "acceptedDonorName"  to donorName,
                    "acceptedDonorPhone" to donorPhone
                )
            ).await()
            Unit
        }
    }.also { it.onFailure { e -> Log.e("RaktaSeva", "fulfillRequest failed: ${e.message}", e) } }
}