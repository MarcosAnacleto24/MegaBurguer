package com.example.megaburguer.presenter.home.admin.manage_menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentManageMenuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageMenuFragment : Fragment() {
    private var _binding: FragmentManageMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}