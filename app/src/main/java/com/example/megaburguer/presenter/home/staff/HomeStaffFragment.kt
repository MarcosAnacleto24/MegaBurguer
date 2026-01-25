package com.example.megaburguer.presenter.home.staff

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.FragmentHomeStaffBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.PrinterHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HomeStaffFragment : Fragment() {

    private var _binding: FragmentHomeStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeStaffAdapter: HomeStaffAdapter
    private val viewModel: HomeStaffViewModel by viewModels()

    private val sharedViewModel: SharedOrderViewModel by activityViewModels()

    val tenMinutes: Long = 10L * 60L * 1000L

    private val handler = android.os.Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {

            val currentList = homeStaffAdapter.currentList

            releaseTrappedTables(currentList)

            handler.postDelayed(this, 10000) // a cada 10 segundos
        }
    }

    // 1. O Launcher que gerencia a resposta do usuário
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permissão aceita!
            // Aqui você pode iniciar a lógica de "Escutar pedidos para imprimir" se quiser
            Toast.makeText(requireContext(), getString(R.string.txt_message_permission_yes_staff), Toast.LENGTH_SHORT).show()
        } else {
            // Permissão negada
            showBottomSheet(message = getString(R.string.txt_message_permission_not_staff))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAndRequestPermissions()

        initListeners()

        getUser()

        configRecycleView()

        getTables()

        observeTables()

        observeOrderPrint()

        openTable()

    }

    private fun initListeners() {

        binding.btnLogout.setOnClickListener {
            showBottomSheet(
                message = getString(R.string.msg_bottom_sheet_logout),
                titleButton = R.string.btn_bottom_sheet_logout,
                onClick = {
                    FirebaseHelper.getAuth().signOut()
                    findNavController().navigate(
                        R.id.loginFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeStaff, true).build()
                    )
                }
            )

        }

        binding.btnViewExtract.setOnClickListener {
            findNavController().navigate(R.id.action_homeStaffFragment_to_extractFragment)
        }

        // Escuta se veio algum resultado da tela anterior
        setFragmentResultListener("close_request") { _, bundle ->
            val closed = bundle.getBoolean("account_closed")
            val tableName = bundle.getString("table_name")
            if (closed) {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.txt_message_close_account_success_table_details, tableName),
                    Toast.LENGTH_SHORT
                ).show()

            }

            val extractClean = bundle.getBoolean("extract_clean")
            val extractMessage = bundle.getString("extract_message")
            if (extractClean) {
                Toast.makeText(requireContext(), extractMessage, Toast.LENGTH_SHORT).show()
            }

        }

    }


    private fun getUser() {
        viewModel.getUser(FirebaseHelper.getUserId()).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    binding.textGreeting.text =
                        getString(R.string.txt_greeting_staff, stateView.data?.name)
                }

                is StateView.Error -> {
                    binding.textGreeting.text = getString(R.string.txt_greeting_waiter_sub)
                }

            }
        }

    }

    private fun configRecycleView() {
        homeStaffAdapter = HomeStaffAdapter(
            onTableClick = { table, position ->
                if (table.status == TableStatus.OPEN) {

                    homeStaffAdapter.notifyItemChanged(position)
                    updateTableStatus(table.id, TableStatus.CLOSED, FirebaseHelper.getUserId())
                    val action =
                        HomeStaffFragmentDirections.actionHomeStaffFragmentToTableDetailsFragment(
                            table
                        )
                    findNavController().navigate(action)

                } else if (table.lockedBy == FirebaseHelper.getUserId()) {

                    homeStaffAdapter.notifyItemChanged(position)
                    updateTableStatus(table.id, TableStatus.CLOSED, FirebaseHelper.getUserId())
                    val action =
                        HomeStaffFragmentDirections.actionHomeStaffFragmentToTableDetailsFragment(
                            table
                        )
                    findNavController().navigate(action)

                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.txt_table_busy_home_waiter),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        )

        with(binding.recyclerView) {
            setHasFixedSize(true)
            adapter = homeStaffAdapter
        }


    }

    private fun getTables() {
        viewModel.getTables().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {

                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    homeStaffAdapter.submitList(stateView.data)
                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                    stateView.message?.let {
                        showBottomSheet(message = it)
                    }
                }

            }

        }
    }

    private fun releaseTrappedTables(tables: List<Table>) {
        val now = System.currentTimeMillis()
        tables.forEach { table ->
            if (table.status == TableStatus.CLOSED && (now - table.lastUpdated > tenMinutes)) {
                updateTableStatus(table.id, TableStatus.OPEN)
            }
        }
    }

    private fun updateTableStatus(tableId: String, newStatus: TableStatus, userId: String = "") {
        viewModel.updateTableStatus(tableId, newStatus, userId)
            .observe(viewLifecycleOwner) { stateView ->
                when (stateView) {
                    is StateView.Loading -> {

                    }

                    is StateView.Success -> {

                    }

                    is StateView.Error -> {
                        stateView.message?.let {
                            showBottomSheet(message = it)
                        }
                    }

                }
            }
    }

    private fun observeTables() {
        viewModel.observeTables().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    homeStaffAdapter.submitList(stateView.data)
                }

                is StateView.Error -> {

                }
            }

        }
    }

    private fun openTable() {
        sharedViewModel.tableStatusEvent.observe(viewLifecycleOwner) { pair ->
            pair?.let { (tableId, status) ->
                updateTableStatus(tableId, status)
                sharedViewModel.consumeEvent() // Limpa o evento após consumir
            }
        }
    }

    private fun observeOrderPrint() {
        viewModel.observeOrderPrint().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {

                    val orderList = stateView.data ?: emptyList()

                    // Só processa se tiver itens e se tiver permissão
                    if (orderList.isNotEmpty() && hasBluetoothPermission()) {
                        processAndPrintOrders(orderList)
                    } else if (orderList.isNotEmpty() && !hasBluetoothPermission()) {
                        // Opcional: Avisar que tem pedidos mas sem permissão
                        Toast.makeText(requireContext(), getString(R.string.txt_message_orders_line_staff), Toast.LENGTH_LONG).show()
                    }
                }

                is StateView.Error -> {


                }
            }

        }
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

    private fun checkAndRequestPermissions() {
        // Só faz sentido pedir permissão em tempo de execução no Android 12 (S) ou superior
        // Em versões antigas, a permissão é dada na instalação do app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val missingConnect = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED

            val missingScan = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED

            if (missingConnect || missingScan) {
                // É AQUI QUE O LAUNCHER É CHAMADO
                requestBluetoothPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        }
    }

    // Função que agrupa e manda imprimir
    private fun processAndPrintOrders(allOrders: List<OrderItem>) {
        val groupedOrders = allOrders.groupBy { it.idTable }

        groupedOrders.forEach { (tableId, items) ->
            // Manda imprimir este grupo específico
            printOrderGroup(tableId, items)
        }
    }

    private fun printOrderGroup(tableId: String, items: List<OrderItem>) {
        // Calculo do total desse grupo
        val total = items.sumOf { it.price.toDouble() * it.quantity }

        lifecycleScope.launch(Dispatchers.IO) {
            val printerHelper = PrinterHelper()

            // Dica: Você pode modificar o PrinterHelper para aceitar o "tableId" e imprimir no cabeçalho "Mesa: X"
            val result = printerHelper.printBluetooth(items, total)

            withContext(Dispatchers.Main) {
                if (result == "Success") {
                    Toast.makeText(requireContext(), "Mesa $tableId impressa!", Toast.LENGTH_SHORT).show()

                    // 2. SUCESSO? MANDAR DELETAR APENAS ESSES ITENS
                    val idsToDelete = items.map { it.id } // Pega os IDs do Firebase
                    viewModel.deletePrintedItems(idsToDelete).observe(viewLifecycleOwner) {
                        // Pode observar o resultado da deleção se quiser, mas geralmente não precisa fazer nada
                    }
                } else {
                    // Falhou a impressão? Não deleta. Assim tentará de novo na próxima atualização.
                    //showBottomSheet(message = "Erro ao imprimir Mesa $tableId: $result")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
        _binding = null
    }


}