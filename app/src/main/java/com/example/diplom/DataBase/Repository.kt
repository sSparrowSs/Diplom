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

    suspend fun addMaterialReceive(name: String, quantity: Int, providerName: String = "", unit: String = "", time: String) {
        realm.write {
            val existingProvider = query<Provider>("nameProvider == $0", providerName).first().find()
            val provider = existingProvider ?: copyToRealm(Provider().apply {
                nameProvider = providerName
            })

            val existingMaterial = query<Material>("nameMaterial == $0", name).first().find()
            val material = existingMaterial ?: copyToRealm(Material().apply {
                nameMaterial = name
                category = ""
                this.unit = unit
            })

            copyToRealm(ReceiveMaterial().apply {
                idReceiveMaterial = RealmUUID.random()
                this.material = material
                this.provider = provider
                this.quantity = quantity
                this.receivedAt = time
            })
        }
    }

    fun getAllReceiveMaterials(): Flow<List<ReceiveMaterial>> {
        return realm.query<ReceiveMaterial>().asFlow().map { it.list }
    }

    fun getAllSendMaterials(): Flow<List<SendMaterial>> {
        return realm.query(SendMaterial::class).asFlow().map { it.list }
    }

    suspend fun updateMaterialReceive(
        material: ReceiveMaterial,
        name: String,
        quantity: Int,
        providerName: String,
        date: String
    ) {
        realm.write {
            findLatest(material)?.apply {
                this.quantity = quantity
                this.receivedAt = date
                this.material = query<Material>("nameMaterial == $0", name).first().find()
                this.provider = if (providerName == "Неизвестно") {
                    null
                } else {
                    query<Provider>("nameProvider == $0", providerName).first().find()
                }
            }
        }
    }

    suspend fun deleteMaterialReceive(material: ReceiveMaterial) {
        realm.write {
            findLatest(material)?.let { delete(it) }
        }
    }

    suspend fun updateMaterialSend(
        sendMaterial: SendMaterial,
        materialName: String,
        quantity: Int,
        address: String,
        date: String
    ) {
        realm.write {
            findLatest(sendMaterial)?.apply {
                this.quantity = quantity
                this.nameAddress = address
                this.sentAt = date

                this.material = query<Material>("nameMaterial == $0", materialName).first().find()
            }
        }
    }

    suspend fun deleteMaterialSend(sendMaterial: SendMaterial) {
        realm.write {
            findLatest(sendMaterial)?.let { delete(it) }
        }
    }

    suspend fun addMaterialSend(
        name: String,
        quantity: Int,
        address: String,
        type: String,
        date: String
    ) {
        realm.write {
            val material = query<Material>("nameMaterial == $0", name).first().find()
            if (material != null) {
                copyToRealm(
                    SendMaterial().apply {
                        this.material = material
                        this.quantity = quantity
                        this.nameAddress = address
                        this.sentAt = date
                    }
                )
            }
        }
    }
}