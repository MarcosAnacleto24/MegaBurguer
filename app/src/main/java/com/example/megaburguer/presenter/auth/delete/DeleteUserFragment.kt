package com.example.megaburguer.presenter.auth.delete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.User
import com.example.megaburguer.databinding.FragmentDeleteUserBinding
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteUserFragment : Fragment() {
    private var _binding: FragmentDeleteUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeleteUserViewModel by viewModels()

    private val userList = mutableListOf<User>()

    private var selectedUser: User? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteUserBinding.inflate(inflater, container, false)
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
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, userNames)

        binding.deleteSelectUser.setAdapter(adapter)
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDeleteUser.setOnClickListener {
            validateData()
        }

        binding.deleteSelectUser.setOnItemClickListener { _, _, position, _ ->
            selectedUser = userList[position]

            binding.deleteSelectUser.setText(selectedUser?.name, false)
        }

    }

    private fun validateData() {
        val userName = binding.deleteSelectUser.text.toString().trim()


        if (selectedUser != null && userName == selectedUser?.name) {
            deleteUser(selectedUser!!.id)
        } else {
            showBottomSheet(message = getString(R.string.name_empty_delete_user))
        }



    }
    
    private fun deleteUser(uid: String) {
        viewModel.deleteUser(uid).observe(viewLifecycleOwner) { stateView -> 
            when(stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }
                
                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), getString(R.string.delete_user_success_message), Toast.LENGTH_SHORT).show()
                    binding.deleteSelectUser.setText("")
                    getUsers()
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