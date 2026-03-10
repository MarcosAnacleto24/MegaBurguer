package com.example.megaburguer.util

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.megaburguer.R
import com.example.megaburguer.databinding.LayoutBottomSheetBinding
import com.example.megaburguer.databinding.LayoutBottomSheetObservationBinding
import com.example.megaburguer.databinding.LayoutBottomSheetViewObservationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale

fun Fragment.showBottomSheet(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String,
    onClick: () -> Unit = {}
) {

    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val bottomSheetBinding: LayoutBottomSheetBinding =
        LayoutBottomSheetBinding.inflate(layoutInflater, null, false)

    bottomSheetBinding.txtTitle.text = getString(titleDialog ?: R.string.title_bottom_sheet)
    bottomSheetBinding.txtMessage.text = message
    bottomSheetBinding.btnOk.text = getString(titleButton ?: R.string.btn_bottom_sheet)

    bottomSheetBinding.btnOk.setOnClickListener {
        onClick()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()


}

fun Fragment.showObservationDialog(
    nameItem: String,
    priceItem: Long,
    onSaveClick: (String) -> Unit,
    themeResId: Int = android.R.style.Theme_Material_Light_Dialog // ou outro tema se quiser
) {
    val dialog = Dialog(requireContext(), themeResId)
    val binding = LayoutBottomSheetObservationBinding.inflate(layoutInflater, null, false)

    binding.nameItem.text = nameItem
    binding.txtPrice.text = priceItem.toCurrency()

    binding.btnSave.setOnClickListener {
        onSaveClick(binding.editObservation.text.toString())
        dialog.dismiss()
    }
    binding.btnCancel.setOnClickListener { dialog.dismiss() }
    binding.btnClose.setOnClickListener { dialog.dismiss() }

    dialog.setContentView(binding.root)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    // Foca o EditText e abre o teclado automaticamente
    binding.editObservation.requestFocus()
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(binding.editObservation, InputMethodManager.SHOW_IMPLICIT)

    dialog.show()
}

fun Fragment.showViewObservationDialog(
    nameItem: String,
    priceItem: Long,
    observationItem: String,
    themeResId: Int = android.R.style.Theme_Material_Light_Dialog // ou outro tema se quiser
) {
    val dialog = Dialog(requireContext(), themeResId)
    val binding = LayoutBottomSheetViewObservationBinding.inflate(layoutInflater, null, false)

    binding.nameItem.text = nameItem
    binding.txtPrice.text = priceItem.toCurrency()
    binding.editObservation.setText(observationItem)


    binding.btnSave.setOnClickListener {
        dialog.dismiss()
    }

    binding.btnClose.setOnClickListener { dialog.dismiss() }

    dialog.setContentView(binding.root)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.show()
}


fun Fragment.showBottomSheetModal(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String,
    onClose: () -> Unit // Essa ação roda SEMPRE que o dialog fechar
) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val bottomSheetBinding = LayoutBottomSheetBinding.inflate(layoutInflater, null, false)

    bottomSheetBinding.txtTitle.text = getString(titleDialog ?: R.string.title_bottom_sheet)
    bottomSheetBinding.txtMessage.text = message
    bottomSheetBinding.btnOk.text = getString(titleButton ?: R.string.btn_bottom_sheet)


    bottomSheetBinding.btnOk.setOnClickListener {
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setOnDismissListener {
        onClose()
    }

    // Configurações padrão para impedir que o usuário cancele tocando fora (opcional, mas recomendado para login)
    bottomSheetDialog.setCanceledOnTouchOutside(true) // Pode deixar true, pois o dismiss vai jogar pro login de qualquer jeito

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()
}


fun Fragment.hideKeyboard() {
    val view = activity?.currentFocus
    if (view != null) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Long.toCurrency(): String {
    val nf = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("pt").setRegion("BR").build()
    )
    // Dividimos por 100.0 apenas na hora de formatar para a String de exibição
    return nf.format(this / 100.0)
}

fun Int.toCurrency(): String = this.toLong().toCurrency()
