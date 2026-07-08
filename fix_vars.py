with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

afericao_vars = """
    var showCalibReportDialogInStock by remember { mutableStateOf(false) }
    var reportDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var selectedNozzleIdsForReport by remember { mutableStateOf(setOf<String>()) }
"""

analise_vars = """
    var showQualReportDialog by remember { mutableStateOf(false) }
    var reportDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var selectedFuelTypesForReport by remember { mutableStateOf(setOf<String>()) }
"""

idx1 = text.find("val mContext = androidx.compose.ui.platform.LocalContext.current", text.find("fun AfericaoScreen"))
if idx1 != -1 and "selectedNozzleIdsForReport" not in text[idx1:idx1+1000]:
    text = text[:idx1] + afericao_vars + "\n    " + text[idx1:]

idx2 = text.find("val mContext = androidx.compose.ui.platform.LocalContext.current", text.find("fun AnaliseScreen"))
if idx2 != -1 and "selectedFuelTypesForReport" not in text[idx2:idx2+1000]:
    text = text[:idx2] + analise_vars + "\n    " + text[idx2:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
