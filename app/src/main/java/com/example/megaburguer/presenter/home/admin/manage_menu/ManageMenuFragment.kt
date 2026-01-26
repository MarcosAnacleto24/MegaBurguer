package com.example.megaburguer.presenter.home.admin.manage_menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.megaburguer.R
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.databinding.FragmentManageMenuBinding
import com.example.megaburguer.util.BaseFragment
import com.example.megaburguer.util.MoneyTextWatcher
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageMenuFragment : BaseFragment() {
    private var _binding: FragmentManageMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageMenuViewModel by viewModels()
    private lateinit var manageMenuAdapter: ManageMenuAdapter
    private val fullMenuList = mutableListOf<Menu>()
    private lateinit var typeCategory: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            configDropdown()

            initListeners()

            configRecycleView()

            getMenus()
    }

    private fun configDropdown() {
        // As opções que você quer mostrar no menu
        val userTypes = arrayOf("Hambúrgueres", "Porções", "Bebidas", "Combos")

        // O adapter que conecta as opções ao componente
        val adapter =
            ArrayAdapter(requireContext(), R.layout.item_dropdown, userTypes)

        // Conecta o adapter ao seu AutoCompleteTextView
        binding.editCategory.setAdapter(adapter)
    }

    private fun initListeners() {
        binding.editPrice.addTextChangedListener(MoneyTextWatcher(binding.editPrice, 1000.00f))

        // Listener para as categorias
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            // Como é singleSelection, a lista terá apenas um ID
            if (checkedIds.isNotEmpty()) {
                updateFilteredList()
            }
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddItem.setOnClickListener {
            hideKeyboard()
            validateData()
        }

        // Escuta se veio algum resultado da tela anterior
        setFragmentResultListener("close_request") { _, bundle ->
            val itemUpdated = bundle.getBoolean("item_updated")
            val itemMessage = bundle.getString("item_message")
            if (itemUpdated) {

                Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()

            }

            val extractClean = bundle.getBoolean("extract_clean")
            val extractMessage = bundle.getString("extract_message")
            if (extractClean) {
                Toast.makeText(requireContext(), extractMessage, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun configRecycleView() {
        manageMenuAdapter = ManageMenuAdapter(
            onDeleteClick = { menuId ->
                showBottomSheet(
                    message = getString(R.string.message_delete_menu),
                    titleButton = R.string.txt_btn_bottom_sheet_delete,
                    onClick = {
                        deleteMenu(menuId)
                    }
                )
            },

            onEditClick = { menu ->
                val action = ManageMenuFragmentDirections.actionManageMenuFragmentToUpdateManageMenuFragment(menu)
                findNavController().navigate(action)

            }
        )

        with(binding.recycleView) {
            setHasFixedSize(true)
            adapter = manageMenuAdapter
        }
    }

    private fun validateData() {
        val nameItem = binding.editChoiceTable.text.toString().trim()
        val price = MoneyTextWatcher.getValueUnMasked(binding.editPrice)
        val category = binding.editCategory.text.toString().trim()



        when {
            nameItem.isEmpty() -> showBottomSheet(message = getString(R.string.txt_name_item_empty))
            price <= 0f -> showBottomSheet(message = getString(R.string.txt_price_empty))
            category.isEmpty()  -> showBottomSheet(message = getString(R.string.txt_category_empty) )

            else -> {

                val menuItem = Menu(
                    id = FirebaseDatabase.getInstance().reference.push().key ?: "",
                    nameItem = nameItem,
                    price = price,
                    category = category
                )

                saveMenu(menuItem)

            }
        }
    }

    private fun saveMenu(menu: Menu) {
        viewModel.saveMenu(menu).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }

                is StateView.Success -> {
                    getMenus()
                    Toast.makeText(requireContext(), getString(R.string.txt_message_add_success_manage_table), Toast.LENGTH_SHORT).show()
                    binding.editChoiceTable.text?.clear()
                    binding.editPrice.text?.clear()
                }

                is StateView.Error -> {

                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }
            }
        }

    }

    private fun getMenus() {
        viewModel.getMenus().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.Success -> {
                    binding.progressBar.isVisible = false

                    fullMenuList.clear()
                    fullMenuList.addAll(stateView.data ?: emptyList())

                    updateFilteredList()

                }

                is StateView.Error -> {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }

            }
        }
    }

    private fun deleteMenu(menuId: String) {
        viewModel.deleteMenu(menuId).observe(viewLifecycleOwner) { stateView ->
            when(stateView) {
                is StateView.Loading -> {

                }


                is StateView.Success -> {
                    getMenus()
                }

                is StateView.Error -> {
                    showBottomSheet(message = stateView.message ?: getString(R.string.error_generic))
                }
            }
        }
    }

    private fun updateFilteredList() {

        val selectedChipId = binding.chipGroupCategories.checkedChipId

        when(selectedChipId) {

            R.id.chip_burgers -> {
                typeCategory = "Hambúrgueres"
            }

            R.id.chip_portions -> {
                typeCategory = "Porções"
            }

            R.id.chip_drinks -> {
                typeCategory = "Bebidas"
            }

            R.id.chip_combos -> {
                typeCategory = "Combos"
            }

        }

        binding.txtTitleItems.text = typeCategory

        // Filtra a lista completa de itens
        val filteredList = fullMenuList.filter { menu ->
            menu.category.equals(typeCategory, ignoreCase = true)
        }

        // Envia a nova lista filtrada para o adapter
        manageMenuAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}