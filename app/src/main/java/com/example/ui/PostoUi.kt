package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode

@Composable
fun PostoAdminApp(viewModel: PostoViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val lowFuelAlerts by viewModel.lowFuelAlerts.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isLoggedIn) {
                LoginScreen(viewModel)
            } else {
                MainLayout(viewModel, currentScreen)
            }

            // HUD notification system
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .widthIn(max = 450.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notifications.forEach { message ->
                    ToastNotification(message = message, onDismiss = { viewModel.dismissNotification(message) })
                }
            }
        }
    }
}

@Composable
fun ToastNotification(message: String, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔔", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: PostoViewModel) {
    var isRegisterTab by remember { mutableStateOf(false) }

    // Login Fields
    var email by remember { mutableStateOf("admin@posto.com") }
    var password by remember { mutableStateOf("123456") }

    // Register Fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regStationName by remember { mutableStateOf("") }
    var regStationCnpj by remember { mutableStateOf("") }
    var regStationEndereco by remember { mutableStateOf("") }

    val error by viewModel.loginError.collectAsStateWithLifecycle()
    val isLoadingAuth by viewModel.isLoadingAuth.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PetrolDark, DarkBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Theme Switcher on Login Screen
        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        IconButton(
            onClick = {
                val nextMode = when (themeMode) {
                    "LIGHT" -> "DARK"
                    "DARK" -> "AUTO"
                    else -> "LIGHT"
                }
                viewModel.setThemeMode(nextMode)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(42.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            val icon = when (themeMode) {
                "LIGHT" -> "☀️"
                "DARK" -> "🌙"
                else -> "🌓"
            }
            Text(text = icon, fontSize = 18.sp, color = Color.White)
        }

        Card(
            modifier = Modifier
                .widthIn(max = 450.dp)
                .padding(16.dp)
                .testTag("login_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "⛽",
                        fontSize = 44.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                item {
                    Text(
                        text = "PostoAdmin",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                item {
                    Text(
                        text = "Gestão de Redes e Controle de Conformidade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                item {
                    val isSupabaseConfigured = viewModel.isSupabaseAvailable()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSupabaseConfigured) Color(0xFFE6F4EA) else Color(0xFFFFF4E5),
                        border = BorderStroke(1.dp, if (isSupabaseConfigured) Color(0xFF34A853).copy(alpha = 0.5f) else Color(0xFFFBBC05).copy(alpha = 0.5f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isSupabaseConfigured) "⚡ Sincronização Supabase Ativa" else "💻 Banco Local (Modo Off-line)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSupabaseConfigured) Color(0xFF137333) else Color(0xFFB06000)
                            )
                        }
                    }
                }

                // Tabs Selector
                item {
                    TabRow(
                        selectedTabIndex = if (isRegisterTab) 1 else 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = {}
                    ) {
                        Tab(
                            selected = !isRegisterTab,
                            onClick = { isRegisterTab = false },
                            text = { Text("Entrar", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = isRegisterTab,
                            onClick = { isRegisterTab = true },
                            text = { Text("Criar Conta", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                if (!isRegisterTab) {
                    // Login view
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Nome de Usuário (Login ou E-mail)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Usuário") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }

                    if (error != null) {
                        item {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { viewModel.login(email, password) },
                            enabled = !isLoadingAuth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(48.dp)
                                .testTag("submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isLoadingAuth) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Entrar no Sistema", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                } else {
                    // Registration view - Simplified to just Login and Password
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    item {
                        Text(
                            text = "Cadastrar Novo Usuário",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    item {
                        Text(
                            text = "Insira um nome de usuário (login) e senha. O sistema irá configurar o seu posto de combustível automaticamente.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Nome de Usuário / Login") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Usuário") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Senha de Acesso") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (error != null) {
                        item {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val ok = viewModel.registerSimplifiedManager(
                                    login = regEmail,
                                    pass = regPassword
                                )
                                if (ok) {
                                    // Feedback is handled inside ViewModel (auto-login or toast)
                                    isRegisterTab = false
                                }
                            },
                            enabled = !isLoadingAuth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isLoadingAuth) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Criar Conta e Iniciar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: PostoViewModel, currentScreen: String) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Row(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        if (isTablet) {
            // Sidebar Navigation for larger screens / layouts (React sidebar look)
            NavigationSidebar(viewModel, currentScreen)
        }

        Column(modifier = Modifier.weight(1f)) {
            MainHeader(viewModel, isTablet)

            // Dynamic screen loading with scroll
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    "DASHBOARD" -> DashboardScreen(viewModel)
                    "ESTOQUE" -> StockScreen(viewModel)
                    "FUNCIONARIOS" -> EmployeesScreen(viewModel)
                    "CALENDARIO" -> CalendarScreen(viewModel)
                    "RELATORIOS" -> ReportsScreen(viewModel)
                    "SISTEMAS" -> SystemsScreen(viewModel)
                    "CONTROLE_QUALIDADE" -> ControleQualidadeScreen(viewModel)
                    "REGISTRO_AFERICAO" -> RegistrarAfericaoScreen(viewModel)
                    "REGISTRO_ANALISE" -> RegistrarAnaliseScreen(viewModel)
                }
            }

            if (!isTablet) {
                // Bottom Tab Navigation bar for mobile layout
                MobileBottomNavBar(viewModel, currentScreen)
            }
        }
    }
}

@Composable
fun NavigationSidebar(viewModel: PostoViewModel, currentScreen: String) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(),
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                Text(text = "⛽", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PostoAdmin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SidebarItem(
            label = "Início",
            icon = Icons.Default.Home,
            selected = currentScreen == "DASHBOARD",
            onClick = { viewModel.navigateTo("DASHBOARD") }
        )
        SidebarItem(
            label = "Estoque",
            icon = Icons.Default.LocalGasStation,
            selected = currentScreen == "ESTOQUE",
            onClick = { viewModel.navigateTo("ESTOQUE") }
        )
        SidebarItem(
            label = "Equipe",
            icon = Icons.Default.People,
            selected = currentScreen == "FUNCIONARIOS",
            onClick = { viewModel.navigateTo("FUNCIONARIOS") }
        )
        SidebarItem(
            label = "Calendário",
            icon = Icons.Default.DateRange,
            selected = currentScreen == "CALENDARIO",
            onClick = { viewModel.navigateTo("CALENDARIO") }
        )
        SidebarItem(
            label = "Relatórios",
            icon = Icons.Default.Assessment,
            selected = currentScreen == "RELATORIOS",
            onClick = { viewModel.navigateTo("RELATORIOS") }
        )
        SidebarItem(
            label = "Sistemas",
            icon = Icons.Default.Lock,
            selected = currentScreen == "SISTEMAS",
            onClick = { viewModel.navigateTo("SISTEMAS") }
        )
        SidebarItem(
            label = "Qualidade",
            icon = Icons.Default.VerifiedUser,
            selected = currentScreen == "CONTROLE_QUALIDADE",
            onClick = { viewModel.navigateTo("CONTROLE_QUALIDADE") }
        )
        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = MaterialTheme.colorScheme.error) },
            label = { Text("Sair", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = { viewModel.logout() }
        )
    }
}

@Composable
fun SidebarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationRailItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        selected = selected,
        onClick = onClick,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = HdPrimary,
            unselectedIconColor = HdTextSecondary,
            selectedTextColor = HdPrimary,
            unselectedTextColor = HdTextSecondary,
            indicatorColor = HdPrimaryLight
        )
    )
}

@Composable
fun MobileBottomNavBar(viewModel: PostoViewModel, currentScreen: String) {
    NavigationBar(
        containerColor = HdSurface,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(width = 1.dp, color = HdBorder, shape = RoundedCornerShape(0.dp)),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início", fontSize = 10.sp, fontWeight = if (currentScreen == "DASHBOARD") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "DASHBOARD",
            onClick = { viewModel.navigateTo("DASHBOARD") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Estoque") },
            label = { Text("Estoque", fontSize = 10.sp, fontWeight = if (currentScreen == "ESTOQUE") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "ESTOQUE",
            onClick = { viewModel.navigateTo("ESTOQUE") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Qualidade") },
            label = { Text("Qualidade", fontSize = 10.sp, fontWeight = if (currentScreen == "CONTROLE_QUALIDADE") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "CONTROLE_QUALIDADE",
            onClick = { viewModel.navigateTo("CONTROLE_QUALIDADE") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
    }
}

@Composable
fun MainHeader(viewModel: PostoViewModel, isTablet: Boolean) {
    val lowFuelAlerts by viewModel.lowFuelAlerts.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val tanks by viewModel.fuelTanks.collectAsStateWithLifecycle()

    val screenTitle = when (currentScreen) {
        "DASHBOARD" -> "Dashboard"
        "ESTOQUE" -> "Tanques de Combustível"
        "FUNCIONARIOS" -> "Equipe & Escalas"
        "CALENDARIO" -> "Calendário"
        "RELATORIOS" -> "Relatórios Diários"
        else -> "PostoAdmin"
    }

    Column {
        // High Density Header Pattern
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HdSurface)
                .border(width = 1.dp, color = HdBorder, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "POSTOADMIN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HdPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = screenTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HdTextPrimary,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                 IconButton(
                     onClick = {
                         val nextMode = when (themeMode) {
                             "LIGHT" -> "DARK"
                             "DARK" -> "AUTO"
                             else -> "LIGHT"
                         }
                         viewModel.setThemeMode(nextMode)
                     },
                     modifier = Modifier
                         .size(42.dp)
                         .background(HdPrimaryLight, CircleShape)
                 ) {
                     val icon = when (themeMode) {
                         "LIGHT" -> "☀️"
                         "DARK" -> "🌙"
                         else -> "🌓"
                     }
                     Text(text = icon, fontSize = 18.sp)
                 }

                 // High Density Notification Icon with Badge dot
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HdRedLight)
                        .clickable { viewModel.navigateTo("ESTOQUE") }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔔", fontSize = 18.sp)
                    if (lowFuelAlerts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(HdRed, shape = CircleShape)
                                .border(1.5.dp, HdSurface, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                // Initial Avatar "AD"
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HdGrayLight)
                        .border(2.dp, HdSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AD",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HdTextSecondary
                    )
                }

                // Logout Button
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HdRedLight,
                        contentColor = HdRed
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sair",
                        tint = HdRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sair",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HdRed
                    )
                }
            }
        }

        // High Density Fuel Warning Banner
        if (lowFuelAlerts.isNotEmpty()) {
            val criticalTank = tanks.find { it.isLowLevel }
            val tankName = criticalTank?.name ?: "Combustível"
            val tankLevelPercent = criticalTank?.let { (it.currentLevel / it.capacity * 100).toInt() } ?: 10

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HdRed)
                    .clickable { viewModel.navigateTo("ESTOQUE") }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "⚠️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = "ALERTA DE ESTOQUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$tankName: Nível Crítico ($tankLevelPercent%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ABASTECER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: PostoViewModel) {
    val tanks by viewModel.fuelTanks.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val alerts by viewModel.lowFuelAlerts.collectAsStateWithLifecycle()
    val reports by viewModel.dailyReports.collectAsStateWithLifecycle()
    val schedules by viewModel.shiftSchedules.collectAsStateWithLifecycle()

    val totalStockLiters = tanks.sumOf { it.currentLevel }
    val totalCapacity = tanks.sumOf { it.capacity }
    val totalLitersSoldRecent = reports.take(3).sumOf { it.litersSold }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and intro
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dashboard Principal 📊",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Bem-vindo ao PostoAdmin! Veja o status operacional do posto em tempo real.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                val mContext = androidx.compose.ui.platform.LocalContext.current
                val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
                val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
                
                Button(
                    onClick = {
                        val currentMonthYear = java.text.SimpleDateFormat("MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        PdfReportGenerator.generateMonthlySummaryReport(
                            context = mContext,
                            razaoSocial = stationRazaoSocial.ifBlank { "Posto Administrativo" },
                            cnpj = stationCnpj.ifBlank { "12.345.678/0001-99" },
                            tanks = tanks,
                            reports = reports.filter { it.date.startsWith(currentMonthYear.split("/")[1] + "-" + currentMonthYear.split("/")[0]) },
                            monthYear = currentMonthYear
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resumo do Mês", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stats Row
        item {
            val config = LocalConfiguration.current
            val isWide = config.screenWidthDp >= 600

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Estoque Total",
                        value = "${String.format(Locale.getDefault(), "%,.0f", totalStockLiters)}L",
                        sub = "Capacidade: ${String.format(Locale.getDefault(), "%,.0f", totalCapacity)}L",
                        icon = "⛽",
                        color = PetrolPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Vendas Recentes",
                        value = "${String.format(Locale.getDefault(), "%,.0f", totalLitersSoldRecent)} L",
                        sub = "Últimos 3 dias de LMC",
                        icon = "📊",
                        color = AmberSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Frentistas Ativos",
                        value = "${employees.size}",
                        sub = "Prontos para pista",
                        icon = "👥",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Alertas de Nível",
                        value = "${alerts.size}",
                        sub = if (alerts.isEmpty()) "Todos os tanques ok" else "Tanques críticos!",
                        icon = "🚨",
                        color = if (alerts.isEmpty()) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Estoque",
                            value = "${String.format(Locale.getDefault(), "%,.0f", totalStockLiters)}L",
                            sub = "Total",
                            icon = "⛽",
                            color = PetrolPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Vendas (Litros)",
                            value = "${String.format(Locale.getDefault(), "%,.0f", totalLitersSoldRecent)} L",
                            sub = "Recentes",
                            icon = "📊",
                            color = AmberSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Equipe",
                            value = "${employees.size}",
                            sub = "Colaboradores",
                            icon = "👥",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Alertas",
                            value = "${alerts.size}",
                            sub = if (alerts.isEmpty()) "Seguro" else "Combustível Baixo!",
                            icon = "🚨",
                            color = if (alerts.isEmpty()) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active low level fuel notification system detailed list
        if (alerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚠️", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = "Combustíveis abaixo do nível mínimo de segurança:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        alerts.forEach { alert ->
                            Text(
                                text = "• $alert",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Interactive customer flow simulator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Simulador Rápido de Movimento ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Simule uma venda instantânea para testar as reações do estoque e os alertas do sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tanks.forEach { tank ->
                            Button(
                                onClick = { viewModel.simulateSale(tank.id, 400.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tank.isLowLevel) MaterialTheme.colorScheme.error else PetrolPrimary
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = tank.name.split(" ").firstOrNull() ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "-400L", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Operational Overview List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shift summary
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val todayDayNumber = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
                        val todayDayStr = "Dia %02d".format(todayDayNumber)
                        val todaySchedules = schedules.filter { it.dayOfWeek == todayDayStr }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Escala Ativa de Hoje ($todayDayStr) 🕒",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HdPrimaryLight.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Tabela do Calendário",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HdPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        if (todaySchedules.isNotEmpty()) {
                            // Beautiful matrix table of today's shifts
                            val shifts = listOf("Manhã", "Tarde", "Noite", "Folga")

                            shifts.forEach { shiftName ->
                                val (bgColor, textColor) = when (shiftName) {
                                    "Manhã" -> Pair(PetrolLight, PetrolDark)
                                    "Tarde" -> Pair(AmberLight, AmberDark)
                                    "Noite" -> Pair(HdRedLight, HdRedDark)
                                    else -> Pair(HdGreenLight, HdGreen)
                                }
                                val scheduledForShift = todaySchedules.filter { it.shift.contains(shiftName, ignoreCase = true) }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HdGrayLight.copy(alpha = 0.3f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.cardColors(containerColor = bgColor)
                                    ) {
                                        Text(
                                            text = if (shiftName == "Folga") "🟢 Folga" else "🕒 $shiftName",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (scheduledForShift.isEmpty()) {
                                            Text(
                                                text = "Livre",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = HdTextSecondary.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            scheduledForShift.forEach { sh ->
                                                Text(
                                                    text = sh.employeeName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HdTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Fallback if no calendar schedule exists yet
                            Text(
                                text = "Nenhuma escala definida no calendário para hoje. Exibindo turnos padrão cadastrados:",
                                style = MaterialTheme.typography.labelSmall,
                                color = HdTextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (employees.isEmpty()) {
                                Text("Nenhum frentista cadastrado.", style = MaterialTheme.typography.bodyMedium, color = HdTextSecondary)
                            } else {
                                employees.forEach { emp ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emp.name, style = MaterialTheme.typography.bodyMedium)
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (emp.activeShift) {
                                                    "Manhã" -> PetrolLight
                                                    "Tarde" -> AmberLight
                                                    else -> Color(0xFFE2E8F0)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = emp.activeShift,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PetrolDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            DashboardPlannerCard(viewModel = viewModel)
        }
    }
}

@Composable
fun DashboardPlannerCard(viewModel: PostoViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val schedules by viewModel.shiftSchedules.collectAsStateWithLifecycle()

    var selectedMonth by remember { mutableStateOf("Julho de 2026") }
    var selectedDayInCalendar by remember { mutableStateOf("Dia 01") }
    val monthsList = listOf("Julho de 2026", "Agosto de 2026", "Setembro de 2026", "Outubro de 2026")
    val daysOfMonth = (1..31).map { "Dia %02d".format(it) }
    val shiftNames = listOf(
        "Manhã (06h - 14h)",
        "Tarde (14h - 22h)",
        "Noite (22h - 06h)",
        "Horista (10h-18h)",
        "Horista 2 (09h-18h)",
        "Folga (Descanso)"
    )

    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var selectedEmployeeId by remember { mutableIntStateOf(0) }
    var schedShift by remember { mutableStateOf("Manhã (06h - 14h)") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planejamento Mensal de Turnos (Planner) 📅",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Visualize e gerencie a escala dos funcionários em tempo real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                var expandedMonthDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expandedMonthDropdown = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "📅 $selectedMonth ▾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedMonthDropdown,
                        onDismissRequest = { expandedMonthDropdown = false }
                    ) {
                        monthsList.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m, fontSize = 12.sp) },
                                onClick = {
                                    selectedMonth = m
                                    expandedMonthDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Grid inside the card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val weekDays = listOf("D", "S", "T", "Q", "Q", "S", "S")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HdGrayLight, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { wd ->
                            Text(
                                text = wd,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = HdPrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val (startOffset, totalDays) = getMonthDetails(selectedMonth)
                    val prevMonthTotalDays = when (selectedMonth) {
                        "Julho de 2026" -> 30
                        "Agosto de 2026" -> 31
                        "Setembro de 2026" -> 31
                        "Outubro de 2026" -> 30
                        else -> 30
                    }
                    val totalSlots = startOffset + totalDays
                    val numRows = kotlin.math.ceil(totalSlots.toDouble() / 7.0).toInt()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until numRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0..6) {
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - startOffset + 1

                                    if (dayNumber < 1) {
                                        val prevDayNumber = prevMonthTotalDays + dayNumber
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HdGrayLight.copy(alpha = 0.3f))
                                                .border(0.5.dp, HdBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(3.dp)
                                        ) {
                                            Text(
                                                text = "$prevDayNumber",
                                                fontSize = 10.sp,
                                                color = HdTextSecondary.copy(alpha = 0.4f),
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            )
                                        }
                                    } else if (dayNumber in 1..totalDays) {
                                        val dayStr = "Dia %02d".format(dayNumber)
                                        val daySchedules = schedules.filter { it.dayOfWeek == dayStr }
                                        val isSelected = selectedDayInCalendar == dayStr
                                        val hasSchedules = daySchedules.isNotEmpty()

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) HdPrimaryLight
                                                    else if (hasSchedules) HdSurface
                                                    else HdGrayLight.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) HdPrimary
                                                        else if (hasSchedules) HdPrimary.copy(alpha = 0.3f)
                                                        else HdBorder,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedDayInCalendar = dayStr
                                                }
                                                .padding(3.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    fontWeight = if (isSelected || hasSchedules) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) HdPrimaryDark else if (hasSchedules) HdTextPrimary else HdTextSecondary,
                                                    modifier = Modifier.align(Alignment.End)
                                                )

                                                if (hasSchedules) {
                                                    // Display a tiny badge for each schedule
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        daySchedules.take(3).forEach { sched ->
                                                            val badgeDetails = getShiftBadgeDetails(sched.shift)
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .clip(CircleShape)
                                                                    .background(badgeDetails.second)
                                                            )
                                                        }
                                                        if (daySchedules.size > 3) {
                                                            Text(
                                                                text = "+${daySchedules.size - 3}",
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = HdTextSecondary
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty cell for days after end of month
                                        val postDayNumber = dayNumber - totalDays
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HdGrayLight.copy(alpha = 0.3f))
                                                .border(0.5.dp, HdBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(3.dp)
                                        ) {
                                            Text(
                                                text = "$postDayNumber",
                                                fontSize = 10.sp,
                                                color = HdTextSecondary.copy(alpha = 0.4f),
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Shift Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Escala de $selectedDayInCalendar ($selectedMonth) 📋",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HdPrimaryDark
                )

                Button(
                    onClick = {
                        if (employees.isNotEmpty()) {
                            selectedEmployeeId = employees.first().id
                            schedShift = "Manhã (06h - 14h)"
                            showAddScheduleDialog = true
                        } else {
                            viewModel.addToast("Cadastre funcionários antes de criar escalas!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Escalar", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val selectedDaySchedules = schedules.filter { it.dayOfWeek == selectedDayInCalendar }
            if (selectedDaySchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum frentista escalado para este dia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedDaySchedules.forEach { sched ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HdGrayLight)
                                .border(1.dp, HdBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(PetrolLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sched.employeeName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = PetrolDark,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = sched.employeeName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HdTextPrimary
                                    )
                                    val badgeDetails = getShiftBadgeDetails(sched.shift)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(badgeDetails.second)
                                        )
                                        Text(
                                            text = sched.shift,
                                            fontSize = 10.sp,
                                            color = badgeDetails.second,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteShiftSchedule(sched) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAddScheduleDialog) {
        Dialog(onDismissRequest = { showAddScheduleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Escalar Funcionário (Dashboard) 🕒",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    var expandedEmpDropdown by remember { mutableStateOf(false) }
                    val currentSelectedEmpName = employees.find { it.id == selectedEmployeeId }?.name ?: "Selecionar..."

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedEmpDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Colaborador: $currentSelectedEmpName")
                        }
                        DropdownMenu(
                            expanded = expandedEmpDropdown,
                            onDismissRequest = { expandedEmpDropdown = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text(emp.name) },
                                    onClick = {
                                        selectedEmployeeId = emp.id
                                        expandedEmpDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Pre-filled Day
                    OutlinedTextField(
                        value = selectedDayInCalendar,
                        onValueChange = {},
                        label = { Text("Dia") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    var expandedShiftDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedShiftDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Turno: $schedShift")
                        }
                        DropdownMenu(
                            expanded = expandedShiftDropdown,
                            onDismissRequest = { expandedShiftDropdown = false }
                        ) {
                            shiftNames.forEach { sh ->
                                DropdownMenuItem(
                                    text = { Text(sh) },
                                    onClick = {
                                        schedShift = sh
                                        expandedShiftDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddScheduleDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val emp = employees.find { it.id == selectedEmployeeId }
                                if (emp != null) {
                                    viewModel.addShiftSchedule(emp.id, emp.name, selectedDayInCalendar, schedShift)
                                }
                                showAddScheduleDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, sub: String, icon: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    color = HdTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HdTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // High density status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HdGreenLight)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = HdGreen
                    )
                }
                Text(
                    text = sub,
                    fontSize = 11.sp,
                    color = HdTextSecondary
                )
            }
        }
    }
}

@Composable
fun StockScreen(viewModel: PostoViewModel) {
    val tanks by viewModel.fuelTanks.collectAsStateWithLifecycle()
    val nozzles by viewModel.nozzles.collectAsStateWithLifecycle()
    val calibrations by viewModel.calibrations.collectAsStateWithLifecycle()
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()
    val fuelDeliveries by viewModel.fuelDeliveries.collectAsStateWithLifecycle()
    val auditLogEntries by viewModel.auditLogEntries.collectAsStateWithLifecycle()
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    val mContext = androidx.compose.ui.platform.LocalContext.current

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Tanques, 1 = Bicos de Bomba, 2 = Aferições, 3 = Conformidade, 4 = Auditoria & Compliance
    var selectedAuditFilter by remember { mutableStateOf("Todos") }

    // Dialog & Form states for Tank
    var showAddTankDialog by remember { mutableStateOf(false) }
    var editingTank by remember { mutableStateOf<FuelTank?>(null) }
    var tankName by remember { mutableStateOf("") }
    var tankCapacity by remember { mutableStateOf("20000") }
    var tankLevel by remember { mutableStateOf("15000") }
    var tankThreshold by remember { mutableStateOf("4000") }
    var tankPrice by remember { mutableStateOf("5.89") }
    var selectedTankColor by remember { mutableStateOf("#005AC1") }

    // Dialog & Form states for Nozzle
    var showAddNozzleDialog by remember { mutableStateOf(false) }
    var editingNozzle by remember { mutableStateOf<Nozzle?>(null) }
    var nozzleNumberInput by remember { mutableStateOf("") }
    var nozzlePumpInput by remember { mutableStateOf("") }
    var nozzleTankSelectionId by remember { mutableIntStateOf(0) }
    var nozzleStatusInput by remember { mutableStateOf("Ativo") }
    var selectedNozzleColor by remember { mutableStateOf("#005AC1") }

    // Audit and Compliance Export Dialog states
    var showExportPreviewDialog by remember { mutableStateOf(false) }
    var exportPreviewText by remember { mutableStateOf("") }
    var exportTypeSelected by remember { mutableStateOf("CSV_AUDITS") }

    // Dialog & Form states for Calibration
    var showAddCalibDialog by remember { mutableStateOf(false) }
    var calibDate by remember { mutableStateOf("2026-07-04") }
    var calibReference by remember { mutableStateOf("") }
    var calibNominal by remember { mutableStateOf("20.0") }
    var calibMeasured by remember { mutableStateOf("20.0") }

    var calibDeviationMl by remember { mutableStateOf("0") }
    var calibLaudo by remember { mutableStateOf("Aferição física periódica do bico.") }
    var showCalibReportDialog by remember { mutableStateOf(false) }
    var selectedCalibrationIds by remember { mutableStateOf(setOf<Int>()) }

    var calibInspector by remember { mutableStateOf("") }

    // Dialog & Form states for Conformity/Quality Analysis
    var showAddConformityDialog by remember { mutableStateOf(false) }
    var confDate by remember { mutableStateOf("2026-07-04") }
    var confFuelType by remember { mutableStateOf("Gasolina Comum") }
    var confDensityMeasured by remember { mutableStateOf("0.742") }
    var confTemperature by remember { mutableStateOf("23.5") }
    var confAspectColor by remember { mutableStateOf("Límpido e isento de impurezas") }
    var confWaterPhaseFinalVolume by remember { mutableStateOf("63.5") }
    var confTechnician by remember { mutableStateOf("") }
    var confObservation by remember { mutableStateOf("") }

    var selectedFuelTypesForReport by remember { mutableStateOf(setOf<String>()) }
    var showQualReportDialog by remember { mutableStateOf(false) }
    
    // Also missing variables for delivery, apparently some got mixed up
    var deliveryDate by remember { mutableStateOf("") }
    var deliveryInvoice by remember { mutableStateOf("") }
    var deliveryFuelType by remember { mutableStateOf("Gasolina Comum") }
    var deliveryVolume by remember { mutableStateOf("") }
    var deliveryDriverName by remember { mutableStateOf("") }
    var deliveryDriverCnh by remember { mutableStateOf("") }
    var deliveryTruckPlate by remember { mutableStateOf("") }
    var showAddDeliveryDialog by remember { mutableStateOf(false) }


    // Dialog & Form states for Fuel Delivery Receival
    var deliveryConformityId by remember { mutableStateOf<Int?>(null) }

    // Dialog & Form states for Manual Audit Log Entry
    var showAddAuditDialog by remember { mutableStateOf(false) }
    var auditDate by remember { mutableStateOf("2026-07-04") }

    // NEW: Report Dialog States
    var reportStartDate by remember { mutableStateOf("2026-07-01") }
    var reportEndDate by remember { mutableStateOf("2026-07-07") }

    var auditTime by remember { mutableStateOf("10:00") }
    var auditActionType by remember { mutableStateOf("Auditoria Manual") }
    var auditTarget by remember { mutableStateOf("Geral do Posto") }
    var auditDetails by remember { mutableStateOf("") }
    var auditOperator by remember { mutableStateOf("Marcos Souza") }
    var auditComplianceStatus by remember { mutableStateOf("Regular") } // "Regular", "Aviso", "Irregular"

    var selectedTankForRefuel by remember { mutableStateOf<FuelTank?>(null) }
    var inputLiters by remember { mutableStateOf("5000") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading
        item {
            Column {
                Text(
                    text = "Gestão de Combustíveis & Pista ⛽",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Monitore tanques de armazenamento, configure bicos injetores e registre laudos de aferição legal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Sub-tabs
        item {
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, HdBorder, RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Tanques (${tanks.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Bicos (${nozzles.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )


                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Auditoria (${auditLogEntries.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        // TAB 0: TANQUES
        if (activeSubTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tanques Cadastrados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            editingTank = null
                            tankName = ""
                            tankCapacity = "20000"
                            tankLevel = "15000"
                            tankThreshold = "4000"
                            tankPrice = "5.89"
                            showAddTankDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Tanque")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Novo Tanque")
                    }
                }
            }

            if (tanks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, HdBorder),
                        colors = CardDefaults.cardColors(containerColor = HdSurface)
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum tanque cadastrado no sistema.")
                        }
                    }
                }
            } else {
                items(tanks) { tank ->
                    val fillPercent = (tank.currentLevel / tank.capacity).toFloat().coerceIn(0f, 1f)
                    val isLow = tank.isLowLevel

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, HdBorder),
                        colors = CardDefaults.cardColors(containerColor = HdSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = tank.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = HdTextPrimary
                                            )
                                            if (isLow) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Card(
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(containerColor = HdRedLight)
                                                ) {
                                                    Text(
                                                        text = "⚠️ CRÍTICO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HdRed,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Preço de Venda: R$ ${String.format(Locale.getDefault(), "%.2f", tank.pricePerLiter)}/L",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HdPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selectedTankForRefuel = tank
                                                inputLiters = "5000"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Abastecer", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Abastecer", fontSize = 12.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                editingTank = tank
                                                tankName = tank.name
                                                tankCapacity = tank.capacity.toString()
                                                tankLevel = tank.currentLevel.toString()
                                                tankThreshold = tank.threshold.toString()
                                                tankPrice = tank.pricePerLiter.toString()
                                                selectedTankColor = tank.color
                                                showAddTankDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar Tanque",
                                                tint = HdPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteFuelTank(tank)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Excluir Tanque",
                                                tint = HdRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Volume details
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "Volume: ${String.format(Locale.getDefault(), "%,.1f", tank.currentLevel)}L / ${String.format(Locale.getDefault(), "%,.0f", tank.capacity)}L",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = HdTextPrimary
                                        )
                                        Text(
                                            text = "${(fillPercent * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLow) HdRed else HdGreen
                                        )
                                    }

                                    // Minimal thin progress bar for additional visual reference
                                    val parsedProductColor = remember(tank.color) {
                                        try {
                                            Color(android.graphics.Color.parseColor(tank.color))
                                        } catch (e: Exception) {
                                            HdPrimary
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(HdGrayLight)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fillPercent)
                                                .background(
                                                    if (isLow) Brush.horizontalGradient(colors = listOf(HdRed, Color(0xFFF87171)))
                                                    else Brush.horizontalGradient(colors = listOf(parsedProductColor, parsedProductColor.copy(alpha = 0.7f)))
                                                )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Mínimo Crítico: ${String.format(Locale.getDefault(), "%,.0f", tank.threshold)}L",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HdTextSecondary
                                        )
                                        Text(
                                            text = if (isLow) "Abastecimento urgente!" else "Nível regular",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isLow) HdRed else HdGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Simulation Actions
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(HdGrayLight)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Simulador:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HdTextSecondary
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.simulateSale(tank.id, 100.0) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("-100L", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.simulateSale(tank.id, 1000.0) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("-1000L", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Beautiful animated tank cylinder
                            AnimatedTankView(
                                fuelName = tank.name,
                                currentLevel = tank.currentLevel,
                                capacity = tank.capacity,
                                threshold = tank.threshold,
                                isLow = isLow,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: BICOS DE BOMBA
        if (activeSubTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bicos Injetores Cadastrados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            editingNozzle = null
                            nozzleNumberInput = ""
                            nozzlePumpInput = "Bomba 1 - Ilha Norte"
                            nozzleTankSelectionId = if (tanks.isNotEmpty()) tanks.first().id else 0
                            nozzleStatusInput = "Ativo"
                            showAddNozzleDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Bico")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Bico")
                    }
                }
            }

            if (nozzles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, HdBorder),
                        colors = CardDefaults.cardColors(containerColor = HdSurface)
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum bico de combustível cadastrado.")
                        }
                    }
                }
            } else {
                nozzles.forEach { nozzle ->
                    item(key = "nozzle_${nozzle.id}") {
                        val nozzleCalibs = calibrations.filter { it.referenceName == "Bico ${nozzle.nozzleNumber}" || it.referenceName == nozzle.nozzleNumber }
                        val lastCalib = nozzleCalibs.firstOrNull()
                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isExpanded) HdPrimary else HdBorder),
                            colors = CardDefaults.cardColors(containerColor = HdSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        val parsedNozzleColor = remember(nozzle.color) {
                                            try {
                                                Color(android.graphics.Color.parseColor(nozzle.color))
                                            } catch (e: Exception) {
                                                HdPrimary
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(parsedNozzleColor.copy(alpha = 0.15f))
                                                .border(2.dp, parsedNozzleColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "⛽", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Bico ${nozzle.nozzleNumber}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = HdTextPrimary
                                            )
                                            Text(
                                                text = nozzle.pumpName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = HdTextSecondary
                                            )
                                        }
                                    }

                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (nozzle.status) {
                                                "Ativo" -> HdGreenLight
                                                "Em Manutenção" -> HdAmberLight
                                                else -> HdRedLight
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = nozzle.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (nozzle.status) {
                                                "Ativo" -> HdGreen
                                                "Em Manutenção" -> HdAmber
                                                else -> HdRed
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = "Combustível Vinculado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HdTextSecondary
                                        )
                                        Text(
                                            text = nozzle.fuelType,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = HdPrimaryDark
                                        )
                                        Text(
                                            text = "Tanque: ${nozzle.tankName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HdTextSecondary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Último Desvio",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HdTextSecondary
                                        )
                                        if (lastCalib != null) {
                                            val devMl = ((lastCalib.measuredVolume - 20.0) * 1000.0).toInt()
                                            val devStr = if (devMl >= 0) "+$devMl ml" else "$devMl ml"
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (lastCalib.isConforme) HdGreen else HdRed)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$devStr (${if (lastCalib.isConforme) "Conforme" else "Reprovado"})",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (lastCalib.isConforme) HdGreen else HdRed
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Sem registro ⚠️",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = HdAmber
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = HdBorder, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                calibDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                                calibReference = "Bico ${nozzle.nozzleNumber}"
                                                calibNominal = "20.0"
                                                calibMeasured = "20.0"
                                                calibDeviationMl = "0"
                                                calibInspector = "Carlos Silva"
                                                calibLaudo = "Aferição física periódica do bico."
                                                showAddCalibDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = "Aferir", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aferir Bico", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { isExpanded = !isExpanded },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                contentDescription = "Histórico",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Histórico (${nozzleCalibs.size})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingNozzle = nozzle
                                                nozzleNumberInput = nozzle.nozzleNumber
                                                nozzlePumpInput = nozzle.pumpName
                                                nozzleTankSelectionId = nozzle.tankId
                                                nozzleStatusInput = nozzle.status
                                                selectedNozzleColor = nozzle.color
                                                showAddNozzleDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar Bico",
                                                tint = HdPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteNozzle(nozzle) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remover",
                                                tint = HdRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = HdBorder, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Histórico de Auditoria & Aferições (Bico ${nozzle.nozzleNumber}) 📜",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HdPrimaryDark
                                    )

                                    if (nozzleCalibs.isEmpty()) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Nenhum histórico de calibração registrado para este bico.",
                                                    fontSize = 12.sp,
                                                    color = HdTextSecondary
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            nozzleCalibs.forEach { cal ->
                                                val devMl = ((cal.measuredVolume - 20.0) * 1000.0).toInt()
                                                val devStr = if (devMl >= 0) "+$devMl ml" else "$devMl ml"
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(0.5.dp, HdBorder),
                                                    colors = CardDefaults.cardColors(containerColor = HdGrayLight)
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text("📅", fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
                                                                Text(
                                                                    text = cal.date,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = HdTextPrimary
                                                                )
                                                            }

                                                            Card(
                                                                shape = RoundedCornerShape(4.dp),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = if (cal.isConforme) HdGreenLight else HdRedLight
                                                                )
                                                            ) {
                                                                Text(
                                                                    text = if (cal.isConforme) "Regular" else "Irregular",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = if (cal.isConforme) HdGreen else HdRed,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = "Volume Medido: ${String.format(Locale.getDefault(), "%.2f", cal.measuredVolume)} L",
                                                                fontSize = 11.sp,
                                                                color = HdTextPrimary
                                                            )
                                                            Text(
                                                                text = "Desvio: $devStr",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (cal.isConforme) HdTextPrimary else HdRed
                                                            )
                                                        }

                                                        Text(
                                                            text = "Fiscal/Operador: ${cal.inspector}",
                                                            fontSize = 10.sp,
                                                            color = HdTextSecondary
                                                        )

                                                        if (cal.laudo.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Laudo: ${cal.laudo}",
                                                                fontSize = 10.sp,
                                                                color = HdTextSecondary,
                                                                lineHeight = 14.sp
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            TextButton(
                                                                onClick = { viewModel.deleteCalibration(cal) },
                                                                contentPadding = PaddingValues(0.dp),
                                                                modifier = Modifier.height(24.dp)
                                                            ) {
                                                                Text("Excluir", color = HdRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: AFERIÇÕES E CALIBRAÇÕES
    // TAB 3: CONFORMIDADE DE COMBUSTÍVEL
        if (activeSubTab == 2) {
        // Dashboard Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("compliance_dashboard_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HdBorder),
                colors = CardDefaults.cardColors(containerColor = HdSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monitor de Conformidade Regulatória ⚖️",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = HdTextPrimary
                        )
                        
                        val hasCalibrationIssue = calibrations.any { !it.isConforme }
                        val hasQualityIssue = conformityRecords.any { !it.isConforme }
                        val riskLevel = if (hasCalibrationIssue || hasQualityIssue) "MÉDIO" else "BAIXO"
                        val riskColor = if (hasCalibrationIssue || hasQualityIssue) HdAmber else HdGreen
                        val riskBg = if (hasCalibrationIssue || hasQualityIssue) HdAmberLight else HdGreenLight
                        
                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = riskBg)
                        ) {
                            Text(
                                text = "RISCO: $riskLevel",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = riskColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val calibConforme = calibrations.count { it.isConforme }
                        val calibTotal = calibrations.size
                        val calibPct = if (calibTotal > 0) (calibConforme.toDouble() / calibTotal * 100).toInt() else 100
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, HdBorder),
                            colors = CardDefaults.cardColors(containerColor = HdGrayLight)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Bicos INMETRO", fontSize = 11.sp, color = HdTextSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$calibConforme/$calibTotal Conformes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HdTextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { if (calibTotal > 0) calibConforme.toFloat() / calibTotal else 1f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (calibPct >= 100) HdGreen else if (calibPct >= 60) HdAmber else HdRed,
                                    trackColor = HdBorder,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$calibPct% conforme", fontSize = 10.sp, color = HdTextSecondary)
                            }
                        }
                        
                        val qualConforme = conformityRecords.count { it.isConforme }
                        val qualTotal = conformityRecords.size
                        val qualPct = if (qualTotal > 0) (qualConforme.toDouble() / qualTotal * 100).toInt() else 100
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, HdBorder),
                            colors = CardDefaults.cardColors(containerColor = HdGrayLight)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Qualidade ANP", fontSize = 11.sp, color = HdTextSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$qualConforme/$qualTotal Conformes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HdTextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { if (qualTotal > 0) qualConforme.toFloat() / qualTotal else 1f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (qualPct >= 90) HdGreen else if (qualPct >= 50) HdAmber else HdRed,
                                    trackColor = HdBorder,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$qualPct% conforme", fontSize = 10.sp, color = HdTextSecondary)
                            }
                        }
                    }
                    
                    val nonConformities = mutableListOf<String>()
                    calibrations.forEach { if (!it.isConforme) nonConformities.add("Bico ${it.referenceName} REPROVADO em ${it.date} (Erro: ${String.format(Locale.getDefault(), "%.2f", it.errorPercent)}%).") }
                    conformityRecords.forEach { if (!it.isConforme) nonConformities.add("Análise de ${it.fuelType} REPROVADA em ${it.date}.") }
                    
                    if (nonConformities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HdRedLight),
                            border = BorderStroke(1.dp, HdRed.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠️ Não-Conformidades Pendentes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HdRedDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                nonConformities.forEach { text ->
                                    Text("• $text", fontSize = 11.sp, color = HdRedDark, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Header for logs + Manual Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Histórico de Auditoria & Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HdTextPrimary
                )
                
                Button(
                    onClick = { showAddAuditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_audit_log_button")
                ) {
                    Text("+ Adicionar Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Filter chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Aferições", "Qualidade", "Auditorias").forEach { filterName ->
                        val isSelected = selectedAuditFilter == filterName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAuditFilter = filterName },
                            label = { Text(filterName, fontSize = 11.sp) }
                        )
                    }
                }

                // Export Buttons for Audit Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val logs = when (selectedAuditFilter) {
                                "Aferições" -> auditLogEntries.filter { it.actionType.contains("Aferição") || it.actionType.contains("Exclusão de Aferição") }
                                "Qualidade" -> auditLogEntries.filter { it.actionType.contains("Qualidade") || it.actionType.contains("Conformidade") }
                                "Auditorias" -> auditLogEntries.filter { it.actionType.contains("Auditoria") || it.actionType.contains("Manual") }
                                else -> auditLogEntries
                            }
                            if (logs.isEmpty()) {
                                viewModel.addToast("Sem dados para exportar!")
                            } else {
                                exportTypeSelected = "CSV_AUDITS"
                                exportPreviewText = generateAuditsCSV(logs)
                                showExportPreviewDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).testTag("export_audits_csv_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar CSV", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar CSV", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val logs = when (selectedAuditFilter) {
                                "Aferições" -> auditLogEntries.filter { it.actionType.contains("Aferição") || it.actionType.contains("Exclusão de Aferição") }
                                "Qualidade" -> auditLogEntries.filter { it.actionType.contains("Qualidade") || it.actionType.contains("Conformidade") }
                                "Auditorias" -> auditLogEntries.filter { it.actionType.contains("Auditoria") || it.actionType.contains("Manual") }
                                else -> auditLogEntries
                            }
                            if (logs.isEmpty()) {
                                viewModel.addToast("Sem dados para exportar!")
                            } else {
                                exportTypeSelected = "TEXT_AUDITS"
                                exportPreviewText = generateAuditsTextTable(logs)
                                showExportPreviewDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PetrolDark),
                        modifier = Modifier.weight(1f).testTag("copy_audits_table_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Copiar Tabela", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar Tabela", fontSize = 11.sp)
                    }
                }
            }
        }
        
        // Timeline entries
        val filteredLogs = when (selectedAuditFilter) {
            "Aferições" -> auditLogEntries.filter { it.actionType.contains("Aferição") || it.actionType.contains("Exclusão de Aferição") }
            "Qualidade" -> auditLogEntries.filter { it.actionType.contains("Qualidade") || it.actionType.contains("Conformidade") }
            "Auditorias" -> auditLogEntries.filter { it.actionType.contains("Auditoria") || it.actionType.contains("Manual") }
            else -> auditLogEntries
        }
        
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum registro encontrado 📄", fontWeight = FontWeight.Bold, color = HdTextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Use o botão acima para registrar uma nova auditoria ou fiscalização.", fontSize = 11.sp, color = HdTextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(filteredLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("audit_log_card_${log.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = BorderStroke(1.dp, HdBorder)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            val indicatorColor = when (log.complianceStatus) {
                                "Irregular" -> HdRed
                                "Aviso" -> HdAmber
                                else -> HdGreen
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(indicatorColor)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(45.dp)
                                    .background(HdBorder)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.actionType,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = HdTextPrimary
                                )
                                
                                val statusColor = when (log.complianceStatus) {
                                    "Irregular" -> HdRed
                                    "Aviso" -> HdAmber
                                    else -> HdGreen
                                }
                                val statusBg = when (log.complianceStatus) {
                                    "Irregular" -> HdRedLight
                                    "Aviso" -> HdAmberLight
                                    else -> HdGreenLight
                                }
                                Card(
                                    shape = RoundedCornerShape(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = statusBg)
                                ) {
                                    Text(
                                        text = log.complianceStatus.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = "Foco: ${log.target} | Resp: ${log.operator}",
                                fontSize = 11.sp,
                                color = HdTextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            
                            Text(
                                text = "📅 ${log.date} às ${log.time}",
                                fontSize = 11.sp,
                                color = HdTextSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = HdTextPrimary,
                                lineHeight = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { viewModel.deleteAuditLogEntry(log) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Remover Log", color = HdRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Modal DIALOG for Adding New Tank
    if (showAddTankDialog) {
        Dialog(onDismissRequest = { showAddTankDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (editingTank != null) "Editar Tanque de Combustível ⛽" else "Novo Tanque de Combustível ⛽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = tankName,
                        onValueChange = { tankName = it },
                        label = { Text("Nome do Combustível (ex: Gasolina Comum)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tankCapacity,
                        onValueChange = { tankCapacity = it },
                        label = { Text("Capacidade Total (Litros)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tankLevel,
                        onValueChange = { tankLevel = it },
                        label = { Text("Volume Inicial em Estoque") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tankThreshold,
                        onValueChange = { tankThreshold = it },
                        label = { Text("Limite Crítico de Alerta (Litros)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tankPrice,
                        onValueChange = { tankPrice = it },
                        label = { Text("Preço de Venda (R$ por Litro)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Cor do Produto / Combustível:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val row1 = listOf("#005AC1", "#0288D1", "#FF9800", "#E65100", "#4CAF50", "#2E7D32")
                        val row2 = listOf("#FBC02D", "#D32F2F", "#FFD700", "#9C27B0", "#E91E63", "#607D8B")

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row1.forEach { colorStr ->
                                val isSelected = selectedTankColor == colorStr
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(android.graphics.Color.parseColor(colorStr)), shape = CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedTankColor = colorStr }
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row2.forEach { colorStr ->
                                val isSelected = selectedTankColor == colorStr
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(android.graphics.Color.parseColor(colorStr)), shape = CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedTankColor = colorStr }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddTankDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val cap = tankCapacity.toDoubleOrNull() ?: 20000.0
                                val lev = tankLevel.toDoubleOrNull() ?: 15000.0
                                val thr = tankThreshold.toDoubleOrNull() ?: 4000.0
                                val prc = tankPrice.toDoubleOrNull() ?: 5.89
                                if (tankName.isNotBlank()) {
                                    val currentEditing = editingTank
                                    if (currentEditing != null) {
                                        viewModel.updateFuelTank(
                                            currentEditing.copy(
                                                name = tankName,
                                                capacity = cap,
                                                currentLevel = lev,
                                                threshold = thr,
                                                pricePerLiter = prc,
                                                color = selectedTankColor
                                            )
                                        )
                                    } else {
                                        viewModel.addFuelTank(
                                            name = tankName,
                                            capacity = cap,
                                            currentLevel = lev,
                                            threshold = thr,
                                            pricePerLiter = prc,
                                            color = selectedTankColor
                                        )
                                    }
                                }
                                showAddTankDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Modal DIALOG for Adding New Nozzle
    if (showAddNozzleDialog) {
        Dialog(onDismissRequest = { showAddNozzleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (editingNozzle != null) "Editar Bico de Bomba ⛽" else "Cadastrar Bico de Bomba ⛽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nozzleNumberInput,
                        onValueChange = { nozzleNumberInput = it },
                        label = { Text("Número do Bico (ex: 01, 02)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nozzlePumpInput,
                        onValueChange = { nozzlePumpInput = it },
                        label = { Text("Bomba Associada (ex: Bomba 1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tank selection dropdown simulations / simple grid chips
                    Text(text = "Conectar ao Tanque / Combustível:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        tanks.forEach { tk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (nozzleTankSelectionId == tk.id) HdPrimaryLight else HdGrayLight)
                                    .clickable { nozzleTankSelectionId = tk.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = nozzleTankSelectionId == tk.id, onClick = { nozzleTankSelectionId = tk.id })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = tk.name, fontSize = 13.sp, color = HdTextPrimary)
                            }
                        }
                    }

                    // Status selection chips
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        listOf("Ativo", "Em Manutenção", "Bloqueado").forEach { st ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (nozzleStatusInput == st) HdPrimaryLight else HdGrayLight)
                                    .clickable { nozzleStatusInput = st }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = st, fontSize = 11.sp, color = if (nozzleStatusInput == st) HdPrimaryDark else HdTextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "Cor do Bico / Mangueira:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val availableNozzleColors = listOf("#005AC1", "#0288D1", "#FF9800", "#4CAF50", "#9C27B0", "#E91E63", "#1A1B20")
                        availableNozzleColors.forEach { colorStr ->
                            val isSelected = selectedNozzleColor == colorStr
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(android.graphics.Color.parseColor(colorStr)), shape = CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedNozzleColor = colorStr }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddNozzleDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val selectedTank = tanks.find { it.id == nozzleTankSelectionId }
                                if (nozzleNumberInput.isNotBlank() && selectedTank != null) {
                                    val currentEditing = editingNozzle
                                    if (currentEditing != null) {
                                        viewModel.updateNozzle(
                                            currentEditing.copy(
                                                nozzleNumber = nozzleNumberInput,
                                                pumpName = nozzlePumpInput,
                                                tankId = selectedTank.id,
                                                tankName = selectedTank.name,
                                                fuelType = selectedTank.name,
                                                status = nozzleStatusInput,
                                                color = selectedNozzleColor
                                            )
                                        )
                                    } else {
                                        viewModel.addNozzle(
                                            nozzleNumber = nozzleNumberInput,
                                            pumpName = nozzlePumpInput,
                                            tankId = selectedTank.id,
                                            tankName = selectedTank.name,
                                            fuelType = selectedTank.name,
                                            status = nozzleStatusInput,
                                            color = selectedNozzleColor
                                        )
                                    }
                                }
                                showAddNozzleDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (editingNozzle != null) "Salvar" else "Cadastrar")
                        }
                    }
                }
            }
        }
    }

    // Modal DIALOG for Registering Calibration

    // Modal dialog to Refuel Tank (Original layout feature preserved)
    if (selectedTankForRefuel != null) {
        val tank = selectedTankForRefuel!!
        Dialog(onDismissRequest = { selectedTankForRefuel = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Abastecer Tanque 🚚",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adicione combustível ao tanque de ${tank.name}. Capacidade máxima é de ${String.format(Locale.getDefault(), "%,.0f", tank.capacity)}L.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = HdTextSecondary
                    )

                    OutlinedTextField(
                        value = inputLiters,
                        onValueChange = { inputLiters = it },
                        label = { Text("Quantidade (Litros)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTankForRefuel = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val l = inputLiters.toDoubleOrNull() ?: 0.0
                                viewModel.refuelTank(tank.id, l)
                                selectedTankForRefuel = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }

    // Modal DIALOG for Adding New Fuel Conformity analysis

    // Modal DIALOG for Registering Fuel Delivery and Driver
    if (showAddDeliveryDialog) {
        Dialog(onDismissRequest = { showAddDeliveryDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HdBorder),
                colors = CardDefaults.cardColors(containerColor = HdSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lançar Entrega & Nota Fiscal 🚚",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = HdTextPrimary
                    )
                    Text(
                        text = "Cadastre a nota de combustível recebida, registre os dados do motorista e vincule à conformidade físico-química.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = deliveryDate,
                        onValueChange = { deliveryDate = it },
                        label = { Text("Data de Recebimento (AAAA-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryInvoice,
                        onValueChange = { deliveryInvoice = it },
                        label = { Text("Número da Nota Fiscal (NF-e)") },
                        placeholder = { Text("Ex: NF-e 87342") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Fuel Type Selection
                    var expandedFuelDropdown by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Tipo de Combustível Entregue", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedFuelDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(deliveryFuelType, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = expandedFuelDropdown,
                                onDismissRequest = { expandedFuelDropdown = false }
                            ) {
                                listOf("Gasolina Comum", "Etanol Comum", "Diesel S10").forEach { fuel ->
                                    DropdownMenuItem(
                                        text = { Text(fuel) },
                                        onClick = {
                                            deliveryFuelType = fuel
                                            expandedFuelDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = deliveryVolume,
                        onValueChange = { deliveryVolume = it },
                        label = { Text("Volume Recebido (Litros)") },
                        placeholder = { Text("Ex: 10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dados do Motorista & Transporte",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HdPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    OutlinedTextField(
                        value = deliveryDriverName,
                        onValueChange = { deliveryDriverName = it },
                        label = { Text("Nome do Motorista") },
                        placeholder = { Text("Ex: Carlos Silveira") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryDriverCnh,
                        onValueChange = { deliveryDriverCnh = it },
                        label = { Text("CNH do Motorista") },
                        placeholder = { Text("Ex: 123456789-0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryTruckPlate,
                        onValueChange = { deliveryTruckPlate = it },
                        label = { Text("Placa do Caminhão") },
                        placeholder = { Text("Ex: ABC-1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Linked Conformity Quality Analysis selection
                    var expandedConfDropdown by remember { mutableStateOf(false) }
                    val currentLinkedConf = conformityRecords.find { it.id == deliveryConformityId }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Vincular Análise de Qualidade (Opcional)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedConfDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (currentLinkedConf != null) 
                                        "Laudo ${currentLinkedConf.date} - ${currentLinkedConf.fuelType} (${if (currentLinkedConf.isConforme) "Conf" else "Irreg"})"
                                        else "Nenhum laudo selecionado", 
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = expandedConfDropdown,
                                onDismissRequest = { expandedConfDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nenhum (Sem laudo)") },
                                    onClick = {
                                        deliveryConformityId = null
                                        expandedConfDropdown = false
                                    }
                                )
                                conformityRecords.forEach { rec ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text("Laudo de ${rec.date} - ${rec.fuelType} (${rec.technicianName}) - ${if (rec.isConforme) "Conforme" else "Irregular"}") 
                                        },
                                        onClick = {
                                            deliveryConformityId = rec.id
                                            expandedConfDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDeliveryDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val volVal = deliveryVolume.toDoubleOrNull()
                                if (deliveryInvoice.isBlank()) {
                                    viewModel.addToast("Por favor, preencha o número da Nota Fiscal!")
                                } else if (volVal == null || volVal <= 0) {
                                    viewModel.addToast("Insira um volume numérico válido maior que 0!")
                                } else if (deliveryDriverName.isBlank()) {
                                    viewModel.addToast("Por favor, insira o nome do motorista!")
                                } else {
                                    viewModel.addFuelDelivery(
                                        date = deliveryDate,
                                        invoiceNumber = deliveryInvoice,
                                        fuelType = deliveryFuelType,
                                        volume = volVal,
                                        driverName = deliveryDriverName,
                                        driverCnh = deliveryDriverCnh,
                                        truckPlate = deliveryTruckPlate,
                                        conformityRecordId = deliveryConformityId
                                    )
                                    showAddDeliveryDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Gravar")
                        }
                    }
                }
            }
        }
    }

    // Modal DIALOG for Registering Manual Audit Log Entry
    if (showAddAuditDialog) {
        Dialog(onDismissRequest = { showAddAuditDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Registrar Log de Auditoria ⚖️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = auditDate,
                        onValueChange = { auditDate = it },
                        label = { Text("Data (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = auditTime,
                        onValueChange = { auditTime = it },
                        label = { Text("Hora (HH:MM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = auditActionType,
                        onValueChange = { auditActionType = it },
                        label = { Text("Tipo de Ação (ex: Auditoria Manual)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = auditTarget,
                        onValueChange = { auditTarget = it },
                        label = { Text("Foco / Equipamento (ex: Bico 01)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = auditDetails,
                        onValueChange = { auditDetails = it },
                        label = { Text("Detalhes da Auditoria") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = auditOperator,
                        onValueChange = { auditOperator = it },
                        label = { Text("Responsável / Fiscal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Status de Conformidade:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Regular", "Aviso", "Irregular").forEach { stat ->
                                val isSelected = auditComplianceStatus == stat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { auditComplianceStatus = stat },
                                    label = { Text(stat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddAuditDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                if (auditActionType.isNotBlank() && auditTarget.isNotBlank() && auditDetails.isNotBlank()) {
                                    viewModel.addAuditLogEntry(
                                        date = auditDate,
                                        time = auditTime,
                                        actionType = auditActionType,
                                        target = auditTarget,
                                        details = auditDetails,
                                        operator = auditOperator,
                                        complianceStatus = auditComplianceStatus
                                    )
                                    auditDetails = ""
                                    showAddAuditDialog = false
                                } else {
                                    viewModel.addToast("Por favor, preencha todos os campos obrigatórios.")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Modal Export Calibration Report (STOCK SCREEN VERSION)
}
    @Composable
    fun EmployeesScreen(viewModel: PostoViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val schedules by viewModel.shiftSchedules.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Equipe, 1 = Escala de Turnos
    var scaleViewType by remember { mutableIntStateOf(0) } // 0 = Calendário, 1 = Tabela

    var showExportPreviewDialog by remember { mutableStateOf(false) }
    var exportPreviewText by remember { mutableStateOf("") }

    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var empName by remember { mutableStateOf("") }
    var empRole by remember { mutableStateOf("Frentista") }
    var empPhone by remember { mutableStateOf("") }
    var empShift by remember { mutableStateOf("Manhã") }

    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var schedDay by remember { mutableStateOf("Dia 01") }
    var schedShift by remember { mutableStateOf("Manhã (06h - 14h)") }
    var selectedEmployeeId by remember { mutableIntStateOf(0) }
    var selectedDayInCalendar by remember { mutableStateOf("Dia 01") }

    var dragAllocationShift by remember { mutableStateOf("Manhã (06h - 14h)") }
    val daysOfMonth = (1..31).map { "Dia %02d".format(it) }
    val shiftNames = listOf("Manhã (06h - 14h)", "Tarde (14h - 22h)", "Noite (22h - 06h)", "Horista (10h-18h)", "Horista 2 (09h-18h)", "Folga (Descanso)")
    var hideEmptyDays by remember { mutableStateOf(true) }
    var selectedMonth by remember { mutableStateOf("Julho de 2026") }
    val monthsList = listOf("Julho de 2026", "Agosto de 2026", "Setembro de 2026", "Outubro de 2026")

    // Drag and Drop States
    var scheduleToMove by remember { mutableStateOf<com.example.data.ShiftSchedule?>(null) }
    var draggedEmployee by remember { mutableStateOf<com.example.data.Employee?>(null) }
    var draggedSchedule by remember { mutableStateOf<com.example.data.ShiftSchedule?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredDay by remember { mutableStateOf<String?>(null) }

    val dropTargets = remember { mutableStateMapOf<String, Rect>() }
    val employeePositions = remember { mutableStateMapOf<Int, Offset>() }
    val schedulePositions = remember { mutableStateMapOf<Int, Offset>() }
    var screenRootOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenRootOffset = coordinates.positionInRoot()
            }
    ) {
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Gestão de Colaboradores 👥",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cadastre novos frentistas/caixas e defina as escalas de plantão e turnos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Custom tabs
        item {
            TabRow(selectedTabIndex = activeTab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Equipe Cadastrada", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Escala de Turnos (Abas)", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeTab == 0) {
            // TEAM MANAGEMENT TAB
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Colaboradores (${employees.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            empName = ""
                            empPhone = ""
                            empRole = "Frentista"
                            empShift = "Manhã"
                            showAddEmployeeDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar")
                    }
                }
            }

            if (employees.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum funcionário cadastrado. Clique em Adicionar.")
                        }
                    }
                }
            } else {
                items(employees) { employee ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PetrolLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = employee.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = PetrolDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = employee.name, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Card(
                                            shape = RoundedCornerShape(6.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (employee.role) {
                                                    "Gerente" -> PetrolLight
                                                    "Caixa" -> AmberLight
                                                    else -> Color(0xFFE2E8F0)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = employee.role,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PetrolDark,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = employee.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteEmployee(employee) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        } else {
            // SHIFTS TAB
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var expandedMonthDropdown by remember { mutableStateOf(false) }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                OutlinedButton(onClick = { expandedMonthDropdown = true }) {
                                    Text("📅 $selectedMonth ▾", fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = expandedMonthDropdown,
                                    onDismissRequest = { expandedMonthDropdown = false }
                                ) {
                                    monthsList.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                selectedMonth = m
                                                expandedMonthDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (schedules.isEmpty()) {
                                        viewModel.addToast("Nenhuma escala cadastrada para exportar.")
                                    } else {
                                        exportPreviewText = generateSchedulesCSV(selectedMonth, schedules)
                                        showExportPreviewDialog = true
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Exportar", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (employees.isEmpty()) {
                                        viewModel.addToast("Erro: Cadastre frentistas antes de auto-preencher!")
                                    } else {
                                        val totalDays = getMonthDetails(selectedMonth).second
                                        viewModel.autoFillShiftSchedules(selectedMonth, totalDays)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Auto-Preencher", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Preencher", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (employees.isNotEmpty()) {
                                    selectedEmployeeId = employees.first().id
                                    schedDay = "Dia 01"
                                    schedShift = "Manhã (06h - 14h)"
                                    showAddScheduleDialog = true
                                } else {
                                    viewModel.addToast("Cadastre funcionários antes de criar escalas!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Escalar")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Escalar")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Escala de Plantões Mensal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(
                                checked = hideEmptyDays,
                                onCheckedChange = { hideEmptyDays = it }
                            )
                            Text("Ocultar dias vazios", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                TabRow(
                    selectedTabIndex = scaleViewType,
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = scaleViewType == 0,
                        onClick = { scaleViewType = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📅", modifier = Modifier.padding(end = 4.dp))
                                Text("Calendário")
                            }
                        }
                    )
                    Tab(
                        selected = scaleViewType == 1,
                        onClick = { scaleViewType = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📊", modifier = Modifier.padding(end = 4.dp))
                                Text("Tabela")
                            }
                        }
                    )
                }
            }

            if (scaleViewType == 0) {
                // Draggable Employees Row
                if (employees.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdPrimaryLight.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, HdPrimary.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "👉 Arraste um colaborador para o calendário para agendar um turno:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HdPrimaryDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    employees.forEach { emp ->
                                        var isDraggingThis by remember { mutableStateOf(false) }

                                        Box(
                                            modifier = Modifier
                                                .onGloballyPositioned { coordinates ->
                                                    employeePositions[emp.id] = coordinates.positionInRoot()
                                                }
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isDraggingThis) HdPrimaryLight.copy(alpha = 0.5f) else PetrolLight)
                                                .border(
                                                    1.dp,
                                                    if (isDraggingThis) HdPrimary else PetrolDark.copy(alpha = 0.2f),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .pointerInput(emp) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = { offset ->
                                                            draggedEmployee = emp
                                                            draggedSchedule = null
                                                            dragStartOffset = offset
                                                            dragOffset = Offset.Zero
                                                            
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            dragOffset += dragAmount

                                                            // Compute finger root coordinates
                                                            val rootStart = employeePositions[emp.id] ?: Offset.Zero
                                                            val fingerRootPos = rootStart + dragStartOffset + dragOffset

                                                            // Find drop target day
                                                            val matchedDay = dropTargets.entries.find { entry ->
                                                                entry.value.contains(fingerRootPos)
                                                            }?.key
                                                            hoveredDay = matchedDay
                                                        },
                                                        onDragEnd = {
                                                            
                                                            if (hoveredDay != null) {
                                                                selectedEmployeeId = emp.id
                                                                schedDay = hoveredDay!!
                                                                schedShift = dragAllocationShift
                                                                scheduleToMove = null
                                                                showAddScheduleDialog = true
                                                            }
                                                            draggedEmployee = null
                                                            hoveredDay = null
                                                        },
                                                        onDragCancel = {
                                                            
                                                            draggedEmployee = null
                                                            hoveredDay = null
                                                        }
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(PetrolDark),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = emp.name.take(1).uppercase(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Text(
                                                    text = emp.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PetrolDark
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = HdPrimary.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Turno de Alocação:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HdPrimaryDark
                                    )
                                    var expandedDragShiftDropdown by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(
                                            onClick = { expandedDragShiftDropdown = true },
                                            modifier = Modifier.testTag("dragShiftType"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            val badgeDetails = getShiftBadgeDetails(dragAllocationShift)
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeDetails.second),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = badgeDetails.third,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = dragAllocationShift, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeDetails.second)
                                        }
                                        DropdownMenu(
                                            expanded = expandedDragShiftDropdown,
                                            onDismissRequest = { expandedDragShiftDropdown = false }
                                        ) {
                                            shiftNames.forEach { sh ->
                                                val details = getShiftBadgeDetails(sh)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .clip(CircleShape)
                                                                    .background(details.second),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = details.third,
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                            Text(sh, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                        }
                                                    },
                                                    onClick = {
                                                        dragAllocationShift = sh
                                                        expandedDragShiftDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Monthly Calendar Grid
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Calendar header with single letter days matching the planner reference image
                            val weekDays = listOf("D", "S", "T", "Q", "Q", "S", "S")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HdGrayLight, RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekDays.forEach { wd ->
                                    Text(
                                        text = wd,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = HdPrimaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Calculate month grid geometry with previous month trailing days
                            val (startOffset, totalDays) = getMonthDetails(selectedMonth)
                            val prevMonthTotalDays = when (selectedMonth) {
                                "Julho de 2026" -> 30 // Junho
                                "Agosto de 2026" -> 31 // Julho
                                "Setembro de 2026" -> 31 // Agosto
                                "Outubro de 2026" -> 30 // Setembro
                                else -> 30
                            }
                            val totalSlots = startOffset + totalDays
                            val numRows = kotlin.math.ceil(totalSlots.toDouble() / 7.0).toInt()

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0 until numRows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (col in 0..6) {
                                            val cellIndex = row * 7 + col
                                            val dayNumber = cellIndex - startOffset + 1

                                            if (dayNumber < 1) {
                                                // Trailing days of previous month (greyed-out planner style)
                                                val prevDayNumber = prevMonthTotalDays + dayNumber
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(92.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(HdGrayLight.copy(alpha = 0.3f))
                                                        .border(0.5.dp, HdBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(3.dp)
                                                ) {
                                                    Text(
                                                        text = "$prevDayNumber",
                                                        fontSize = 10.sp,
                                                        color = HdTextSecondary.copy(alpha = 0.4f),
                                                        modifier = Modifier.align(Alignment.TopEnd)
                                                    )
                                                }
                                            } else if (dayNumber in 1..totalDays) {
                                                // Interactive days of current month
                                                val dayStr = "Dia %02d".format(dayNumber)
                                                val daySchedules = schedules.filter { it.dayOfWeek == dayStr }
                                                val isSelected = selectedDayInCalendar == dayStr
                                                val hasSchedules = daySchedules.isNotEmpty()
                                                val isHovered = hoveredDay == dayStr

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(92.dp)
                                                        .onGloballyPositioned { coordinates ->
                                                            dropTargets[dayStr] = coordinates.boundsInRoot()
                                                        }
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isHovered) HdPrimaryLight.copy(alpha = 0.8f)
                                                            else if (isSelected) HdPrimaryLight
                                                            else if (hasSchedules) HdSurface
                                                            else HdGrayLight.copy(alpha = 0.5f)
                                                        )
                                                        .border(
                                                            width = if (isHovered || isSelected) 2.dp else 1.dp,
                                                            color = if (isHovered) HdPrimary
                                                                else if (isSelected) HdPrimary
                                                                else if (hasSchedules) HdPrimary.copy(alpha = 0.3f)
                                                                else HdBorder,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            selectedDayInCalendar = dayStr
                                                            schedDay = dayStr
                                                        }
                                                        .padding(3.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "$dayNumber",
                                                            fontWeight = if (isSelected || hasSchedules) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 10.sp,
                                                            color = if (isSelected) HdPrimaryDark else if (hasSchedules) HdTextPrimary else HdTextSecondary,
                                                            modifier = Modifier.align(Alignment.End)
                                                        )

                                                        if (hasSchedules) {
                                                            Column(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                                            ) {
                                                                val maxVisible = 3
                                                                daySchedules.take(maxVisible).forEach { sched ->
                                                                    val (bgColor, primaryColor, label) = getShiftBadgeDetails(sched.shift)
                                                                    val initials = sched.employeeName.split(" ")
                                                                        .filter { it.isNotBlank() }
                                                                        .take(2)
                                                                        .map { it.take(1).uppercase() }
                                                                        .joinToString("")

                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clip(RoundedCornerShape(4.dp))
                                                                            .background(bgColor)
                                                                            .border(0.5.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                                            .padding(horizontal = 3.dp, vertical = 2.dp),
                                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(11.dp)
                                                                                .clip(CircleShape)
                                                                                .background(primaryColor),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Text(
                                                                                text = initials,
                                                                                fontSize = 6.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                color = Color.White
                                                                            )
                                                                        }
                                                                        Text(
                                                                            text = "$initials: $label",
                                                                            fontSize = 6.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = primaryColor,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis
                                                                        )
                                                                    }
                                                                }
                                                                if (daySchedules.size > maxVisible) {
                                                                    Text(
                                                                        text = "+${daySchedules.size - maxVisible}",
                                                                        fontSize = 6.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = HdTextSecondary,
                                                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            Spacer(modifier = Modifier.height(1.dp))
                                                        }
                                                    }
                                                }
                                            } else {
                                                // Filler days of next month (greyed-out planner style)
                                                val nextDayNumber = dayNumber - totalDays
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(92.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(HdGrayLight.copy(alpha = 0.3f))
                                                        .border(0.5.dp, HdBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(3.dp)
                                                ) {
                                                    Text(
                                                        text = "$nextDayNumber",
                                                        fontSize = 10.sp,
                                                        color = HdTextSecondary.copy(alpha = 0.4f),
                                                        modifier = Modifier.align(Alignment.TopEnd)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Shift Legend matching the reference image layout and design
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val legendItems = listOf(
                                    Triple("M", "Manhã 06h-14h", Color(0xFF1976D2)),
                                    Triple("T", "Tarde 14h-22h", Color(0xFF388E3C)),
                                    Triple("P", "Plantão/Noite 22h-06h", Color(0xFFC62828)),
                                    Triple("H", "Horista 10h-18h", Color(0xFF7B1FA2)),
                                    Triple("H2", "Horista 2 09h-18h", Color(0xFF512DA8)),
                                    Triple("F", "Folga", Color(0xFF78909C)),
                                    Triple("R", "Repouso", Color(0xFF8D6E63))
                                )

                                legendItems.forEach { (label, desc, color) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "($desc)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = HdTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Day Details & Action Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Plantão do $selectedDayInCalendar",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = HdTextPrimary
                                    )
                                    Text(
                                        text = selectedMonth,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HdTextSecondary
                                    )
                                }

                                Button(
                                    onClick = {
                                        schedDay = selectedDayInCalendar
                                        if (employees.isNotEmpty()) {
                                            selectedEmployeeId = employees.first().id
                                            showAddScheduleDialog = true
                                        } else {
                                            viewModel.addToast("Cadastre funcionários antes de criar escalas!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Novo Plantão", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Escalar", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            val daySchedules = schedules.filter { it.dayOfWeek == selectedDayInCalendar }
                            if (daySchedules.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("💤", fontSize = 24.sp, modifier = Modifier.padding(bottom = 6.dp))
                                        Text(
                                            text = "Sem escalas para este dia.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = HdTextSecondary
                                        )
                                        Text(
                                            text = "Clique em Escalar para agendar um plantão.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HdTextSecondary.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    daySchedules.forEach { sched ->
                                        
                                        val isDraggingThis = draggedSchedule?.id == sched.id
                                        val isFolga = sched.shift.contains("Folga", ignoreCase = true)
                                        Row(
                                            modifier = Modifier
                                                .onGloballyPositioned { coordinates ->
                                                    schedulePositions[sched.id] = coordinates.positionInRoot()
                                                }
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isDraggingThis) HdPrimaryLight.copy(alpha = 0.5f) else HdGrayLight)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isDraggingThis) HdPrimary else HdBorder,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .pointerInput(sched) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = { offset ->
                                                            draggedEmployee = null
                                                            draggedSchedule = sched
                                                            dragStartOffset = offset
                                                            dragOffset = Offset.Zero
                                                            
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            dragOffset += dragAmount

                                                            // Compute finger root position
                                                            val rootStart = schedulePositions[sched.id] ?: Offset.Zero
                                                            val fingerRootPos = rootStart + dragStartOffset + dragOffset

                                                            // Find drop target day
                                                            val matchedDay = dropTargets.entries.find { entry ->
                                                                entry.value.contains(fingerRootPos)
                                                            }?.key
                                                            hoveredDay = matchedDay
                                                        },
                                                        onDragEnd = {
                                                            
                                                            if (hoveredDay != null) {
                                                                selectedEmployeeId = sched.employeeId
                                                                schedDay = hoveredDay!!
                                                                schedShift = sched.shift
                                                                scheduleToMove = sched
                                                                showAddScheduleDialog = true
                                                            }
                                                            draggedSchedule = null
                                                            hoveredDay = null
                                                        },
                                                        onDragCancel = {
                                                            
                                                            draggedSchedule = null
                                                            hoveredDay = null
                                                        }
                                                    )
                                                }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(PetrolLight),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = sched.employeeName.take(1).uppercase(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = PetrolDark,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = sched.employeeName,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = HdTextPrimary
                                                    )
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isFolga) HdGreenLight else PetrolLight
                                                        ),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = sched.shift,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (isFolga) HdGreen else PetrolDark,
                                                            fontWeight = FontWeight.Medium,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteShiftSchedule(sched) },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(HdRedLight)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remover Plantão",
                                                    tint = HdRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Interactive spreadsheet-style monthly roster table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Tabela Geral de Escalas 📊",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HdPrimaryDark
                                    )
                                    Text(
                                        text = "Toque em um frentista para remover ou no botão (+) de uma célula para escalar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HdTextSecondary
                                    )
                                }
                                
                                // Reset filters or add quick scale option
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HdPrimaryLight),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${schedules.size} Escalas Criadas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = HdPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val (_, totalDays) = getMonthDetails(selectedMonth)
                            val sortedDays = (1..totalDays).map { "Dia %02d".format(it) }
                            val filteredDays = if (hideEmptyDays) {
                                sortedDays.filter { d -> schedules.any { it.dayOfWeek == d } }
                            } else {
                                sortedDays
                            }

                            if (employees.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum colaborador cadastrado. Adicione frentistas na primeira aba.")
                                }
                            } else if (filteredDays.isEmpty() && hideEmptyDays) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Nenhuma escala agendada para este mês ainda.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Desmarque 'Ocultar dias vazios' acima para visualizar todos os dias e criar a escala do mês.", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary, textAlign = TextAlign.Center)
                                    }
                                }
                            } else {
                                // Horizontal scrollable sheet/table of shifts
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        // TABLE HEADER
                                        Row(
                                            modifier = Modifier
                                                .background(HdPrimaryLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Dia", modifier = Modifier.width(65.dp), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = HdPrimaryDark, textAlign = TextAlign.Center)
                                            Text("☀️ Manhã", modifier = Modifier.width(135.dp), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = PetrolDark, textAlign = TextAlign.Center)
                                            Text("⛅ Tarde", modifier = Modifier.width(135.dp), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = HdAmberDark, textAlign = TextAlign.Center)
                                            Text("🌙 Noite", modifier = Modifier.width(135.dp), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = HdRedDark, textAlign = TextAlign.Center)
                                            Text("🟢 Folga", modifier = Modifier.width(110.dp), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = HdGreen, textAlign = TextAlign.Center)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // TABLE ROWS
                                        filteredDays.forEachIndexed { idx, dayStr ->
                                            val daySchedules = schedules.filter { it.dayOfWeek == dayStr }
                                            val rowBg = if (idx % 2 == 1) HdGrayLight.copy(alpha = 0.4f) else Color.Transparent

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(rowBg)
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Column 1: Dia
                                                Box(
                                                    modifier = Modifier.width(65.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = HdPrimaryLight.copy(alpha = 0.3f)),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = dayStr.removePrefix("Dia "),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = HdPrimaryDark,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                // Column 2: Manhã (06h - 14h)
                                                Box(modifier = Modifier.width(135.dp).padding(horizontal = 4.dp)) {
                                                    val shiftScheds = daySchedules.filter { it.shift.contains("Manhã", ignoreCase = true) }
                                                    ShiftCellContent(
                                                        schedules = shiftScheds,
                                                        onAddClick = {
                                                            schedDay = dayStr
                                                            schedShift = "Manhã (06h - 14h)"
                                                            selectedEmployeeId = employees.first().id
                                                            showAddScheduleDialog = true
                                                        },
                                                        onDeleteClick = { sched -> viewModel.deleteShiftSchedule(sched) }
                                                    )
                                                }

                                                // Column 3: Tarde (14h - 22h)
                                                Box(modifier = Modifier.width(135.dp).padding(horizontal = 4.dp)) {
                                                    val shiftScheds = daySchedules.filter { it.shift.contains("Tarde", ignoreCase = true) }
                                                    ShiftCellContent(
                                                        schedules = shiftScheds,
                                                        onAddClick = {
                                                            schedDay = dayStr
                                                            schedShift = "Tarde (14h - 22h)"
                                                            selectedEmployeeId = employees.first().id
                                                            showAddScheduleDialog = true
                                                        },
                                                        onDeleteClick = { sched -> viewModel.deleteShiftSchedule(sched) }
                                                    )
                                                }

                                                // Column 4: Noite (22h - 06h)
                                                Box(modifier = Modifier.width(135.dp).padding(horizontal = 4.dp)) {
                                                    val shiftScheds = daySchedules.filter { it.shift.contains("Noite", ignoreCase = true) }
                                                    ShiftCellContent(
                                                        schedules = shiftScheds,
                                                        onAddClick = {
                                                            schedDay = dayStr
                                                            schedShift = "Noite (22h - 06h)"
                                                            selectedEmployeeId = employees.first().id
                                                            showAddScheduleDialog = true
                                                        },
                                                        onDeleteClick = { sched -> viewModel.deleteShiftSchedule(sched) }
                                                    )
                                                }

                                                // Column 5: Folga
                                                Box(modifier = Modifier.width(110.dp).padding(horizontal = 4.dp)) {
                                                    val shiftScheds = daySchedules.filter { it.shift.contains("Folga", ignoreCase = true) }
                                                    ShiftCellContent(
                                                        schedules = shiftScheds,
                                                        onAddClick = {
                                                            schedDay = dayStr
                                                            schedShift = "Folga (Descanso)"
                                                            selectedEmployeeId = employees.first().id
                                                            showAddScheduleDialog = true
                                                        },
                                                        onDeleteClick = { sched -> viewModel.deleteShiftSchedule(sched) }
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = HdBorder.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Add Employee dialog
    if (showAddEmployeeDialog) {
        Dialog(onDismissRequest = { showAddEmployeeDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Novo Funcionário 👥",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = empName,
                        onValueChange = { empName = it },
                        label = { Text("Nome Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = empPhone,
                        onValueChange = { empPhone = it },
                        label = { Text("Telefone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Cargo:",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Frentista", "Caixa", "Gerente").forEach { r ->
                                FilterChip(
                                    selected = empRole == r,
                                    onClick = { empRole = r },
                                    label = { Text(r, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Turno:",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Manhã", "Tarde", "Noite").forEach { s ->
                                FilterChip(
                                    selected = empShift == s,
                                    onClick = { empShift = s },
                                    label = { Text(s, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEmployeeDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                viewModel.addEmployee(empName, empRole, empPhone, empShift)
                                showAddEmployeeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Modal Add Shift Schedule dialog
    if (showAddScheduleDialog) {
        Dialog(onDismissRequest = { showAddScheduleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Escalar Funcionário 🕒",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    var expandedEmpDropdown by remember { mutableStateOf(false) }
                    val currentSelectedEmpName = employees.find { it.id == selectedEmployeeId }?.name ?: "Selecionar..."

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedEmpDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Colaborador: $currentSelectedEmpName")
                        }
                        DropdownMenu(
                            expanded = expandedEmpDropdown,
                            onDismissRequest = { expandedEmpDropdown = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text(emp.name) },
                                    onClick = {
                                        selectedEmployeeId = emp.id
                                        expandedEmpDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    var expandedDayDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedDayDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Dia: $schedDay")
                        }
                        DropdownMenu(
                            expanded = expandedDayDropdown,
                            onDismissRequest = { expandedDayDropdown = false }
                        ) {
                            daysOfMonth.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        schedDay = day
                                        expandedDayDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    var expandedShiftDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedShiftDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Turno: $schedShift")
                        }
                        DropdownMenu(
                            expanded = expandedShiftDropdown,
                            onDismissRequest = { expandedShiftDropdown = false }
                        ) {
                            shiftNames.forEach { sh ->
                                DropdownMenuItem(
                                    text = { Text(sh) },
                                    onClick = {
                                        schedShift = sh
                                        expandedShiftDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddScheduleDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val emp = employees.find { it.id == selectedEmployeeId }
                                if (emp != null) {
                                    val oldSched = scheduleToMove
                                    if (oldSched != null) {
                                        viewModel.deleteShiftSchedule(oldSched)
                                        scheduleToMove = null
                                    }
                                    viewModel.addShiftSchedule(emp.id, emp.name, schedDay, schedShift)
                                }
                                showAddScheduleDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }

        // Floating Drag Overlay
        if (draggedEmployee != null || draggedSchedule != null) {
            val label = draggedEmployee?.name ?: draggedSchedule?.employeeName ?: ""
            val startPos = if (draggedEmployee != null) {
                employeePositions[draggedEmployee!!.id] ?: Offset.Zero
            } else {
                schedulePositions[draggedSchedule!!.id] ?: Offset.Zero
            }
            val currentFingerPos = startPos + dragStartOffset + dragOffset
            val localFingerPos = currentFingerPos - screenRootOffset

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (localFingerPos.x - 60.dp.toPx()).toInt().coerceAtLeast(0),
                            y = (localFingerPos.y - 30.dp.toPx()).toInt().coerceAtLeast(0)
                        )
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(HdPrimary.copy(alpha = 0.9f))
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Export Preview Dialog for Shift Schedules
        if (showExportPreviewDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showExportPreviewDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Exportação de Escalas CSV 📄",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Escala de $selectedMonth pronta para exportar. Copie o conteúdo ou compartilhe como arquivo CSV diretamente no WhatsApp da equipe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HdTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Scrollable CSV preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HdGrayLight)
                                .border(1.dp, HdBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = exportPreviewText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                color = HdTextPrimary
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Escala de Plantões", exportPreviewText)
                                        clipboard.setPrimaryClip(clip)
                                        viewModel.addToast("Copiado para a área de transferência!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar CSV", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        shareScheduleCSV(context, selectedMonth, exportPreviewText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartilhar", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Compartilhar", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    shareScheduleCSVWhatsApp(context, selectedMonth, exportPreviewText, viewModel)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("💬 Enviar via WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showExportPreviewDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fechar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(viewModel: PostoViewModel) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("Reunião") }
    var eventDate by remember { mutableStateOf("2026-07-06") }
    var eventTime by remember { mutableStateOf("10:00") }
    var eventDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calendário de Marcações 📅",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gerencie manutenções, inspeções ANP e entregas programadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = {
                        eventTitle = ""
                        eventDesc = ""
                        showAddEventDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agendar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agendar")
                }
            }
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum compromisso agendado.")
                    }
                }
            }
        } else {
            items(appointments) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = event.date.takeLast(2),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AmberDark
                                    )
                                    Text(
                                        text = event.time,
                                        fontSize = 10.sp,
                                        color = AmberDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = event.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Data", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = event.date, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteAppointment(event) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Concluir/Remover", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Modal Add Event Dialog
    if (showAddEventDialog) {
        Dialog(onDismissRequest = { showAddEventDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Novo Agendamento 📅",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Título do Evento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tipo de Agendamento:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    val categories = listOf(
                        "Reunião" to "🤝",
                        "Treinamento" to "📚",
                        "Manutenção" to "🛠️",
                        "Ocorrência" to "⚠️",
                        "Evento" to "🎉"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { (cat, emoji) ->
                            val isSelected = eventCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else HdGrayLight)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { eventCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = cat, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = if (isSelected) MaterialTheme.colorScheme.primary else HdTextPrimary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Data (AAAA-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = { eventTime = it },
                        label = { Text("Horário (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventDesc,
                        onValueChange = { eventDesc = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEventDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val fullTitle = "[$eventCategory] $eventTitle"
                                viewModel.addAppointment(fullTitle, eventDate, eventTime, eventDesc)
                                showAddEventDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(viewModel: PostoViewModel) {
    val reports by viewModel.dailyReports.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()

    var showAddReportDialog by remember { mutableStateOf(false) }
    var reportDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var reportFuelName by remember { mutableStateOf("Gasolina Comum") }
    var reportOpeningStock by remember { mutableStateOf("15000") }
    var reportReceivedVolume by remember { mutableStateOf("5000") }
    var reportSalesLiters by remember { mutableStateOf("2200") }
    var reportRevenue by remember { mutableStateOf("12800") }
    var reportTxCount by remember { mutableStateOf("150") }
    var reportObservation by remember { mutableStateOf("") }

    val context = LocalContext.current
    var filterStartDate by remember { mutableStateOf("2026-07-01") }
    var filterEndDate by remember { mutableStateOf("2026-07-31") }
    var filterFuelType by remember { mutableStateOf("Todos") }
    val fuelOptions = listOf("Todos", "Gasolina Comum", "Etanol Hidratado", "Diesel S10")
    var showExportPreviewDialog by remember { mutableStateOf(false) }
    var exportPreviewText by remember { mutableStateOf("") }
    var exportTypeSelected by remember { mutableStateOf("CSV") } // "CSV" or "TABELA"

    val filteredReportsForExport = remember(reports, filterStartDate, filterEndDate, filterFuelType) {
        reports.filter { r ->
            val dateMatch = r.date >= filterStartDate && r.date <= filterEndDate
            val fuelMatch = filterFuelType == "Todos" || r.fuelName.equals(filterFuelType, ignoreCase = true)
            dateMatch && fuelMatch
        }.sortedBy { it.date }
    }

    var showCalibReportDialog by remember { mutableStateOf(false) }
    var calibFilterStartDate by remember { mutableStateOf("2026-07-01") }
    var calibFilterEndDate by remember { mutableStateOf("2026-07-07") }
    val nozzles by viewModel.nozzles.collectAsStateWithLifecycle()
    val calibrations by viewModel.calibrations.collectAsStateWithLifecycle()
    var selectedNozzleIds by remember { mutableStateOf(setOf<String>()) }
    
    // Initialize selectedNozzleIds when dialog opens or nozzles load
    LaunchedEffect(nozzles) {
        if (selectedNozzleIds.isEmpty() && nozzles.isNotEmpty()) {
            selectedNozzleIds = nozzles.map { it.nozzleNumber }.toSet()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Livro de Movimentação de Combustíveis (LMC) 📋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controle diário obrigatório de estoque, recebimento e vendas de combustíveis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = {
                        reportDate = "2026-07-04"
                        reportFuelName = "Gasolina Comum"
                        reportOpeningStock = "15000"
                        reportReceivedVolume = "0"
                        reportSalesLiters = "2200"
                        reportRevenue = "12800"
                        reportTxCount = "150"
                        reportObservation = ""
                        showAddReportDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Fechamento")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lançar LMC")
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Balanço Geral de Vendas LMC 💰",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Faturamento Total Acumulado: R$ ${String.format(Locale.getDefault(), "%,.2f", reports.sumOf { it.totalSales })}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Volume total comercializado: ${String.format(Locale.getDefault(), "%,.0f", reports.sumOf { it.litersSold })} Litros em ${reports.sumOf { it.transactionsCount }} atendimentos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // NEW: Calibration Records Export Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, HdBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = HdPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registros de Calibração",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HdTextPrimary
                        )
                    }
                    Button(
                        onClick = { showCalibReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gerar Relatório",
                            color = Color(0xFFDC2626),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Export & Interactive Table Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📊", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Column {
                                Text(
                                    text = "Exportação & Tabela de Vendas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HdTextPrimary
                                )
                                Text(
                                    text = "Filtre e gere relatórios estruturados para contabilidade.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HdTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filters Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = filterStartDate,
                            onValueChange = { filterStartDate = it },
                            label = { Text("Início (AAAA-MM-DD)", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = filterEndDate,
                            onValueChange = { filterEndDate = it },
                            label = { Text("Fim (AAAA-MM-DD)", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fuel filter & dropdown
                    var expandedFuelFilter by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedFuelFilter = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HdTextPrimary)
                        ) {
                            Text(text = "Filtro Combustível: $filterFuelType ▾", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        DropdownMenu(
                            expanded = expandedFuelFilter,
                            onDismissRequest = { expandedFuelFilter = false }
                        ) {
                            fuelOptions.forEach { fuel ->
                                DropdownMenuItem(
                                    text = { Text(fuel, fontSize = 12.sp) },
                                    onClick = {
                                        filterFuelType = fuel
                                        expandedFuelFilter = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TABLE PREVIEW
                    Text(
                        text = "Pré-visualização da Tabela",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HdTextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, HdBorder, RoundedCornerShape(8.dp))
                    ) {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column(modifier = Modifier.widthIn(min = 620.dp)) {
                                // Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PetrolLight)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Data", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PetrolDark)
                                    Text("Combustível", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PetrolDark)
                                    Text("Vol. Vendido", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PetrolDark, textAlign = TextAlign.End)
                                    Text("Faturamento", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PetrolDark, textAlign = TextAlign.End)
                                    Text("Vendas", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PetrolDark, textAlign = TextAlign.End)
                                }

                                if (filteredReportsForExport.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Nenhum fechamento para os filtros ativos.", fontSize = 12.sp, color = HdTextSecondary)
                                    }
                                } else {
                                    filteredReportsForExport.forEach { r ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(r.date, modifier = Modifier.weight(1f), fontSize = 11.sp, color = HdTextPrimary)
                                            Text(r.fuelName, modifier = Modifier.weight(1.8f), fontSize = 11.sp, color = HdTextPrimary, fontWeight = FontWeight.Medium)
                                            Text("${String.format(Locale.getDefault(), "%,.0f", r.litersSold)} L", modifier = Modifier.weight(1.3f), fontSize = 11.sp, textAlign = TextAlign.End, color = HdTextPrimary)
                                            Text("R$ ${String.format(Locale.getDefault(), "%,.2f", r.totalSales)}", modifier = Modifier.weight(1.5f), fontSize = 11.sp, textAlign = TextAlign.End, color = HdPrimary, fontWeight = FontWeight.SemiBold)
                                            Text("${r.transactionsCount}", modifier = Modifier.weight(0.9f), fontSize = 11.sp, textAlign = TextAlign.End, color = HdTextSecondary)
                                        }
                                        HorizontalDivider(color = HdBorder.copy(alpha = 0.5f))
                                    }

                                    // TOTALS ROW
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(HdPrimaryLight.copy(alpha = 0.3f))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL", modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = HdPrimaryDark)
                                        Text("-", modifier = Modifier.weight(1.8f), fontSize = 11.sp, color = HdTextSecondary)
                                        Text("${String.format(Locale.getDefault(), "%,.0f", filteredReportsForExport.sumOf { it.litersSold })} L", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, color = HdPrimaryDark)
                                        Text("R$ ${String.format(Locale.getDefault(), "%,.2f", filteredReportsForExport.sumOf { it.totalSales })}", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, color = HdPrimaryDark)
                                        Text("${filteredReportsForExport.sumOf { it.transactionsCount }}", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, color = HdPrimaryDark)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ACTION BUTTONS GROUP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                if (filteredReportsForExport.isEmpty()) {
                                    viewModel.addToast("Sem dados para exportar!")
                                } else {
                                    exportTypeSelected = "CSV"
                                    exportPreviewText = generateCSV(filteredReportsForExport)
                                    showExportPreviewDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f).testTag("export_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar CSV", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                if (filteredReportsForExport.isEmpty()) {
                                    viewModel.addToast("Sem dados para exportar!")
                                } else {
                                    exportTypeSelected = "TABELA"
                                    exportPreviewText = generateTextTable(filteredReportsForExport)
                                    showExportPreviewDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PetrolDark),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f).testTag("copy_table_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Copiar Tabela", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                if (filteredReportsForExport.isEmpty()) {
                                    viewModel.addToast("Sem dados para exportar!")
                                } else {
                                    exportReportsToPdf(
                                        context = context,
                                        reports = filteredReportsForExport,
                                        stationName = stationRazaoSocial.ifBlank { "Posto Estrela Cadente" },
                                        stationCnpj = stationCnpj.ifBlank { "12.345.678/0001-99" }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f).testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Exportar PDF", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // List / Table Header
        item {
            Text(
                text = "Fechamentos Diários Registrados (LMC)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Historical entries (rendered as tables)
        if (reports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum fechamento lançado no LMC.")
                    }
                }
            }
        } else {
            items(reports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = BorderStroke(1.dp, HdBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📋", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text(
                                        text = "${report.fuelName} — ${report.date}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = HdTextPrimary
                                    )
                                    Text(
                                        text = "${report.transactionsCount} vendas | Faturamento: R$ ${String.format(Locale.getDefault(), "%,.2f", report.totalSales)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HdTextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteDailyReport(report) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover Relatório", tint = HdRed)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HdBorder)

                        // Stock movement table/row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Estoque Abertura", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                Text(text = "${String.format(Locale.getDefault(), "%,.0f", report.openingStock)} L", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text(text = "Recebido (+)", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                Text(text = "${String.format(Locale.getDefault(), "%,.0f", report.receivedVolume)} L", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = if (report.receivedVolume > 0) HdGreen else HdTextPrimary)
                            }
                            Column {
                                Text(text = "Vendido (-)", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                Text(text = "${String.format(Locale.getDefault(), "%,.0f", report.litersSold)} L", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = HdRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Estoque Fechamento", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                Text(text = "${String.format(Locale.getDefault(), "%,.0f", report.closingStock)} L", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = HdPrimaryDark)
                            }
                        }

                        if (report.observation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                                    Text(text = "📝 ", style = MaterialTheme.typography.bodySmall)
                                    Text(text = report.observation, style = MaterialTheme.typography.bodySmall, color = HdTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        if (showAddReportDialog) {
Dialog(onDismissRequest = { showAddReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Lançar Livro LMC Diário 📋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = reportDate,
                        onValueChange = { reportDate = it },
                        label = { Text("Data do Fechamento (AAAA-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportFuelName,
                        onValueChange = { reportFuelName = it },
                        label = { Text("Combustível (ex: Gasolina Comum)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportOpeningStock,
                        onValueChange = { reportOpeningStock = it },
                        label = { Text("Estoque de Abertura (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportReceivedVolume,
                        onValueChange = { reportReceivedVolume = it },
                        label = { Text("Volume Recebido no Dia (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportSalesLiters,
                        onValueChange = { reportSalesLiters = it },
                        label = { Text("Volume Vendido no Dia (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportRevenue,
                        onValueChange = { reportRevenue = it },
                        label = { Text("Faturamento de Vendas (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportTxCount,
                        onValueChange = { reportTxCount = it },
                        label = { Text("Quantidade de Vendas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportObservation,
                        onValueChange = { reportObservation = it },
                        label = { Text("Observação / Ocorrências") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // Real-time stock balancing preview
                    val op = reportOpeningStock.toDoubleOrNull() ?: 0.0
                    val rec = reportReceivedVolume.toDoubleOrNull() ?: 0.0
                    val sold = reportSalesLiters.toDoubleOrNull() ?: 0.0
                    val closing = op + rec - sold

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HdPrimaryLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Estoque de Fechamento Estimado:", fontSize = 12.sp, color = HdPrimaryDark, fontWeight = FontWeight.Bold)
                            Text(text = "${String.format(Locale.getDefault(), "%,.0f", closing)} L", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = HdPrimaryDark)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddReportDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                val rev = reportRevenue.toDoubleOrNull() ?: 0.0
                                val lit = reportSalesLiters.toDoubleOrNull() ?: 0.0
                                val tx = reportTxCount.toIntOrNull() ?: 0
                                viewModel.addDailyReport(
                                    date = reportDate,
                                    fuelName = reportFuelName,
                                    openingStock = op,
                                    receivedVolume = rec,
                                    litersSold = lit,
                                    closingStock = closing,
                                    totalSales = rev,
                                    transactionsCount = tx,
                                    observation = reportObservation
                                )
                                showAddReportDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Dialog to show export preview and allow copying / sharing
    if (showExportPreviewDialog) {
        Dialog(onDismissRequest = { showExportPreviewDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val titleText = when (exportTypeSelected) {
                        "CSV" -> "Exportação de Vendas CSV 📄"
                        "CSV_AUDITS" -> "Exportação de Auditoria CSV 📄"
                        "TEXT_AUDITS" -> "Tabela de Auditoria Formatada 📋"
                        else -> "Tabela de Vendas Formatada 📋"
                    }

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Dados prontos para exportar ou copiar. Use os botões abaixo para copiar, compartilhar ou enviar por WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Scrollable code preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HdGrayLight)
                            .border(1.dp, HdBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportPreviewText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = HdTextPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Relatorio Posto", exportPreviewText)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.addToast("Copiado para a área de transferência!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Copiar", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copiar", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Relatório Posto")
                                        putExtra(Intent.EXTRA_TEXT, exportPreviewText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartilhar", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compartilhar", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, exportPreviewText)
                                        setPackage("com.whatsapp")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, exportPreviewText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar via..."))
                                    viewModel.addToast("WhatsApp não instalado. Usando compartilhamento padrão.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💬 Enviar via WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showExportPreviewDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}



private fun generateCSV(data: List<DailyReport>): String {
    val sb = java.lang.StringBuilder()
    sb.append("Data,Combustivel,Volume Vendido (L),Faturamento (RS),Transacoes\n")
    data.forEach { r ->
        sb.append("${r.date},${r.fuelName},${r.litersSold},${r.totalSales},${r.transactionsCount}\n")
    }
    val totalLiters = data.sumOf { it.litersSold }
    val totalRevenue = data.sumOf { it.totalSales }
    val totalTx = data.sumOf { it.transactionsCount }
    sb.append("TOTAL,-,${String.format(Locale.US, "%.1f", totalLiters)},${String.format(Locale.US, "%.2f", totalRevenue)},$totalTx\n")
    return sb.toString()
}

private fun generateTextTable(data: List<DailyReport>): String {
    val sb = java.lang.StringBuilder()
    sb.append("+------------+--------------------+------------------+------------------+------------+\n")
    sb.append("| Data       | Combustível        | Vol. Vendido (L) | Faturamento (R$) | Transações |\n")
    sb.append("+------------+--------------------+------------------+------------------+------------+\n")
    data.forEach { r ->
        val datePadded = r.date.padEnd(10)
        val fuelPadded = if (r.fuelName.length > 18) r.fuelName.take(15) + "..." else r.fuelName.padEnd(18)
        val volFormatted = String.format(Locale.getDefault(), "%,.0f L", r.litersSold)
        val volPadded = volFormatted.padStart(16)
        val salesFormatted = String.format(Locale.getDefault(), "R$ %,.2f", r.totalSales)
        val salesPadded = salesFormatted.padStart(16)
        val txPadded = r.transactionsCount.toString().padStart(10)
        sb.append("| $datePadded | $fuelPadded | $volPadded | $salesPadded | $txPadded |\n")
    }
    sb.append("+------------+--------------------+------------------+------------------+------------+\n")
    val totalLiters = String.format(Locale.getDefault(), "%,.0f L", data.sumOf { it.litersSold }).padStart(16)
    val totalRevenue = String.format(Locale.getDefault(), "R$ %,.2f", data.sumOf { it.totalSales }).padStart(16)
    val totalTx = data.sumOf { it.transactionsCount }.toString().padStart(10)
    sb.append("| TOTAL      |                    | $totalLiters | $totalRevenue | $totalTx |\n")
    sb.append("+------------+--------------------+------------------+------------------+------------+\n")
    return sb.toString()
}

private fun exportReportsToPdf(
    context: Context,
    reports: List<DailyReport>,
    stationName: String,
    stationCnpj: String
) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        
        // A4 Landscape is 842 wide x 595 tall
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        
        // DRAW BRANDED HEADER BLOCK
        paint.color = android.graphics.Color.rgb(37, 99, 235) // Blue-600 (HdPrimary)
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRect(0f, 0f, 842f, 80f, paint)
        
        // Header Text - Razão Social
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(stationName.uppercase(Locale.getDefault()), 40f, 42f, paint)
        
        // Header Text - Subtitles
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.rgb(219, 234, 254) // Blue-100
        canvas.drawText("CNPJ: $stationCnpj | LIVRO DE MOVIMENTAÇÃO DE COMBUSTÍVEIS (LMC)", 40f, 60f, paint)
        
        // Right header info
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("RELATÓRIO DIÁRIO DE OPERAÇÃO", 802f, 42f, paint)
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.rgb(219, 234, 254) // Blue-100
        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Gerado em: $currentDate", 802f, 60f, paint)
        
        // Reset Alignment
        paint.textAlign = android.graphics.Paint.Align.LEFT
        
        // SECTION TITLE
        paint.color = android.graphics.Color.rgb(31, 41, 55) // Gray-800
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Histórico Completo de Vendas e Fechamento de Caixa", 40f, 115f, paint)
        
        // TABLE CONFIGURATION
        val colX = floatArrayOf(40f, 130f, 260f, 340f, 420f, 500f, 580f, 720f)
        val headers = arrayOf("Data", "Combustível", "Est. Inicial", "Recebido", "Vol. Vendido", "Est. Final", "Faturamento", "Vendas")
        
        // Draw Table Header Background
        paint.color = android.graphics.Color.rgb(243, 244, 246) // Gray-100
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRect(40f, 130f, 802f, 155f, paint)
        
        // Draw Table Header border lines
        paint.color = android.graphics.Color.rgb(209, 213, 219) // Gray-300
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(40f, 130f, 802f, 155f, paint)
        
        // Draw Table Header Texts
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.rgb(55, 65, 81) // Gray-700
        paint.textSize = 9f
        paint.isFakeBoldText = true
        
        for (i in headers.indices) {
            val align = if (i in 2..6) android.graphics.Paint.Align.RIGHT else android.graphics.Paint.Align.LEFT
            paint.textAlign = align
            val xPos = if (align == android.graphics.Paint.Align.RIGHT) {
                if (i == 6) colX[i] + 110f else colX[i] + 65f
            } else colX[i]
            canvas.drawText(headers[i], xPos, 146f, paint)
        }
        
        // TABLE BODY
        paint.textAlign = android.graphics.Paint.Align.LEFT
        var y = 175f
        var totalSold = 0.0
        var totalSales = 0.0
        var totalTx = 0
        
        val sortedReports = reports.sortedByDescending { it.date }
        
        sortedReports.forEachIndexed { idx, rep ->
            if (y > 510f) {
                paint.color = android.graphics.Color.rgb(156, 163, 175)
                paint.textSize = 9f
                paint.isFakeBoldText = false
                canvas.drawText("... Excedeu o limite da página. Baixe o CSV para ver todo o histórico.", 40f, y, paint)
                return@forEachIndexed
            }
            
            // Alternating Row Background
            if (idx % 2 == 1) {
                paint.color = android.graphics.Color.rgb(249, 250, 251) // Gray-50
                paint.style = android.graphics.Paint.Style.FILL
                canvas.drawRect(40f, y - 15f, 802f, y + 8f, paint)
            }
            
            // Row Border Bottom
            paint.color = android.graphics.Color.rgb(229, 231, 235) // Gray-200
            paint.style = android.graphics.Paint.Style.STROKE
            canvas.drawLine(40f, y + 8f, 802f, y + 8f, paint)
            
            // Draw content
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = android.graphics.Color.rgb(17, 24, 39) // Gray-900
            paint.textSize = 9f
            paint.isFakeBoldText = false
            
            // Data
            paint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText(rep.date, colX[0], y, paint)
            
            // Fuel Name
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.rgb(37, 99, 235) // blue
            canvas.drawText(rep.fuelName, colX[1], y, paint)
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.rgb(17, 24, 39)
            
            // Opening Stock
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f L", rep.openingStock), colX[2] + 65f, y, paint)
            
            // Received
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f L", rep.receivedVolume), colX[3] + 65f, y, paint)
            
            // Liters Sold
            paint.isFakeBoldText = true
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f L", rep.litersSold), colX[4] + 65f, y, paint)
            paint.isFakeBoldText = false
            
            // Closing Stock
            canvas.drawText(String.format(Locale.getDefault(), "%,.0f L", rep.closingStock), colX[5] + 65f, y, paint)
            
            // Faturamento
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.rgb(22, 163, 74) // Green-600
            canvas.drawText(String.format(Locale.getDefault(), "R$ %,.2f", rep.totalSales), colX[6] + 110f, y, paint)
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.rgb(17, 24, 39)
            
            // Tx Count
            paint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText(rep.transactionsCount.toString(), colX[7], y, paint)
            
            totalSold += rep.litersSold
            totalSales += rep.totalSales
            totalTx += rep.transactionsCount
            
            y += 22f
        }
        
        // TOTAL ROW
        paint.color = android.graphics.Color.rgb(219, 234, 254) // Blue-100
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRect(40f, y - 10f, 802f, y + 15f, paint)
        
        paint.color = android.graphics.Color.rgb(29, 78, 216) // Blue-700
        paint.style = android.graphics.Paint.Style.STROKE
        canvas.drawRect(40f, y - 10f, 802f, y + 15f, paint)
        
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.rgb(30, 58, 138) // Blue-900
        paint.textSize = 10f
        paint.isFakeBoldText = true
        
        paint.textAlign = android.graphics.Paint.Align.LEFT
        canvas.drawText("SOMA TOTAL", colX[0], y + 6f, paint)
        
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.getDefault(), "%,.0f L", totalSold), colX[4] + 65f, y + 6f, paint)
        canvas.drawText(String.format(Locale.getDefault(), "R$ %,.2f", totalSales), colX[6] + 110f, y + 6f, paint)
        
        paint.textAlign = android.graphics.Paint.Align.LEFT
        canvas.drawText(totalTx.toString(), colX[7], y + 6f, paint)
        
        // SIGNATURE / AUDIT SECTION
        val sigY = y + 45f
        if (sigY < 550f) {
            paint.color = android.graphics.Color.rgb(156, 163, 175) // Gray-400
            paint.strokeWidth = 1f
            paint.style = android.graphics.Paint.Style.STROKE
            
            canvas.drawLine(40f, sigY, 240f, sigY, paint)
            canvas.drawLine(562f, sigY, 762f, sigY, paint)
            
            paint.style = android.graphics.Paint.Style.FILL
            paint.textSize = 8f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.rgb(107, 114, 128) // Gray-500
            
            canvas.drawText("Assinatura do Gerente Geral", 40f, sigY + 12f, paint)
            canvas.drawText("Visto do Autor de Conformidade", 562f, sigY + 12f, paint)
        }
        
        // FOOTER
        paint.color = android.graphics.Color.rgb(156, 163, 175) // Gray-400
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.LEFT
        canvas.drawText("Gerado pelo aplicativo Meu Posto - Controle LMC. Todos os direitos reservados.", 40f, 575f, paint)
        
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("Página 1 de 1", 802f, 575f, paint)
        
        pdfDocument.finishPage(page)
        
        // SAVE FILE IN CACHE & SHARE
        val pdfFile = java.io.File(context.cacheDir, "relatorio_diario_posto.pdf")
        val fileOutputStream = java.io.FileOutputStream(pdfFile)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        // Share Intent using FileProvider
        val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Relatório LMC - Fechamento de Caixa")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = android.content.Intent.createChooser(intent, "Exportar Relatório PDF")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

private fun generateAuditsCSV(data: List<AuditLogEntry>): String {
    val sb = java.lang.StringBuilder()
    sb.append("Data,Hora,Acao,Alvo,Operador,Status,Detalhes\n")
    data.forEach { l ->
        sb.append("${l.date},${l.time},${l.actionType},${l.target},${l.operator},${l.complianceStatus},${l.details}\n")
    }
    return sb.toString()
}

private fun generateAuditsTextTable(data: List<AuditLogEntry>): String {
    val sb = java.lang.StringBuilder()
    sb.append("+------------+-------+-------------------------+----------------------+--------------------+------------+\n")
    sb.append("| Data       | Hora  | Acao / Evento           | Alvo / Registro      | Operador / Respons.| Status     |\n")
    sb.append("+------------+-------+-------------------------+----------------------+--------------------+------------+\n")
    data.forEach { l ->
        val datePadded = l.date.padEnd(10)
        val timePadded = l.time.padEnd(5)
        val actionPadded = if (l.actionType.length > 23) l.actionType.take(20) + "..." else l.actionType.padEnd(23)
        val targetPadded = if (l.target.length > 20) l.target.take(17) + "..." else l.target.padEnd(20)
        val opPadded = if (l.operator.length > 18) l.operator.take(15) + "..." else l.operator.padEnd(18)
        val statusPadded = l.complianceStatus.padEnd(10)
        sb.append("| $datePadded | $timePadded | $actionPadded | $targetPadded | $opPadded | $statusPadded |\n")
    }
    sb.append("+------------+-------+-------------------------+----------------------+--------------------+------------+\n")
    return sb.toString()
}

private fun generateBackupJson(
    fuelTanks: List<FuelTank>,
    employees: List<Employee>,
    shifts: List<ShiftSchedule>,
    appointments: List<Appointment>,
    reports: List<DailyReport>,
    nozzles: List<Nozzle>,
    calibrations: List<Calibration>,
    conformity: List<FuelConformityRecord>,
    audits: List<AuditLogEntry>,
    credentials: List<SystemCredential>,
    users: List<UserAccount>
): String {
    val backup = org.json.JSONObject()
    
    val tanksArray = org.json.JSONArray()
    fuelTanks.forEach { t ->
        val obj = org.json.JSONObject().apply {
            put("id", t.id)
            put("name", t.name)
            put("capacity", t.capacity)
            put("currentLevel", t.currentLevel)
            put("threshold", t.threshold)
            put("pricePerLiter", t.pricePerLiter)
            put("stationCnpj", t.stationCnpj)
            put("color", t.color)
        }
        tanksArray.put(obj)
    }
    backup.put("fuelTanks", tanksArray)

    val empArray = org.json.JSONArray()
    employees.forEach { e ->
        val obj = org.json.JSONObject().apply {
            put("id", e.id)
            put("name", e.name)
            put("role", e.role)
            put("phone", e.phone)
            put("activeShift", e.activeShift)
            put("stationCnpj", e.stationCnpj)
        }
        empArray.put(obj)
    }
    backup.put("employees", empArray)

    val shiftArray = org.json.JSONArray()
    shifts.forEach { s ->
        val obj = org.json.JSONObject().apply {
            put("id", s.id)
            put("employeeId", s.employeeId)
            put("employeeName", s.employeeName)
            put("dayOfWeek", s.dayOfWeek)
            put("shift", s.shift)
            put("stationCnpj", s.stationCnpj)
        }
        shiftArray.put(obj)
    }
    backup.put("shiftSchedules", shiftArray)

    val appArray = org.json.JSONArray()
    appointments.forEach { a ->
        val obj = org.json.JSONObject().apply {
            put("id", a.id)
            put("title", a.title)
            put("date", a.date)
            put("time", a.time)
            put("description", a.description)
            put("stationCnpj", a.stationCnpj)
        }
        appArray.put(obj)
    }
    backup.put("appointments", appArray)

    val repArray = org.json.JSONArray()
    reports.forEach { r ->
        val obj = org.json.JSONObject().apply {
            put("id", r.id)
            put("date", r.date)
            put("fuelName", r.fuelName)
            put("openingStock", r.openingStock)
            put("receivedVolume", r.receivedVolume)
            put("litersSold", r.litersSold)
            put("closingStock", r.closingStock)
            put("totalSales", r.totalSales)
            put("transactionsCount", r.transactionsCount)
            put("observation", r.observation)
            put("stationCnpj", r.stationCnpj)
        }
        repArray.put(obj)
    }
    backup.put("dailyReports", repArray)

    val nozArray = org.json.JSONArray()
    nozzles.forEach { n ->
        val obj = org.json.JSONObject().apply {
            put("id", n.id)
            put("nozzleNumber", n.nozzleNumber)
            put("pumpName", n.pumpName)
            put("tankId", n.tankId)
            put("tankName", n.tankName)
            put("fuelType", n.fuelType)
            put("status", n.status)
            put("stationCnpj", n.stationCnpj)
            put("color", n.color)
        }
        nozArray.put(obj)
    }
    backup.put("nozzles", nozArray)

    val calArray = org.json.JSONArray()
    calibrations.forEach { c ->
        val obj = org.json.JSONObject().apply {
            put("id", c.id)
            put("date", c.date)
            put("referenceName", c.referenceName)
            put("nominalVolume", c.nominalVolume)
            put("measuredVolume", c.measuredVolume)
            put("errorPercent", c.errorPercent)
            put("inspector", c.inspector)
            put("laudo", c.laudo)
            put("isConforme", c.isConforme)
            put("stationCnpj", c.stationCnpj)
        }
        calArray.put(obj)
    }
    backup.put("calibrations", calArray)

    val confArray = org.json.JSONArray()
    conformity.forEach { cf ->
        val obj = org.json.JSONObject().apply {
            put("id", cf.id)
            put("date", cf.date)
            put("fuelType", cf.fuelType)
            put("densityMeasured", cf.densityMeasured)
            put("temperature", cf.temperature)
            put("ethanolPercent", cf.ethanolPercent)
            put("aspectColor", cf.aspectColor)
            put("isConforme", cf.isConforme)
            put("technicianName", cf.technicianName)
            put("observation", cf.observation)
            put("stationCnpj", cf.stationCnpj)
        }
        confArray.put(obj)
    }
    backup.put("fuelConformityRecords", confArray)

    val audArray = org.json.JSONArray()
    audits.forEach { ad ->
        val obj = org.json.JSONObject().apply {
            put("id", ad.id)
            put("date", ad.date)
            put("time", ad.time)
            put("actionType", ad.actionType)
            put("target", ad.target)
            put("details", ad.details)
            put("operator", ad.operator)
            put("complianceStatus", ad.complianceStatus)
            put("stationCnpj", ad.stationCnpj)
        }
        audArray.put(obj)
    }
    backup.put("auditLogEntries", audArray)

    val credArray = org.json.JSONArray()
    credentials.forEach { cr ->
        val obj = org.json.JSONObject().apply {
            put("id", cr.id)
            put("systemName", cr.systemName)
            put("category", cr.category)
            put("login", cr.login)
            put("password", cr.password)
            put("description", cr.description)
        }
        credArray.put(obj)
    }
    backup.put("systemCredentials", credArray)

    val userArray = org.json.JSONArray()
    users.forEach { u ->
        val obj = org.json.JSONObject().apply {
            put("email", u.email)
            put("name", u.name)
            put("role", u.role)
            put("password", u.password)
            put("stationName", u.stationName)
            put("stationCnpj", u.stationCnpj)
            put("stationEndereco", u.stationEndereco)
            put("parentManagerEmail", u.parentManagerEmail)
            put("bankName", u.bankName)
            put("bankAgency", u.bankAgency)
            put("bankAccount", u.bankAccount)
            put("bankPixKey", u.bankPixKey)
        }
        userArray.put(obj)
    }
    backup.put("userAccounts", userArray)

    return backup.toString(4)
}

private fun getMonthDetails(month: String): Pair<Int, Int> {
    return when (month) {
        "Julho de 2026" -> Pair(3, 31) // July 1st is Wednesday (Sunday=0, Mon=1, Tue=2, Wed=3)
        "Agosto de 2026" -> Pair(6, 31) // August 1st is Saturday
        "Setembro de 2026" -> Pair(2, 30) // September 1st is Tuesday
        "Outubro de 2026" -> Pair(4, 31) // October 1st is Thursday
        else -> Pair(3, 31)
    }
}

@Composable
fun SystemsScreen(viewModel: PostoViewModel) {
    val isUnlocked by viewModel.isSystemsUnlocked.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationEndereco by viewModel.stationEndereco.collectAsStateWithLifecycle()
    val bankName by viewModel.bankName.collectAsStateWithLifecycle()
    val bankAgency by viewModel.bankAgency.collectAsStateWithLifecycle()
    val bankAccount by viewModel.bankAccount.collectAsStateWithLifecycle()
    val bankPixKey by viewModel.bankPixKey.collectAsStateWithLifecycle()
    val credentials by viewModel.systemCredentials.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Dialog state for editing bank info
    var showEditBankDialog by remember { mutableStateOf(false) }
    var editBankName by remember { mutableStateOf("") }
    var editBankAgency by remember { mutableStateOf("") }
    var editBankAccount by remember { mutableStateOf("") }
    var editBankPix by remember { mutableStateOf("") }

    // Dialog state for editing station info
    var showEditStationDialog by remember { mutableStateOf(false) }
    var editStationRazaoSocial by remember { mutableStateOf("") }
    var editStationCnpj by remember { mutableStateOf("") }
    var editStationEndereco by remember { mutableStateOf("") }

    // Dialog state for adding credential
    var showAddCredDialog by remember { mutableStateOf(false) }
    var newCredName by remember { mutableStateOf("") }
    var newCredCategory by remember { mutableStateOf("Operacional") }
    var newCredLogin by remember { mutableStateOf("") }
    var newCredPass by remember { mutableStateOf("") }
    var newCredDesc by remember { mutableStateOf("") }

    // Dialog state for adding authorized viewers
    var showAddViewerDialog by remember { mutableStateOf(false) }
    var newViewerName by remember { mutableStateOf("") }
    var newViewerEmail by remember { mutableStateOf("") }
    var newViewerPassword by remember { mutableStateOf("") }

    // State for tracking visible passwords per credential ID
    var visiblePasswords by remember { mutableStateOf(setOf<Int>()) }

    if (!isUnlocked) {
        // LOCK SCREEN
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HdGrayLight.copy(alpha = 0.5f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HdBorder),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(HdPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Cadeado",
                            tint = HdPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Área Restrita da Gerência",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = HdTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Para acessar credenciais dos sistemas do posto, contas bancárias e dados corporativos, digite a senha do gerente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HdTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Senha do Gerente") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("systems_password_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Senha", tint = HdTextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                                    tint = HdTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HdPrimary,
                            unfocusedBorderColor = HdBorder
                        )
                    )

                    if (loginError != null) {
                        Text(
                            text = loginError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val success = viewModel.unlockSystems(passwordInput)
                            if (success) {
                                loginError = null
                                passwordInput = ""
                            } else {
                                loginError = "Senha inválida! Tente novamente."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("systems_unlock_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Acessar Painel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Surface(
                        color = PetrolLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💡 Senha padrão de teste: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = PetrolDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "adm001",
                                style = MaterialTheme.typography.bodySmall,
                                color = PetrolDark,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    } else {
        // UNLOCKED SCREEN
        var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Sistemas, 1 = Conta Bancária, 2 = Dados do Posto, 3 = Visualizadores, 4 = Cloud

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unlocked Dashboard Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HdGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Desbloqueado", tint = HdGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Painel Administrativo",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = HdTextPrimary
                            )
                            Text(
                                text = "Dados sensíveis e credenciais corporativas",
                                style = MaterialTheme.typography.bodySmall,
                                color = HdTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.lockSystems() },
                        colors = ButtonDefaults.buttonColors(containerColor = HdRedLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Bloquear", tint = HdRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bloquear", fontSize = 11.sp, color = HdRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tabs navigation row
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = HdSurface,
                contentColor = HdPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, HdBorder, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Logins", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Banco", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Posto", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("Equipe", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Cloud", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (activeSubTab) {
                0 -> {
                    // TAB 0: SYSTEM CREDENTIALS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Credenciais dos Sistemas (${credentials.size}) 🔑",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HdTextPrimary
                        )

                        Button(
                            onClick = {
                                newCredName = ""
                                newCredCategory = "Operacional"
                                newCredLogin = ""
                                newCredPass = ""
                                newCredDesc = ""
                                showAddCredDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Credencial", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Credencial", fontSize = 11.sp)
                        }
                    }

                    if (credentials.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdSurface),
                            border = BorderStroke(1.dp, HdBorder)
                        ) {
                            Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhuma credencial cadastrada.", color = HdTextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        credentials.forEach { cred ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = HdSurface),
                                border = BorderStroke(1.dp, HdBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = cred.systemName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = HdTextPrimary
                                                )
                                                // Badge for category
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = when (cred.category) {
                                                            "Operacional" -> PetrolLight
                                                            "Fiscal" -> HdAmberLight
                                                            "Equipamentos" -> HdPrimaryLight
                                                            "Segurança" -> HdGreenLight
                                                            else -> HdGrayLight
                                                        }
                                                    ),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = cred.category,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (cred.category) {
                                                            "Operacional" -> PetrolDark
                                                            "Fiscal" -> HdAmberDark
                                                            "Equipamentos" -> HdPrimaryDark
                                                            "Segurança" -> HdGreen
                                                            else -> HdTextSecondary
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            if (cred.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = cred.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = HdTextSecondary
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteSystemCredential(cred.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remover Credencial", tint = HdRed, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = HdBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Login row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Usuário / Login:", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                            Text(
                                                text = cred.login,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = HdTextPrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Login ${cred.systemName}", cred.login)
                                                clipboard.setPrimaryClip(clip)
                                                viewModel.addToast("Login copiado!")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar usuário", tint = HdPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Password row
                                    val isPassVisible = visiblePasswords.contains(cred.id)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Senha do Sistema:", style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                                            Text(
                                                text = if (isPassVisible) cred.password else "••••••••••••",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPassVisible) PetrolDark else HdTextSecondary
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    visiblePasswords = if (isPassVisible) {
                                                        visiblePasswords - cred.id
                                                    } else {
                                                        visiblePasswords + cred.id
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (isPassVisible) "Esconder senha" else "Mostrar senha",
                                                    tint = HdTextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Senha ${cred.systemName}", cred.password)
                                                    clipboard.setPrimaryClip(clip)
                                                    viewModel.addToast("Senha copiada!")
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar senha", tint = HdPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: BANK ACCOUNT DETAILS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Conta Bancária Corporativa 🏦",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HdTextPrimary
                        )

                        Button(
                            onClick = {
                                editBankName = bankName
                                editBankAgency = bankAgency
                                editBankAccount = bankAccount
                                editBankPix = bankPixKey
                                showEditBankDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Dados", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Dados", fontSize = 11.sp)
                        }
                    }

                    // Luxury Card Representation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HdPrimaryDark),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONTA CORRENTE PJ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "🇧🇷 BRASIL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = bankName.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("AGÊNCIA", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(bankAgency, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("CONTA CORRENTE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(bankAccount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Bank Info Details & Copy Actions List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            BankInfoRow(context, viewModel, "Instituição Financeira", bankName)
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.3f))
                            BankInfoRow(context, viewModel, "Agência", bankAgency)
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.3f))
                            BankInfoRow(context, viewModel, "Conta Corrente PJ", bankAccount)
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.3f))
                            BankInfoRow(context, viewModel, "Chave PIX (CNPJ)", bankPixKey)
                        }
                    }
                }
                2 -> {
                    // TAB 2: STATION DETAILS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Informações do Posto (Matriz) 🏢",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HdTextPrimary
                        )

                        Button(
                            onClick = {
                                editStationRazaoSocial = stationRazaoSocial
                                editStationCnpj = stationCnpj
                                editStationEndereco = stationEndereco
                                showEditStationDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Posto", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Informações", fontSize = 11.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StationDetailItem(context, viewModel, "Razão Social", stationRazaoSocial, "🏢")
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.4f))
                            StationDetailItem(context, viewModel, "CNPJ", stationCnpj, "📄")
                            HorizontalDivider(color = HdBorder.copy(alpha = 0.4f))
                            StationDetailItem(context, viewModel, "Endereço Completo", stationEndereco, "📍")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sessão Ativa do Usuário 👤",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HdTextPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Usuário conectado atualmente no aplicativo via autenticação integrada.",
                                fontSize = 11.sp,
                                color = HdTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HdGrayLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentUser?.name ?: "Usuário Admin",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = HdTextPrimary
                                    )
                                    Text(
                                        text = currentUser?.email ?: "admin@posto.com",
                                        fontSize = 11.sp,
                                        color = HdTextSecondary
                                    )
                                    Text(
                                        text = "Perfil: ${currentUser?.role ?: "Gerente"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HdPrimary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.logout() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HdRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sair",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sair da Conta", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 3: AUTHORIZED VIEWERS MANAGEMENT
                    val userList by viewModel.userAccounts.collectAsStateWithLifecycle()
                    val manager = viewModel.currentUser.collectAsStateWithLifecycle().value
                    val viewers = userList.filter { it.parentManagerEmail == manager?.email }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visualizadores Cadastrados (${viewers.size}/5) 👥",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HdTextPrimary
                        )

                        if (!viewModel.isReadOnly.value) {
                            Button(
                                onClick = {
                                    newViewerName = ""
                                    newViewerEmail = ""
                                    newViewerPassword = ""
                                    showAddViewerDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                shape = RoundedCornerShape(8.dp),
                                enabled = viewers.size < 5
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Novo Visualizador", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo Acesso", fontSize = 11.sp)
                            }
                        }
                    }

                    Text(
                        text = "Gerentes podem autorizar até 5 outros usuários para visualizar os relatórios e estoques do posto em tempo real. Esses usuários terão acesso estritamente de leitura (sem direito a alterar dados).",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary
                    )

                    if (viewers.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdSurface),
                            border = BorderStroke(1.dp, HdBorder)
                        ) {
                            Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhum usuário visualizador cadastrado.", color = HdTextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        viewers.forEach { viewer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = HdSurface),
                                border = BorderStroke(1.dp, HdBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PetrolLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = PetrolDark, modifier = Modifier.size(20.dp))
                                        }
                                        Column {
                                            Text(
                                                text = viewer.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = HdTextPrimary
                                            )
                                            Text(
                                                text = viewer.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = HdTextSecondary
                                            )
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = HdAmberLight),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Acesso: Somente Leitura ✓",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HdAmberDark,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!viewModel.isReadOnly.value) {
                                        IconButton(
                                            onClick = { viewModel.deleteViewer(viewer.email) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remover Acesso", tint = HdRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // TAB 4: SUPABASE CLOUD SYNC
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Nuvem Supabase ☁️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HdTextPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Sincronize e restaure todo o banco de dados com a nuvem de forma segura em 1 clique.",
                                fontSize = 11.sp,
                                color = HdTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            val initialConfig = remember { viewModel.getSavedSupabaseConfig() }
                            var sbUrl by remember { mutableStateOf(initialConfig.first) }
                            var sbKey by remember { mutableStateOf(initialConfig.second) }
                            var supabaseAvailableTrigger by remember { mutableStateOf(viewModel.isSupabaseAvailable()) }

                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (supabaseAvailableTrigger) Color(0xFFD4EDDA) else Color(0xFFFFF3CD),
                                border = BorderStroke(1.dp, if (supabaseAvailableTrigger) Color(0xFFC3E6CB) else Color(0xFFFFEBAA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (supabaseAvailableTrigger) "⚡ Supabase Cloud Sync: ATIVO e CONFIGURADO" else "⚠️ Supabase Off-line (Usando banco Room local)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (supabaseAvailableTrigger) Color(0xFF155724) else Color(0xFF856404)
                                    )
                                }
                            }

                            var showConfigForm by remember { mutableStateOf(false) }

                            OutlinedButton(
                                onClick = { showConfigForm = !showConfigForm },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, HdPrimary)
                            ) {
                                Icon(
                                    if (showConfigForm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = HdPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showConfigForm) "Ocultar Parâmetros de Conexão" else "Configurar Credenciais Supabase",
                                    fontSize = 11.sp,
                                    color = HdPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showConfigForm) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = sbUrl,
                                        onValueChange = { sbUrl = it },
                                        label = { Text("Supabase URL", fontSize = 11.sp) },
                                        placeholder = { Text("https://xxxx.supabase.co", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                    )

                                    OutlinedTextField(
                                        value = sbKey,
                                        onValueChange = { sbKey = it },
                                        label = { Text("Anon / Public Key", fontSize = 11.sp) },
                                        placeholder = { Text("eyJhbGciOi...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                    )

                                    // SQL Quick setup tip
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = HdPrimaryLight.copy(alpha = 0.2f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "💡 Guia Rápido de Configuração:",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HdTextPrimary,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            Text(
                                                text = "1. Crie uma conta gratuita em supabase.com e inicie um projeto.\n2. No SQL Editor, execute o seguinte comando:\n\ncreate table posto_backups (\n  id text primary key,\n  data jsonb,\n  updated_at timestamp with time zone default now()\n);\n\n3. Desative RLS para posto_backups ou crie política de acesso total.",
                                                fontSize = 9.sp,
                                                lineHeight = 12.sp,
                                                color = HdTextSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (sbUrl.isEmpty() || sbKey.isEmpty()) {
                                                    viewModel.addToast("Preencha todos os campos do Supabase!")
                                                } else {
                                                    viewModel.saveSupabaseConfig(sbUrl, sbKey)
                                                    supabaseAvailableTrigger = viewModel.isSupabaseAvailable()
                                                    showConfigForm = false
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Salvar", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Salvar", fontSize = 11.sp)
                                        }

                                        if (sbUrl.isNotEmpty() || sbKey.isNotEmpty()) {
                                            Button(
                                                onClick = {
                                                    viewModel.clearSupabaseConfig()
                                                    sbUrl = ""
                                                    sbKey = ""
                                                    supabaseAvailableTrigger = viewModel.isSupabaseAvailable()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Limpar", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Limpar", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.syncToSupabase(stationCnpj) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (supabaseAvailableTrigger) HdPrimary else Color.Gray),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = supabaseAvailableTrigger
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = "Backup", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enviar Backup", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.downloadFromSupabase(stationCnpj) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (supabaseAvailableTrigger) Color(0xFF0D6EFD) else Color.Gray),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = supabaseAvailableTrigger
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = "Restaurar", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Baixar Dados", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HdSurface),
                        border = BorderStroke(1.dp, HdBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Backup do Banco de Dados Local (Offline) 💾",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HdTextPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Exporte todos os dados atuais do posto (tanques, frentistas, agendas, bicos, aferições e logs) para um arquivo JSON no seu dispositivo.",
                                fontSize = 11.sp,
                                color = HdTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    try {
                                        val backupJson = viewModel.exportFullStateToJson()
                                        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                        val filename = "backup_posto_admin_$dateStr.json"
                                        
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, filename)
                                            putExtra(Intent.EXTRA_TEXT, backupJson)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Compartilhar ou Salvar Backup JSON"))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Erro ao gerar backup: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Baixar Backup", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Baixar Backup (.json)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS SECTION

    // 1. EDIT BANK INFO DIALOG
    if (showEditBankDialog) {
        Dialog(onDismissRequest = { showEditBankDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Editar Dados Bancários 🏦",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = HdTextPrimary
                    )

                    OutlinedTextField(
                        value = editBankName,
                        onValueChange = { editBankName = it },
                        label = { Text("Nome do Banco") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBankAgency,
                        onValueChange = { editBankAgency = it },
                        label = { Text("Agência") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBankAccount,
                        onValueChange = { editBankAccount = it },
                        label = { Text("Conta Corrente") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBankPix,
                        onValueChange = { editBankPix = it },
                        label = { Text("Chave PIX (CNPJ)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditBankDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                viewModel.updateBankInfo(
                                    name = editBankName,
                                    agency = editBankAgency,
                                    account = editBankAccount,
                                    pix = editBankPix
                                )
                                showEditBankDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // 2. EDIT STATION INFO DIALOG
    if (showEditStationDialog) {
        Dialog(onDismissRequest = { showEditStationDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Editar Dados do Posto 🏢",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = HdTextPrimary
                    )

                    OutlinedTextField(
                        value = editStationRazaoSocial,
                        onValueChange = { editStationRazaoSocial = it },
                        label = { Text("Razão Social") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStationCnpj,
                        onValueChange = { editStationCnpj = it },
                        label = { Text("CNPJ") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStationEndereco,
                        onValueChange = { editStationEndereco = it },
                        label = { Text("Endereço Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditStationDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                viewModel.updateStationInfo(
                                    razaoSocial = editStationRazaoSocial,
                                    cnpj = editStationCnpj,
                                    endereco = editStationEndereco
                                )
                                showEditStationDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // 3. ADD SYSTEM CREDENTIAL DIALOG
    if (showAddCredDialog) {
        Dialog(onDismissRequest = { showAddCredDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nova Credencial de Sistema 🔑",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = HdTextPrimary
                    )

                    OutlinedTextField(
                        value = newCredName,
                        onValueChange = { newCredName = it },
                        label = { Text("Nome do Sistema *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple category selector row
                    Text(text = "Categoria:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf("Operacional", "Fiscal", "Equipamentos", "Segurança")
                        categories.forEach { cat ->
                            val isSel = newCredCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) HdPrimaryLight else HdGrayLight)
                                    .clickable { newCredCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) HdPrimaryDark else HdTextSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCredLogin,
                        onValueChange = { newCredLogin = it },
                        label = { Text("Usuário / E-mail *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCredPass,
                        onValueChange = { newCredPass = it },
                        label = { Text("Senha *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCredDesc,
                        onValueChange = { newCredDesc = it },
                        label = { Text("Descrição / Endereço IP / URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddCredDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                if (newCredName.isBlank() || newCredLogin.isBlank() || newCredPass.isBlank()) {
                                    viewModel.addToast("Por favor, preencha todos os campos obrigatórios!")
                                } else {
                                    viewModel.addSystemCredential(
                                        name = newCredName,
                                        category = newCredCategory,
                                        login = newCredLogin,
                                        pass = newCredPass,
                                        desc = newCredDesc
                                    )
                                    showAddCredDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cadastrar")
                        }
                    }
                }
            }
        }
    }

    // 4. ADD AUTHORIZED VIEWER DIALOG
    if (showAddViewerDialog) {
        Dialog(onDismissRequest = { showAddViewerDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Autorizar Novo Visualizador 👥",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = HdTextPrimary
                    )

                    Text(
                        text = "Crie um login secundário com acesso apenas de visualização ligado ao seu posto de combustíveis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary
                    )

                    OutlinedTextField(
                        value = newViewerName,
                        onValueChange = { newViewerName = it },
                        label = { Text("Nome do Visualizador *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newViewerEmail,
                        onValueChange = { newViewerEmail = it },
                        label = { Text("E-mail do Visualizador *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newViewerPassword,
                        onValueChange = { newViewerPassword = it },
                        label = { Text("Senha do Visualizador *") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddViewerDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                if (newViewerName.isBlank() || newViewerEmail.isBlank() || newViewerPassword.isBlank()) {
                                    viewModel.addToast("Por favor, preencha todos os campos obrigatórios!")
                                } else {
                                    val success = viewModel.registerViewer(
                                        name = newViewerName,
                                        email = newViewerEmail,
                                        pass = newViewerPassword
                                    )
                                    if (success) {
                                        showAddViewerDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Autorizar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BankInfoRow(context: Context, viewModel: PostoViewModel, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = HdTextPrimary)
        }
        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.setPrimaryClip(clip)
                viewModel.addToast("$label copiado!")
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HdPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun StationDetailItem(context: Context, viewModel: PostoViewModel, label: String, value: String, emoji: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(HdGrayLight),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = HdTextSecondary)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HdTextPrimary
                )
            }
        }
        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.setPrimaryClip(clip)
                viewModel.addToast("$label copiado!")
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HdPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AnimatedTankView(
    fuelName: String,
    currentLevel: Double,
    capacity: Double,
    threshold: Double,
    isLow: Boolean,
    modifier: Modifier = Modifier
) {
    val fillPercent = (currentLevel / capacity).toFloat().coerceIn(0f, 1f)

    // Fluid wave animation phase
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Fluid colors based on fuel type
    val isGasolinaAditivada = fuelName.contains("Aditivada", ignoreCase = true)
    val isGasolinaComum = fuelName.contains("Gasolina", ignoreCase = true) && !isGasolinaAditivada
    val isEtanol = fuelName.contains("Etanol", ignoreCase = true)
    val isDieselS10 = fuelName.contains("S10", ignoreCase = true)
    val isDieselS500 = fuelName.contains("S500", ignoreCase = true)
    val isGenericDiesel = fuelName.contains("Diesel", ignoreCase = true) && !isDieselS10 && !isDieselS500

    val fluidColors = when {
        isLow -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C)) // Red alarm for low level
        isGasolinaAditivada -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)) // Blue
        isGasolinaComum -> listOf(Color(0xFFFBBF24), Color(0xFFD97706)) // Yellow/Amber
        isEtanol -> listOf(Color(0xFF10B981), Color(0xFF047857)) // Green
        isDieselS10 -> listOf(Color(0xFF94A3B8), Color(0xFF475569)) // Silver/Prata
        isDieselS500 -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C)) // Red
        isGenericDiesel -> listOf(Color(0xFFCBD5E1), Color(0xFF64748B)) // Default Silver
        else -> listOf(Color(0xFF60A5FA), Color(0xFF2563EB)) // Default Blue
    }

    // Secondary wave color (for overlapping depth effect)
    val backgroundWaveColor = fluidColors.first().copy(alpha = 0.4f)
    val foregroundWaveBrush = Brush.verticalGradient(colors = fluidColors)

    Box(
        modifier = modifier
            .width(100.dp)
            .height(180.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dimensions of the actual storage tank part
            val tankLeft = width * 0.12f
            val tankRight = width * 0.88f
            val tankWidth = tankRight - tankLeft
            val tankTop = height * 0.15f
            val tankBottom = height * 0.85f
            val tankHeight = tankBottom - tankTop

            // 1. Draw Metallic Tank Top Cap (Dome)
            val capPath = Path().apply {
                moveTo(tankLeft, tankTop)
                cubicTo(
                    tankLeft, tankTop - 25f,
                    tankRight, tankTop - 25f,
                    tankRight, tankTop
                )
                lineTo(tankLeft, tankTop)
            }
            drawPath(path = capPath, color = Color(0xFF94A3B8)) // Slate-400

            // 2. Draw Metallic Tank Bottom Base (Pedestal)
            val baseRectPath = Path().apply {
                moveTo(tankLeft - 5f, tankBottom)
                lineTo(tankRight + 5f, tankBottom)
                lineTo(tankRight, tankBottom + 12f)
                lineTo(tankLeft, tankBottom + 12f)
                close()
            }
            drawPath(path = baseRectPath, color = Color(0xFF64748B)) // Slate-500

            // 3. Draw glass container body background
            val bodyPath = Path().apply {
                moveTo(tankLeft, tankTop)
                lineTo(tankRight, tankTop)
                lineTo(tankRight, tankBottom)
                lineTo(tankLeft, tankBottom)
                close()
            }
            drawPath(path = bodyPath, color = Color(0xFFF1F5F9)) // Very light gray interior

            // 4. Draw Liquid inside the body (clipped to the glass body shape)
            clipPath(path = bodyPath) {
                // Calculate current liquid level height
                val liquidY = tankBottom - (tankHeight * fillPercent)

                // Only draw liquid if level is greater than 0
                if (fillPercent > 0.01f) {
                    // Background Wave Path (offset phase by Math.PI for opposite motion)
                    val bgWavePath = Path()
                    bgWavePath.moveTo(tankLeft, tankBottom)
                    bgWavePath.lineTo(tankLeft, liquidY)

                    val step = 5f
                    var x = tankLeft
                    while (x <= tankRight) {
                        val relativeX = (x - tankLeft) / tankWidth
                        val y = liquidY + 6f * kotlin.math.sin(relativeX * 2 * Math.PI + phase + Math.PI).toFloat()
                        bgWavePath.lineTo(x, y)
                        x += step
                    }
                    bgWavePath.lineTo(tankRight, tankBottom)
                    bgWavePath.close()

                    drawPath(path = bgWavePath, color = backgroundWaveColor)

                    // Foreground Wave Path
                    val fgWavePath = Path()
                    fgWavePath.moveTo(tankLeft, tankBottom)
                    fgWavePath.lineTo(tankLeft, liquidY)

                    x = tankLeft
                    while (x <= tankRight) {
                        val relativeX = (x - tankLeft) / tankWidth
                        val y = liquidY + 6f * kotlin.math.sin(relativeX * 2 * Math.PI - phase).toFloat()
                        fgWavePath.lineTo(x, y)
                        x += step
                    }
                    fgWavePath.lineTo(tankRight, tankBottom)
                    fgWavePath.close()

                    drawPath(path = fgWavePath, brush = foregroundWaveBrush)
                }

                // Draw horizontal volume ticks on the glass body (on top of liquid)
                val tickColor = Color(0x33475569) // Semi-transparent slate
                for (percent in listOf(0.25f, 0.5f, 0.75f)) {
                    val tickY = tankBottom - (tankHeight * percent)
                    drawLine(
                        color = tickColor,
                        start = androidx.compose.ui.geometry.Offset(tankLeft, tickY),
                        end = androidx.compose.ui.geometry.Offset(tankLeft + 12f, tickY),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = tickColor,
                        start = androidx.compose.ui.geometry.Offset(tankRight - 12f, tickY),
                        end = androidx.compose.ui.geometry.Offset(tankRight, tickY),
                        strokeWidth = 2f
                    )
                }
            }

            // 5. Draw reflections & highlights on top of glass
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(tankLeft + 6f, tankTop + 4f),
                end = androidx.compose.ui.geometry.Offset(tankLeft + 6f, tankBottom - 4f),
                strokeWidth = 3f
            )

            // Outer tank glass border outline
            drawPath(
                path = bodyPath,
                color = Color(0xFFCBD5E1), // Slate-300
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
private fun ShiftCellContent(
    schedules: List<com.example.data.ShiftSchedule>,
    onAddClick: () -> Unit,
    onDeleteClick: (com.example.data.ShiftSchedule) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (schedules.isEmpty()) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Escalar",
                    tint = HdTextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            schedules.forEach { sched ->
                val isFolga = sched.shift.contains("Folga", ignoreCase = true)
                val bgColor = if (isFolga) HdGreenLight else PetrolLight
                val textColor = if (isFolga) HdGreen else PetrolDark
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .clickable { onDeleteClick(sched) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sched.employeeName.split(" ").firstOrNull() ?: sched.employeeName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remover",
                        tint = HdRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Adicionar mais",
                    tint = HdTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun generateSchedulesCSV(month: String, schedules: List<com.example.data.ShiftSchedule>): String {
    val sb = java.lang.StringBuilder()
    sb.append("Escala de Trabalho - $month\n")
    sb.append("Dia,Manha,Tarde,Noite,Folga\n")
    
    val totalDays = getMonthDetails(month).second
    val sortedDays = (1..totalDays).map { "Dia %02d".format(it) }
    
    sortedDays.forEach { dayStr ->
        val daySchedules = schedules.filter { it.dayOfWeek == dayStr }
        
        val manhaList = daySchedules.filter { it.shift.contains("Manhã", ignoreCase = true) }.map { it.employeeName }
        val tardeList = daySchedules.filter { it.shift.contains("Tarde", ignoreCase = true) }.map { it.employeeName }
        val noiteList = daySchedules.filter { it.shift.contains("Noite", ignoreCase = true) }.map { it.employeeName }
        val folgaList = daySchedules.filter { it.shift.contains("Folga", ignoreCase = true) }.map { it.employeeName }
        
        val manha = manhaList.joinToString(" / ")
        val tarde = tardeList.joinToString(" / ")
        val noite = noiteList.joinToString(" / ")
        val folga = folgaList.joinToString(" / ")
        
        sb.append("${dayStr.removePrefix("Dia ")},\"$manha\",\"$tarde\",\"$noite\",\"$folga\"\n")
    }
    return sb.toString()
}

private fun shareScheduleCSV(context: android.content.Context, month: String, csvText: String) {
    try {
        val sanitizedMonth = month.replace(" ", "_").lowercase()
        val file = java.io.File(context.cacheDir, "escala_$sanitizedMonth.csv")
        val writer = java.io.FileWriter(file)
        writer.write(csvText)
        writer.close()

        val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Escala de Plantão - $month")
            putExtra(android.content.Intent.EXTRA_TEXT, "Segue a escala de plantão para $month.")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = android.content.Intent.createChooser(intent, "Compartilhar Escala CSV")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Erro ao compartilhar CSV: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

private fun shareScheduleCSVWhatsApp(context: android.content.Context, month: String, csvText: String, viewModel: PostoViewModel) {
    try {
        val sanitizedMonth = month.replace(" ", "_").lowercase()
        val file = java.io.File(context.cacheDir, "escala_$sanitizedMonth.csv")
        val writer = java.io.FileWriter(file)
        writer.write(csvText)
        writer.close()

        val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Escala de Plantão - $month")
                putExtra(android.content.Intent.EXTRA_TEXT, "Segue anexa a escala de plantão de $month.")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Escala de Plantão - $month")
                putExtra(android.content.Intent.EXTRA_TEXT, "Segue anexa a escala de plantão de $month.")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(intent, "Compartilhar via...")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            viewModel.addToast("WhatsApp não instalado. Usando compartilhamento padrão.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        viewModel.addToast("Erro ao gerar/compartilhar CSV: ${e.message}")
    }
}

@Composable
private fun getShiftBadgeDetails(shiftName: String): Triple<Color, Color, String> {
    return when {
        shiftName.contains("Manhã", ignoreCase = true) -> Triple(
            Color(0xFFE3F2FD), // Light Blue Bg
            Color(0xFF1976D2), // Blue Primary/Text
            "M"
        )
        shiftName.contains("Tarde", ignoreCase = true) -> Triple(
            Color(0xFFE8F5E9), // Light Green Bg
            Color(0xFF388E3C), // Green Primary/Text
            "T"
        )
        shiftName.contains("Noite", ignoreCase = true) || shiftName.contains("Plantão", ignoreCase = true) -> Triple(
            Color(0xFFFFEBEE), // Light Red Bg
            Color(0xFFC62828), // Red Primary/Text
            "P"
        )
        shiftName.contains("Horista 2", ignoreCase = true) -> Triple(
            Color(0xFFEDE7F6), // Light Purple Bg
            Color(0xFF512DA8), // Deep Purple Primary/Text
            "H2"
        )
        shiftName.contains("Horista", ignoreCase = true) -> Triple(
            Color(0xFFF3E5F5), // Light Violet Bg
            Color(0xFF7B1FA2), // Violet Primary/Text
            "H"
        )
        shiftName.contains("Folga", ignoreCase = true) -> Triple(
            Color(0xFFECEFF1), // Light Grey Bg
            Color(0xFF78909C), // Grey Primary/Text
            "F"
        )
        shiftName.contains("Repouso", ignoreCase = true) -> Triple(
            Color(0xFFEFEBE9), // Light Brown Bg
            Color(0xFF8D6E63), // Brown Primary/Text
            "R"
        )
        else -> Triple(
            Color(0xFFECEFF1),
            Color(0xFF78909C),
            "F"
        )
    }
}


@Composable
fun ControleQualidadeScreen(viewModel: PostoViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = HdSurface,
            contentColor = HdPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Aferição (Bombas)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Análise (Combustível)", fontWeight = FontWeight.Bold) }
            )
        }
        
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                AfericaoScreen(viewModel)
            } else {
                AnaliseScreen(viewModel)
            }
        }
    }
}


@Composable
fun AfericaoScreen(viewModel: PostoViewModel) {
    val nozzles by viewModel.nozzles.collectAsStateWithLifecycle()
    val calibrations by viewModel.calibrations.collectAsStateWithLifecycle()
    val auditLogEntries by viewModel.auditLogEntries.collectAsStateWithLifecycle()
    var showAddCalibDialog by remember { mutableStateOf(false) }
    var calibDate by remember { mutableStateOf("") }
    var calibTime by remember { mutableStateOf("") }
    var calibReference by remember { mutableStateOf("") }
    var calibNominal by remember { mutableStateOf("20.0") }
    var calibMeasured by remember { mutableStateOf("20.0") }

    var calibDeviationMl by remember { mutableStateOf("0") }
    var calibLaudo by remember { mutableStateOf("Aferição física periódica do bico.") }
    var showCalibReportDialog by remember { mutableStateOf(false) }
    var selectedCalibrationIds by remember { mutableStateOf(setOf<Int>()) }

    

var calibInspector by remember { mutableStateOf("") }
    
    var reportDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    
    
    val mContext = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Legal Compliance Metric Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HdBorder),
                    colors = CardDefaults.cardColors(containerColor = HdSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Controle Legal de Bombas (INMETRO) ⚖️",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = HdTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Margem de erro legal admitida nas aferições: ±0.5% (equivalente a ±100ml de erro em volume nominal de 20L). Bicos com erros acima do limite devem ser interditados imediatamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = HdTextSecondary
                            )

                            val compliantCount = calibrations.count { it.isConforme }
                            val totalCount = calibrations.size
                            val pct = if (totalCount > 0) (compliantCount.toDouble() / totalCount * 100).toInt() else 100

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (pct >= 90) HdGreenLight else HdRedLight)
                                ) {
                                    Text(
                                        text = "$pct% DE CONFORMIDADE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pct >= 90) HdGreen else HdRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$compliantCount de $totalCount aferições regulares",
                                    fontSize = 11.sp,
                                    color = HdTextSecondary
                                )
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
                    }
                }
            }

            if (calibrations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, HdBorder),
                        colors = CardDefaults.cardColors(containerColor = HdSurface)
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhuma aferição cadastrada.")
                        }
                    }
                }
            } else {
                items(calibrations) { cal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HdBorder),
                        colors = CardDefaults.cardColors(containerColor = HdSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Checkbox(
                                        checked = selectedCalibrationIds.contains(cal.id),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                selectedCalibrationIds = selectedCalibrationIds + cal.id
                                            } else {
                                                selectedCalibrationIds = selectedCalibrationIds - cal.id
                                            }
                                        }
                                    )
                                    Text(text = "🔬", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(text = cal.referenceName, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                                        Text(text = "Data: ${cal.date} | Fiscal: ${cal.inspector}", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                                    }
                                }

                                // Conformity Indicator Badge
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (cal.isConforme) HdGreenLight else HdRedLight)
                                ) {
                                    Text(
                                        text = if (cal.isConforme) "✓ CONFORME" else "✗ REPROVADO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cal.isConforme) HdGreen else HdRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HdBorder)

                            // Volume stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Volume Nominal", fontSize = 11.sp, color = HdTextSecondary)
                                    Text(text = "${String.format(Locale.getDefault(), "%.2f", cal.nominalVolume)} L", fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(text = "Volume Medido", fontSize = 11.sp, color = HdTextSecondary)
                                    Text(text = "${String.format(Locale.getDefault(), "%.2f", cal.measuredVolume)} L", fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Erro Calculado", fontSize = 11.sp, color = HdTextSecondary)
                                    val errStr = if (cal.errorPercent >= 0) "+${String.format(Locale.getDefault(), "%.2f", cal.errorPercent)}%" else "${String.format(Locale.getDefault(), "%.2f", cal.errorPercent)}%"
                                    Text(
                                        text = errStr,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (cal.isConforme) HdTextPrimary else HdRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Laudo Técnico:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                                    Text(text = cal.laudo, style = MaterialTheme.typography.bodySmall, color = HdTextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { viewModel.deleteCalibration(cal) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Excluir Registro", color = HdRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }


    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarAfericaoScreen(viewModel: PostoViewModel) {
    val nozzles by viewModel.nozzles.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var calibDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var calibReference by remember { mutableStateOf("") }
    var calibNominal by remember { mutableStateOf("20.0") }
    var calibMeasured by remember { mutableStateOf("20.0") }
    var calibDeviationMl by remember { mutableStateOf("0") }
    var calibInspector by remember { mutableStateOf("Carlos Silva") }
    var calibLaudo by remember { mutableStateOf("Medição dentro do regulamento legal do INMETRO.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HdSurface),
            border = BorderStroke(1.dp, HdBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Registrar Nova Aferição Física ⚖️",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HdPrimary
                )
                
                Text(
                    text = "Insira os dados da medida de 20 litros para aferição do bico.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HdTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = calibDate,
                    onValueChange = { calibDate = it },
                    label = { Text("Data da Aferição (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Identificação do Bico / Equipamento *",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )

                OutlinedTextField(
                    value = calibReference,
                    onValueChange = { calibReference = it },
                    label = { Text("Nome do Bico ou Equipamento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (nozzles.isNotEmpty()) {
                    Text(
                        text = "Seleção rápida:",
                        style = MaterialTheme.typography.bodySmall,
                        color = HdTextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        nozzles.forEach { nz ->
                            val isSelected = calibReference == "Bico ${nz.nozzleNumber}" || calibReference == nz.nozzleNumber
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HdPrimaryLight else HdGrayLight)
                                    .clickable { calibReference = "Bico ${nz.nozzleNumber}" }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Bico ${nz.nozzleNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) HdPrimaryDark else HdTextPrimary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Erro de Calibração (ml na Medida de 20L):",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )

                OutlinedTextField(
                    value = calibDeviationMl,
                    onValueChange = { 
                        if (it.isEmpty() || it == "-" || it == "+") {
                            calibDeviationMl = it
                        } else {
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null) {
                                calibDeviationMl = it
                            }
                        }
                    },
                    label = { Text("Desvio do Volume (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = (calibDeviationMl.toDoubleOrNull() ?: 0.0) !in ANPUtils.MIN_CALIB_DEVIATION_ML..ANPUtils.MAX_CALIB_DEVIATION_ML,
                    supportingText = {
                        val parsed = calibDeviationMl.toDoubleOrNull()
                        if (parsed == null && calibDeviationMl.isNotEmpty() && calibDeviationMl != "-" && calibDeviationMl != "+") {
                            Text("Valor inválido", color = MaterialTheme.colorScheme.error)
                        } else if (parsed != null && parsed !in ANPUtils.MIN_CALIB_DEVIATION_ML..ANPUtils.MAX_CALIB_DEVIATION_ML) {
                            Text("Fora do limite técnico de ±200ml!", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Limite técnico: -200 ml a +200 ml")
                        }
                    }
                )

                val devOptions = listOf("0", "-20", "-40", "-60", "-80", "-100", "+20", "+40", "+60", "+80", "+100")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    devOptions.forEach { item ->
                        val isSelected = calibDeviationMl == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) HdPrimaryLight else HdGrayLight)
                                .clickable { calibDeviationMl = item }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = if (item == "0") "0 ml" else "$item ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = calibInspector,
                    onValueChange = { calibInspector = it },
                    label = { Text("Nome do Fiscal/Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = calibLaudo,
                    onValueChange = { calibLaudo = it },
                    label = { Text("Laudo / Observações Técnicas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                val deviationVal = calibDeviationMl.toDoubleOrNull()
                val isValidDeviation = deviationVal != null && deviationVal in ANPUtils.MIN_CALIB_DEVIATION_ML..ANPUtils.MAX_CALIB_DEVIATION_ML
                val finalDeviation = if (isValidDeviation) deviationVal!! else 0.0
                val errPct = ANPUtils.calculateVolumeErrorPercent(finalDeviation)
                val isConf = ANPUtils.isVolumeErrorCompliant(finalDeviation)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isConf && isValidDeviation) HdGreenLight else HdRedLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Resultado:", fontWeight = FontWeight.Bold)
                        val errStr = if (finalDeviation >= 0) "+${finalDeviation.toInt()} ml" else "${finalDeviation.toInt()} ml"
                        Text(
                            text = "$errStr (${String.format(java.util.Locale.getDefault(), "%.2f", errPct)}%) | ${if (isConf) "CONFORME" else "REPROVADO"}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isConf) HdGreen else HdRed
                        )
                    }
                }

                Button(
                    onClick = {
                        val devParsed = calibDeviationMl.toDoubleOrNull()
                        if (calibReference.isBlank()) {
                            viewModel.addToast("Informe a identificação do bico!")
                        } else if (calibInspector.isBlank()) {
                            viewModel.addToast("Informe o responsável!")
                        } else if (devParsed == null) {
                            viewModel.addToast("Informe um valor de desvio válido!")
                        } else {
                            viewModel.addCalibration(
                                date = calibDate,
                                referenceName = calibReference,
                                nominalVolume = 20.0,
                                measuredVolume = 20.0 + (devParsed / 1000.0),
                                errorPercent = errPct,
                                inspector = calibInspector,
                                laudo = calibLaudo,
                                isConforme = isConf
                            )
                            viewModel.navigateTo("AFERICOES")
                            viewModel.addToast("Aferição registrada com sucesso!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Registro de Aferição", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarAnaliseScreen(viewModel: PostoViewModel) {
    var fuelType by remember { mutableStateOf("Gasolina Comum") }
    var technicianName by remember { mutableStateOf("Carlos Silva") }
    var densityMeasured by remember { mutableStateOf("745.5") }
    var temperature by remember { mutableStateOf("25.0") }
    var ethanolPercent by remember { mutableStateOf("27") }
    var aspectColor by remember { mutableStateOf("Límpido e Isento de Impurezas") }
    var observation by remember { mutableStateOf("Amostra atende aos requisitos da Resolução ANP.") }
    var date by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HdSurface),
            border = BorderStroke(1.dp, HdBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Registrar Análise de Combustível 🧪",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data da Coleta (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(text = "Tipo de Combustível", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                val fuels = listOf("Gasolina Comum", "Gasolina Aditivada", "Etanol Comum", "Diesel S10", "Diesel S500")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fuels.forEach { f ->
                        FilterChip(
                            selected = fuelType == f,
                            onClick = { fuelType = f },
                            label = { Text(f) }
                        )
                    }
                }

                OutlinedTextField(
                    value = densityMeasured,
                    onValueChange = { densityMeasured = it },
                    label = { Text("Massa Específica (kg/m³)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperatura da Amostra (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (fuelType.contains("Gasolina")) {
                    OutlinedTextField(
                        value = ethanolPercent,
                        onValueChange = { ethanolPercent = it },
                        label = { Text("Teor de Etanol (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = aspectColor,
                    onValueChange = { aspectColor = it },
                    label = { Text("Aspecto e Cor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = technicianName,
                    onValueChange = { technicianName = it },
                    label = { Text("Nome do Técnico/Responsável") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Conclusão da Análise") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Button(
                    onClick = {
                        viewModel.addFuelConformityRecord(
                            date = date,
                            fuelType = fuelType,
                            densityMeasured = densityMeasured.toDoubleOrNull() ?: 0.0,
                            temperature = temperature.toDoubleOrNull() ?: 0.0,
                            ethanolPercent = ethanolPercent.toDoubleOrNull() ?: 0.0,
                            aspectColor = aspectColor,
                            technicianName = technicianName,
                            observation = observation,
                            isConforme = true
                        )
                        viewModel.navigateTo("ANALISES")
                        viewModel.addToast("Análise registrada com sucesso!")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Análise de Qualidade", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnaliseScreen(viewModel: PostoViewModel) {
    val conformityRecords by viewModel.fuelConformityRecords.collectAsStateWithLifecycle()
    var showQualReportDialog by remember { mutableStateOf(false) }
    var selectedConformityIds by remember { mutableStateOf(setOf<Int>()) }
    var reportDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    
    val stationRazaoSocial by viewModel.stationRazaoSocial.collectAsStateWithLifecycle()
    val stationCnpj by viewModel.stationCnpj.collectAsStateWithLifecycle()
    val mContext = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HdBorder),
                colors = CardDefaults.cardColors(containerColor = HdSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Controle de Qualidade de Combustível (ANP) 🧪",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = HdTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Amostras e análises físico-químicas regulares conforme regulamentação da ANP.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HdTextSecondary
                        )
                        val compliantCount = conformityRecords.count { it.isConforme }
                        val totalCount = conformityRecords.size
                        val pct = if (totalCount > 0) (compliantCount.toDouble() / totalCount * 100).toInt() else 100
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = if (pct >= 90) HdGreenLight else HdRedLight)
                            ) {
                                Text(
                                    text = "$pct% DE CONFORMIDADE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 90) HdGreen else HdRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$compliantCount de $totalCount análises regulares",
                                fontSize = 11.sp,
                                color = HdTextSecondary
                            )
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
                    text = "Análises Recentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { 
                            val filtered = conformityRecords.filter { it.id in selectedConformityIds }
                            if (filtered.isEmpty()) {
                                viewModel.addToast("Selecione pelo menos uma análise para gerar o relatório.")
                            } else {
                                PdfReportGenerator.generateConformityReport(
                                    mContext, 
                                    stationRazaoSocial.ifBlank { "Posto Administrativo" }, 
                                    stationCnpj.ifBlank { "12.345.678/0001-99" }, 
                                    filtered
                                )
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B)),
                        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Relatório PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.navigateTo("REGISTRO_ANALISE") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar", fontSize = 12.sp)
                    }
                }
            }
        }
        if (conformityRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, HdBorder),
                    colors = CardDefaults.cardColors(containerColor = HdSurface)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma análise cadastrada.")
                    }
                }
            }
        } else {
            items(conformityRecords) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HdBorder),
                    colors = CardDefaults.cardColors(containerColor = HdSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Checkbox(
                                    checked = selectedConformityIds.contains(rec.id),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedConformityIds = selectedConformityIds + rec.id
                                        } else {
                                            selectedConformityIds = selectedConformityIds - rec.id
                                        }
                                    }
                                )
                                Text(text = "🧪", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text(text = rec.fuelType, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                                    Text(text = "Data: ${rec.date} | Amostra: ${rec.technicianName}", style = MaterialTheme.typography.bodySmall, color = HdTextSecondary)
                                }
                            }
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (rec.isConforme) HdGreenLight else HdRedLight)
                            ) {
                                Text(
                                    text = if (rec.isConforme) "✓ CONFORME" else "✗ REPROVADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rec.isConforme) HdGreen else HdRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HdBorder)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdGrayLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Detalhes da Análise:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HdTextSecondary)
                                Text(text = "Massa Específica: ${rec.densityMeasured} kg/m³\nTeor de Álcool (Gasolina): ${rec.ethanolPercent}%\nAspecto/Cor: ${rec.aspectColor}\nTermômetro: ${rec.temperature}°C", style = MaterialTheme.typography.bodySmall, color = HdTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Conclusão: ${rec.observation}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HdTextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { viewModel.deleteFuelConformityRecord(rec) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Excluir Registro", color = HdRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
