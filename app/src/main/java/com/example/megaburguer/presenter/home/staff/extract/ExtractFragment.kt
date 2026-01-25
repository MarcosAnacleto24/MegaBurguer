package com.example.megaburguer.presenter.home.staff.extract

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.FragmentExtractBinding
import com.example.megaburguer.util.GetMask
import com.example.megaburguer.util.PrinterHelper
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ExtractFragment : Fragment() {

    private var _binding: FragmentExtractBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExtractViewModel by viewModels()

    private lateinit var extractAdapter: ExtractAdapter

    private val extractList = mutableListOf<OrderItem>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtractBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        getExtract()

        configRecyclerView()

    }

    private fun initListeners() {

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPrint.setOnClickListener {
            if (extractList.isNotEmpty()) {
                if (hasBluetoothPermission()) {
                    printExtract()
                } else {
                    showBottomSheet(message = getString(R.string.txt_message_not_permission_bluetooth))
                }
            } else {
                showBottomSheet(message = getString(R.string.txt_message_print_bottom_sheet_extract))
            }

        }

        binding.btnCleanExtract.setOnClickListener {
            if (extractList.isNotEmpty()) {
                cleanExtract()
            } else {
                showBottomSheet(message = getString(R.string.txt_message_bottom_sheet_extract))
            }
        }

    }

    private fun getExtract() {
        viewModel.getExtract().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false

                    extractList.clear()
                    extractList.addAll(stateView.data ?: emptyList())
                    extractAdapter.submitList(extractList)

                    validateDate()

                    configInformation()
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

    private fun configRecyclerView() {
        extractAdapter = ExtractAdapter()

        with(binding.recyclerView) {
            setHasFixedSize(true)
            adapter = extractAdapter
        }

    }

    private fun validateDate() {

        if (extractList.isNotEmpty()) {
            binding.cardExtract.isVisible = true
            binding.txtInfo.isVisible = false
        } else {
            binding.cardExtract.isVisible = false
            binding.txtInfo.text = getString(R.string.txt_info_extract)
        }



    }

    private fun configInformation() {
        val ptBr = Locale.forLanguageTag("pt-BR")
        val date = SimpleDateFormat("dd/MM/yyyy - HH:mm", ptBr).format(Date())
        val totalPrice = extractList.sumOf { it.price.toLong() * it.quantity }

        binding.txtDate.text = date
        binding.txtPriceTotal.text = getString(R.string.txt_value_sub_total_extract_line,
            GetMask.getFormatedValue(totalPrice.toFloat()))
    }

    private fun hasBluetoothPermission(): Boolean {
        // Se for Android antigo (< 12), sempre retorna true (permissão é dada na instalação)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        // Se for Android novo (12+), verifica se foi concedida
        val connectGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        val scanGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        return connectGranted && scanGranted
    }

    private fun cleanExtract() {
        viewModel.deleteExtract().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    val bundle = Bundle().apply {
                        putBoolean("extract_clean", true)
                        putString("extract_message", getString(R.string.txt_extract_clean_success))
                    }

                    // Define o resultado para a tela anterior pegar
                    setFragmentResult("close_request", bundle)

                    findNavController().popBackStack()
                }

                is StateView.Error -> {

                    stateView.message?.let {
                        showBottomSheet(message = it)
                    }
                }
            }

        }
    }

    private fun printExtract() {
        binding.progressBar.isVisible = true
        // 1. Verifique permissões antes (especialmente Bluetooth no Android 12)
        // Se já tiver permissão:

        val total = extractList.sumOf {  it.price.toDouble() * it.quantity }

        // Roda em uma thread de IO (Background)
        lifecycleScope.launch(Dispatchers.IO) {

            // Chama o helper que você criou (que retorna String)
            val result = PrinterHelper().printBluetooth(extractList, total)

            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false

                if (result == "Success") {
                    Toast.makeText(requireContext(), getString(R.string.txt_message_send_success), Toast.LENGTH_SHORT).show()
                } else {
                    showBottomSheet(message = result)
                }
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}