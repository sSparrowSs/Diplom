package com.example.diplom.DataBase

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.RealmObject

open class Material : RealmObject {
    var id: RealmUUID = RealmUUID.random()
    var name: String = ""
    var quantity: Int = 0
    var category: String = ""
    var provider: String = ""
    var unit: String = ""
}

open class Adress : RealmObject {
    var id: RealmUUID = RealmUUID.random()
    var nameAdress: String = ""
    var unitAdress: String = ""
    var sendDate: String = ""
}