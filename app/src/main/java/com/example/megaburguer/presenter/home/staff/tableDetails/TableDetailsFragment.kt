package com.example.megaburguer.presenter.home.staff.tableDetails

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.FragmentTableDetailsBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.GetMask
import com.example.megaburguer.util.PrinterHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TableDetailsFragment : Fragment() {

    private var _binding: FragmentTableDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TableDetailsViewModel by viewModels()
    private lateinit var tableDetailsAdapter: TableDetailsAdapter
    private val args: TableDetailsFragmentArgs by navArgs()
    private val itemQuantityMap = mutableMapOf<String, Int>() // id do item -> quantidade
    private val currentOrderItems = mutableListOf<OrderItem>()
    private val sharedViewModel: SharedOrderViewModel by activityViewModels()
    private var orderSent = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTableDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        getOrderList(args.table.id)

        configRecyclerView()

    }

    private fun initListeners() {

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCloseAccountSaveExtract.setOnClickListener {
            if (currentOrderItems.isNotEmpty()) {
                val itemsUpdate = currentOrderItems.map { item ->
                    val newQtd = itemQuantityMap[item.id] ?: item.quantity
                    item.copy(quantity = newQtd)
                }

                saveExtractList(itemsUpdate)

            } else {
                showBottomSheet(message = getString(R.string.txt_message_bottom_sheet_table_details))
            }
        }

        binding.btnPrint.setOnClickListener {
            if (currentOrderItems.isNotEmpty()) {
                // VERIFICAÇÃO SIMPLES
                if (hasBluetoothPermission()) {
                    // Tem permissão? Prepara a lista e imprime
                    val itemsUpdate = currentOrderItems.map { item ->
                        val newQtd = itemQuantityMap[item.id] ?: item.quantity
                        item.copy(quantity = newQtd)
                    }
                    printOrder(itemsUpdate)
                } else {
                    // Não tem permissão? Manda o usuário voltar ou avisa
                    showBottomSheet(message = getString(R.string.txt_message_not_permission_bluetooth))
                }

            } else {
                showBottomSheet(message = getString(R.string.txt_message_print_bottom_sheet_table_details))
            }
        }


    }

    private fun getOrderList(tableId: String) {
        viewModel.getOrderList(tableId).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    currentOrderItems.clear()
                    currentOrderItems.addAll(stateView.data ?: emptyList())
                    tableDetailsAdapter.submitList(currentOrderItems.toList())

                    validateData(currentOrderItems)

                    configInformation()
                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    private fun validateData(orderListItem: List<OrderItem>) {

        if (orderListItem.isEmpty()) {
            binding.txtInfo.text = getString(R.string.txt_info_table_details)

        } else {
            binding.txtInfo.isVisible = false
        }

    }

    private fun configInformation() {

        val waitersList = currentOrderItems.map { it.nameWaiter }.distinct()
        val waitersString = waitersList.joinToString(separator = ", ")
        val totalItems = currentOrderItems.sumOf { it.quantity }
        val totalPrice = currentOrderItems.sumOf { it.price.toLong() * it.quantity }

        binding.txtNameWaiter.text = waitersString

        binding.txtTitle.text = getString(R.string.txt_title_table_details, args.table.number)

        binding.txtTotalItemsNumber.text = totalItems.toString()

        binding.txtTotalValueReal.text = getString(R.string.txt_price_snack_manage_menu,
            GetMask.getFormatedValue(totalPrice.toFloat()))

    }

    private fun configRecyclerView() {
        tableDetailsAdapter = TableDetailsAdapter(
            onRemoveItemClick = { orderItem, position ->
                itemQuantityMap.remove(orderItem.id)
                currentOrderItems.removeAt(position)
                tableDetailsAdapter.submitList(currentOrderItems.toList())
                updateTotals()
            },

            onMoreClick = { orderItem, position -> onMoreItem(orderItem, position) },

            onLessClick = { orderItem, position -> onLessItem(orderItem, position) },

            quantityMap = itemQuantityMap
        )

        with(binding.recycleView) {
            setHasFixedSize(true)
            adapter = tableDetailsAdapter
        }
    }

    private fun onMoreItem(orderItem: OrderItem, position: Int) {
        val currentQtd = itemQuantityMap[orderItem.id] ?: orderItem.quantity
        itemQuantityMap[orderItem.id] = currentQtd + 1
        tableDetailsAdapter.notifyItemChanged(position)

        updateTotals()
    }

    private fun onLessItem(orderItem: OrderItem, position: Int) {
        val currentQtd = itemQuantityMap[orderItem.id] ?: orderItem.quantity
        if (currentQtd > 1) {
            itemQuantityMap[orderItem.id] = currentQtd - 1
            tableDetailsAdapter.notifyItemChanged(position)
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

    private fun hasBluetoothPermission(): Boolean {
        // Se for Android antigo (< 12), sempre retorna true (permissão é dada na instalação)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        // Se for Android novo (12+), verifica se foi concedida
        val connectGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        val scanGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        return connectGranted && scanGranted
    }


    private fun printOrder(orderListItem: List<OrderItem>) {
        binding.progressBar.isVisible = true
        // 1. Verifique permissões antes (especialmente Bluetooth no Android 12)
        // Se já tiver permissão:

        val total = orderListItem.sumOf {  it.price.toDouble() * it.quantity }

        // Roda em uma thread de IO (Background)
        lifecycleScope.launch(Dispatchers.IO) {

            // Chama o helper que você criou (que retorna String)
            val result = PrinterHelper().printBluetooth(orderListItem, total)

            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false

                if (result == "Success") {
                    Toast.makeText(requireContext(), getString(R.string.txt_message_send_success), Toast.LENGTH_SHORT).show()
                } else {
                    showBottomSheet(message = result)
                }
            }

        }
    }

    private fun saveExtractList(orderItemList: List<OrderItem>) {
        viewModel.saveExtractList(orderItemList).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {

                    closeAccount()

                }

                is StateView.Error -> {
                    stateView.message?.let {
                        showBottomSheet(message = it)
                    }
                }
            }
        }
    }

    private fun closeAccount(idTable: String = args.table.id) {
        viewModel.deleteOrderItem(idTable).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {


                    val bundle = Bundle().apply {
                        putBoolean("account_closed", true)
                        putString("table_name", getString(R.string.txt_title_table, args.table.number))
                    }

                    // Define o resultado para a tela anterior pegar
                    setFragmentResult("close_request", bundle)

                    orderSent = true

                    sharedViewModel.setTableStatus(idTable, TableStatus.OPEN)

                    // Navega de volta
                    findNavController().popBackStack()

                }

                is StateView.Error -> {
                    stateView.message?.let {
                        showBottomSheet(message = it)
                    }
                }

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (!orderSent) {
            sharedViewModel.setTableStatus(args.table.id, TableStatus.OPEN)

        }
        _binding = null
    }


}