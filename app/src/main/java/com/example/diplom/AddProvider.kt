package com.example.diplom

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.diplom.DataBase.Repository
import com.example.diplom.LayoutMaterial
import com.example.diplom.databinding.ActivityAddProviderBinding
import kotlinx.coroutines.launch

class AddProvider : AppCompatActivity() {
    private lateinit var binding: ActivityAddProviderBinding
    private lateinit var repository: Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddProviderBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.AddProvider.setOnClickListener {
            AddProviderr()
        }

    }

    private fun AddProviderr() {

        val name = binding.NameProvider.text.toString()

        lifecycleScope.launch {
            repository.addProvider(name)
            Toast.makeText(this@AddProvider, "Провайдер сохранён", Toast.LENGTH_SHORT).show()
        }
    }

}