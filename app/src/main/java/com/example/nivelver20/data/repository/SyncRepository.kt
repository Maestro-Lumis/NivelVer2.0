package com.example.nivelver20.data.repository

import android.content.Context
import android.util.Log
import com.example.nivelver20.data.local.realm.UserRealm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Координирует работу Realm (локально) и Firestore (облако)

class SyncRepository private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SyncRepository? = null

        fun getInstance(context: Context): SyncRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val realmRepo = RealmRepository.getInstance(context)
    private val firestoreRepo = FirestoreRepository()
    private val syncScope = CoroutineScope(Dispatchers.IO)

    init {
        Log.d("SyncRepo", "SyncRepository initialized")

        // Автоматическая синхронизация при запуске
        syncScope.launch {
            syncFromCloud()
        }
    }

    // Cоздать пользователя
    suspend fun createUser(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Локально (всегда работает)
            realmRepo.createUser(username, password)
            Log.d("SyncRepo", "Local: User created")

            // 2. В облако
            val cloudResult = firestoreRepo.createUser(username, password)

            if (cloudResult.isSuccess) {
                Log.d("SyncRepo", "Cloud: User synced")
            } else {
                Log.w("SyncRepo", "Cloud: Sync failed (will retry later)")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepo", "Error creating user: ${e.message}")
            Result.failure(e)
        }
    }

    //Получить пользователя по username
    suspend fun getUserByUsername(username: String): UserRealm? {
        return realmRepo.getUserByUsername(username)
    }

    //Проверить пароль пользователя
    suspend fun verifyUserPassword(username: String, password: String): UserRealm? {
        return realmRepo.verifyUserPassword(username, password)
    }

    // Получить всех пользователей (Flow для реактивности)
    fun getAllUsers(): Flow<List<UserRealm>> {
        return realmRepo.getAllUsers()
    }

    // Обновить nivel пользователя

    suspend fun updateUserNivel(username: String, newNivel: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Локально
            realmRepo.updateUserNivel(username, newNivel)
            Log.d("SyncRepo", "Local: Nivel updated")

            // В облако
            val cloudResult = firestoreRepo.updateUserNivel(username, newNivel)

            if (cloudResult.isSuccess) {
                Log.d("SyncRepo", "Cloud: Nivel synced")
            } else {
                Log.w("SyncRepo", "Cloud: Sync failed")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepo", "Error updating nivel: ${e.message}")
            Result.failure(e)
        }
    }


    //Синхронизация из облака в локальную БД
    suspend fun syncFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncRepo", "Starting sync from cloud...")

            // Получаем данные из облака
            val cloudResult = firestoreRepo.getAllUsers()

            if (cloudResult.isFailure) {
                Log.w("SyncRepo", "Cloud unavailable, using local data")
                return@withContext Result.success(Unit)
            }

            val cloudUsers = cloudResult.getOrNull() ?: emptyList()
            Log.d("SyncRepo", "Got ${cloudUsers.size} users from cloud")

            // Обновляем локальную БД
            cloudUsers.forEach { cloudUser ->
                val localUser = realmRepo.getUserByUsername(cloudUser.username)

                if (localUser == null) {
                    // Пользователя нет локально
                    realmRepo.realm.write {
                        copyToRealm(UserRealm().apply {
                            username = cloudUser.username
                            password = cloudUser.password
                            nivel = cloudUser.nivel
                            lastModified = cloudUser.timestamp
                        })
                    }
                    Log.d("SyncRepo", "Created from cloud: ${cloudUser.username}")

                } else if (cloudUser.timestamp > localUser.lastModified) {
                    // Облачная версия новее
                    realmRepo.realm.write {
                        query<UserRealm>("username == $0", cloudUser.username)
                            .first()
                            .find()
                            ?.apply {
                                nivel = cloudUser.nivel
                                lastModified = cloudUser.timestamp
                            }
                    }
                    Log.d("SyncRepo", "Updated from cloud: ${cloudUser.username}")
                }
            }

            Log.d("SyncRepo", "Sync from cloud completed")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SyncRepo", "Sync error: ${e.message}")
            Result.failure(e)
        }
    }

    //Синхронизация из локальной БД в облако

    suspend fun syncToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncRepo", "Starting sync to cloud...")

            val localUsers = mutableListOf<UserRealm>()
            realmRepo.getAllUsers().collect { users ->
                localUsers.addAll(users)
            }

            var successCount = 0
            var failCount = 0

            localUsers.forEach { user ->
                val result = firestoreRepo.createUser(user.username, user.password, user.nivel)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failCount++
                }
            }

            Log.d("SyncRepo", "Sync to cloud completed: $successCount success, $failCount failed")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SyncRepo", "Sync to cloud error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun forceFullSync(): Result<Unit> {
        Log.d("SyncRepo", "Starting full bidirectional sync...")

        // Сначала загружаем из облака
        val downloadResult = syncFromCloud()
        if (downloadResult.isFailure) {
            return downloadResult
        }

        // Затем загружаем в облако
        return syncToCloud()
    }


    fun close() {
        realmRepo.close()
        Log.d("SyncRepo", "SyncRepository closed")
    }
}