package com.example.diplom.DataBase

import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.RealmObject

class Material : RealmObject {
    var idMaterial: RealmUUID = RealmUUID.random()
    var nameMaterial: String = ""
    var category: String = ""
    var quantity: Int = 0
    var unit: String = ""
}

class Users : RealmObject {
    var idUser: RealmUUID = RealmUUID.random()
    var loginUser: String = ""
    var passwordUser: String = ""
}

class Provider : RealmObject {
    var idProvider: RealmUUID = RealmUUID.random()
    var nameProvider: String = ""
}

class ReceiveMaterial : RealmObject {
    var idReceiveMaterial: RealmUUID = RealmUUID.random()
    var provider: Provider? = null
    var material: Material? = null
    var quantity: Int = 0
    var receivedAt: String = ""
}

class SendMaterial : RealmObject {
    var idSendMaterial: RealmUUID = RealmUUID.random()
    var nameAddress: String = ""
    var material: Material? = null
    var quantity: Int = 0
    var sentAt: String = ""
}