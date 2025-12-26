package com.example.megaburguer.presenter.home.waiter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentHomeWaiterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeWaiterFragment : Fragment() {


    private var _binding: FragmentHomeWaiterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeWaiterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}