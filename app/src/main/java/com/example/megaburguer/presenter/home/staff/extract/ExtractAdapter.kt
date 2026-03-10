package com.example.megaburguer.presenter.home.staff.extract

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.R
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.ItemExtractLineBinding
import com.example.megaburguer.util.toCurrency

class ExtractAdapter : ListAdapter<OrderItem,
        ExtractAdapter.MyViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Cria o "molde" (ViewHolder) para cada item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemExtractLineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // Conecta os dados de uma mesa específica à sua representação visual
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem)
    }

    // A classe interna que representa o "molde" de cada item
    class MyViewHolder(val binding: ItemExtractLineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItem) {

            binding.txtQtyAndName.text = binding.root.context.getString(R.string.txt_quantity_item_extract_line,
                orderItem.quantity.toString(), orderItem.nameItem)

            binding.txtUnitPrice.text = binding.root.context.getString(
                R.string.txt_value_each_extract_line,
                orderItem.price.toCurrency()
            )

            val totalLineCents = orderItem.price * orderItem.quantity

            binding.txtLineTotal.text = totalLineCents.toCurrency()


        }
    }
}