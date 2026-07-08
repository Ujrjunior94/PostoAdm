with open("app/src/main/java/com/example/ui/PostoUi.kt", "r") as f:
    lines = f.readlines()

start_idx = 8820
end_idx = 8870

fixed = """                                )
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
                        text = "Laudos e Aferições Recentes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { 
                                val filtered = calibrations.filter { it.id in selectedCalibrationIds }
                                if (filtered.isEmpty()) {
                                    viewModel.addToast("Selecione pelo menos um registro de aferição para gerar o relatório.")
                                } else {
                                    PdfReportGenerator.generateCalibrationReport(
                                        mContext, 
                                        stationRazaoSocial.ifBlank { "Posto Administrativo" }, 
                                        stationCnpj.ifBlank { "12.345.678/0001-99" }, 
                                        filtered
                                    )
                                }
                            },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HdRed),
                            border = BorderStroke(1.dp, HdRed.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Relatório PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.navigateTo("REGISTRO_AFERICAO") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Registrar Aferição", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Registrar", fontSize = 12.sp)
                        }
                    }"""

for i, line in enumerate(lines[8820:8870]):
    if "OutlinedButton(" in line and "val filtered = calibrations.filter" in lines[i+1]:
        print("Found at", 8820+i)

lines[8824:8867] = [l + "\n" for l in fixed.split("\n")]
with open("app/src/main/java/com/example/ui/PostoUi.kt", "w") as f:
    f.writelines(lines)
