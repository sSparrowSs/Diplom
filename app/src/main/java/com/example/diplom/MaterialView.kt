package com.example.diplom

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
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.MaterialAdapter
import com.example.diplom.DataBase.Repository
import com.example.diplom.databinding.ActivityMaterialViewBinding
import com.example.diplom.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MaterialView : AppCompatActivity() {
    private lateinit var binding: ActivityMaterialViewBinding
    private lateinit var adapter: MaterialAdapter
    private lateinit var config: RealmConfiguration
    private lateinit var realm: Realm
    private lateinit var repository: Repository

    private val selectedFilters = mutableSetOf<String>()
    private var allMaterials = listOf<Material>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMaterialViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(binding.root)

        config = RealmConfiguration.Builder(
            schema = setOf(Material::class)
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
            showBottomSheetDialogEdit(material)
        }
        binding.ViewMaterial.layoutManager = LinearLayoutManager(this)
        binding.ViewMaterial.adapter = adapter



        observeMaterials()

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

    private fun observeMaterials() {
        lifecycleScope.launch {
            repository.getAllMaterials().collectLatest { materials ->
                allMaterials = materials
                filterMaterialList(binding.SearchMaterial.query.toString())
            }
        }
    }


    private fun filterMaterialList(query: String) {
        val filtered = allMaterials.filter { material ->
            val matchesFilter = selectedFilters.isEmpty() || selectedFilters.any { filter ->
                material.category.contains(filter, ignoreCase = true)
            }
            val matchesQuery = material.name.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }

        adapter.updateData(filtered)
    }


    private fun setupSpinner() {
        val sortOptions = listOf("Все типы", "Конструкция", "Отделка", "Утеплители и изоляция", "Электрика", "Крепеж и монтаж", "Сантехника")

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
                    selectedFilters.add(selected)
                    binding.Sort.visibility = View.VISIBLE
                    binding.Sort.text = selectedFilters.joinToString(", ") + " ×"
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

        val categories = listOf("Конструкция", "Отделка", "Утеплители и изоляция", "Электрика", "Крепеж и монтаж", "Сантехника")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetBinding.Category.adapter = spinnerAdapter

        sheetBinding.ButtonSave.setOnClickListener {
            val name = sheetBinding.MaterialName.text.toString().trim()
            val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
            val category = sheetBinding.Category.selectedItem.toString()

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
                repository.addMaterial(name, quantity, category, "шт")
                Toast.makeText(this@MaterialView, "Материал сохранён", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showBottomSheetDialogEdit(material: Material) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        sheetBinding.TextHead.text = "Изменить"
        sheetBinding.ButtonSave.text = "Изменить"
        sheetBinding.DeleteIcon.visibility = View.VISIBLE

        dialog.setContentView(sheetBinding.root)

        val categories = listOf("Конструкция", "Отделка", "Утеплители и изоляция", "Электрика", "Крепеж и монтаж", "Сантехника")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetBinding.Category.adapter = spinnerAdapter

        // Предзаполнение полей
        sheetBinding.MaterialName.setText(material.name)
        sheetBinding.MaterialQuantity.setText(material.quantity.toString())
        val categoryIndex = categories.indexOf(material.category)
        if (categoryIndex >= 0) sheetBinding.Category.setSelection(categoryIndex)

        sheetBinding.ButtonSave.setOnClickListener {
            val name = sheetBinding.MaterialName.text.toString().trim()
            val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
            val category = sheetBinding.Category.selectedItem.toString()

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
                repository.updateMaterial(name, quantity, category, "шт")
                Toast.makeText(this@MaterialView, "Материал обновлён", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        sheetBinding.DeleteIcon.setOnClickListener {
            val name = sheetBinding.MaterialName.text.toString().trim()
            val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
            val category = sheetBinding.Category.selectedItem.toString()
            val quantity = quantityStr.toIntOrNull()

            showDeleteConfirmationDialog {
                lifecycleScope.launch {
                if (quantity != null) {

                    repository.deleteMaterial(name, quantity, category, "шт")
                    Toast.makeText(this@MaterialView, "Материал удален", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        val dialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.MyAlertDialogTheme))
            .setTitle("Удалить материал")
            .setMessage("Вы уверены, что хотите удалить?")
            .setPositiveButton("Удалить") { dialogInterface, _ ->
                onConfirm()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)
        }

        dialog.show()
    }



    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}