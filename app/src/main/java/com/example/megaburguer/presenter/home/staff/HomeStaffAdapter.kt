package com.example.megaburguer.presenter.home.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.ItemTablesBinding

class HomeStaffAdapter(
    private val onTableClick: (table: Table, position: Int) -> Unit,
) : ListAdapter<Table, HomeStaffAdapter.MyViewHolder>(DIFF_CALLBACK) {

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
            ItemTablesBinding.inflate(
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
    inner class MyViewHolder(val binding: ItemTablesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(table: Table) {
            // Define o texto do número da mesa
            binding.txtTableNumber.text = table.number

            val context = binding.root.context


            if (table.status == TableStatus.CLOSED ) {
                binding.cardTable.strokeColor = ContextCompat.getColor(context, R.color.mega_burger_orange_strong)
                binding.cardTable.setOnClickListener {
                    onTableClick(table, adapterPosition)
                }

            } else {

                binding.cardTable.strokeColor = ContextCompat.getColor(context, R.color.mega_burger_gray)
                // Configura o clique do card da mesa
                binding.cardTable.setOnClickListener {
                    onTableClick(table, adapterPosition)
                }
            }


        }
    }
}