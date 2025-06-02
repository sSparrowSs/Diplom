package com.example.diplom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.Provider
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

        // Создаём конфигурацию Realm
        config = RealmConfiguration.Builder(
            schema = setOf(Material::class, Provider::class)
        ).build()
        realm = Realm.open(config)

        // 👇 Добавляем провайдеров один раз
        insertDefaultProviders()
        insertDefaultMaterial()

        binding.ButtonLogin.setOnClickListener {
            startActivity(Intent(this@MainActivity, MaterialView::class.java))
        }

        binding.AddProvider.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddProvider::class.java))
        }
    }

    private fun insertDefaultProviders() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = realm.query<Provider>().find()
            if (existing.isEmpty()) {
                val providerNames = listOf("Неизвестно", "ЛеруаМерлен", "Петрович", "Оби")
                realm.write {
                    providerNames.forEach { name ->
                        copyToRealm(Provider().apply {
                            nameProvider = name
                        })
                    }
                }
            }
        }
    }

    private fun insertDefaultMaterial() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = realm.query<Material>().find()
            if (existing.isEmpty()) {
                realm.write {
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
                        }
                    )
                    materials.forEach { copyToRealm(it) }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}