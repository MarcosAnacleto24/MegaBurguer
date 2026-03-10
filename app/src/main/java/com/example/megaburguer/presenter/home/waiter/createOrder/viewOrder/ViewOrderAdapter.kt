package com.example.megaburguer.presenter.home.waiter.createOrder.viewOrder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.R
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.ItemViewOrdersBinding
import com.example.megaburguer.util.toCurrency

class ViewOrderAdapter(
    private val onRemoveItemClick: (orderItem: OrderItem, position: Int) -> Unit,
    private val onViewObservationClick: (orderItem: OrderItem) -> Unit,
    private val onMoreClick: (orderItem: OrderItem, position: Int) -> Unit,
    private val onLessClick: (orderItem: OrderItem, position: Int) -> Unit,
    private val quantityMap: Map<String, Int>,

) : ListAdapter<OrderItem, ViewOrderAdapter.MyViewHolder>(DIFF_CALLBACK) {
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
            ItemViewOrdersBinding.inflate(
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
    inner class MyViewHolder(val binding: ItemViewOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItem) {

            binding.btnViewObs.isVisible = orderItem.observation.isNotEmpty()

            binding.nameItem.text = orderItem.nameItem

            binding.txtValueEach.text = binding.root.context.
            getString(R.string.txt_value_each_view_order,
                orderItem.price.toCurrency())



            // Descobrimos a quantidade atual (ou do Map ou do próprio item)
            val currentQuantity = quantityMap[orderItem.id] ?: orderItem.quantity
            binding.txtQuantityItem.text = currentQuantity.toString()

            // Fazemos o cálculo do Subtotal (Preço em centavos * Quantidade)
            val subTotalCents = orderItem.price * currentQuantity

            // Exibimos usando a nova extensão .toCurrency() que criamos
            binding.txtValueSubTotal.text = binding.root.context.getString(
                R.string.txt_value_sub_total_view_order,
                subTotalCents.toCurrency()
            )


            binding.btnRemove.setOnClickListener {
                onRemoveItemClick(orderItem, adapterPosition)
            }

            binding.btnViewObs.setOnClickListener {
                onViewObservationClick(orderItem)

            }

            binding.btnMore.setOnClickListener {
                onMoreClick(orderItem, adapterPosition)
            }

            binding.btnLess.setOnClickListener {
                onLessClick(orderItem, adapterPosition)
            }


        }
    }
}