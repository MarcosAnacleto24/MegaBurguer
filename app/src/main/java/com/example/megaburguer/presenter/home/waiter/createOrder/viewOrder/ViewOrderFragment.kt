package com.example.megaburguer.presenter.home.waiter.createOrder.viewOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.FragmentViewOrderBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.GetMask
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showViewObservationDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ViewOrderFragment : Fragment() {
    private var _binding: FragmentViewOrderBinding? = null
    private val binding get() = _binding!!
    private val args: ViewOrderFragmentArgs by navArgs()
    private val viewModel: ViewOrderViewModel by viewModels()
    private val sharedViewModel: SharedOrderViewModel by activityViewModels()
    private lateinit var viewOrderAdapter: ViewOrderAdapter
    private val itemQuantityMap = mutableMapOf<String, Int>() // id do item -> quantidade
    private val currentOrderItems = mutableListOf<OrderItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        configRecyclerView()

        configInformation()
    }

    private fun initListeners() {

        binding.back.setOnClickListener {
            findNavController().popBackStack()

        }

        binding.btnSendKitchen.setOnClickListener {
            val itemsUpdate = currentOrderItems.map { item ->
                val newQtd = itemQuantityMap[item.id] ?: item.quantity
                item.copy(quantity = newQtd)
            }

            saveOrderItemList(itemsUpdate)
        }

    }

    private fun configInformation() {

        currentOrderItems.clear()
        currentOrderItems.addAll(args.orderItems.toList())
        viewOrderAdapter.submitList(currentOrderItems.toList())

        val totalItems = currentOrderItems.sumOf { it.quantity }
        val totalPrice = currentOrderItems.sumOf { it.price.toLong() * it.quantity }

        binding.txtSubtitle.text = getString(R.string.txt_title_table, args.table.number)

        binding.txtTotalItemsNumber.text = totalItems.toString()

        binding.txtTotalValueReal.text = getString(R.string.txt_price_snack_manage_menu,
            GetMask.getFormatedValue(totalPrice.toFloat()))


        getUser()
    }

    private fun getUser() {
        viewModel.getUser(FirebaseHelper.getUserId()).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    binding.txtNameWaiter.text = stateView.data?.name
                }

                is StateView.Error -> {

                }

            }
        }

    }

    private fun saveOrderItemList(orderItemList: List<OrderItem>) {
        viewModel.saveOrderItemList(orderItemList).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    sharedViewModel.setTableStatus(args.table.id, TableStatus.OPEN)

                    findNavController().navigate(R.id.homeWaiter, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeWaiter, false).build())
                }

                is StateView.Error -> {

                }
            }
        }
    }

    private fun configRecyclerView() {
        viewOrderAdapter = ViewOrderAdapter(
            onRemoveItemClick = { orderItem, position ->
                itemQuantityMap.remove(orderItem.id)
                currentOrderItems.removeAt(position)
                viewOrderAdapter.submitList(currentOrderItems.toList())
                updateTotals()
            },

            onViewObservationClick = { orderItem ->
                showViewObservationDialog(
                    nameItem = orderItem.nameItem,
                    priceItem = orderItem.price,
                    observationItem = orderItem.observation,
                )

            },

            onMoreClick = { orderItem, position -> onMoreItem(orderItem, position) },

            onLessClick = { orderItem, position -> onLessItem(orderItem, position) },

            quantityMap = itemQuantityMap
        )

        with(binding.recycleView) {
            setHasFixedSize(true)
            adapter = viewOrderAdapter
        }
    }

    private fun onMoreItem(orderItem: OrderItem, position: Int) {
        val currentQtd = itemQuantityMap[orderItem.id] ?: orderItem.quantity
        itemQuantityMap[orderItem.id] = currentQtd + 1
        viewOrderAdapter.notifyItemChanged(position)

        updateTotals()
    }

    private fun onLessItem(orderItem: OrderItem, position: Int) {
        val currentQtd = itemQuantityMap[orderItem.id] ?: orderItem.quantity
        if (currentQtd > 1) {
            itemQuantityMap[orderItem.id] = currentQtd - 1
            viewOrderAdapter.notifyItemChanged(position)
        }

        updateTotals()
    }

    private fun updateTotals() {

        // Atualize as quantidades conforme o itemQuantityMap
        val itemsUpdate = currentOrderItems.map { item ->
            val newQtd = itemQuantityMap[item.id] ?: item.quantity
            item.copy(quantity = newQtd)
        }

        val totalItems = itemsUpdate.sumOf { it.quantity }
        val totalPrice = itemsUpdate.sumOf { it.price.toLong() * it.quantity }

        binding.txtTotalItemsNumber.text = totalItems.toString()
        binding.txtTotalValueReal.text = getString(
            R.string.txt_price_snack_manage_menu,
            GetMask.getFormatedValue(totalPrice.toFloat())
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}