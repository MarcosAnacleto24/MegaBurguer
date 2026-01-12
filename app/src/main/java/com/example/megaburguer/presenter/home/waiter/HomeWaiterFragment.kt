package com.example.megaburguer.presenter.home.waiter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.databinding.FragmentHomeWaiterBinding
import com.example.megaburguer.util.FirebaseHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeWaiterFragment : Fragment() {

    private var _binding: FragmentHomeWaiterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeWaiterViewModel by viewModels()
    private lateinit var homeWaiterAdapter: HomeWaiterAdapter 


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeWaiterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initListeners()
        
        configRecycleView()
        
        getTables()
    }

    private fun initListeners() {

        binding.btnLogout.setOnClickListener {
            showBottomSheet(
                message = getString(R.string.msg_bottom_sheet_logout),
                titleButton = R.string.btn_bottom_sheet_logout,
                onClick = {
                    FirebaseHelper.getAuth().signOut()
                    findNavController().navigate(R.id.loginFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.homeWaiter, true).build())
                }
            )

        }

    }
    
    private fun configRecycleView() {
        homeWaiterAdapter = HomeWaiterAdapter { table ->

            val action = HomeWaiterFragmentDirections.actionHomeWaiterFragmentToCreateOrderFragment(table)
            findNavController().navigate(action)

        }

        with(binding.recyclerView){
            setHasFixedSize(true)
            adapter = homeWaiterAdapter
        }

        
    }
    
    private fun getTables() {
        viewModel.getTables().observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }
                
                is StateView.Success -> {
                    binding.progressBar.isVisible = false
                    homeWaiterAdapter.submitList(stateView.data)
                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
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