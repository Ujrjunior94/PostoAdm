with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Replace duplicate declarations
# I will use regex to find duplicates inside AfericaoScreen and AnaliseScreen

def remove_dup_decl(var_name, text):
    lines = text.split('\n')
    new_lines = []
    seen = 0
    # only remove if seen > 0 in the same function
    # actually, since I added them to AfericaoScreen, let's just find the exact string I added and remove it.
    pass

# The exact string I added:
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

text = text.replace(afericao_vars, "")
text = text.replace(analise_vars, "")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
