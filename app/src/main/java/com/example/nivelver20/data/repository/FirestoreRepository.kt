package com.example.nivelver20.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class CloudUser(
    val username: String = "",
    val password: String = "",
    val nivel: String = "A0",
    val timestamp: Long = 0L
)

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    init {
        Log.d("FirestoreRepo", "Firestore initialized")
    }

    // Создать пользователя в облаке
    suspend fun createUser(username: String, password: String, nivel: String = "A0"): Result<Unit> {
        return try {
            val userData = hashMapOf(
                "username" to username,
                "password" to password,
                "nivel" to nivel,
                "timestamp" to System.currentTimeMillis()
            )

            usersCollection
                .document(username)
                .set(userData, SetOptions.merge())
                .await()

            Log.d("FirestoreRepo", "ser created: $username")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Create error: ${e.message}")
            Result.failure(e)
        }
    }

    //Получить пользователя из облака
    suspend fun getUser(username: String): Result<CloudUser?> {
        return try {
            val doc = usersCollection
                .document(username)
                .get()
                .await()

            if (doc.exists()) {
                val user = CloudUser(
                    username = doc.getString("username") ?: "",
                    password = doc.getString("password") ?: "",
                    nivel = doc.getString("nivel") ?: "A0",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
                Log.d("FirestoreRepo", "User found: $username")
                Result.success(user)
            } else {
                Log.d("FirestoreRepo", "User not found: $username")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get error: ${e.message}")
            Result.failure(e)
        }
    }

    // Получить всех пользователей из облака
    suspend fun getAllUsers(): Result<List<CloudUser>> {
        return try {
            val snapshot = usersCollection.get().await()

            val users = snapshot.documents.mapNotNull { doc ->
                try {
                    CloudUser(
                        username = doc.getString("username") ?: return@mapNotNull null,
                        password = doc.getString("password") ?: "",
                        nivel = doc.getString("nivel") ?: "A0",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "⚠Skip invalid user: ${e.message}")
                    null
                }
            }

            Log.d("FirestoreRepo", "Fetched ${users.size} users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "GetAll error: ${e.message}")
            Result.failure(e)
        }
    }

    //Обновить nivel пользователя
    suspend fun updateUserNivel(username: String, newNivel: String): Result<Unit> {
        return try {
            usersCollection
                .document(username)
                .update(
                    mapOf(
                        "nivel" to newNivel,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
                .await()

            Log.d("FirestoreRepo", "Nivel updated: $username -> $newNivel")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Update error: ${e.message}")
            Result.failure(e)
        }
    }
}

    // Удалить пользователя из облака (позже)
    /*

    suspend fun deleteUser(username: String): Result<Unit> {
        return try {
            usersCollection
                .document(username)
                .delete()
                .await()

            Log.d("FirestoreRepo", "User deleted: $username")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Delete error: ${e.message}")
            Result.failure(e)
        }
    }
}*/