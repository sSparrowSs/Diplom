package com.example.diplom.DataBase

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class AppDatabase: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) // Инициализация Realm

        // Конфигурация Realm
        val config = RealmConfiguration.Builder()
            .name("myrealm.realm") // Имя файла БД
            .schemaVersion(1) // Версия схемы
            .build()

        Realm.setDefaultConfiguration(config)
    }
}