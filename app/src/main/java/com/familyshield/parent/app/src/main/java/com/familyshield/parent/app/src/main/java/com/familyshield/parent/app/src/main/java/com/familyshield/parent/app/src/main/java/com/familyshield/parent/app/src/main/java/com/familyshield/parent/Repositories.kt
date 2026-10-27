package com.familyshield.parent

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    val currentUser: FirebaseUser? get() = Backend.auth.currentUser

    suspend fun signIn(email: String, password: String) {
        Backend.auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String) {
        Backend.auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        Backend.auth.signOut()
    }
}

class DeviceRepository {
    /** Reads families/{parentUid}/devices. Writes happen only through server-side functions. */
    fun observe(uid: String): Flow<List<ChildDevice>> = callbackFlow {
        val registration = Backend.db
            .collection("families").document(uid).collection("devices")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.documents?.map { it.toDevice() } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    private fun DocumentSnapshot.toDevice(): ChildDevice {
        val apps = mutableListOf<AppUsage>()
        val raw = get("topApps") as? List<*>
        raw?.forEach { item ->
            val map = item as? Map<*, *>
            val name = map?.get("name") as? String
            if (name != null) {
                apps.add(AppUsage(name, (map["minutes"] as? Number)?.toInt() ?: 0))
            }
        }
        return ChildDevice(
            id = id,
            name = getString("name") ?: "Child device",
            batteryPercent = getLong("batteryPercent")?.toInt(),
            charging = getBoolean("charging") ?: false,
            lastSyncMillis = getTimestamp("lastSync")?.toDate()?.time,
            screenTimeTodayMinutes = getLong("screenTimeTodayMinutes")?.toInt(),
            screenTimeWeekMinutes = getLong("screenTimeWeekMinutes")?.toInt(),
            topApps = apps.sortedByDescending { it.minutes }
        )
    }
}

/** All mutations go through callable Cloud Functions so the server can validate them. */
class PairingRepository {
    suspend fun createCode(): PairingCode {
        val res = Backend.functions.getHttpsCallable("createPairingCode").call().await()
        val data = res.data as? Map<*, *> ?: error("Unexpected server response")
        val code = data["code"] as? String ?: error("Server did not return a code")
        val expires = (data["expiresAtMillis"] as? Number)?.toLong()
            ?: (System.currentTimeMillis() + 5 * 60 * 1000L)
        return PairingCode(code, expires)
    }

    suspend fun unlinkDevice(deviceId: String) {
        Backend.functions.getHttpsCallable("unlinkDevice")
            .call(mapOf("deviceId" to deviceId)).await()
    }

    suspend fun deleteFamilyData() {
        Backend.functions.getHttpsCallable("deleteFamilyData").call().await()
    }
}
