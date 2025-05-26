package com.example.diplom.DataBase

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Material : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var quantity: Int = 0
}