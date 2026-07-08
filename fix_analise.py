with open("app/src/main/java/com/example/ui/PostoUi.kt", "r") as f:
    lines = f.readlines()

start_idx = 9450
end_idx = 9480

fixed = """        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Análises Recentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { 
                            val filtered = conformityRecords.filter { it.id in selectedConformityIds }
                            if (filtered.isEmpty()) {
                                viewModel.addToast("Selecione pelo menos uma análise para gerar o relatório.")
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B)),"""

for i, line in enumerate(lines[9450:9480]):
    if "OutlinedButton(" in line and "val filtered = conformityRecords.filter" in lines[i+9450+1-9450]:
        pass

lines[9451:9479] = [l + "\n" for l in fixed.split("\n")]
with open("app/src/main/java/com/example/ui/PostoUi.kt", "w") as f:
    f.writelines(lines)
