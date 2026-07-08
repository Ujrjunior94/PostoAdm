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

def find_block_by_str(s):
    for i, line in enumerate(lines):
        if s in line:
            return find_block(i + 1)
    return 0, 0

a_start, a_end = find_block_by_str("if (activeSubTab == 2) {")
c_start, c_end = find_block_by_str("if (activeSubTab == 3) {")
d1_start, d1_end = find_block_by_str("if (showAddCalibDialog) {")
d2_start, d2_end = find_block_by_str("if (showAddConformityDialog) {")

afericoes_code = "".join(lines[a_start:a_end-1])
qualidade_code = "".join(lines[c_start:c_end-1])
afericoes_dialog = "".join(lines[d1_start-1:d1_end])
qualidade_dialog = "".join(lines[d2_start-1:d2_end])

def remove_blocks(blocks_to_remove):
    # blocks_to_remove is list of (start, end)
    to_remove = set()
    for s, e in blocks_to_remove:
        for i in range(s-1, e):
            to_remove.add(i)
    
    new_lines = []
    for i, line in enumerate(lines):
        if i not in to_remove:
            new_lines.append(line)
    return new_lines

lines = remove_blocks([(a_start, a_end), (c_start, c_end), (d1_start, d1_end), (d2_start, d2_end)])

text = "".join(lines)

# Remove the tabs from StockScreen
tab2 = """                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Aferições (${calibrations.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )"""
tab3 = """                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("Qualidade (${conformityRecords.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )"""

text = text.replace(tab2, "")
text = text.replace(tab3, "")

# Also, update "Auditoria" tab which is activeSubTab == 4 to activeSubTab == 2
# Wait, "0 = Tanques, 1 = Bicos de Bomba, 2 = Aferições, 3 = Conformidade, 4 = Auditoria & Compliance"
text = text.replace("activeSubTab == 4", "activeSubTab == 2")
text = text.replace("activeSubTab = 4", "activeSubTab = 2")

afericao_screen = f'''
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
{afericoes_code}
    }}

{afericoes_dialog}
}}
'''

analise_screen = f'''
@Composable
fun AnaliseScreen(viewModel: PostoViewModel) {{
    val tanks by viewModel.fuelTanks.collectAsStateWithLifecycle()
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()
    val fuelDeliveries by viewModel.fuelDeliveries.collectAsStateWithLifecycle()
    var showAddConformityDialog by remember {{ mutableStateOf(false) }}
    var confDate by remember {{ mutableStateOf("") }}
    var confTime by remember {{ mutableStateOf("") }}
    var confFuelType by remember {{ mutableStateOf("Gasolina Comum") }}
    var confDensity by remember {{ mutableStateOf("750.0") }}
    var confTemp by remember {{ mutableStateOf("20.0") }}
    var confAspect by remember {{ mutableStateOf("Límpido e Isento de Impurezas") }}
    var confObservation by remember {{ mutableStateOf("") }}
    var confIsConforme by remember {{ mutableStateOf(true) }}
    var deliveryConformityId by remember {{ mutableStateOf<String?>(null) }}
    val mContext = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {{
{qualidade_code}
    }}

{qualidade_dialog}
}}
'''

text = text + "\n" + afericao_screen + "\n" + analise_screen

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

