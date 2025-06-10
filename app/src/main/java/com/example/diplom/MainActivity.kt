package com.example.diplom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.Provider
import com.example.diplom.DataBase.ReceiveMaterial
import com.example.diplom.DataBase.SendMaterial
import com.example.diplom.DataBase.Users
import com.example.diplom.databinding.ActivityMainBinding
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.realm.kotlin.ext.toRealmList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var config: RealmConfiguration
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        config = RealmConfiguration.Builder(
            schema = setOf(Material::class, Provider::class, ReceiveMaterial::class, SendMaterial::class, Users::class)
        ).build()
        realm = Realm.open(config)

        insertDefaultProviders()
        insertDefaultMaterial()
        insertDefaultAdress()
        insertDefaultReceiveMaterial()
        insertDefaultUser()

        binding.textInsertAccount.setOnClickListener {
            binding.UserLogin.setText("User")
            binding.UserPassword.setText("AH_IO0wxUF")
        }

        binding.ButtonLogin.setOnClickListener {
            val loginInput = binding.UserLogin.text.toString().trim()
            val passwordInput = binding.UserPassword.text.toString().trim()

            lifecycleScope.launch(Dispatchers.IO) {
                val user = realm.query<Users>(
                    "loginUser == $0 AND passwordUser == $1",
                    loginInput, passwordInput
                ).first().find()

                if (user != null) {
                    startActivity(Intent(this@MainActivity, MaterialView::class.java))
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Неверный логин или пароль",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun insertDefaultUser() {
        CoroutineScope(Dispatchers.IO).launch {
            realm.write {
                val existing = this.query<Users>().find()
                delete(existing)

                copyToRealm(Users().apply {
                    loginUser = "User"
                    passwordUser = "AH_IO0wxUF"
                })
            }
        }
    }

    private fun insertDefaultProviders() {
        CoroutineScope(Dispatchers.IO).launch {
            realm.write {
                val existing = this.query<Provider>().find()
                delete(existing)

                val providerNames = listOf("Неизвестно", "ЛеруаМерлен", "Петрович", "Оби")
                providerNames.forEach { name ->
                    copyToRealm(Provider().apply {
                        nameProvider = name
                    })
                }
            }
        }
    }

    private fun insertDefaultMaterial() {
        CoroutineScope(Dispatchers.IO).launch {
            realm.write {
                val existing = this.query<Material>().find()
                delete(existing)

                val materials = listOf(
                    Material().apply {
                        nameMaterial = "Брус 150x150"
                        category = "Конструкция"
                        quantity = 100
                        unit = "м"
                    },
                    Material().apply {
                        nameMaterial = "Штукатурка Rotband"
                        category = "Отделка"
                        quantity = 30
                        unit = "шт"
                    },
                    Material().apply {
                        nameMaterial = "Провод ВВГнг 3x2.5"
                        category = "Электрика"
                        quantity = 200
                        unit = "м"
                    },
                    Material().apply {
                        nameMaterial = "Саморезы по дереву"
                        category = "Крепеж и монтаж"
                        quantity = 500
                        unit = "шт"
                    },
                    Material().apply {
                        nameMaterial = "Полипропиленовая труба"
                        category = "Сантехника"
                        quantity = 50
                        unit = "м"
                    },
                    Material().apply {
                        nameMaterial = "OSB плита"
                        category = "Конструкция"
                        quantity = 100
                        unit = "м²"
                    },
                    Material().apply {
                        nameMaterial = "Грунтовка Ceresit"
                        category = "Отделка"
                        quantity = 40
                        unit = "шт"
                    }
                )
                materials.forEach { copyToRealm(it) }
            }
        }
    }

    private fun insertDefaultReceiveMaterial() {
        CoroutineScope(Dispatchers.IO).launch {
            realm.write {
                val existing = this.query<ReceiveMaterial>().find()
                delete(existing)

                val providers = this.query<Provider>().find()
                val materials = this.query<Material>().find()

                if (providers.isNotEmpty() && materials.isNotEmpty()) {
                    val receiveMaterials = listOf(
                        ReceiveMaterial().apply {
                            provider = providers[1 % providers.size]
                            material = materials[0]
                            quantity = 79124
                            receivedAt = "01.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[2 % providers.size]
                            material = materials[1]
                            quantity = 782
                            receivedAt = "02.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[3 % providers.size]
                            material = materials[2]
                            quantity = 893
                            receivedAt = "03.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[2 % providers.size]
                            material = materials[3]
                            quantity = 6245
                            receivedAt = "03.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[1 % providers.size]
                            material = materials[4]
                            quantity = 12378
                            receivedAt = "03.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[1 % providers.size]
                            material = materials[5]
                            quantity = 340
                            receivedAt = "04.06.2025"
                        },
                        ReceiveMaterial().apply {
                            provider = providers[1 % providers.size]
                            material = materials[6]
                            quantity = 150
                            receivedAt = "04.06.2025"
                        }
                    )
                    receiveMaterials.forEach { copyToRealm(it) }
                }
            }
        }
    }

    private fun insertDefaultAdress() {
        lifecycleScope.launch(Dispatchers.IO) {
            realm.write {
                val existing = this.query<SendMaterial>().find()
                delete(existing)

                val materials = this.query<Material>().find()

                if (materials.isNotEmpty()) {
                    val sendMaterials = listOf(
                        SendMaterial().apply {
                            nameAddress = "Садовая 4"
                            material = materials[0]
                            quantity = 23
                            sentAt = "04.06.2025"
                        },
                        SendMaterial().apply {
                            nameAddress = "Ул. Пушкина 25"
                            material = materials[1]
                            quantity = 7
                            sentAt = "05.06.2025"
                        },
                        SendMaterial().apply {
                            nameAddress = "Ул. Курска 2"
                            material = materials[2]
                            quantity = 64
                            sentAt = "06.06.2025"
                        },
                        SendMaterial().apply {
                            nameAddress = "Новостройка, корпус 1"
                            material = materials[3]
                            quantity = 1200
                            sentAt = "06.06.2025"
                        },
                        SendMaterial().apply {
                            nameAddress = "ул. Ленина 10"
                            material = materials[4]
                            quantity = 300
                            sentAt = "07.06.2025"
                        },
                        SendMaterial().apply {
                            nameAddress = "ул. Архитекторов 9"
                            material = materials[5]
                            quantity = 45
                            sentAt = "07.06.2025"
                        }
                    )
                    sendMaterials.forEach { copyToRealm(it) }
                }
            }
        }
    }
}