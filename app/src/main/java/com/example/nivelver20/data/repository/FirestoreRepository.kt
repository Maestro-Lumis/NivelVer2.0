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

data class VocabularioWord(
    val es: String = "",
    val ru: String = "",
    val nivel: String = ""
)

data class LecturaOpcion(
    val correcta: Boolean = false,
    val texto: String = ""
)

data class LecturaQuestionFirestore(
    val nivel: String = "",
    val opciones: List<LecturaOpcion> = emptyList(),
    val pregunta: String = "",
    val texto: String = ""
)

data class AudioOpcion(
    val correcta: Boolean = false,
    val texto: String = ""
)

data class AudioQuestionFirestore(
    val audioUrl: String = "",
    val nivel: String = "",
    val opciones: List<AudioOpcion> = emptyList(),
    val pregunta: String = ""
)
data class GrammarOpcion(
    val correcta: Boolean = false,
    val texto: String = ""
)

data class GrammarQuestionFirestore(
    val nivel: String = "",
    val tipo: String = "",
    val pregunta: String = "",
    val opciones: List<GrammarOpcion> = emptyList(),
    val fraseIncorrecta: String? = null,
    val errorPalabra: String? = null,
    val indicePalabra: Int? = null,
    val palabras: List<String>? = null,
    val respuestaCorrecta: String? = null,
    val explicacion: String? = null
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
    private val vocabularioCollection = firestore.collection("vocabulario")
    private val lecturaCollection = firestore.collection("lectura")
    private val audioCollection = firestore.collection("audio")
    private val grammarCollection = firestore.collection("grammar_questions") // NEW!

    init {
        @Suppress("DEPRECATION") val settings = FirebaseFirestoreSettings.Builder()
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

    // ========== VOCABULARIO ==========
    suspend fun getWordsByNivel(nivel: String): Result<List<VocabularioWord>> {
        return try {
            Log.d("FirestoreRepo", "Loading words for nivel: $nivel")

            val snapshot = vocabularioCollection
                .whereEqualTo("nivel", nivel)
                .get()
                .await()

            val words = snapshot.documents.mapNotNull { doc ->
                try {
                    VocabularioWord(
                        es = doc.getString("es") ?: "",
                        ru = doc.getString("ru") ?: "",
                        nivel = doc.getString("nivel") ?: ""
                    )
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "Skip invalid word: ${e.message}")
                    null
                }
            }

            Log.d("FirestoreRepo", "Loaded ${words.size} words for nivel $nivel")
            Result.success(words)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get words error: ${e.message}")
            Result.failure(e)
        }
    }

    // ========== LECTURA ==========
    suspend fun getLecturaQuestionsByNivel(nivel: String): Result<List<LecturaQuestionFirestore>> {
        return try {
            Log.d("FirestoreRepo", "Loading lectura questions for nivel: $nivel")

            val snapshot = lecturaCollection
                .whereEqualTo("nivel", nivel)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null

                    val pregunta = data["pregunta"] as? String ?: ""
                    val texto = data["texto"] as? String ?: ""
                    val nivelValue = data["nivel"] as? String ?: ""

                    val opcionesData = data["opciones"] as? List<*> ?: emptyList<Any>()
                    val opciones = opcionesData.mapNotNull { opcion ->
                        val opcionMap = opcion as? Map<*, *> ?: return@mapNotNull null
                        LecturaOpcion(
                            correcta = opcionMap["correcta"] as? Boolean ?: false,
                            texto = opcionMap["texto"] as? String ?: ""
                        )
                    }

                    LecturaQuestionFirestore(
                        nivel = nivelValue,
                        opciones = opciones,
                        pregunta = pregunta,
                        texto = texto
                    )
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "Skip invalid lectura question: ${e.message}")
                    null
                }
            }

            Log.d("FirestoreRepo", "Loaded ${questions.size} lectura questions for nivel $nivel")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get lectura questions error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLecturaText(pregunta: String): Result<String> {
        return try {
            val snapshot = lecturaCollection
                .whereEqualTo("pregunta", pregunta)
                .limit(1)
                .get()
                .await()

            val texto = snapshot.documents.firstOrNull()?.getString("texto") ?: ""
            Log.d("FirestoreRepo", "Loaded text for question: $pregunta")
            Result.success(texto)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get lectura text error: ${e.message}")
            Result.failure(e)
        }
    }

    // ========== AUDIO ==========
    suspend fun getAudioQuestionsByNivel(nivel: String): Result<List<AudioQuestionFirestore>> {
        return try {
            Log.d("FirestoreRepo", "Loading audio questions for nivel: $nivel")

            val snapshot = audioCollection
                .whereEqualTo("nivel", nivel)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null

                    val audioUrl = data["audioUrl"] as? String ?: ""
                    val pregunta = data["pregunta"] as? String ?: ""
                    val nivelValue = data["nivel"] as? String ?: ""

                    val opcionesData = data["opciones"] as? List<*> ?: emptyList<Any>()
                    val opciones = opcionesData.mapNotNull { opcion ->
                        val opcionMap = opcion as? Map<*, *> ?: return@mapNotNull null
                        AudioOpcion(
                            correcta = opcionMap["correcta"] as? Boolean ?: false,
                            texto = opcionMap["texto"] as? String ?: ""
                        )
                    }

                    AudioQuestionFirestore(
                        audioUrl = audioUrl,
                        nivel = nivelValue,
                        opciones = opciones,
                        pregunta = pregunta
                    )
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "Skip invalid audio question: ${e.message}")
                    null
                }
            }

            Log.d("FirestoreRepo", "Loaded ${questions.size} audio questions for nivel $nivel")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get audio questions error: ${e.message}")
            Result.failure(e)
        }
    }

    // ========== GRAMMAR ==========
    suspend fun getGrammarQuestionsByNivel(nivel: String): Result<List<GrammarQuestionFirestore>> {
        return try {
            Log.d("FirestoreRepo", "Loading grammar questions for nivel: $nivel")

            val snapshot = grammarCollection
                .whereEqualTo("nivel", nivel)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null

                    val nivelValue = data["nivel"] as? String ?: ""
                    val tipo = data["tipo"] as? String ?: ""
                    val pregunta = data["pregunta"] as? String ?: ""
                    val fraseIncorrecta = data["fraseIncorrecta"] as? String
                    val errorPalabra = data["errorPalabra"] as? String
                    val indicePalabra = (data["indicePalabra"] as? Long)?.toInt()
                    val respuestaCorrecta = data["respuestaCorrecta"] as? String
                    val explicacion = data["explicacion"] as? String

                    val opcionesData = data["opciones"] as? List<*> ?: emptyList<Any>()
                    val opciones = opcionesData.mapNotNull { opcion ->
                        val opcionMap = opcion as? Map<*, *> ?: return@mapNotNull null
                        GrammarOpcion(
                            correcta = opcionMap["correcta"] as? Boolean ?: false,
                            texto = opcionMap["texto"] as? String ?: ""
                        )
                    }

                    val palabrasData = data["palabras"] as? List<*>
                    val palabras = palabrasData?.mapNotNull { it as? String }

                    GrammarQuestionFirestore(
                        nivel = nivelValue,
                        tipo = tipo,
                        pregunta = pregunta,
                        opciones = opciones,
                        fraseIncorrecta = fraseIncorrecta,
                        errorPalabra = errorPalabra,
                        indicePalabra = indicePalabra,
                        palabras = palabras,
                        respuestaCorrecta = respuestaCorrecta,
                        explicacion = explicacion
                    )
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "Skip invalid grammar question: ${e.message}")
                    null
                }
            }

            Log.d("FirestoreRepo", "Loaded ${questions.size} grammar questions for nivel $nivel")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Get grammar questions error: ${e.message}")
            Result.failure(e)
        }
    }
}