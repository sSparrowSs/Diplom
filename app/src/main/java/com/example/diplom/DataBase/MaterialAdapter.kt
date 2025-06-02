package com.example.diplom.DataBase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemMaterialBinding

class MaterialAdapter(
    private var items: List<Material>,
    private val onEditClick: (Material) -> Unit
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    inner class MaterialViewHolder(val binding: ItemMaterialBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            nameText.text = item.nameMaterial
            Type.text = item.category
            quantityText.text = "${item.quantity} ${item.unit}"

            editMaterial.setOnClickListener {
                onEditClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Material>) {
        items = newItems
        notifyDataSetChanged()
    }
}