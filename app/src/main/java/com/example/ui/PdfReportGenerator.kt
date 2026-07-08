package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Calibration
import com.example.data.FuelTank
import com.example.data.DailyReport
import com.example.data.FuelConformityRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
    private const val TAG = "PdfReportGenerator"

    fun generateLmcReport(
        context: Context,
        razaoSocial: String,
        cnpj: String,
        reports: List<DailyReport>
    ) {
        if (reports.isEmpty()) {
            Toast.makeText(context, "Nenhum relatório para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        
        // Paints
        val titlePaint = Paint().apply {
            textSize = 16f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 9f
            color = Color.WHITE
            isAntiAlias = true
        }
        val headerTablePaint = Paint().apply {
            textSize = 8f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyTextPaint = Paint().apply {
            textSize = 7.5f
            color = Color.BLACK
            isAntiAlias = true
        }
        val bodyBoldPaint = Paint().apply {
            textSize = 7.5f
            color = Color.BLACK
            isFakeBoldText = true
            isAntiAlias = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Header
        val headerRectPaint = Paint().apply { color = Color.rgb(0, 90, 193); style = Paint.Style.FILL } // Blue for LMC
        canvas.drawRect(0f, 0f, 595f, 75f, headerRectPaint)
        
        canvas.drawText(razaoSocial.uppercase(), 40f, 30f, titlePaint)
        canvas.drawText("CNPJ: $cnpj", 40f, 45f, subTitlePaint)
        
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("RELATÓRIO LMC - FECHAMENTO DIÁRIO", 555f, 30f, titlePaint.apply { textAlign = Paint.Align.RIGHT })
        canvas.drawText("Gerado em: $dateStr", 555f, 45f, subTitlePaint.apply { textAlign = Paint.Align.RIGHT })

        var currentY = 100f
        
        // Table Header
        val tableHeaderRectPaint = Paint().apply { color = Color.rgb(0, 90, 193); style = Paint.Style.FILL }
        canvas.drawRect(30f, currentY - 15f, 565f, currentY + 10f, tableHeaderRectPaint)
        
        val columnX = listOf(35f, 90f, 150f, 210f, 270f, 330f, 390f, 450f, 500f)
        val headers = listOf("Data", "Produto", "Est. Ini", "Entrada", "Venda (L)", "Est. Fim", "Venda (R$)", "Atend.", "Obs")
        
        headers.forEachIndexed { i, text ->
            canvas.drawText(text, columnX[i], currentY, headerTablePaint)
        }
        currentY += 25f

        for (report in reports.sortedByDescending { it.date }) {
            if (currentY > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            val formattedDate = try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
                formatter.format(parser.parse(report.date)!!)
            } catch (e: Exception) { report.date }

            canvas.drawText(formattedDate, columnX[0], currentY, bodyBoldPaint)
            canvas.drawText(if (report.fuelName.length > 10) report.fuelName.take(8) + ".." else report.fuelName, columnX[1], currentY, bodyTextPaint)
            canvas.drawText(String.format("%.0f", report.openingStock), columnX[2], currentY, bodyTextPaint)
            canvas.drawText(String.format("%.0f", report.receivedVolume), columnX[3], currentY, bodyTextPaint)
            canvas.drawText(String.format("%.1f", report.litersSold), columnX[4], currentY, bodyBoldPaint)
            canvas.drawText(String.format("%.0f", report.closingStock), columnX[5], currentY, bodyTextPaint)
            canvas.drawText(String.format("R$ %.0f", report.totalSales), columnX[6], currentY, bodyBoldPaint)
            canvas.drawText(report.transactionsCount.toString(), columnX[7], currentY, bodyTextPaint)
            
            val obs = if (report.observation.length > 15) report.observation.take(12) + "..." else report.observation
            canvas.drawText(obs, columnX[8], currentY, bodyTextPaint)

            currentY += 5f
            canvas.drawLine(30f, currentY, 565f, currentY, Paint().apply { color = Color.rgb(229, 231, 235); strokeWidth = 0.5f })
            currentY += 15f
        }

        pdfDocument.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Relatorio_LMC_$timeStamp.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório LMC PDF"))
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar PDF LMC", e)
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun generateConformityReport(
        context: Context,
        razaoSocial: String,
        cnpj: String,
        records: List<FuelConformityRecord>
    ) {
        if (records.isEmpty()) {
            Toast.makeText(context, "Nenhuma análise para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        
        // Paints
        val titlePaint = Paint().apply {
            textSize = 16f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 9f
            color = Color.WHITE
            isAntiAlias = true
        }
        val headerTablePaint = Paint().apply {
            textSize = 8.5f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyTextPaint = Paint().apply {
            textSize = 8f
            color = Color.BLACK
            isAntiAlias = true
        }
        val bodyBoldPaint = Paint().apply {
            textSize = 8f
            color = Color.BLACK
            isFakeBoldText = true
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            textSize = 8f
            color = Color.rgb(107, 114, 128)
            isAntiAlias = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Header
        val headerRectPaint = Paint().apply { color = Color.rgb(0, 121, 107); style = Paint.Style.FILL } // Teal for Quality
        canvas.drawRect(0f, 0f, 595f, 75f, headerRectPaint)
        
        canvas.drawText(razaoSocial.uppercase(), 40f, 30f, titlePaint)
        canvas.drawText("CNPJ: $cnpj", 40f, 45f, subTitlePaint)
        
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("RELATÓRIO DE ANÁLISE DE QUALIDADE", 555f, 30f, titlePaint.apply { textAlign = Paint.Align.RIGHT })
        canvas.drawText("Gerado em: $dateStr", 555f, 45f, subTitlePaint.apply { textAlign = Paint.Align.RIGHT })

        var currentY = 100f
        
        // Table Header
        val tableHeaderRectPaint = Paint().apply { color = Color.rgb(0, 121, 107); style = Paint.Style.FILL }
        canvas.drawRect(40f, currentY - 15f, 555f, currentY + 10f, tableHeaderRectPaint)
        
        val columnX = listOf(45f, 110f, 180f, 240f, 300f, 380f, 480f)
        val headers = listOf("Data", "Produto", "Densidade", "Temp.", "Etanol%", "Técnico", "Status")
        
        headers.forEachIndexed { i, text ->
            canvas.drawText(text, columnX[i], currentY, headerTablePaint)
        }
        currentY += 25f

        for (rec in records.sortedByDescending { it.date }) {
            if (currentY > 750) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            val formattedDate = try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(parser.parse(rec.date)!!)
            } catch (e: Exception) { rec.date }

            canvas.drawText(formattedDate, columnX[0], currentY, bodyTextPaint)
            canvas.drawText(rec.fuelType, columnX[1], currentY, bodyBoldPaint)
            canvas.drawText(String.format("%.4f", rec.densityMeasured), columnX[2], currentY, bodyTextPaint)
            canvas.drawText("${rec.temperature}°C", columnX[3], currentY, bodyTextPaint)
            canvas.drawText(if (rec.fuelType.contains("Gasolina")) "${rec.ethanolPercent}%" else "--", columnX[4], currentY, bodyTextPaint)
            canvas.drawText(if (rec.technicianName.length > 15) rec.technicianName.take(12) + "..." else rec.technicianName, columnX[5], currentY, bodyTextPaint)
            
            val statusColor = if (rec.isConforme) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
            val statusPaint = Paint().apply {
                textSize = 8f
                color = statusColor
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(if (rec.isConforme) "CONFORME" else "REPROVADO", columnX[6], currentY, statusPaint)

            currentY += 5f
            canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = Color.rgb(229, 231, 235); strokeWidth = 0.5f })
            currentY += 15f
        }

        pdfDocument.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Relatorio_Qualidade_$timeStamp.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório Qualidade PDF"))
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar PDF Qualidade", e)
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun generateCalibrationReport(
        context: Context,
        razaoSocial: String,
        cnpj: String,
        calibrations: List<Calibration>
    ) {
        if (calibrations.isEmpty()) {
            Toast.makeText(context, "Nenhuma calibração para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        
        // Fonts and Paints
        val titlePaint = Paint().apply {
            textSize = 16f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            textSize = 9f
            color = Color.WHITE
            isAntiAlias = true
        }
        val headerTablePaint = Paint().apply {
            textSize = 8.5f
            color = Color.WHITE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyTextPaint = Paint().apply {
            textSize = 8f
            color = Color.BLACK
            isAntiAlias = true
        }
        val bodyBoldPaint = Paint().apply {
            textSize = 8f
            color = Color.BLACK
            isFakeBoldText = true
            isAntiAlias = true
        }
        val groupHeaderPaint = Paint().apply {
            textSize = 9f
            color = Color.BLACK
            isFakeBoldText = true
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            textSize = 8f
            color = Color.rgb(107, 114, 128)
            isAntiAlias = true
        }

        // Page info: A4 size is 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // --- DRAW HEADER (RED RECTANGLE) ---
        val headerRectPaint = Paint().apply {
            color = Color.rgb(220, 38, 38) // Corporate Red
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 75f, headerRectPaint)
        
        canvas.drawText(razaoSocial.uppercase(), 40f, 30f, titlePaint)
        canvas.drawText("CNPJ: $cnpj", 40f, 45f, subTitlePaint)
        
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("RELATÓRIO DE AFERIÇÃO (20L)", 555f, 30f, titlePaint.apply { textAlign = Paint.Align.RIGHT })
        canvas.drawText("Gerado em: $dateStr", 555f, 45f, subTitlePaint.apply { textAlign = Paint.Align.RIGHT })

        var currentY = 100f
        
        // --- DRAW TABLE HEADER ---
        val tableHeaderRectPaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, currentY - 15f, 555f, currentY + 10f, tableHeaderRectPaint)
        
        val columnX = listOf(45f, 150f, 220f, 300f, 380f, 480f)
        val headers = listOf("Bico / Bomba", "V. Nominal", "V. Medido", "Desvio (ml)", "Laudo (Inmetro)", "Status")
        
        headers.forEachIndexed { i, text ->
            canvas.drawText(text, columnX[i], currentY, headerTablePaint)
        }
        currentY += 25f

        // --- GROUP BY DATE ---
        val groupedByDate = calibrations.sortedByDescending { it.date }.groupBy { it.date }
        
        for ((date, cals) in groupedByDate) {
            // Check for new page
            if (currentY > 750) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            // Date Group Header
            canvas.drawRect(40f, currentY - 12f, 555f, currentY + 8f, Paint().apply { color = Color.rgb(241, 245, 249) })
            val formattedDate = try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(parser.parse(date)!!)
            } catch (e: Exception) { date }
            
            canvas.drawText("🗓️ AFERIÇÕES DO DIA: $formattedDate", 45f, currentY, groupHeaderPaint)
            currentY += 20f

            for (cal in cals) {
                // Check for new page
                if (currentY > 780) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 50f
                }

                val deviationMl = (cal.measuredVolume - cal.nominalVolume) * 1000
                val deviationStr = "${if (deviationMl > 0) "+" else ""}${String.format("%.0f", deviationMl)} ml"

                canvas.drawText(cal.referenceName, columnX[0], currentY, bodyBoldPaint)
                canvas.drawText("${String.format("%.1f", cal.nominalVolume)} L", columnX[1], currentY, bodyTextPaint)
                canvas.drawText("${String.format("%.3f", cal.measuredVolume)} L", columnX[2], currentY, bodyTextPaint)
                canvas.drawText(deviationStr, columnX[3], currentY, bodyBoldPaint)
                canvas.drawText(if (cal.laudo.length > 20) cal.laudo.take(17) + "..." else cal.laudo, columnX[4], currentY, bodyTextPaint)
                
                val statusColor = if (cal.isConforme) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
                val statusPaint = Paint().apply {
                    textSize = 8f
                    color = statusColor
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.drawText(if (cal.isConforme) "CONFORME" else "REPROVADO", columnX[5], currentY, statusPaint)

                currentY += 5f
                canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = Color.rgb(229, 231, 235); strokeWidth = 0.5f })
                currentY += 15f
            }
        }

        // --- SIGNATURES ---
        if (currentY > 700) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = 100f
        } else {
            currentY += 40f
        }

        val linePaint = Paint().apply { color = Color.rgb(209, 213, 219); strokeWidth = 1f }
        canvas.drawLine(60f, currentY, 240f, currentY, linePaint)
        canvas.drawText("Responsável pela Aferição", 150f, currentY + 12f, footerPaint.apply { textAlign = Paint.Align.CENTER })
        
        canvas.drawLine(350f, currentY, 530f, currentY, linePaint)
        canvas.drawText("Visto da Fiscalização", 440f, currentY + 12f, footerPaint.apply { textAlign = Paint.Align.CENTER })

        pdfDocument.finishPage(page)

        // Save file
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Relatorio_Afericao_$timeStamp.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            
            // Share the PDF
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório PDF"))
            
            Log.d(TAG, "PDF shared from: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar PDF", e)
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }


    fun generateMonthlySummaryReport(
        context: Context,
        razaoSocial: String,
        cnpj: String,
        tanks: List<FuelTank>,
        reports: List<DailyReport>,
        monthYear: String // e.g. "07/2026"
    ) {
        val pdfDocument = PdfDocument()
        
        val titlePaint = Paint().apply {
            textSize = 16f
            color = Color.WHITE
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            color = Color.WHITE
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val subTitlePaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
            isFakeBoldText = true
        }
        val boldTextPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
            isFakeBoldText = true
        }
        
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // --- Header ---
        val headerBgPaint = Paint().apply { color = Color.parseColor("#00796B") }
        canvas.drawRect(0f, 0f, 595f, 60f, headerBgPaint)
        canvas.drawText("RESUMO MENSAL: FATURAMENTO E ESTOQUE", 20f, 35f, titlePaint)
        
        var yPos = 80f
        canvas.drawText("Mês/Ano: $monthYear", 20f, yPos, textPaint)
        yPos += 20f
        canvas.drawText("Razão Social: $razaoSocial", 20f, yPos, textPaint)
        yPos += 20f
        canvas.drawText("CNPJ: $cnpj", 20f, yPos, textPaint)
        yPos += 30f
        
        // --- Faturamento (Billing) Summary ---
        canvas.drawText("1. Faturamento Mensal", 20f, yPos, subTitlePaint)
        yPos += 20f
        
        val totalFaturamento = reports.sumOf { it.totalSales }
        val totalVolumeVendido = reports.sumOf { it.litersSold }
        val totalTransacoes = reports.sumOf { it.transactionsCount }
        
        canvas.drawText(String.format(java.util.Locale.getDefault(), "Total Faturado: R$ %,.2f", totalFaturamento), 20f, yPos, boldTextPaint)
        yPos += 15f
        canvas.drawText(String.format(java.util.Locale.getDefault(), "Volume Total Vendido: %,.2f Litros", totalVolumeVendido), 20f, yPos, textPaint)
        yPos += 15f
        canvas.drawText("Total de Transações Registradas: $totalTransacoes", 20f, yPos, textPaint)
        yPos += 30f
        
        // --- Estoque (Stock) Summary ---
        canvas.drawText("2. Status Atual do Estoque", 20f, yPos, subTitlePaint)
        yPos += 20f
        
        val totalCapacity = tanks.sumOf { it.capacity }
        val totalCurrentLevel = tanks.sumOf { it.currentLevel }
        val totalEstoquePerc = if (totalCapacity > 0.0) (totalCurrentLevel / totalCapacity * 100) else 0.0
        
        canvas.drawText(String.format(java.util.Locale.getDefault(), "Estoque Total no Posto: %,.2f Litros / %,.2f Litros (%.1f%%)", totalCurrentLevel, totalCapacity, totalEstoquePerc), 20f, yPos, boldTextPaint)
        yPos += 20f
        
        // Draw Tank Table
        val colNames = listOf("Produto (Tanque)", "Capacidade (L)", "Volume Atual (L)", "Preço L")
        val colX = listOf(20f, 200f, 320f, 440f)
        
        canvas.drawRect(20f, yPos - 12f, 575f, yPos + 6f, headerBgPaint)
        for (i in colNames.indices) {
            canvas.drawText(colNames[i], colX[i], yPos, headerPaint)
        }
        yPos += 20f
        
        for (tank in tanks) {
            if (yPos > 800f) {
                pdfDocument.finishPage(page)
                // In a real app, handle pagination. Keeping it simple for summary.
            }
            canvas.drawText(tank.name, colX[0], yPos, textPaint)
            canvas.drawText(String.format(java.util.Locale.getDefault(), "%.1f", tank.capacity), colX[1], yPos, textPaint)
            canvas.drawText(String.format(java.util.Locale.getDefault(), "%.1f", tank.currentLevel), colX[2], yPos, textPaint)
            canvas.drawText(String.format(java.util.Locale.getDefault(), "R$ %.3f", tank.pricePerLiter), colX[3], yPos, textPaint)
            yPos += 15f
        }
        yPos += 20f
        
        // --- End of Report ---
        canvas.drawLine(20f, yPos, 575f, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        yPos += 15f
        canvas.drawText("Relatório gerado automaticamente pelo sistema PostoAdmin.", 20f, yPos, textPaint)
        
        pdfDocument.finishPage(page)
        
        val fileName = "Resumo_Mensal_${monthYear.replace("/", "_")}.pdf"
        val file = java.io.File(context.cacheDir, fileName)
        
        try {
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_SUBJECT, fileName)
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Compartilhar Resumo Mensal"))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
