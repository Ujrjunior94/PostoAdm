with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# For AfericaoScreen
old_afericao_text = 'Text("Selecione o período e os bicos para gerar o laudo de aferição agrupado por dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)'
old_afericao_row = '''                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = reportStartDate, onValueChange = { reportStartDate = it }, label = { Text("Início (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reportEndDate, onValueChange = { reportEndDate = it }, label = { Text("Fim (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }'''
new_afericao_text = 'Text("Selecione a data e os bicos para gerar o laudo de aferição do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)\n                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())'

text = text.replace(old_afericao_text + '\n' + old_afericao_row, new_afericao_text)

old_afericao_filter = 'val filtered = calibrations.filter { it.date >= reportStartDate && it.date <= reportEndDate && selectedNozzleIdsForReport.any { id -> it.referenceName.contains(id) } }'
new_afericao_filter = 'val filtered = calibrations.filter { it.date == reportDate && selectedNozzleIdsForReport.any { id -> it.referenceName.contains(id) } }'
text = text.replace(old_afericao_filter, new_afericao_filter)

# For AnaliseScreen
old_analise_text = 'Text("Selecione o período e os combustíveis para gerar o relatório de análise de qualidade ANP.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)'
old_analise_row = '''                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = reportStartDate, onValueChange = { reportStartDate = it }, label = { Text("Início (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reportEndDate, onValueChange = { reportEndDate = it }, label = { Text("Fim (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }'''
new_analise_text = 'Text("Selecione a data e os combustíveis para gerar o relatório de análise de qualidade ANP do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)\n                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())'

text = text.replace(old_analise_text + '\n' + old_analise_row, new_analise_text)

old_analise_filter = 'val filtered = conformityRecords.filter { it.date >= reportStartDate && it.date <= reportEndDate && selectedFuelTypesForReport.contains(it.fuelType) }'
new_analise_filter = 'val filtered = conformityRecords.filter { it.date == reportDate && selectedFuelTypesForReport.contains(it.fuelType) }'
text = text.replace(old_analise_filter, new_analise_filter)

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

