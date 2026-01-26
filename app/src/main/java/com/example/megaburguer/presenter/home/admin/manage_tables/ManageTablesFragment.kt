package com.example.megaburguer.presenter.home.admin.manage_tables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.FragmentManageTablesBinding
import com.example.megaburguer.util.BaseFragment
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageTablesFragment : BaseFragment() {

    private var _binding: FragmentManageTablesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageTablesViewModel by viewModels()

    private lateinit var manageTablesAdapter: ManageTablesAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageTablesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        configRecycleView()

        getTables()
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnManage.setOnClickListener {
            hideKeyboard()
            validateData()
        }


        binding.editChoiceTable.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                hideKeyboard()
                validateData()

                binding.editChoiceTable.clearFocus()

                return@setOnEditorActionListener true
            }
            false
        }

    }

    private fun configRecycleView() {
        manageTablesAdapter = ManageTablesAdapter { tableId ->
            showBottomSheet(
                message = getString(R.string.message_delete_table),
                titleButton = R.string.txt_btn_bottom_sheet_delete,
                onClick = {
                    deleteTable(tableId)
                }
            )

        }

        with(binding.recycleView){
            setHasFixedSize(true)
            adapter = manageTablesAdapter
        }

    }

    private fun validateData() {
        val numberTable = binding.editChoiceTable.text.toString().trim()

        if (numberTable.isNotEmpty()) {

            val table = Table(
                id = FirebaseDatabase.getInstance().reference.push().key ?: "",
                number = numberTable
            )
            saveTable(table)

        } else {
            showBottomSheet(message = getString(R.string.txt_description_add_table))
        }
    }

    private fun saveTable(table: Table) {
        viewModel.saveTable(table).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }
                is StateView.Success -> {
                    getTables()
                    Toast.makeText(requireContext(), getString(R.string.create_table_success), Toast.LENGTH_SHORT).show()
                    binding.editChoiceTable.text?.clear()
                }

                is StateView.Error -> {
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }
            }
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
                   manageTablesAdapter.submitList(stateView.data)
               }

               is StateView.Error -> {
                   binding.progressBar.isVisible = false
                   showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
               }
           }

        }
    }

    private fun deleteTable(tableId: String) {
        viewModel.deleteTable(tableId).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }
                is StateView.Success -> {

                    getTables()
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
        _binding = null
    }


}