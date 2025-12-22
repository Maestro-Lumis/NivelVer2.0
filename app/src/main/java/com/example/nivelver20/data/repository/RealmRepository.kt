package com.example.nivelver20.data.repository

import android.content.Context
import android.util.Log
import com.example.nivelver20.data.local.realm.*
import com.example.nivelver20.utils.SecurityUtils
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class RealmRepository private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: RealmRepository? = null

        fun getInstance(context: Context): RealmRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealmRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val config = RealmConfiguration.Builder(
        schema = setOf(
            UserRealm::class,
            WordRealm::class,
            LecturaRealm::class,
            AudioRealm::class
        )
    )
        .name("nivelver.realm")
        .schemaVersion(3)
        .directory(context.filesDir.absolutePath)
        .build()

    val realm: Realm = Realm.open(config)  // Сделано публичным для SyncRepository

    init {
        Log.d("RealmDB", "========================================")
        Log.d("RealmDB", "Realm Database Initialized")
        Log.d("RealmDB", "Path: ${config.path}")
        Log.d("RealmDB", "Directory: ${File(config.path).parent}")
        Log.d("RealmDB", "Is in filesDir: ${config.path.contains("files")}")
        Log.d("RealmDB", "Is in cacheDir: ${config.path.contains("cache")}")
        Log.d("RealmDB", "File exists: ${File(config.path).exists()}")
        Log.d("RealmDB", "File size: ${File(config.path).length() / 1024} KB")

        val userCount = realm.query<UserRealm>().count().find()
        val wordCount = realm.query<WordRealm>().count().find()
        Log.d("RealmDB", "Users in DB: $userCount")
        Log.d("RealmDB", "Words in DB: $wordCount")
        Log.d("RealmDB", "========================================")
    }

    //  USER OPERATIONS
    suspend fun createUser(username: String, password: String) {
        realm.write {
            copyToRealm(UserRealm().apply {
                this.username = username
                this.password = SecurityUtils.hashPassword(password)
                this.nivel = "A0"
                this.lastModified = System.currentTimeMillis()
            })
        }

        val count = realm.query<UserRealm>().count().find()
        Log.d("RealmDB", "User created. Total users: $count")
    }

    suspend fun getUserByUsername(username: String): UserRealm? {
        val user = realm.query<UserRealm>("username == $0", username).first().find()
        Log.d("RealmDB", "getUserByUsername($username): ${if (user != null) "found" else "not found"}")
        return user
    }

    suspend fun verifyUserPassword(username: String, password: String): UserRealm? {
        val user = getUserByUsername(username)
        return if (user != null && SecurityUtils.verifyPassword(password, user.password)) {
            user
        } else {
            null
        }
    }

    fun getAllUsers(): Flow<List<UserRealm>> {
        return realm.query<UserRealm>().asFlow().map { it.list }
    }

    suspend fun updateUserNivel(username: String, newNivel: String) {
        realm.write {
            val user = query<UserRealm>("username == $0", username).first().find()
            user?.apply {
                nivel = newNivel
                lastModified = System.currentTimeMillis()
            }
        }
    }

    // === WORD OPERATIONS ===
    suspend fun addWord(spanish: String, russian: String, nivel: String) {
        realm.write {
            copyToRealm(WordRealm().apply {
                this.spanish = spanish
                this.russian = russian
                this.nivel = nivel
            })
        }
    }

    fun getWordsByNivel(nivel: String): Flow<List<WordRealm>> {
        return realm.query<WordRealm>("nivel == $0", nivel).asFlow().map { it.list }
    }

    // === LECTURA OPERATIONS ===
    suspend fun addLectura(text: String, question: String, answers: String, correctAnswer: Int, nivel: String) {
        realm.write {
            copyToRealm(LecturaRealm().apply {
                this.text = text
                this.question = question
                this.answers = answers
                this.correctAnswer = correctAnswer
                this.nivel = nivel
            })
        }
    }

    fun getLecturaByNivel(nivel: String): Flow<List<LecturaRealm>> {
        return realm.query<LecturaRealm>("nivel == $0", nivel).asFlow().map { it.list }
    }

    // === AUDIO OPERATIONS ===
    suspend fun addAudio(audioPath: String, question: String, answers: String, correctAnswer: Int, nivel: String) {
        realm.write {
            copyToRealm(AudioRealm().apply {
                this.audioPath = audioPath
                this.question = question
                this.answers = answers
                this.correctAnswer = correctAnswer
                this.nivel = nivel
            })
        }
    }

    fun getAudioByNivel(nivel: String): Flow<List<AudioRealm>> {
        return realm.query<AudioRealm>("nivel == $0", nivel).asFlow().map { it.list }
    }

    fun close() {
        realm.close()
        Log.d("RealmDB", "Realm closed")
    }
}