package com.example.diplom

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Typeface
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.DataBase.SendMaterial
import com.example.diplom.DataBase.AddressAdapter
import com.example.diplom.DataBase.Material
import com.example.diplom.DataBase.Provider
import com.example.diplom.DataBase.ReceiveAdapter
import com.example.diplom.DataBase.ReceiveMaterial
import com.example.diplom.DataBase.Repository
import com.example.diplom.databinding.ActivityLayoutMaterialBinding
import com.example.diplom.databinding.BottomSheetRecSendBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.text.contains

class LayoutMaterial : AppCompatActivity() {
    private lateinit var binding: ActivityLayoutMaterialBinding
    private lateinit var sendAdapter: AddressAdapter
    private lateinit var repository: Repository
    private lateinit var adapter: ReceiveAdapter
    private lateinit var realm: Realm
    private val selectedFilters = mutableSetOf<String>()
    private var allReceiveMaterial = listOf<ReceiveMaterial>()
    private var allSendMaterial = listOf<SendMaterial>()

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

        val materialName = intent.getStringExtra("material_name") ?: ""

        // Инициализируем Realm и репозиторий
        val config = RealmConfiguration.Builder(
            schema = setOf(Material::class, Provider::class, ReceiveMaterial::class, SendMaterial::class)
        ).build()
        realm = Realm.open(config)
        repository = Repository(realm)

        // Инициализируем адаптеры с пустыми списками
        adapter = ReceiveAdapter(emptyList()) { receiveMaterial ->
            showBottomSheetDialogEdit(receiveMaterial)
        }

        sendAdapter = AddressAdapter(emptyList()) { sendMaterial ->
            showBottomSheetDialogEditSend(sendMaterial)
        }


        // Устанавливаем LayoutManager и первый адаптер (например, receive)
        binding.ViewReceiveMaterial.layoutManager = LinearLayoutManager(this)
        binding.ViewReceiveMaterial.adapter = adapter

        setupSpinner(materialName)
        observeMaterials(materialName)
        observeSendMaterials(materialName)


        binding.ButtonReceive.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.textReceive.setOnClickListener {
            binding.textReceive.setTypeface(null, Typeface.BOLD)
            binding.textSend.setTypeface(null, Typeface.NORMAL)
            binding.ViewReceiveMaterial.adapter = adapter
        }

        binding.textSend.setOnClickListener {
            binding.textSend.setTypeface(null, Typeface.BOLD)
            binding.textReceive.setTypeface(null, Typeface.NORMAL)
            binding.ViewReceiveMaterial.adapter = sendAdapter
        }

        binding.ViewAll.setOnClickListener {
            adapter.updateData(allReceiveMaterial)
            sendAdapter.updateData(allSendMaterial)
            binding.ViewAll.visibility = View.INVISIBLE
        }

    }

    private fun observeSendMaterials(materialName: String) {
        lifecycleScope.launch {
            repository.getAllSendMaterials().collectLatest { sendMaterials ->
                allSendMaterial = sendMaterials
                val filtered = sendMaterials.filter {
                    it.material?.nameMaterial == materialName
                }
                sendAdapter.updateData(filtered)
            }
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

    private fun filterMaterialList(materialName: String) {
        val filtered = allReceiveMaterial.filter { receiveMaterial ->
            val providerName = receiveMaterial.provider?.nameProvider ?: ""
            val matchesMaterial = receiveMaterial.material?.nameMaterial == materialName
            val matchesProvider = selectedFilters.isEmpty() || selectedFilters.any { filter ->
                providerName.contains(filter, ignoreCase = true)
            }

            matchesMaterial && matchesProvider
        }

        adapter.updateData(filtered)
    }


    private fun setupSpinner(materialName: String) {
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

                    filterMaterialList(materialName)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetRecSendBinding.inflate(layoutInflater)

        val calendar = Calendar.getInstance()
        var selectedDateTime: String? = null

        sheetBinding.editDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this@LayoutMaterial, { _, selectedYear, selectedMonth, selectedDay ->
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val timePicker = TimePickerDialog(this@LayoutMaterial, { _, selectedHour, selectedMinute ->
                    selectedDateTime = String.format(
                        "%02d.%02d.%d %02d:%02d",
                        selectedDay, selectedMonth + 1, selectedYear,
                        selectedHour, selectedMinute
                    )
                    sheetBinding.editDate.setText(selectedDateTime)
                }, hour, minute, true)

                timePicker.show()
            }, year, month, day)

            datePicker.show()
        }

        sheetBinding.TextHead.text = "Добавить получение"
        sheetBinding.DeleteIcon.visibility = View.INVISIBLE
        dialog.setContentView(sheetBinding.root)

        val kol = listOf("шт", "м", "м²", "м³")
        val spinnerAdapterKol = ArrayAdapter(this, android.R.layout.simple_spinner_item, kol)
        spinnerAdapterKol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetBinding.TypeKol.adapter = spinnerAdapterKol

        lifecycleScope.launch {
            // Загружаем имена материалов из базы
            val materialNames = realm.query(Material::class).find().map { it.nameMaterial }
            val spinnerAdapterMaterial = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, materialNames)
            spinnerAdapterMaterial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.MaterialSpinner.adapter = spinnerAdapterMaterial

            // Загружаем поставщиков
            val providers = realm.query(Provider::class).find().map { it.nameProvider }
            val post = listOf("Неизвестно") + providers

            val spinnerAdapterPost = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, post)
            spinnerAdapterPost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.Postavshik.adapter = spinnerAdapterPost

            sheetBinding.ButtonSave.setOnClickListener {
                val name = sheetBinding.MaterialSpinner.selectedItem?.toString()?.trim() ?: ""
                val quantityStr = sheetBinding.MaterialQuantity.text.toString().trim()
                val postavshik = sheetBinding.Postavshik.selectedItem.toString()
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
                    repository.addMaterialReceive(name, quantity, postavshik, typekol, selectedDateTime ?: "Без даты")
                    Toast.makeText(this@LayoutMaterial, "Материал сохранён", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    private fun showBottomSheetDialogEdit(material: ReceiveMaterial) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetRecSendBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.TextHead.text = "Редактировать получение"
        sheetBinding.ButtonSave.text = "Изменить"
        sheetBinding.DeleteIcon.visibility = View.VISIBLE
        sheetBinding.TypeKol.visibility = View.GONE // скрываем, так как поля больше нет

        lifecycleScope.launch {
            val materialNames = realm.query(Material::class).find().map { it.nameMaterial }
            val spinnerAdapterMaterial = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, materialNames)
            spinnerAdapterMaterial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.MaterialSpinner.adapter = spinnerAdapterMaterial

            val providers = realm.query(Provider::class).find().map { it.nameProvider }
            val post = listOf("Неизвестно") + providers

            val spinnerAdapterPost = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, post)
            spinnerAdapterPost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.Postavshik.adapter = spinnerAdapterPost

            val materialName = material.material?.nameMaterial ?: ""
            val providerName = material.provider?.nameProvider ?: "Неизвестно"

            sheetBinding.MaterialQuantity.setText(material.quantity.toString())
            sheetBinding.editDate.setText(material.receivedAt)
            sheetBinding.MaterialSpinner.setSelection(materialNames.indexOf(materialName).coerceAtLeast(0))
            sheetBinding.Postavshik.setSelection(post.indexOf(providerName).coerceAtLeast(0))

            sheetBinding.ButtonSave.setOnClickListener {
                val updatedName = sheetBinding.MaterialSpinner.selectedItem.toString()
                val updatedQuantity = sheetBinding.MaterialQuantity.text.toString().toIntOrNull() ?: 0
                val updatedProvider = sheetBinding.Postavshik.selectedItem.toString()
                val updatedDate = sheetBinding.editDate.text.toString()

                if (updatedName.isBlank() || updatedQuantity <= 0 || updatedDate.isBlank()) {
                    Toast.makeText(this@LayoutMaterial, "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    repository.updateMaterialReceive(material, updatedName, updatedQuantity, updatedProvider, updatedDate)
                    Toast.makeText(this@LayoutMaterial, "Материал обновлён", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            sheetBinding.DeleteIcon.setOnClickListener {
                lifecycleScope.launch {
                    repository.deleteMaterialReceive(material)
                    Toast.makeText(this@LayoutMaterial, "Материал удалён", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    private fun showBottomSheetDialogEditSend(sendMaterial: SendMaterial) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetRecSendBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.TextHead.text = "Редактировать отправку"
        sheetBinding.ButtonSave.text = "Изменить"
        sheetBinding.DeleteIcon.visibility = View.VISIBLE
        sheetBinding.TypeKol.visibility = View.GONE
        sheetBinding.Postavshik.visibility = View.GONE // здесь нет поставщика
        sheetBinding.MaterialSpinner.visibility = View.INVISIBLE // здесь нет поставщика
        sheetBinding.labelPost.visibility = View.GONE

        lifecycleScope.launch {
            val materialNames = realm.query(Material::class).find().map { it.nameMaterial }
            val spinnerAdapterMaterial = ArrayAdapter(this@LayoutMaterial, android.R.layout.simple_spinner_item, materialNames)
            spinnerAdapterMaterial.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetBinding.MaterialSpinner.adapter = spinnerAdapterMaterial

            val materialName = sendMaterial.material?.nameMaterial ?: ""

            sheetBinding.MaterialQuantity.setText(sendMaterial.quantity.toString())
            sheetBinding.editDate.setText(sendMaterial.sentAt)
            sheetBinding.MaterialSpinner.setSelection(materialNames.indexOf(materialName).coerceAtLeast(0))

            // Название адреса вместо поставщика
            sheetBinding.editAddress.visibility = View.VISIBLE
            sheetBinding.editAddress.setText(sendMaterial.nameAddress)

            sheetBinding.ButtonSave.setOnClickListener {
                val updatedName = sheetBinding.MaterialSpinner.selectedItem.toString()
                val updatedQuantity = sheetBinding.MaterialQuantity.text.toString().toIntOrNull() ?: 0
                val updatedDate = sheetBinding.editDate.text.toString()
                val updatedAddress = sheetBinding.editAddress.text.toString()

                if (updatedName.isBlank() || updatedQuantity <= 0 || updatedDate.isBlank() || updatedAddress.isBlank()) {
                    Toast.makeText(this@LayoutMaterial, "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    repository.updateMaterialSend(sendMaterial, updatedName, updatedQuantity, updatedAddress, updatedDate)
                    Toast.makeText(this@LayoutMaterial, "Отправка обновлена", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            sheetBinding.DeleteIcon.setOnClickListener {
                lifecycleScope.launch {
                    repository.deleteMaterialSend(sendMaterial)
                    Toast.makeText(this@LayoutMaterial, "Отправка удалена", Toast.LENGTH_SHORT).show()
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