package com.example.megaburguer.presenter.home.staff.extract

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.ItemCreateOrdersBinding
import com.example.megaburguer.databinding.ItemExtractLineBinding
import com.example.megaburguer.databinding.ItemViewOrdersBinding
import com.example.megaburguer.util.GetMask

class ExtractAdapter() : ListAdapter<OrderItem,
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
    inner class MyViewHolder(val binding: ItemExtractLineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItem) {

            binding.txtQtyAndName.text = binding.root.context.getString(R.string.txt_quantity_item_extract_line,
                orderItem.quantity.toString(), orderItem.nameItem)

            binding.txtUnitPrice.text = binding.root.context.getString(
                R.string.txt_value_each_extract_line,
                GetMask.getFormatedValue(orderItem.price)
            )

            binding.txtLineTotal.text = binding.root.context.getString(
                R.string.txt_value_sub_total_extract_line,
                GetMask.getFormatedValue(orderItem.price * orderItem.quantity)
            )


        }
    }
}