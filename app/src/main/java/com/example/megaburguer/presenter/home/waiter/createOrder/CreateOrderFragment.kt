package com.example.megaburguer.presenter.home.waiter.createOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.megaburguer.R
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.databinding.FragmentCreateOrderBinding
import com.example.megaburguer.presenter.home.SharedOrderViewModel
import com.example.megaburguer.util.StateView
import com.example.megaburguer.util.showBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateOrderFragment : Fragment() {
    private var _binding: FragmentCreateOrderBinding? = null
    private val binding get() = _binding!!
    private val args: CreateOrderFragmentArgs by navArgs()
    private lateinit var createOrderAdapter: CreateOrderAdapter

    private val viewModel: CreateOrderViewModel by viewModels()
    private val fullMenuList = mutableListOf<Menu>()

    private val itemQuantityMap = mutableMapOf<String, Int>() // id do item -> quantidade

    private lateinit var typeCategory: String

    private val sharedViewModel: SharedOrderViewModel by activityViewModels()

    private var orderSent = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        configTitleOrder()

        configRecycleView()

        getMenus()
    }

    private fun initListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        // Listener para as categorias
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            // Como é singleSelection, a lista terá apenas um ID
            if (checkedIds.isNotEmpty()) {
                updateFilteredList()
            }
        }

        binding.btnViewOrders.setOnClickListener {
            val orderItems = itemQuantityMap
                .filter { it.value > 0 }
                .map { (id, qtd) ->
                    val menu = fullMenuList.find { it.id == id }
                    OrderItem(
                        id = id,
                        nameItem = menu?.nameItem ?: "",
                        price = menu?.price ?: 0f,
                        quantity = qtd
                    )
                }

            if (orderItems.isNotEmpty()) {
                val action = CreateOrderFragmentDirections.actionCreateOrderFragmentToViewOrderFragment(orderItems.toTypedArray())
                findNavController().navigate(action)
            } else {
                showBottomSheet(message = getString(R.string.message_empty_order))
            }

        }
    }

    private fun configTitleOrder() {
        binding.txtTitle.text = getString(R.string.txt_title_create_order, args.table.number)
    }

    private fun configRecycleView() {
        createOrderAdapter = CreateOrderAdapter(
            onAddItemClick = { menu, position -> onAddItem(menu, position)  },
            quantityMap = itemQuantityMap

        )

        with(binding.recycleView) {
            setHasFixedSize(true)
            adapter = createOrderAdapter
        }

    }

    private fun onAddItem(menu: Menu, position: Int) {
        val currentQtd = itemQuantityMap[menu.id] ?: 0
        itemQuantityMap[menu.id] = currentQtd + 1
        createOrderAdapter.notifyItemChanged(position)
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
                    stateView.message?.let {
                        showBottomSheet(message = it)
                    }
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

        // Filtra a lista completa de itens
        val filteredList = fullMenuList.filter { menu ->
            menu.category.equals(typeCategory, ignoreCase = true)
        }

        // Envia a nova lista filtrada para o adapter
        createOrderAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!orderSent) {
            sharedViewModel.setTableStatus(args.table.id, TableStatus.OPEN)
        }
        _binding = null
    }


}