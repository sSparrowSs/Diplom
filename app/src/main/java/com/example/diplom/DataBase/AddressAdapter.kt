package com.example.diplom.DataBase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemAdressBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AddressAdapter(
    private var items: List<SendMaterial>,
    private val onEditClick: (SendMaterial) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AdressViewHolder>() {

    inner class AdressViewHolder(val binding: ItemAdressBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdressViewHolder {
        val binding = ItemAdressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdressViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textAdress.text = item.nameAddress?.ifBlank { "Без адреса" } ?: "Без адреса"
            textMaterial.text = item.material?.nameMaterial?.takeIf { it.isNotEmpty() } ?: "Без материала"
            textUnit.text = if (item.quantity > 0) item.quantity.toString() else "Не известно"

            val dateString = item.sentAt.ifBlank { null }

            val date = if (dateString != null) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                } catch (e1: Exception) {
                    try {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)
                    } catch (e2: Exception) {
                        null
                    }
                }
            } else null

            textTime.text = date?.let {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
            } ?: "Без даты"

            editMaterial.setOnClickListener {
                onEditClick(item)
            }
        }
    }


    override fun getItemCount(): Int = items.size

    fun updateData(newData: List<SendMaterial>) {
        items = newData
        notifyDataSetChanged()
    }

}