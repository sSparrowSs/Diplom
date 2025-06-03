package com.example.diplom.DataBase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemProviderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiveAdapter(
    private var items: List<ReceiveMaterial>,
    private val onEditClick: (ReceiveMaterial) -> Unit
) : RecyclerView.Adapter<ReceiveAdapter.ReceiveViewHolder>() {

    inner class ReceiveViewHolder(val binding: ItemProviderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiveViewHolder {
        val binding = ItemProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiveViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textProvider.text = item.provider?.nameProvider?.takeIf { it.isNotEmpty() } ?: "Без поставщика"
            textMaterial.text = item.material?.nameMaterial?.takeIf { it.isNotEmpty() } ?: "Без материала"
            textUnit.text = if (item.quantity > 0) item.quantity.toString() else "Не известно"

            val dateString = item.receivedAt.ifBlank { null }

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

    fun updateData(newItems: List<ReceiveMaterial>) {
        items = newItems
        notifyDataSetChanged()
    }
}