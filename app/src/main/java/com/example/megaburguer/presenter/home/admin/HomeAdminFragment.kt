package com.example.megaburguer.presenter.home.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentHomeAdminBinding
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeAdminFragment : Fragment() {

    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeAdminViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        getUser()

    }

    private fun initListeners() {

        binding.btnLogout.setOnClickListener {
            showBottomSheet(
                message = getString(R.string.msg_bottom_sheet_logout),
                titleButton = R.string.btn_bottom_sheet_logout,
                onClick = {
                    FirebaseHelper.getAuth().signOut()
                    findNavController().navigate(R.id.loginFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeAdmin, true).build())
                }
            )

        }

        binding.cardManageTables.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_manageTablesFragment)
        }
        binding.cardRegisterUser.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_registerFragment)
        }
        binding.cardChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_recoverFragment)
        }
        binding.cardDeleteUser.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_deleteUserFragment)
        }
        binding.cardManageMenu.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_manageMenuFragment)
        }
    }

    private fun getUser() {
        viewModel.getUser(FirebaseHelper.getUserId()).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    binding.textGreeting.text = getString(R.string.txt_greeting_admin, stateView.data?.name)

                }

                is StateView.Error -> {
                    binding.textGreeting.text = getString(R.string.txt_greeting_admin_sub)
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }

            }
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}