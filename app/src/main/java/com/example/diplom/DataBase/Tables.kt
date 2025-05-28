package com.example.diplom.DataBase

import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.RealmObject

open class Material : RealmObject {
    var id: RealmUUID = RealmUUID.random()  // Используем RealmUUID
    var name: String = ""
    var quantity: Int = 0
    var category: String = ""
    var unit: String = ""
}