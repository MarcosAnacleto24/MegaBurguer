package com.example.megaburguer.presenter.home.admin.manage_menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.databinding.ItemManageMenuBinding
import com.example.megaburguer.util.GetMask

class ManageMenuAdapter(
    private val onDeleteClick: (menuId: String) -> Unit,
    private val onEditClick: (menu: Menu) -> Unit
) : ListAdapter<Menu, ManageMenuAdapter.MyViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Menu>() {
            override fun areItemsTheSame(oldItem: Menu, newItem: Menu): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Menu, newItem: Menu): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Cria o "molde" (ViewHolder) para cada item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemManageMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // Conecta os dados de uma mesa específica à sua representação visual
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val menuItem = getItem(position)
        holder.bind(menuItem)
    }

    // A classe interna que representa o "molde" de cada item
    inner class MyViewHolder(val binding: ItemManageMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menu: Menu) {

            binding.nameItem.text = menu.nameItem
            binding.txtNumber.text = binding.root.context.
            getString(R.string.txt_price_snack_manage_menu,
                GetMask.getFormatedValue(menu.price))


            binding.btnEdit.setOnClickListener {
                onEditClick(menu)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(menu.id)
            }

        }
    }
}