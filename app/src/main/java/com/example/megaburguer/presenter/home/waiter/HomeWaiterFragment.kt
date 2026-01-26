package com.example.megaburguer.presenter.home.waiter

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.FragmentHomeWaiterBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class HomeWaiterFragment : Fragment() {

    private var _binding: FragmentHomeWaiterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeWaiterViewModel by viewModels()
    private lateinit var homeWaiterAdapter: HomeWaiterAdapter
    private val sharedViewModel: SharedOrderViewModel by activityViewModels()
    private lateinit var nameUser: String
    val tenMinutes: Long = 10L * 60L * 1000L
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {

            val currentList = homeWaiterAdapter.currentList

            releaseTrappedTables(currentList)
            handler.postDelayed(this, 10000) // a cada 10 segundos
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeWaiterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        getUser()
        
        configRecycleView()

        getTables()

        observeTables()

        openTable()

    }

    private fun initListeners() {

        binding.btnLogout.setOnClickListener {
            showBottomSheet(
                message = getString(R.string.msg_bottom_sheet_logout),
                titleButton = R.string.btn_bottom_sheet_logout,
                onClick = {
                    FirebaseHelper.getAuth().signOut()
                    findNavController().navigate(R.id.loginFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeWaiter, true).build())
                }
            )

        }

    }

    private fun getUser() {
        viewModel.getUser(FirebaseHelper.getUserId()).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    binding.textGreeting.text = getString(R.string.txt_greeting_waiter, stateView.data?.name)
                    nameUser = stateView.data?.name.toString()
                }

                is StateView.Error -> {
                    binding.textGreeting.text = getString(R.string.txt_greeting_waiter_sub)
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }

            }
        }

        // Escuta se veio algum resultado da tela anterior
        setFragmentResultListener("close_request") { _, bundle ->
            val orderSend = bundle.getBoolean("send_success")
            val orderMessage = bundle.getString("order_print_message")
            if (orderSend) {

                Toast.makeText(requireContext(), orderMessage, Toast.LENGTH_SHORT).show()

            }

            val extractClean = bundle.getBoolean("extract_clean")
            val extractMessage = bundle.getString("extract_message")
            if (extractClean) {
                Toast.makeText(requireContext(), extractMessage, Toast.LENGTH_SHORT).show()
            }

        }

    }
    
    private fun configRecycleView() {
        homeWaiterAdapter = HomeWaiterAdapter (
            onTableClick = { table, position ->
                if (table.status == TableStatus.OPEN) {

                    homeWaiterAdapter.notifyItemChanged(position)
                    updateTableStatus(table.id, TableStatus.CLOSED, FirebaseHelper.getUserId())
                    val action = HomeWaiterFragmentDirections.actionHomeWaiterFragmentToCreateOrderFragment(table, nameUser)
                    findNavController().navigate(action)

                } else if(table.lockedBy == FirebaseHelper.getUserId()) {

                    homeWaiterAdapter.notifyItemChanged(position)
                    updateTableStatus(table.id, TableStatus.CLOSED, FirebaseHelper.getUserId())
                    val action = HomeWaiterFragmentDirections.actionHomeWaiterFragmentToCreateOrderFragment(table, nameUser)
                    findNavController().navigate(action)

                } else {
                    Toast.makeText(requireContext(), getString(R.string.txt_table_busy_home_waiter), Toast.LENGTH_SHORT).show()
                }

            }

        )

        with(binding.recyclerView){
            setHasFixedSize(true)
            adapter = homeWaiterAdapter
        }

        
    }
    
    private fun getTables() {
        viewModel.getTables().observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }
                
                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    homeWaiterAdapter.submitList(stateView.data)
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
        viewModel.updateTableStatus(tableId, newStatus, userId).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
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
        viewModel.observeTables().observe(viewLifecycleOwner){ stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    homeWaiterAdapter.submitList(stateView.data)

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

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }


    override fun onDestroyView() {
        handler.removeCallbacks(refreshRunnable)
        super.onDestroyView()
        _binding = null
    }


}