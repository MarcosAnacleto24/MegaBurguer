package com.example.megaburguer.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class MoneyTextWatcher(
    private val editText: EditText,
    private val maxValue: Float = 1000.00f
) : TextWatcher {

    private var isUpdating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return
        isUpdating = true

        val parsed = parseToBigDecimal(s.toString())
        val value = parsed.toFloat()

        val finalValue = if (value > maxValue) BigDecimal.ZERO else parsed
        val formatted = NumberFormat.getCurrencyInstance(PT_BR).format(finalValue)

        editText.setText(formatted)
        editText.setSelection(formatted.length)
        isUpdating = false
    }

    private fun parseToBigDecimal(value: String): BigDecimal {
        val replaceable = String.format(
            "[%s,.\\s]",
            NumberFormat.getCurrencyInstance(PT_BR).currency?.symbol ?: "R$"
        )

        val cleanString = value.replace(replaceable.toRegex(), "")

        return if (cleanString.isEmpty()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(cleanString).setScale(
                2, RoundingMode.FLOOR
            ).divide(
                BigDecimal(100), RoundingMode.FLOOR
            )
        }
    }

    companion object {
        // A forma moderna, correta e reutilizável de criar o Locale
        private val PT_BR = Locale.forLanguageTag("pt-BR")

        fun getValueUnMasked(editText: EditText): Float {
            val moneyFormatter = NumberFormat.getCurrencyInstance(PT_BR)
            val value = if (editText.text.toString().isEmpty()) {
                0f
            } else {
                try {
                    moneyFormatter.parse(editText.text.toString())?.toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0f
                }
            }
            return value ?: 0f
        }
    }
}

