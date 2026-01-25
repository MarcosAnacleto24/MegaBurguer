package com.example.megaburguer.presenter.auth.recover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.User
import com.example.megaburguer.databinding.FragmentRecoverBinding
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverFragment : Fragment() {
    private var _binding: FragmentRecoverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecoverViewModel by viewModels()

    private val userList = mutableListOf<User>()

    private var selectedUser: User? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUsers()

        initListeners()


    }

    private fun getUsers() {
        viewModel.getUsers().observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    userList.clear()
                    userList.addAll(stateView.data ?: emptyList())
                    configDropdown()
                }

                is StateView.Error -> {
                    showBottomSheet(message = stateView.message ?:getString(R.string.error_generic) )
                }
            }
        }
    }

    private fun configDropdown() {


        val userNames = userList.map { it.name }

        val adapter =
            ArrayAdapter(requireContext(), R.layout.item_dropdown, userNames)

        binding.editSelectUser.setAdapter(adapter)
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRecover.setOnClickListener {
            validateData()
        }

        binding.editSelectUser.setOnItemClickListener { _, _, position, _ ->
            selectedUser = userList[position]

            binding.editSelectUser.setText(selectedUser?.name, false)
        }

    }

    private fun validateData() {
        val userName = binding.editSelectUser.text.toString().trim()


        if (selectedUser != null && userName == selectedUser?.name) {
            recoverAccountUser(selectedUser!!.email)
        } else {
            showBottomSheet(message = getString(R.string.name_empty_recover))
        }



    }

    private fun recoverAccountUser(email: String) {
        viewModel.recover(email).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {

                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = getString(R.string.link_recover_password))
                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = stateView.message ?:getString(R.string.error_generic) )
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}