import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# 1. Remove from AfericaoScreen vars:
afericao_vars_to_remove = """    var showQualReportDialog by remember { mutableStateOf(false) }
    var selectedFuelTypesForReport by remember { mutableStateOf(setOf<String>()) }
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()"""

if afericao_vars_to_remove in text:
    text = text.replace(afericao_vars_to_remove, "")
else:
    print("Could not find afericao_vars_to_remove")

# 2. Extract showQualReportDialog block from AfericaoScreen
qual_report_idx = text.find("    if (showQualReportDialog) {")
if qual_report_idx != -1:
    end_qual_report_idx = -1
    count = 0
    started = False
    for i in range(qual_report_idx, len(text)):
        if text[i] == '{':
            count += 1
            started = True
        elif text[i] == '}':
            count -= 1
        if started and count == 0:
            end_qual_report_idx = i + 1
            break
    
    if end_qual_report_idx != -1:
        qual_report_dialog_code = text[qual_report_idx:end_qual_report_idx]
        text = text[:qual_report_idx] + text[end_qual_report_idx:]
    else:
        print("Could not find end of showQualReportDialog")
        qual_report_dialog_code = ""
else:
    print("Could not find showQualReportDialog")
    qual_report_dialog_code = ""

# 3. Create AnaliseScreen
analise_screen_code = """
@Composable
fun AnaliseScreen(viewModel: PostoViewModel) {
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()
    var showQualReportDialog by remember { mutableStateOf(false) }
    var selectedFuelTypesForReport by remember { mutableStateOf(setOf<String>()) }
    var reportDate by remember { mutableStateOf("2026-07-04") }
    
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    val mContext = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HdBorder),
                colors = CardDefaults.cardColors(containerColor = HdSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Controle de Qualidade de Combustível (ANP) 🧪",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = HdTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Amostras e análises físico-químicas regulares conforme regulamentação da ANP.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HdTextSecondary
                        )
                        val compliantCount = conformityRecords.count { it.isConforme }
                        val totalCount = conformityRecords.size
                        val pct = if (totalCount > 0) (compliantCount.toDouble() / totalCount * 100).toInt() else 100
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = if (pct >= 90) HdGreenLight else HdRedLight)
                            ) {
                                Text(
                                    text = "$pct% DE CONFORMIDADE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 90) HdGreen else HdRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$compliantCount de $totalCount análises regulares",
                                fontSize = 11.sp,
                                color = HdTextSecondary
                            )
                        }
                    }
                }
            }
        }
        item {
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
                            showQualReportDialog = true 
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B)),
                        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Relatório PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (conformityRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, HdBorder),
                    colors = CardDefaults.cardColors(containerColor = HdSurface)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma análise cadastrada.")
                    }
                }
            }
        } else {
            items(conformityRecords) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HdBorder),
                    colors = CardDefaults.cardColors(containerColor = HdSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🧪", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text(text = rec.fuelType, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                                    Text(text = "Data: ${rec.date} | Amostra: ${rec.sampleNumber}", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                                }
                            }
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (rec.isConforme) HdGreenLight else HdRedLight)
                            ) {
                                Text(
                                    text = if (rec.isConforme) "✓ CONFORME" else "✗ REPROVADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rec.isConforme) HdGreen else HdRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HdBorder)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Detalhes da Análise:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                                Text(text = "Massa Específica: ${rec.density} kg/m³\\nTeor de Álcool (Gasolina): ${rec.alcoholContent}%\\nAspecto/Cor: ${rec.aspectVisual}\\nTermômetro: ${rec.temperature}°C", style = MaterialTheme.typography.bodySmall, color = HdTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Conclusão: ${rec.laudo}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { /* TODO delete? */ },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Excluir Registro", color = HdRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
"""

analise_screen_code += qual_report_dialog_code + "\n}"

# Replace the old placeholder AnaliseScreen
old_analise = """@Composable
fun AnaliseScreen(viewModel: PostoViewModel) {
    // Moved into AfericaoScreen
}"""
if old_analise in text:
    text = text.replace(old_analise, analise_screen_code)
else:
    print("Could not find old AnaliseScreen placeholder")
    text += analise_screen_code

# Remove some extra brace garbage at the end if present
# But let's be careful. Let's just write and then format.
with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
