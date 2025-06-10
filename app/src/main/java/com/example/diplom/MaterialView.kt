package com.example.diplom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.MaterialAdapter
import com.example.diplom.DataBase.Provider
import com.example.diplom.DataBase.ReceiveMaterial
import com.example.diplom.DataBase.Repository
import com.example.diplom.DataBase.SendMaterial
import com.example.diplom.databinding.ActivityLayoutMaterialBinding
import com.example.diplom.databinding.ActivityMaterialViewBinding
import com.example.diplom.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MaterialView : AppCompatActivity() {
    private lateinit var binding: ActivityMaterialViewBinding
    private lateinit var bindingAdress: ActivityLayoutMaterialBinding
    private lateinit var adapter: MaterialAdapter
    private lateinit var realm: Realm
    private lateinit var repository: Repository
    private val selectedFilters = mutableSetOf<String>()
    private var allMaterials = listOf<Material>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMaterialViewBinding.inflate(layoutInflater)
        bindingAdress = ActivityLayoutMaterialBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(binding.root)

        val config = RealmConfiguration.Builder(
            schema = setOf(
                Material::class,
                Provider::class,
                ReceiveMaterial::class,
                SendMaterial::class
            )
        ).build()

        realm = Realm.open(config)
        repository = Repository(realm)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinner()

        adapter = MaterialAdapter(emptyList()) { material ->
            val intent = Intent(this@MaterialView, LayoutMaterial::class.java)
            intent.putExtra("material_name", material.nameMaterial)
            startActivity(intent)
        }
        binding.ViewMaterial.layoutManager = LinearLayoutManager(this)
        binding.ViewMaterial.adapter = adapter

        observeMaterialsWithQuantity()


        binding.SearchMaterial.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterMaterialList(newText ?: "")
                return true
            }
        })

        binding.ButtonCreate.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun observeMaterialsWithQuantity() {
        lifecycleScope.launch {
            launch {
                realm.query(ReceiveMaterial::class).asFlow()
                    .collect { receiveNotification ->
                        if (receiveNotification is InitialResults || receiveNotification is ResultsChange) {
                            updateMaterialsWithTotalQuantity()
                        }
                    }
            }
            launch {
                realm.query(Material::class).asFlow()
                    .collect { materialNotification ->
                        if (materialNotification is InitialResults || materialNotification is ResultsChange) {
                            updateMaterialsWithTotalQuantity()
                        }
                    }
            }
        }
    }

    private suspend fun updateMaterialsWithTotalQuantity() {
        val materials = realm.query(Material::class).find()
        val receiveList = realm.query(ReceiveMaterial::class).find()
        val sendList = realm.query(SendMaterial::class).find()

        val receivedMap = receiveList
            .groupBy { it.material?.nameMaterial ?: "" }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        val sentMap = sendList
            .groupBy { it.material?.nameMaterial ?: "" }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        val updatedMaterials = materials.map { material ->
            val name = material.nameMaterial
            val received = receivedMap[name] ?: 0
            val sent = sentMap[name] ?: 0
            val netQuantity = received - sent

            Material().apply {
                idMaterial = material.idMaterial
                nameMaterial = name
                category = material.category
                quantity = netQuantity.coerceAtLeast(0)
                unit = material.unit
            }
        }

        withContext(Dispatchers.Main) {
            allMaterials = updatedMaterials
            adapter.updateData(updatedMaterials)
        }
    }


    private fun filterMaterialList(query: String) {
        val filtered = allMaterials.filter { material ->
            val matchesFilter = selectedFilters.isEmpty() || selectedFilters.any { filter ->
                material.category.contains(filter, ignoreCase = true)
            }
            val matchesQuery = material.nameMaterial.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }

        adapter.updateData(filtered)
    }

    private fun setupSpinner() {
        val sortOptions = listOf("Все типы", "Конструкция", "Отделка", "Электрика", "Крепеж и монтаж", "Сантехника")

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.SpinnerSortView.adapter = spinnerAdapter

        binding.SpinnerSortView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = sortOptions[position]

                if (selected == "Все типы") {
                    selectedFilters.clear()
                    binding.Sort.visibility = View.INVISIBLE
                } else {
                    selectedFilters.clear()
                    selectedFilters.add(selected)
                    binding.Sort.visibility = View.VISIBLE
                    binding.Sort.text = selected
                }
                filterMaterialList(binding.SearchMaterial.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.Sort.setOnClickListener {
            selectedFilters.clear()
            binding.Sort.visibility = View.INVISIBLE
            binding.SpinnerSortView.setSelection(0)
            filterMaterialList(binding.SearchMaterial.query.toString())
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetLayoutBinding.inflate(layoutInflater)

        sheetBinding.TextHead.text = "Добавить"
        sheetBinding.DeleteIcon.visibility = View.INVISIBLE

        dialog.setContentView(sheetBinding.root)

        val categories = listOf("Конструкция", "Отделка", "Электрика", "Крепеж и монтаж", "Сантехника")
        val kol = listOf("шт","м", "м²", "м³")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        val spinnerAdapterKol = ArrayAdapter(this, android.R.layout.simple_spinner_item, kol)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdapterKol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sheetBinding.Category.adapter = spinnerAdapter

        sheetBinding.ButtonSave.setOnClickListener {
            val name = sheetBinding.MaterialName.text.toString().trim()
            val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
            val category = sheetBinding.Category.selectedItem.toString()
            val typekol = sheetBinding.TypeKol.selectedItem.toString()

            if (name.isBlank() || quantityStr.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Некорректное количество", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                repository.addMaterial(name, quantity, category, typekol)
                Toast.makeText(this@MaterialView, "Материал сохранён", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}