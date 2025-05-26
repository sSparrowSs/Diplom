package com.example.diplom.DataBase

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository(private val realm: Realm) {

    suspend fun addMaterial(name: String, quantity: Int) {
        realm.write {
            copyToRealm(Material().apply {
                id = RealmUUID.random().toString()
                this.name = name
                this.quantity = quantity
            })
        }
    }

    fun getAllMaterials(): Flow<List<Material>> {
        return realm.query<Material>().asFlow().map { it.list }
    }

    suspend fun deleteMaterialById(id: String) {
        realm.write {
            val material = query<Material>("id == $0", id).first().find()
            material?.let { delete(it) }
        }
    }
}
