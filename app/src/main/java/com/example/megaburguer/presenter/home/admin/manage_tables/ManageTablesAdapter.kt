package com.example.megaburguer.presenter.home.admin.manage_tables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.ItemManageTablesBinding

class ManageTablesAdapter(
    private val onDeleteClick: (tableId: String) -> Unit
) : ListAdapter<Table, ManageTablesAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Table>() {
            override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Cria o "molde" (ViewHolder) para cada item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemManageTablesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // Conecta os dados de uma mesa específica à sua representação visual
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val table = getItem(position)
        holder.bind(table)
    }

    // A classe interna que representa o "molde" de cada item
    inner class MyViewHolder(val binding: ItemManageTablesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(table: Table) {
            // Define o texto do número da mesa
            binding.txtNumber.text = table.number

            // Configura o clique do botão de delete
            binding.btnDelete.setOnClickListener {
                onDeleteClick(table.id)
            }
        }
    }
}