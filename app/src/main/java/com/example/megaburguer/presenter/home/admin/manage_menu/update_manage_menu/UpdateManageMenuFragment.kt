package com.example.megaburguer.presenter.home.admin.manage_menu.update_manage_menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.databinding.FragmentUpdateManageMenuBinding
import com.example.megaburguer.util.BaseFragment
import com.example.megaburguer.util.MoneyTextWatcher
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class UpdateManageMenuFragment : BaseFragment() {

    private var _binding: FragmentUpdateManageMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UpdateManageMenuViewModel by viewModels()

    private val args: UpdateManageMenuFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateManageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configSafeArgs()

        configDropdown()

        initListeners()
    }

    private fun configSafeArgs() {
        binding.editChoiceTable.setText(args.menu.nameItem)

        // Formata o preço inicial usando a mesma lógica do MoneyTextWatcher para garantir consistência
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(args.menu.price)
        binding.editPrice.setText(formattedPrice)

        binding.editCategory.setText(args.menu.category)
    }

    private fun configDropdown() {
        // As opções que você quer mostrar no menu
        val userTypes = arrayOf("Entradas", "Hambúrgueres", "Bebidas", "Combos")

        // O adapter que conecta as opções ao componente
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, userTypes)

        // Conecta o adapter ao seu AutoCompleteTextView
        binding.editCategory.setAdapter(adapter)
    }

    private fun initListeners() {

        binding.editPrice.addTextChangedListener(MoneyTextWatcher(binding.editPrice, 1000.00f))

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnUpdateItem.setOnClickListener {
            hideKeyboard()
            validateData()
        }
    }

    private fun validateData() {
        val nameItem = binding.editChoiceTable.text.toString().trim()
        val price = MoneyTextWatcher.getValueUnMasked(binding.editPrice)
        val category = binding.editCategory.text.toString().trim()

        when {
            nameItem.isEmpty() -> showBottomSheet(message = getString(R.string.txt_update_name_item_empty))
            price <= 0f -> showBottomSheet(message = getString(R.string.txt_update_price_empty))
            category.isEmpty()  -> showBottomSheet(message = getString(R.string.txt_update_category_empty) )

            else -> {
                val menuItem = Menu(
                    id = args.menu.id,
                    nameItem = nameItem,
                    price = price,
                    category = category
                )
                updateMenu(menuItem)
            }
        }
    }

    private fun updateMenu(menu: Menu) {
        viewModel.updateMenu(menu).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false

                    val bundle = Bundle().apply {
                        putBoolean("item_updated", true)
                        putString("item_message", getString(R.string.txt_message_add_success_update_manage_table))
                    }

                    // Define o resultado para a tela anterior pegar
                    setFragmentResult("close_request", bundle)

                    findNavController().popBackStack()
                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
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