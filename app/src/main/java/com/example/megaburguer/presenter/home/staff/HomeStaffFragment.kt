package com.example.megaburguer.presenter.home.staff

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.FragmentHomeStaffBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

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

        initListeners()

        printOrders()

        getUser()

        configRecycleView()

        getTables()

        observeTables()

        openTable()

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

    }

    private fun printOrders() {

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