package com.julianjelfs.laundromatic

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class LaundromaticRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun observeItems(userId: String): Flow<List<LaundryItem>> = callbackFlow {
        val registration = itemCollection(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot
                ?.documents
                .orEmpty()
                .mapNotNull { document -> document.toLaundryItem() }
                .sortedBy(LaundryItem::dueInDays)

            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    suspend fun refreshItems(userId: String): List<LaundryItem> =
        itemCollection(userId)
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { document -> document.toLaundryItem() }
            .sortedBy(LaundryItem::dueInDays)

    suspend fun addItem(
        userId: String,
        name: String,
        intervalInDays: Int,
        daysSinceLastWash: Int,
    ) {
        val lastWashed = LocalDate.now(zoneId)
            .minusDays(daysSinceLastWash.toLong())
            .atTime(12, 0)
            .atZone(zoneId)
            .toInstant()
        itemCollection(userId)
            .add(
                mapOf(
                    "name" to name.trim(),
                    "intervalInDays" to intervalInDays,
                    "lastWashed" to lastWashed.toEpochMilli(),
                ),
            )
            .await()
    }

    suspend fun washItem(userId: String, itemId: String) {
        itemDocument(userId, itemId)
            .set(mapOf("lastWashed" to todayAtNoon().toEpochMilli()), SetOptions.merge())
            .await()
    }

    suspend fun pauseItem(userId: String, itemId: String) {
        itemDocument(userId, itemId)
            .set(mapOf("pausedAt" to System.currentTimeMillis()), SetOptions.merge())
            .await()
    }

    suspend fun resumeItem(userId: String, itemId: String) {
        itemDocument(userId, itemId)
            .set(mapOf("pausedAt" to 0L), SetOptions.merge())
            .await()
    }

    suspend fun deleteItem(userId: String, itemId: String) {
        itemDocument(userId, itemId).delete().await()
    }

    suspend fun setAllPaused(userId: String, paused: Boolean) {
        val snapshot = itemCollection(userId).get().await()
        if (snapshot.isEmpty) return

        val batch = firestore.batch()
        val pausedAt = if (paused) System.currentTimeMillis() else 0L
        snapshot.documents.forEach { document ->
            batch.set(document.reference, mapOf("pausedAt" to pausedAt), SetOptions.merge())
        }
        batch.commit().await()
    }

    private fun itemCollection(userId: String) = firestore.collection("${userId}_items")

    private fun itemDocument(userId: String, itemId: String) = itemCollection(userId).document(itemId)

    private fun DocumentSnapshot.toLaundryItem(): LaundryItem? {
        val name = getString("name") ?: return null
        val intervalInDays = getLong("intervalInDays")?.toInt() ?: return null
        val lastWashed = getLong("lastWashed") ?: return null
        val pausedAt = getLong("pausedAt")?.takeUnless { it == 0L }

        return LaundryItem(
            id = id,
            name = name,
            intervalInDays = intervalInDays,
            lastWashed = lastWashed,
            pausedAt = pausedAt,
        )
    }

    private fun todayAtNoon(): Instant =
        LocalDate.now(zoneId)
            .atTime(12, 0)
            .atZone(zoneId)
            .toInstant()
}

private val zoneId: ZoneId = ZoneId.systemDefault()
