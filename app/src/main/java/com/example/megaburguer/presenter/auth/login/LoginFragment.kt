package com.example.megaburguer.presenter.auth.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentLoginBinding
import com.example.megaburguer.util.StateView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initListeners()
        
    }
    
    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            validateData()
        }
    }
    
    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        
        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                login(email,password)
            } else {
                Toast.makeText(requireContext(), "preencha uma senha", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "preencha um email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String) {
        viewModel.login(email, password).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show()
                }
                
                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                }    
            }
        }

    
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}