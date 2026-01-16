package com.example.megaburguer.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.megaburguer.R
import com.example.megaburguer.databinding.LayoutBottomSheetBinding
import com.example.megaburguer.databinding.LayoutBottomSheetObservationBinding
import com.example.megaburguer.databinding.LayoutBottomSheetViewObservationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
    priceItem: Float,
    onSaveClick: (String) -> Unit,
    themeResId: Int = android.R.style.Theme_Material_Light_Dialog // ou outro tema se quiser
) {
    val dialog = Dialog(requireContext(), themeResId)
    val binding = LayoutBottomSheetObservationBinding.inflate(layoutInflater, null, false)

    binding.nameItem.text = nameItem
    binding.txtPrice.text = getString(
        R.string.txt_price_bottom_sheet_observation,
        GetMask.getFormatedValue(priceItem)
    )

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
    priceItem: Float,
    observationItem: String,
    themeResId: Int = android.R.style.Theme_Material_Light_Dialog // ou outro tema se quiser
) {
    val dialog = Dialog(requireContext(), themeResId)
    val binding = LayoutBottomSheetViewObservationBinding.inflate(layoutInflater, null, false)

    binding.nameItem.text = nameItem
    binding.txtPrice.text = getString(
        R.string.txt_price_bottom_sheet_observation,
        GetMask.getFormatedValue(priceItem)
    )
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