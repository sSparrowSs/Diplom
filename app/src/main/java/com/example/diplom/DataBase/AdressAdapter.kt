package com.example.diplom.DataBase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemAdressBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdressAdapter(
    private var items: List<Adress>,
    private val onEditClick: (Adress) -> Unit
) : RecyclerView.Adapter<AdressAdapter.AdressViewHolder>() {

    inner class AdressViewHolder(val binding: ItemAdressBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdressViewHolder {
        val binding = ItemAdressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdressViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textAdress.text = item.nameAdress.ifEmpty { "Без адреса" }
            textUnit.text = item.unitAdress.ifEmpty { "Без материала" }

            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = try {
                formatter.parse(item.sendDate)
            } catch (e: Exception) {
                null
            }
            textTime.text = date?.let { formatter.format(it) } ?: "Без даты"


            editMaterial.setOnClickListener {
                onEditClick(item)
            }
        }
    }


    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Adress>) {
        items = newItems
        notifyDataSetChanged()
    }
}