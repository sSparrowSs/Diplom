package com.example.diplom.DataBase

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class AppDatabase : Application() {
    lateinit var realm: Realm
        private set

    override fun onCreate() {
        super.onCreate()

        val config = RealmConfiguration.Builder(
            schema = setOf(Material::class)
        )
            .name("myrealm.realm")
            .schemaVersion(1)
            .build()

        realm = Realm.open(config)
    }
}