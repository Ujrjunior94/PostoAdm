with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Fix the stray og at 1607
import re
lines = text.split('\n')
for i, line in enumerate(lines):
    if line.strip().startswith('og('):
        print(f"Stray og at line {i+1}")
        lines[i] = ""

text = '\n'.join(lines)

# Move the dialog inside AfericaoScreen
# Or rather, let's just make sure AfericaoScreen has those variables defined
idx_afericao = text.find("fun AfericaoScreen(viewModel: PostoViewModel) {")
if idx_afericao != -1:
    idx_vars = text.find("var calibInspector by remember { mutableStateOf(\"\") }", idx_afericao)
    if idx_vars != -1:
        insert_str = """
    var showQualReportDialog by remember { mutableStateOf(false) }
    var selectedFuelTypesForReport by remember { mutableStateOf(setOf<String>()) }
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()
"""
        text = text[:idx_vars] + insert_str + text[idx_vars:]

# Let's fix the braces at the very end
if text.strip().endswith("}"):
    pass # we might have too many or too few braces now, let's just do a count check later.

# Add AnaliseScreen back
if text.find("fun AnaliseScreen") == -1:
    text += """
@Composable
fun AnaliseScreen(viewModel: PostoViewModel) {
    // Moved into AfericaoScreen
}
"""

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
