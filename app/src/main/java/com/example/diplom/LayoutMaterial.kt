package com.example.diplom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.DataBase.SendMaterial
import com.example.diplom.DataBase.AddressAdapter
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.MaterialAdapter
import com.example.diplom.DataBase.Provider
import com.example.diplom.DataBase.ReceiveAdapter
import com.example.diplom.DataBase.ReceiveMaterial
import com.example.diplom.DataBase.Repository
import com.example.diplom.MaterialView
import com.example.diplom.databinding.ActivityLayoutMaterialBinding
import com.example.diplom.databinding.BottomSheetLayoutBinding
import com.example.diplom.databinding.BottomSheetRecSendBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.text.contains

class LayoutMaterial : AppCompatActivity() {
    private lateinit var binding: ActivityLayoutMaterialBinding
    private lateinit var adapterAdress: AddressAdapter
    private lateinit var repository: Repository
    private lateinit var adapter: ReceiveAdapter
    private lateinit var realm: Realm
    private val selectedFilters = mutableSetOf<String>()
    private var allReceiveMaterial = listOf<ReceiveMaterial>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLayoutMaterialBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        intent.getStringExtra("material_name") ?: ""
        adapter = ReceiveAdapter(emptyList()) { /* обработка клика */ }
        binding.ViewReceiveMaterial.layoutManager = LinearLayoutManager(this)
        binding.ViewReceiveMaterial.adapter = adapter

        val config = RealmConfiguration.Builder(
            schema = setOf(
                Material::class,
                Provider::class,
                ReceiveMaterial::class,
                SendMaterial::class
            )
        ).build()
        realm = Realm.open(config)

        // Инициализируем repository **здесь**, после открытия Realm
        repository = Repository(realm)

        val materialName = intent.getStringExtra("material_name") ?: ""

        adapter = ReceiveAdapter(emptyList()) { /* обработка клика */ }
        binding.ViewReceiveMaterial.layoutManager = LinearLayoutManager(this)
        binding.ViewReceiveMaterial.adapter = adapter

        setupSpinner()
        loadReceiveMaterials()
        observeMaterials(materialName)

        binding.ButtonReceive.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun loadReceiveMaterials() {
        lifecycleScope.launch {
            val results = realm.query(ReceiveMaterial::class).find()
            adapter.updateData(results.toList())
        }
    }

    private fun observeMaterials(materialName: String) {
        lifecycleScope.launch {
            repository.getAllReceiveMaterials().collectLatest { materials ->
                allReceiveMaterial = materials
                val filtered = allReceiveMaterial.filter {
                    it.material?.nameMaterial == materialName
                }
                adapter.updateData(filtered)
            }
        }
    }

    private fun filterMaterialList() {
        val filtered = allReceiveMaterial.filter { receiveMaterial ->
            val providerName = receiveMaterial.provider?.nameProvider ?: ""
            selectedFilters.isEmpty() || selectedFilters.any { filter ->
                providerName.contains(filter, ignoreCase = true)
            }
        }

        adapter.updateData(filtered)
    }

    private fun setupSpinner() {
        lifecycleScope.launch {
            val providers = realm.query(Provider::class).find()
            val providerNames = providers.map { it.nameProvider }.distinct().sorted()

            val sortOptions = listOf("Все поставщики") + providerNames

            val spinnerAdapter = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, sortOptions)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.SpinnerSortView.adapter = spinnerAdapter

            binding.SpinnerSortView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = sortOptions[position]

                    selectedFilters.clear()

                    if (selected != "Все поставщики") {
                        selectedFilters.add(selected)
                    }

                    filterMaterialList()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetRecSendBinding.inflate(layoutInflater)

        sheetBinding.TextHead.text = "Добавить получение"
        sheetBinding.DeleteIcon.visibility = View.INVISIBLE
        dialog.setContentView(sheetBinding.root)

        val kol = listOf("шт", "м", "м²", "м³")
        val spinnerAdapterKol = ArrayAdapter(this, android.R.layout.simple_spinner_item, kol)
        spinnerAdapterKol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetBinding.TypeKol.adapter = spinnerAdapterKol

        // Загрузка поставщиков из базы данных
        lifecycleScope.launch {
            val providers = realm.query(Provider::class).find().map { it.nameProvider }
            val post = listOf("Неизвестно") + providers  // или без "Все поставщики", если не нужно

            val spinnerAdapterPost = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, post)
            spinnerAdapterPost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.Category.adapter = spinnerAdapterPost

            // Только после того, как данные загружены, устанавливаем onClickListener
            sheetBinding.ButtonSave.setOnClickListener {
                val name = sheetBinding.MaterialName.text.toString().trim()
                val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
                val postavshik = sheetBinding.Category.selectedItem.toString()
                val typekol = sheetBinding.TypeKol.selectedItem.toString()

                if (name.isBlank() || quantityStr.isBlank()) {
                    Toast.makeText(this@LayoutMaterial, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val quantity = quantityStr.toIntOrNull()
                if (quantity == null || quantity <= 0) {
                    Toast.makeText(this@LayoutMaterial, "Некорректное количество", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    repository.addMaterialReceive(name, quantity, postavshik, typekol, "Пока бета")
                    Toast.makeText(this@LayoutMaterial, "Материал сохранён", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}