import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

dashboard_top = """        item {
            Column {
                Text(
                    text = "Dashboard Principal 📊",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Bem-vindo ao PostoAdmin! Veja o status operacional do posto em tempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }"""

new_dashboard_top = """        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dashboard Principal 📊",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Bem-vindo ao PostoAdmin! Veja o status operacional do posto em tempo real.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                val mContext = androidx.compose.ui.platform.LocalContext.current
                val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
                val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
                
                Button(
                    onClick = {
                        val currentMonthYear = java.text.SimpleDateFormat("MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        PdfReportGenerator.generateMonthlySummaryReport(
                            context = mContext,
                            razaoSocial = stationRazaoSocial.ifBlank { "Posto Administrativo" },
                            cnpj = stationCnpj.ifBlank { "12.345.678/0001-99" },
                            tanks = tanks,
                            reports = reports, // In a real app, filter by current month. Currently passing all for demonstration.
                            monthYear = currentMonthYear
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resumo do Mês", fontWeight = FontWeight.Bold)
                }
            }
        }"""

if dashboard_top in text:
    text = text.replace(dashboard_top, new_dashboard_top)
    with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
        f.write(text)
    print("Replaced!")
else:
    print("Not found!")
