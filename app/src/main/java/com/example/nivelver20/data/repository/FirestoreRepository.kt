package com.example.nivelver20.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class CloudUser(
    val username: String = "",
    val password: String = "",
    val nivel: String = "A0",
    val timestamp: Long = 0L
)

class FirestoreRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: FirestoreRepository? = null

        fun getInstance(): FirestoreRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreRepository().also { INSTANCE = it }
            }
        }
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    init {
        // Включаем offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings

        Log.d("FirestoreRepo", "Firestore initialized with offline support")
    }

    // Создать пользователя
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

            Log.d("FirestoreRepo", "User created: $username")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Create error: ${e.message}")
            Result.failure(e)
        }
    }

    // Получить пользователя
    suspend fun getUserByUsername(username: String): Result<CloudUser?> {
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

    // Проверить пароль
    suspend fun verifyUserPassword(username: String, password: String): Result<CloudUser?> {
        return try {
            val userResult = getUserByUsername(username)

            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null && user.password == password) {
                    Log.d("FirestoreRepo", "Password verified: $username")
                    Result.success(user)
                } else {
                    Log.d("FirestoreRepo", "Invalid password: $username")
                    Result.success(null)
                }
            } else {
                Result.failure(userResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Verify error: ${e.message}")
            Result.failure(e)
        }
    }

    // Обновить nivel пользователя
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