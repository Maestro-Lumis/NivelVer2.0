package com.example.nivelver20.data.local.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class UserRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var username: String = ""
    var password: String = ""
    var nivel: String = "A0"
    var lastModified: Long = 0L  // ДОБАВЛЕНО для синхронизации
}

// Модель слова для Vocabulario
class WordRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var spanish: String = ""
    var russian: String = ""
    var nivel: String = "A1"
}

// Модель текста для Lectura
class LecturaRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var text: String = ""
    var question: String = ""
    var answers: String = "" // JSON string со списком ответов
    var correctAnswer: Int = 0
    var nivel: String = "A1"
}

// Модель аудио для Audio
class AudioRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var audioPath: String = ""
    var question: String = ""
    var answers: String = "" // JSON string со списком ответов
    var correctAnswer: Int = 0
    var nivel: String = "A1"
}