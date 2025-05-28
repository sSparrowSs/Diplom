package com.example.diplom.DataBase

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository(private val realm: Realm) {

    suspend fun addMaterial(name: String, quantity: Int, category: String = "", unit: String = "") {
        realm.write {
            copyToRealm(Material().apply {
                id = RealmUUID.random()
                this.name = name
                this.quantity = quantity
                this.category = category
                this.unit = unit
            })
        }
    }

    fun getAllMaterials(): Flow<List<Material>> {
        return realm.query<Material>().asFlow().map { it.list }
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
                it.name = name
                it.quantity = quantity
                it.category = category
                it.unit = unit
            }
        }
    }

}
