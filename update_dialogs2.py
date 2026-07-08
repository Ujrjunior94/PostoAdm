with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Update Afericao Screen Dialog
target1 = """                    Text("Selecione o período e os bicos para gerar o laudo de aferição agrupado por dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = reportStartDate, onValueChange = { reportStartDate = it }, label = { Text("Início (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reportEndDate, onValueChange = { reportEndDate = it }, label = { Text("Fim (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }"""
replace1 = """                    Text("Selecione a data e os bicos para gerar o laudo de aferição do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())"""
text = text.replace(target1, replace1)

# Update Qualidade Screen Dialog
target2 = """                    Text("Selecione o período e os combustíveis para gerar o relatório de análise de qualidade ANP.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = reportStartDate, onValueChange = { reportStartDate = it }, label = { Text("Início (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = reportEndDate, onValueChange = { reportEndDate = it }, label = { Text("Fim (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }"""
replace2 = """                    Text("Selecione a data e os combustíveis para gerar o relatório de análise de qualidade ANP do dia.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                    OutlinedTextField(value = reportDate, onValueChange = { reportDate = it }, label = { Text("Data (YYYY-MM-DD)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth())"""
text = text.replace(target2, replace2)

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

