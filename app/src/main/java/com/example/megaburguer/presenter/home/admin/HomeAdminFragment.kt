package com.example.megaburguer.presenter.home.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentHomeAdminBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeAdminFragment : Fragment() {

    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!

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

    }

    private fun initListeners() {
        binding.cardManageTables.setOnClickListener {

        }
        binding.cardRegisterUser.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_registerFragment)
        }
        binding.cardChangePassword.setOnClickListener {

        }
        binding.cardManageMenu.setOnClickListener {

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}