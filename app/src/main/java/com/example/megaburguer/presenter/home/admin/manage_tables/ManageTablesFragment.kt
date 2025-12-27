package com.example.megaburguer.presenter.home.admin.manage_tables

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.databinding.FragmentManageTablesBinding
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageTablesFragment : Fragment() {

    private var _binding: FragmentManageTablesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageTablesViewModel by viewModels()


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
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnManage.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        val numberTable = binding.editChoiceTable.text.toString().trim()

        if (numberTable.isNotEmpty()) {

            val table = Table(number = numberTable)
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
                    Toast.makeText(requireContext(), "ok", Toast.LENGTH_SHORT).show()
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