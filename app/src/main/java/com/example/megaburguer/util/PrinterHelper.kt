package com.example.megaburguer.util

import android.annotation.SuppressLint
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.megaburguer.data.model.OrderItem
import java.text.NumberFormat
import java.util.Locale

class PrinterHelper() {

    // Função para imprimir via BLUETOOTH (Pega a primeira impressora pareada)
    @SuppressLint("MissingPermission")
    fun printBluetooth(items: List<OrderItem>, total: Double): String {
        try {
            // 1. Tenta pegar a primeira impressora pareada no sistema
            val connection = BluetoothPrintersConnections.selectFirstPaired()
                ?: return "Nenhuma impressora pareada encontrada. Vá nas Configurações do Bluetooth e pareie a impressora primeiro."

            // 2. Tenta conectar (Isso pode falhar se a impressora estiver desligada ou longe)
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            // 3. Monta o texto e imprime
            val textoFormatado = getReceiptDesign(items, total)
            printer.printFormattedText(textoFormatado)

            return "Success" // Retorno de sucesso

        } catch (e: com.dantsu.escposprinter.exceptions.EscPosConnectionException) {
            e.printStackTrace()
            return "Erro de conexão: Verifique se a impressora está ligada e próxima."
        } catch (e: Exception) {
            e.printStackTrace()
            return "Erro ao imprimir: ${e.message}"
        }
    }

    // A MÁGICA DA FORMATAÇÃO ACONTECE AQUI
    private fun getReceiptDesign(items: List<OrderItem>, total: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        val sb = StringBuilder()

        // [C] = Centralizado, [L] = Esquerda, [R] = Direita
        // <b> = Negrito

        sb.append("[C]<b>MEGA BURGUER</b>\n")
        sb.append("[C]<u>EXTRATO DO DIA</u>\n")
        sb.append("[L]\n") // Linha em branco

        // Cabeçalho da tabela
        sb.append("[L]Qtd Item[R]Valor\n")
        sb.append("[C]--------------------------------\n")

        // Itens
        items.forEach { item ->
            // Ex: 2x X-Bacon (Quebra linha se for longo)
            // O comando [L] permite alinhar à esquerda e [R] joga o preço pro final
            sb.append("[L]${item.quantity}x ${item.nameItem}[R]${format.format(item.price)}\n")
        }

        sb.append("[C]--------------------------------\n")
        sb.append("[L]<b>TOTAL</b>[R]<b>${format.format(total)}</b>\n")
        sb.append("[C]\n")
        sb.append("[C]Obrigado pela preferencia!\n")

        return sb.toString()
    }
}