package com.example.diplom

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diplom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация Spinner и добавление пунктов
        setupSpinner()
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
                    1 -> { /* Действие для "Все материалы" */ }
                    2 -> { /* Действие для "Стены" */ }
                    3 -> { /* Действие для "Пол" */ }
                    4 -> { /* Действие для "Потолок" */ }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Действие при отсутствии выбора
            }
        }
    }
}