package com.example.diplom

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.example.diplom.databinding.ActivityMaterialViewBinding

class MaterialView : AppCompatActivity() {
    private lateinit var binding: ActivityMaterialViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMaterialViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinner()

        binding.Sort.setOnClickListener() {
            binding.Sort.visibility = INVISIBLE
        }

    }

    private fun setupSpinner() {
        // Создаем список элементов для Spinner
        val sortOptions = listOf("Все материалы", "Стены", "Пол", "Потолок")

        // Создаем адаптер
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        // Указываем макет для выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Устанавливаем адаптер для Spinner
        binding.SpinnerSortView.adapter = adapter

        // Обработчик выбора элемента
        binding.SpinnerSortView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { binding.Sort.visibility = INVISIBLE }
                    1 -> { binding.Sort.visibility = VISIBLE }
                    2 -> { /* Действие для "Пол" */ }
                    3 -> { /* Действие для "Потолок" */ }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Действие при отсутствии выбора
            }
        }
    }

}