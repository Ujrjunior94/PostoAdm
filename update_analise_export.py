with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

target = """                    OutlinedButton(
                        onClick = { 
                            showQualReportDialog = true 
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B)),
                        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {"""

replacement = """                    OutlinedButton(
                        onClick = { 
                            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
                            val filtered = conformityRecords.filter { it.date.startsWith(currentMonth) }
                            if (filtered.isEmpty()) {
                                viewModel.addToast("Nenhuma análise encontrada para o mês atual.")
                            } else {
                                PdfReportGenerator.generateConformityReport(
                                    mContext, 
                                    stationRazaoSocial.ifBlank { "Posto Administrativo" }, 
                                    stationCnpj.ifBlank { "12.345.678/0001-99" }, 
                                    filtered
                                )
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B)),
                        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {"""

if target in text:
    text = text.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
        f.write(text)
    print("Patched Analise export button!")
else:
    print("Analise target not found!")
