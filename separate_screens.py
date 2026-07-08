import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

def find_block(start_line):
    brace_count = 0
    end_line = start_line
    for i in range(start_line - 1, len(lines)):
        line = lines[i]
        for char in line:
            if char == '{':
                brace_count += 1
            elif char == '}':
                brace_count -= 1
        if brace_count == 0:
            end_line = i + 1
            break
    return start_line, end_line

# First, find StockScreen boundaries
start_stock, end_stock = 0, 0
for i, line in enumerate(lines):
    if "fun StockScreen(" in line:
        start_stock, end_stock = find_block(i + 1)
        break

# Now, inside StockScreen, find activeSubTab == 2 and 3
afericoes_start, afericoes_end = 0, 0
qualidade_start, qualidade_end = 0, 0
for i in range(start_stock - 1, end_stock):
    if "if (activeSubTab == 2) {" in lines[i]:
        afericoes_start, afericoes_end = find_block(i + 1)
    if "if (activeSubTab == 3) {" in lines[i]:
        qualidade_start, qualidade_end = find_block(i + 1)

print(f"Afericoes: {afericoes_start} to {afericoes_end}")
print(f"Qualidade: {qualidade_start} to {qualidade_end}")

afericoes_code = "".join(lines[afericoes_start-1:afericoes_end])
qualidade_code = "".join(lines[qualidade_start-1:qualidade_end])

# Wrap them in a screen
new_afericoes_screen = f'''
@Composable
fun AfericaoScreen(viewModel: PostoViewModel) {{
    val nozzles by viewModel.nozzles.collectAsStateWithLifecycle()
    val calibrations by viewModel.calibrations.collectAsStateWithLifecycle()
    val auditLogEntries by viewModel.auditLogEntries.collectAsStateWithLifecycle()
    var showAddCalibDialog by remember {{ mutableStateOf(false) }}
    var calibDate by remember {{ mutableStateOf("") }}
    var calibTime by remember {{ mutableStateOf("") }}
    var calibReference by remember {{ mutableStateOf("") }}
    var calibNominal by remember {{ mutableStateOf("20.0") }}
    var calibMeasured by remember {{ mutableStateOf("20.0") }}
    var calibInspector by remember {{ mutableStateOf("") }}
    val mContext = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {{
        {afericoes_code.replace('if (activeSubTab == 2) {', '', 1).rsplit('}', 1)[0].strip()}
    }}

    if (showAddCalibDialog) {{
        Dialog(onDismissRequest = {{ showAddCalibDialog = false }}) {{
            Card(shape = RoundedCornerShape(20.dp)) {{
                Column(modifier = Modifier.padding(16.dp)) {{
                    Text("Registrar Aferição Física ⚖️", style = MaterialTheme.typography.titleMedium)
                    // (Implementation detail omitted for brevity here, but I will extract it from the dialogs at the bottom of StockScreen if needed)
                    // Actually dialogs are at the end of StockScreen.
                }}
            }}
        }}
    }}
}}
'''

# Oh wait, the dialogs for them are at the end of StockScreen!
