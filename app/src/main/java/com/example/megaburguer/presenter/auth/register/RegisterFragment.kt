package com.example.megaburguer.presenter.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentRegisterBinding
import com.example.megaburguer.util.BaseFragment
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseFragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configDropdown()

        initListeners()


    }

    private fun configDropdown() {
        // As opções que você quer mostrar no menu
        val userTypes = arrayOf("Garçom", "Administrador", "Cozinha/Caixa")

        // O adapter que conecta as opções ao componente
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, userTypes)

        // Conecta o adapter ao seu AutoCompleteTextView
        binding.editTypeUser.setAdapter(adapter)
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            validateData()
        }


    }

    private fun validateData() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val typeUser = binding.editTypeUser.text.toString()

        when {
            name.isEmpty() -> showBottomSheet(message = getString(R.string.name_empty_register))
            email.isEmpty() -> showBottomSheet(message = getString(R.string.email_empty_register))
            password.isEmpty() -> showBottomSheet(message = getString(R.string.password_empty_register))
            typeUser.isEmpty() -> showBottomSheet(message = getString(R.string.type_user_empty_register))

            else -> {
                hideKeyboard()
                registerUser(name, email, password, typeUser)
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String, typeUser: String) {
        viewModel.register(name, email, password, typeUser).observe(viewLifecycleOwner) { stateView -> 
            
            when(stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }
                
                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    FirebaseHelper.getAuth().signOut()
                    findNavController().navigate(R.id.loginFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeAdmin, true).build())
                    
                }
                
                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = getString((stateView.stringResId ?: R.string.error_generic)))
                }
            }
            
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}