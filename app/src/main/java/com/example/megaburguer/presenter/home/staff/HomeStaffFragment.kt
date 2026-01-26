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

    // Controle para não processar o mesmo ID duas vezes
    private val processingOrderIds = java.util.Collections.synchronizedSet(HashSet<String>())

    // Mutex para garantir que uma impressão espere a outra terminar (Fila)
    private val printerMutex = kotlinx.coroutines.sync.Mutex()

    private val handler = android.os.Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {

            val currentList = homeStaffAdapter.currentList

            releaseTrappedTables(currentList)

            handler.postDelayed(this, 10000) // a cada 10 segundos
        }
    }

    // O Launcher que gerencia a resposta do usuário
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
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
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
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
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
                        showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
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
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
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

                    // Se a lista estiver vazia, limpamos nosso controle para evitar bugs futuros
                    if (orderList.isEmpty()) {
                        processingOrderIds.clear()
                    }

                    if (orderList.isNotEmpty() && hasBluetoothPermission()) {
                        processAndPrintOrders(orderList)
                    } else if (orderList.isNotEmpty() && !hasBluetoothPermission()) {
                        Toast.makeText(requireContext(), getString(R.string.txt_message_orders_line_staff), Toast.LENGTH_LONG).show()
                    }
                }

                is StateView.Error -> {
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))

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
    // Função que agrupa, imprime UM POR UM e deleta
    private fun processAndPrintOrders(allOrders: List<OrderItem>) {
        // FILTRAGEM INTELIGENTE
        // Pega apenas os pedidos que AINDA NÃO estão na lista de processamento
        val newOrders = allOrders.filter { !processingOrderIds.contains(it.id) }

        // Se não tem nada novo (ou seja, são só os pedidos repetidos do "Eco"), para aqui.
        if (newOrders.isEmpty()) return

        // Marca os novos como "Em Processamento" imediatamente
        newOrders.forEach { processingOrderIds.add(it.id) }

        // Agrupa só os novos
        val groupedOrders = newOrders.groupBy { it.idTable }

        lifecycleScope.launch(Dispatchers.IO) {
            // BLOQUEIO DE FILA (MUTEX)
            // Se já tiver uma impressão rodando, isso aqui espera ela terminar antes de começar.
            // Isso evita que duas threads tentem usar o Bluetooth ao mesmo tempo.
            printerMutex.lock()

            try {
                val printerHelper = PrinterHelper()

                groupedOrders.forEach { (_, items) ->
                    val tableNameStr = items.firstOrNull()?.nameTable ?: "0"
                    val tableNumberInt = tableNameStr.filter { it.isDigit() }.toIntOrNull() ?: 0

                    // Imprime
                    val result = printerHelper.printKitchenTicket(items, tableNumberInt)

                    if (result == "Success") {
                        withContext(Dispatchers.Main) {

                            // Deleta do banco
                            val idsToDelete = items.map { it.id }
                            viewModel.deletePrintedItems(idsToDelete).observe(viewLifecycleOwner) {}
                        }

                        // Pausa para cortar
                        try {
                            Thread.sleep(4000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    } else {
                        // EM CASO DE ERRO DE IMPRESSÃO
                        // Removemos os IDs da "Lista Negra" para que o app tente imprimir de novo na próxima atualização
                        items.forEach { processingOrderIds.remove(it.id) }

                        withContext(Dispatchers.Main) {
                            showBottomSheet(message = result)
                        }
                    }
                }
            } finally {
                // Libera a fila para a próxima leva de pedidos
                printerMutex.unlock()
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