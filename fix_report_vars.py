with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Add to AfericaoScreen
afericao_vars = """
    var reportDate by remember { mutableStateOf("2026-07-04") }
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
"""
text = text.replace('val mContext = androidx.compose.ui.platform.LocalContext.current\n\n    LazyColumn(', afericao_vars + '    val mContext = androidx.compose.ui.platform.LocalContext.current\n\n    LazyColumn(')

# Add to AnaliseScreen
analise_vars = """
    var reportDate by remember { mutableStateOf("2026-07-04") }
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
"""
text = text.replace('val mContext = androidx.compose.ui.platform.LocalContext.current\n\n    LazyColumn(', analise_vars + '    val mContext = androidx.compose.ui.platform.LocalContext.current\n\n    LazyColumn(')

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

