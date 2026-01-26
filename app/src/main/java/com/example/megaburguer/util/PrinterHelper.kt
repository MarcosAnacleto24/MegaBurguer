package com.example.megaburguer.util

import android.annotation.SuppressLint
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.megaburguer.data.model.OrderItem
import java.text.NumberFormat
import java.util.Locale

class PrinterHelper() {

    // =================================================================================
    // 1. IMPRESSÃO DE EXTRATO (Relatório do Dia)
    // =================================================================================
    @SuppressLint("MissingPermission")
    fun printDailyExtract(items: List<OrderItem>, total: Double, date: String): String {
        return printRawText(getDailyExtractDesign(items, total, date))
    }

    // =================================================================================
    // 2. IMPRESSÃO DA COZINHA (Pedido de Mesa)
    // =================================================================================
    @SuppressLint("MissingPermission")
    fun printKitchenTicket(items: List<OrderItem>, tableNumber: Int): String {
        return printRawText(getKitchenDesign(items, tableNumber))
    }

    // =================================================================================
    // 3. IMPRESSÃO DE FECHAMENTO (COM CORTE E PAUSA)
    // =================================================================================
    @SuppressLint("MissingPermission")
    fun printClosingAccount(items: List<OrderItem>, total: Double, tableNumber: Int, date: String): String {
        try {
            // 1. Conecta na impressora
            val connection = BluetoothPrintersConnections.selectFirstPaired()
                ?: return "Nenhuma impressora pareada encontrada."

            val charset = EscPosCharsetEncoding("windows-1252", 16)
            val printer = EscPosPrinter(connection, 203, 48f, 32, charset)

            // Reaproveita o desenho base (lista de itens e total)
            val baseReceipt = getAccountBaseDesign(items, total, date)

            // ---------------------------------------------------------
            // PASSO 1: VIA DO ESTABELECIMENTO
            // ---------------------------------------------------------
            val sbEst = StringBuilder()
            sbEst.append("[C]<font size='wide'><b>MEGA BURGUER</b></font>\n")
            sbEst.append("[C]<b>VIA DO ESTABELECIMENTO</b>\n")
            sbEst.append("[C]<font size='big'>MESA $tableNumber</font>\n")
            sbEst.append(baseReceipt)
            sbEst.append("[C]\n") // Espaço extra no final

            // Manda imprimir a primeira via e desconecta
            val result1 = printRawText(sbEst.toString())

            if (result1 != "Success") return result1 // Se der erro na primeira, para.

            // --- PASSO 2: PAUSA OBRIGATÓRIA ---
            try {
                // 4 Segundos: Tempo suficiente para imprimir, cortar e o garçom puxar
                Thread.sleep(4000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            // ---------------------------------------------------------
            // PASSO 3: VIA DO CLIENTE
            // ---------------------------------------------------------
            val sbCli = StringBuilder()
            sbCli.append("[C]<font size='wide'><b>MEGA BURGUER</b></font>\n")
            sbCli.append("[C]<b>VIA DO CLIENTE</b>\n")
            sbCli.append("[C]MESA $tableNumber\n")
            sbCli.append(baseReceipt)
            sbCli.append("[C]\n")
            sbCli.append("[C]Obrigado e volte sempre!\n")

            // Manda imprimir e CORTAR a segunda via
            printer.printFormattedTextAndCut(sbCli.toString())

            return "Success"

        } catch (e: Exception) {
            e.printStackTrace()
            return "Erro ao imprimir: ${e.message}"
        }
    }

    // =================================================================================
    // LÓGICA DE CONEXÃO E ENVIO (Privada)
    // =================================================================================
    @SuppressLint("MissingPermission")
    private fun printRawText(formattedText: String): String {
        var printer: EscPosPrinter? = null
        try {
            val connection = BluetoothPrintersConnections.selectFirstPaired()
                ?: return "Nenhuma impressora pareada encontrada."

            val charset = EscPosCharsetEncoding("windows-1252", 16)

            printer = EscPosPrinter(connection, 203, 48f, 32, charset)

            printer.printFormattedTextAndCut(formattedText)

            return "Success"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Erro ao imprimir: ${e.message}"
        } finally {
            // O SEGREDO ESTÁ AQUI: Força a desconexão após cada impressão.
            // Isso garante que o buffer seja limpo e a próxima conexão seja "nova".
            try {
                printer?.disconnectPrinter()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =================================================================================
    // DESIGNS (TEMPLATES)
    // =================================================================================

    // Design: EXTRATO DO DIA
    private fun getDailyExtractDesign(items: List<OrderItem>, total: Double, date: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        val sb = StringBuilder()

        // [C] = Centralizado, [L] = Esquerda, [R] = Direita
        // <b> = Negrito

        sb.append("[C]<font size='wide'><b>MEGA BURGUER</b></font>\n")
        sb.append("[C]<u>EXTRATO DO DIA</u>\n")
        sb.append("[L]\n") // Linha em branco
        sb.append("[C]<u>$date</u>\n")
        sb.append("[L]\n") // Linha em branco

        // Cabeçalho da tabela
        sb.append("[L]Qtd Item[R]Valor\n")
        sb.append("[C]--------------------------------\n")

        // Itens
        items.forEach { item ->
            // Calcula o total da LINHA (Qtd * Preço Unitário)
            val totalItem = item.price.toDouble() * item.quantity

            // Linha 1: Nome e Total Somado (Igual ao App)
            // Ex: 2x Hamburguer              R$ 22,00
            sb.append("[L]${item.quantity}x ${item.nameItem}[R]${format.format(totalItem)}\n")

            // Linha 2: Valor Unitário (Só mostra ou sempre mostra, conforme seu gosto)
            // Ex: R$ 11,00 cada
            sb.append("[L]<font size='small'>   (${format.format(item.price.toDouble())} cada)</font>\n")

        }

        // Total Geral Grande
        sb.append("[C]--------------------------------\n")
        sb.append("[L]<b>TOTAL:</b>[R]<b>${format.format(total)}</b>\n")
        sb.append("[C]\n")

        return sb.toString()
    }

    // Design: COZINHA (Sem preço, com OBS, Mesa Grande)
    private fun getKitchenDesign(items: List<OrderItem>, tableNumber: Int): String {
        val sb = StringBuilder()

        sb.append("[C]<b>PEDIDO COZINHA</b>\n")
        // Fonte 'big' deixa o número da mesa gigante para o cozinheiro ver de longe
        sb.append("[C]<font size='big'>MESA $tableNumber</font>\n")
        sb.append("[C]--------------------------------\n")
        sb.append("[L]\n")

        items.forEach { item ->
            // Ex: 2x X-Bacon
            sb.append("[L]<font size='wide'><b>${item.quantity}x ${item.nameItem}</b></font>\n")

            // Verifica se tem observação (Assumindo que seu OrderItem tem esse campo)
            // Se não tiver, adicione val observation: String = "" no seu data class
            if (item.observation.isNotEmpty()) {

                // TRUQUE DO ALINHAMENTO:
                // Se o usuário pular linha, nós adicionamos espaços na frente da próxima linha
                // para alinhar visualmente com o começo do texto.
                val obsIndented = formatObservation(item.observation)

                sb.append("[L]   <font size='small'>[OBS: ${obsIndented}]</font>\n")
            }
            sb.append("[L]\n") // Pula linha entre itens
        }

        return sb.toString()
    }

    // Design: CONTA (Base para as duas vias)
    private fun getAccountBaseDesign(items: List<OrderItem>, total: Double, date: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        val sb = StringBuilder()

        sb.append("[L]\n") // Pula linha entre itens
        sb.append("[C]$date\n")
        sb.append("[L]Qtd Item[R]Valor\n")
        sb.append("[C]--------------------------------\n")

        items.forEach { item ->
            val totalItem = item.price.toDouble() * item.quantity
            sb.append("[L]${item.quantity}x ${item.nameItem}[R]${format.format(totalItem)}\n")
            // Detalhe unitário opcional
            sb.append("[L]<font size='small'>   (${format.format(item.price.toDouble())} cada)</font>\n")
        }

        sb.append("[C]--------------------------------\n")
        sb.append("[L]<font size='wide'><b>TOTAL:</b></font>[R]<b>${format.format(total)}</b>\n")

        return sb.toString()
    }

    // Função Auxiliar: Faz a quebra de linha manual para não cortar palavras ao meio
    private fun formatObservation(text: String): String {
        val maxCharsPerLine = 28 // Limite seguro para fonte 'small'
        val indent = "         " // 9 espaços para alinhar embaixo do texto "[OBS: "

        val sb = StringBuilder()
        // 1. Respeita as quebras de linha que o usuário já deu
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLineLength = 0

            for (word in words) {
                // Verifica se a palavra cabe na linha atual
                if (currentLineLength + word.length + 1 > maxCharsPerLine) {
                    // Se não couber, pula linha e adiciona o recuo
                    sb.append("\n$indent")
                    currentLineLength = 0
                }

                // Adiciona espaço entre palavras (menos no começo da linha)
                if (currentLineLength > 0) {
                    sb.append(" ")
                    currentLineLength += 1
                }

                sb.append(word)
                currentLineLength += word.length
            }
            // Adiciona recuo se tiver mais parágrafos vindos do usuário
            if (paragraph != paragraphs.last()) {
                sb.append("\n$indent")
            }
        }
        return sb.toString()
    }




}