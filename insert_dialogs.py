with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

calib_dialog = """
    if (showCalibReportDialogInStock) {
        Dialog(onDismissRequest = { showCalibReportDialogInStock = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = HdRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Relatório de Aferição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                        }
                        IconButton(onClick = { showCalibReportDialogInStock = false }) { Icon(Icons.Default.Close, contentDescription = "Fechar") }
                    }
                    Text("Selecione a data e os bicos para gerar o laudo de aferição do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Bicos a Exportar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                    Card(modifier = Modifier.fillMaxWidth().height(120.dp), border = BorderStroke(1.dp, HdBorder.copy(alpha = 0.6f)), colors = CardDefaults.cardColors(containerColor = HdSurface)) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(nozzles) { nozzle ->
                                val isSelected = selectedNozzleIdsForReport.contains(nozzle.nozzleNumber)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    selectedNozzleIdsForReport = if (isSelected) selectedNozzleIdsForReport - nozzle.nozzleNumber else selectedNozzleIdsForReport + nozzle.nozzleNumber
                                }.padding(vertical = 4.dp)) {
                                    Checkbox(checked = isSelected, onCheckedChange = { selectedNozzleIdsForReport = if (it) selectedNozzleIdsForReport + nozzle.nozzleNumber else selectedNozzleIdsForReport - nozzle.nozzleNumber })
                                    Text("${nozzle.nozzleNumber} (${nozzle.fuelType})", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            val filtered = calibrations.filter { it.date == reportDate && selectedNozzleIdsForReport.any { id -> it.referenceName.contains(id) } }
                            if (filtered.isEmpty()) viewModel.addToast("Sem registros.") else {
                                PdfReportGenerator.generateCalibrationReport(mContext, stationRazaoSocial.ifBlank { "Posto Administrativo" }, stationCnpj.ifBlank { "12.345.678/0001-99" }, filtered)
                                showCalibReportDialogInStock = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HdRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Baixar PDF de Aferição")
                    }
                }
            }
        }
    }
"""

qual_dialog = """
    if (showQualReportDialog) {
        Dialog(onDismissRequest = { showQualReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Relatório de Qualidade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                        }
                        IconButton(onClick = { showQualReportDialog = false }) { Icon(Icons.Default.Close, contentDescription = "Fechar") }
                    }
                    Text("Selecione a data e os combustíveis para gerar o relatório de análise de qualidade ANP do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Produtos a Exportar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                    val fuelTypesList = listOf("Gasolina Comum", "Gasolina Aditivada", "Etanol Hidratado", "Diesel S10", "Diesel S500")
                    Card(modifier = Modifier.fillMaxWidth().height(120.dp), border = BorderStroke(1.dp, HdBorder.copy(alpha = 0.6f)), colors = CardDefaults.cardColors(containerColor = HdSurface)) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(fuelTypesList) { fuel ->
                                val isSelected = selectedFuelTypesForReport.contains(fuel)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    selectedFuelTypesForReport = if (isSelected) selectedFuelTypesForReport - fuel else selectedFuelTypesForReport + fuel
                                }.padding(vertical = 4.dp)) {
                                    Checkbox(checked = isSelected, onCheckedChange = { selectedFuelTypesForReport = if (it) selectedFuelTypesForReport + fuel else selectedFuelTypesForReport - fuel })
                                    Text(fuel, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            val filtered = conformityRecords.filter { it.date == reportDate && selectedFuelTypesForReport.contains(it.fuelType) }
                            if (filtered.isEmpty()) viewModel.addToast("Sem registros.") else {
                                PdfReportGenerator.generateConformityReport(mContext, stationRazaoSocial.ifBlank { "Posto Administrativo" }, stationCnpj.ifBlank { "12.345.678/0001-99" }, filtered)
                                showQualReportDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Baixar PDF de Qualidade")
                    }
                }
            }
        }
    }
"""

# Insert calib_dialog into AfericaoScreen
idx = text.find("@Composable\nfun AnaliseScreen")
if idx != -1:
    last_brace = text.rfind("}", 0, idx)
    if last_brace != -1:
        text = text[:last_brace] + calib_dialog + "\n" + text[last_brace:]

# Insert qual_dialog into AnaliseScreen
last_brace = text.rfind("}")
if last_brace != -1:
    text = text[:last_brace] + qual_dialog + "\n" + text[last_brace:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

