package com.example.megaburguer.presenter.home.waiter.createOrder.viewOrder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentViewOrderBinding


class ViewOrderFragment : Fragment() {

    private var _binding: FragmentViewOrderBinding? = null
    private val binding get() = _binding!!

    private val args: ViewOrderFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewOrderBinding.inflate(inflater, container, false)
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

    }

    private fun configInformation() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}