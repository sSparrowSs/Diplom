package com.example.diplom.DataBase

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.sql.Time

class Repository(private val realm: Realm) {

    suspend fun addMaterial(name: String, quantity: Int, category: String = "", unit: String = "") {
        realm.write {
            copyToRealm(Material().apply {
                idMaterial = RealmUUID.random()
                this.nameMaterial = name
                this.quantity = quantity
                this.category = category
                this.unit = unit
            })
        }
    }

    suspend fun addProvider(name: String) {
        realm.write {
            copyToRealm(Provider().apply {
                idProvider = RealmUUID.random()
                this.nameProvider = name
            })
        }
    }

    suspend fun addMaterialReceive(name: String, quantity: Int, providerName: String = "", unit: String = "", time: String) {
        realm.write {
            // Найти или создать Provider
            val existingProvider = query<Provider>("nameProvider == $0", providerName).first().find()
            val provider = existingProvider ?: copyToRealm(Provider().apply {
                nameProvider = providerName
            })

            // Найти или создать Material
            val existingMaterial = query<Material>("nameMaterial == $0", name).first().find()
            val material = existingMaterial ?: copyToRealm(Material().apply {
                nameMaterial = name
                category = "" // или задай нужную категорию
                this.unit = unit
            })

            // Создание ReceiveMaterial
            copyToRealm(ReceiveMaterial().apply {
                idReceiveMaterial = RealmUUID.random()
                this.material = material
                this.provider = provider
                this.quantity = quantity
                this.receivedAt = time
            })
        }
    }


    fun getAllMaterials(): Flow<List<Material>> {
        return realm.query<Material>().asFlow().map { it.list }
    }

    fun getAllReceiveMaterials(): Flow<List<ReceiveMaterial>> {
        return realm.query<ReceiveMaterial>().asFlow().map { it.list }
    }

    fun getAllSendAdress(): Flow<List<SendMaterial>> {
        return realm.query<SendMaterial>().asFlow().map { it.list }
    }

    suspend fun deleteMaterial(name: String, quantity: Int, category: String, unit: String) {
        realm.write {
            val material = query<Material>().first().find()
            material?.let { delete(it) }
        }
    }

    suspend fun updateMaterial(name: String, quantity: Int, category: String, unit: String) {
        realm.write {
            val material = query<Material>().first().find()
            material?.let {
                it.nameMaterial = name
                it.quantity = quantity
                it.category = category
                it.unit = unit
            }
        }
    }
}