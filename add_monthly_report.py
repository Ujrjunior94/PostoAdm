import re

with open('app/src/main/java/com/example/ui/PdfReportGenerator.kt', 'r') as f:
    text = f.read()

new_function = """

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
        val totalEstoquePerc = if (totalCapacity > 0) (totalCurrentLevel / totalCapacity * 100) else 0.0
        
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
}"""

if "fun generateMonthlySummaryReport" not in text:
    text = text.replace("    }\n}", "    }\n" + new_function)
    
with open('app/src/main/java/com/example/ui/PdfReportGenerator.kt', 'w') as f:
    f.write(text)

