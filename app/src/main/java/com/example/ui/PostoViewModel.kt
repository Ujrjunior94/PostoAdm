package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PostoDatabase.getDatabase(application)
    private val repository = PostoRepository(database.postoDao())

    private val _stationCnpj = MutableStateFlow("12.345.678/0001-99")
    val stationCnpj: StateFlow<String> = _stationCnpj.asStateFlow()

    private val _stationRazaoSocial = MutableStateFlow("Auto Posto Estrela da Alvorada Ltda")
    val stationRazaoSocial: StateFlow<String> = _stationRazaoSocial.asStateFlow()

    private val _stationEndereco = MutableStateFlow("Av. das Nações, 1500 - Centro, São Paulo - SP")
    val stationEndereco: StateFlow<String> = _stationEndereco.asStateFlow()

    private val _bankName = MutableStateFlow("Banco do Brasil")
    val bankName: StateFlow<String> = _bankName.asStateFlow()

    private val _bankAgency = MutableStateFlow("1234-5")
    val bankAgency: StateFlow<String> = _bankAgency.asStateFlow()

    private val _bankAccount = MutableStateFlow("98765-4")
    val bankAccount: StateFlow<String> = _bankAccount.asStateFlow()

    private val _bankPixKey = MutableStateFlow("12.345.678/0001-99")
    val bankPixKey: StateFlow<String> = _bankPixKey.asStateFlow()

    // UI States
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentScreen = MutableStateFlow("DASHBOARD") // "DASHBOARD", "ESTOQUE", "FUNCIONARIOS", "CALENDARIO", "RELATORIOS"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _currentUserRole = MutableStateFlow("Gerente") // "Gerente", "Visualizador"
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    fun setUserRole(role: String) {
        _currentUserRole.value = role
    }

    // Theme selection: "AUTO" (system default), "LIGHT", "DARK"
    private val _themeMode = MutableStateFlow("AUTO")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    // Login Form State
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Active User State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Read-Only mode for viewers (sem direito a alteração de dados)
    val isReadOnly: StateFlow<Boolean> = _currentUser
        .map { it?.role == "Visualizador" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Registered user accounts list from Room
    val userAccounts: StateFlow<List<UserAccount>> = repository.allUserAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Data Flows from Room - combined and isolated by station CNPJ
    val fuelTanks: StateFlow<List<FuelTank>> = repository.allFuelTanks
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shiftSchedules: StateFlow<List<ShiftSchedule>> = repository.allShiftSchedules
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.allAppointments
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyReports: StateFlow<List<DailyReport>> = repository.allDailyReports
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nozzles: StateFlow<List<Nozzle>> = repository.allNozzles
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calibrations: StateFlow<List<Calibration>> = repository.allCalibrations
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelConformityRecords: StateFlow<List<FuelConformityRecord>> = repository.allFuelConformityRecords
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelDeliveries: StateFlow<List<FuelDelivery>> = repository.allFuelDeliveries
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogEntries: StateFlow<List<AuditLogEntry>> = repository.allAuditLogEntries
        .combine(_stationCnpj) { items, cnpj -> items.filter { it.stationCnpj == cnpj } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Predefined manager-only credentials & systems variables
    private val _isSystemsUnlocked = MutableStateFlow(false)
    val isSystemsUnlocked: StateFlow<Boolean> = _isSystemsUnlocked.asStateFlow()

    private val _systemCredentials = MutableStateFlow<List<SystemCredential>>(
        listOf(
            SystemCredential(
                id = 1,
                systemName = "Sistema de Frente de Caixa (PDV)",
                category = "Operacional",
                login = "gerente_pdv",
                password = "pdv_safe_2026",
                description = "Acesso no terminal principal da gerência"
            ),
            SystemCredential(
                id = 2,
                systemName = "Portal SEFAZ (Emissor NF-e)",
                category = "Fiscal",
                login = "financeiro@estrela.com.br",
                password = "sefaz_password_secure",
                description = "https://nfe.fazenda.sp.gov.br"
            ),
            SystemCredential(
                id = 3,
                systemName = "Concentrador de Bombas",
                category = "Equipamentos",
                login = "admin_bomba",
                password = "bomba_con_123!",
                description = "Acesso direto via IP 192.168.1.50"
            ),
            SystemCredential(
                id = 4,
                systemName = "Monitoramento de Tanques (Veeder-Root)",
                category = "Segurança & Estoque",
                login = "veeder_root",
                password = "vr_monitor_2026",
                description = "Console Veeder-Root TLS-450"
            ),
            SystemCredential(
                id = 5,
                systemName = "Monitoramento de Câmeras CFTV",
                category = "Segurança",
                login = "cftv_gerente",
                password = "cftv_password_99",
                description = "Acesso via app Intelbras ISIC Lite"
            )
        )
    )
    val systemCredentials: StateFlow<List<SystemCredential>> = _systemCredentials.asStateFlow()

    fun unlockSystems(password: String): Boolean {
        // Predefined password as requested
        val isCorrect = password == "adm001"
        if (isCorrect) {
            _isSystemsUnlocked.value = true
        }
        return isCorrect
    }

    fun lockSystems() {
        _isSystemsUnlocked.value = false
    }

    fun updateStationInfo(razaoSocial: String, cnpj: String, endereco: String) {
        val manager = currentUser.value
        _stationRazaoSocial.value = razaoSocial
        _stationCnpj.value = cnpj
        _stationEndereco.value = endereco
        
        if (manager != null) {
            viewModelScope.launch {
                val updatedManager = manager.copy(
                    stationName = razaoSocial,
                    stationCnpj = cnpj,
                    stationEndereco = endereco
                )
                repository.insertUserAccount(updatedManager)
                _currentUser.value = updatedManager
                
                // Update linked viewers
                userAccounts.value.filter { it.parentManagerEmail == manager.email }.forEach { viewer ->
                    repository.insertUserAccount(viewer.copy(
                        stationName = razaoSocial,
                        stationCnpj = cnpj,
                        stationEndereco = endereco
                    ))
                }
            }
        }
        addToast("Informações do posto atualizadas!")
    }

    fun updateBankInfo(name: String, agency: String, account: String, pix: String) {
        val manager = currentUser.value
        _bankName.value = name
        _bankAgency.value = agency
        _bankAccount.value = account
        _bankPixKey.value = pix
        
        if (manager != null) {
            viewModelScope.launch {
                val updatedManager = manager.copy(
                    bankName = name,
                    bankAgency = agency,
                    bankAccount = account,
                    bankPixKey = pix
                )
                repository.insertUserAccount(updatedManager)
                _currentUser.value = updatedManager
                
                // Update linked viewers
                userAccounts.value.filter { it.parentManagerEmail == manager.email }.forEach { viewer ->
                    repository.insertUserAccount(viewer.copy(
                        bankName = name,
                        bankAgency = agency,
                        bankAccount = account,
                        bankPixKey = pix
                    ))
                }
            }
        }
        addToast("Dados bancários atualizados!")
    }

    fun addSystemCredential(name: String, category: String, login: String, pass: String, desc: String) {
        if (name.isBlank() || login.isBlank() || pass.isBlank()) {
            addToast("Preencha todos os campos obrigatórios!")
            return
        }
        val currentList = _systemCredentials.value
        val nextId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val newCred = SystemCredential(
            id = nextId,
            systemName = name,
            category = category,
            login = login,
            password = pass,
            description = desc
        )
        _systemCredentials.value = currentList + newCred
        addToast("Credencial cadastrada com sucesso!")
    }

    fun deleteSystemCredential(id: Int) {
        _systemCredentials.value = _systemCredentials.value.filter { it.id != id }
        addToast("Credencial removida.")
    }

    // Low level warnings derived from tanks
    val lowFuelAlerts: StateFlow<List<String>> = fuelTanks
        .map { tanks ->
            tanks.filter { it.isLowLevel }.map { "Alerta: O tanque de ${it.name} está com nível baixo (${String.format(Locale.getDefault(), "%.1f", it.currentLevel)}L / ${String.format(Locale.getDefault(), "%.1f", it.capacity)}L)!" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active in-app notification messages
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        try {
            FirebaseHelper.initializeFromPreferences(application)
        } catch (e: Exception) {
            Log.e("PostoViewModel", "Error initializing dynamic Firebase helper on boot", e)
        }
        viewModelScope.launch {
            // Wait for DB, then seed data if empty
            val tanks = repository.allFuelTanks.first()
            if (tanks.isEmpty()) {
                seedDatabase()
            }
        }
        viewModelScope.launch {
            // Seed default user if user accounts is empty
            val accounts = repository.allUserAccounts.first()
            if (accounts.isEmpty()) {
                val defaultManager = UserAccount(
                    email = "admin@posto.com",
                    name = "Carlos Gerente",
                    role = "Gerente",
                    password = "123456",
                    stationName = "Auto Posto Estrela da Alvorada Ltda",
                    stationCnpj = "12.345.678/0001-99",
                    stationEndereco = "Av. das Nações, 1500 - Centro, São Paulo - SP",
                    bankName = "Banco do Brasil",
                    bankAgency = "1234-5",
                    bankAccount = "98765-4",
                    bankPixKey = "12.345.678/0001-99"
                )
                repository.insertUserAccount(defaultManager)
            }
        }
    }

    private suspend fun seedDatabase() {
        val cnpj = "12.345.678/0001-99"
        // 1. Seed fuel tanks
        val tanks = listOf(
            FuelTank(name = "Gasolina Comum", capacity = 20000.0, currentLevel = 14500.0, threshold = 4000.0, pricePerLiter = 5.89, stationCnpj = cnpj),
            FuelTank(name = "Gasolina Aditivada", capacity = 15000.0, currentLevel = 2400.0, threshold = 3000.0, pricePerLiter = 6.09, stationCnpj = cnpj), // Low level trigger
            FuelTank(name = "Etanol Comum", capacity = 20000.0, currentLevel = 11000.0, threshold = 4000.0, pricePerLiter = 3.99, stationCnpj = cnpj),
            FuelTank(name = "Diesel S10", capacity = 25000.0, currentLevel = 22000.0, threshold = 5000.0, pricePerLiter = 5.79, stationCnpj = cnpj)
        )
        tanks.forEach { repository.insertFuelTank(it) }

        // 2. Seed employees
        val initialEmployees = listOf(
            Employee(name = "Carlos Silva", role = "Frentista", phone = "(11) 98765-4321", activeShift = "Manhã", stationCnpj = cnpj),
            Employee(name = "Ana Oliveira", role = "Caixa", phone = "(11) 97654-3210", activeShift = "Tarde", stationCnpj = cnpj),
            Employee(name = "Marcos Souza", role = "Gerente", phone = "(11) 96543-2109", activeShift = "Manhã", stationCnpj = cnpj),
            Employee(name = "Patrícia Lima", role = "Frentista", phone = "(11) 95432-1098", activeShift = "Noite", stationCnpj = cnpj)
        )
        initialEmployees.forEach { repository.insertEmployee(it) }

        // 3. Seed shift schedules
        val initialSchedules = listOf(
            ShiftSchedule(employeeId = 1, employeeName = "Carlos Silva", dayOfWeek = "Dia 01", shift = "Manhã (06h - 14h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 1, employeeName = "Carlos Silva", dayOfWeek = "Dia 03", shift = "Manhã (06h - 14h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 2, employeeName = "Ana Oliveira", dayOfWeek = "Dia 01", shift = "Tarde (14h - 22h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 3, employeeName = "Marcos Souza", dayOfWeek = "Dia 05", shift = "Manhã (06h - 14h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 4, employeeName = "Patrícia Lima", dayOfWeek = "Dia 06", shift = "Noite (22h - 06h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 1, employeeName = "Carlos Silva", dayOfWeek = "Dia 15", shift = "Tarde (14h - 22h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 2, employeeName = "Ana Oliveira", dayOfWeek = "Dia 15", shift = "Manhã (06h - 14h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 3, employeeName = "Marcos Souza", dayOfWeek = "Dia 20", shift = "Noite (22h - 06h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 4, employeeName = "Patrícia Lima", dayOfWeek = "Dia 25", shift = "Folga (Descanso)", stationCnpj = cnpj)
        )
        initialSchedules.forEach { repository.insertShiftSchedule(it) }

        // 4. Seed calendar appointments
        val initialAppointments = listOf(
            Appointment(title = "Limpeza de Filtros", date = "2026-07-06", time = "09:00", description = "Manutenção preventiva das bombas de etanol.", stationCnpj = cnpj),
            Appointment(title = "Caminhão de Combustível", date = "2026-07-05", time = "14:30", description = "Entrega de 10.000L de Gasolina Comum.", stationCnpj = cnpj),
            Appointment(title = "Vistoria da ANP", date = "2026-07-10", time = "11:00", description = "Verificação anual de conformidade e aferição das bombas.", stationCnpj = cnpj)
        )
        initialAppointments.forEach { repository.insertAppointment(it) }

        // 5. Seed daily reports
        val initialReports = listOf(
            DailyReport(date = "2026-07-01", totalSales = 12450.0, litersSold = 2150.0, transactionsCount = 142, observation = "Operação normal. Alta venda de Gasolina Comum.", stationCnpj = cnpj),
            DailyReport(date = "2026-07-02", totalSales = 14200.0, litersSold = 2400.0, transactionsCount = 160, observation = "Excelente movimento. Abastecimento frota parceira.", stationCnpj = cnpj),
            DailyReport(date = "2026-07-03", totalSales = 11800.0, litersSold = 1950.0, transactionsCount = 130, observation = "Chuva no final de tarde reduziu o fluxo na pista.", stationCnpj = cnpj)
        )
        initialReports.forEach { repository.insertDailyReport(it) }

        // 6. Seed nozzles
        val initialNozzles = listOf(
            Nozzle(nozzleNumber = "01", pumpName = "Bomba 1 - Ilha Norte", tankId = 1, tankName = "Gasolina Comum", fuelType = "Gasolina Comum", status = "Ativo", stationCnpj = cnpj),
            Nozzle(nozzleNumber = "02", pumpName = "Bomba 1 - Ilha Norte", tankId = 2, tankName = "Gasolina Aditivada", fuelType = "Gasolina Aditivada", status = "Ativo", stationCnpj = cnpj),
            Nozzle(nozzleNumber = "03", pumpName = "Bomba 2 - Ilha Sul", tankId = 3, tankName = "Etanol Comum", fuelType = "Etanol Comum", status = "Ativo", stationCnpj = cnpj),
            Nozzle(nozzleNumber = "04", pumpName = "Bomba 3 - Ilha Diesel", tankId = 4, tankName = "Diesel S10", fuelType = "Diesel S10", status = "Ativo", stationCnpj = cnpj),
            Nozzle(nozzleNumber = "05", pumpName = "Bomba 2 - Ilha Sul", tankId = 1, tankName = "Gasolina Comum", fuelType = "Gasolina Comum", status = "Em Manutenção", stationCnpj = cnpj)
        )
        initialNozzles.forEach { repository.insertNozzle(it) }

        // 7. Seed calibrations
        val initialCalibrations = listOf(
            Calibration(date = "2026-07-04", referenceName = "Bico 01", nominalVolume = 20.0, measuredVolume = 19.98, errorPercent = -0.1, inspector = "Carlos Silva", laudo = "Bico calibrado com sucesso, fluxo regular.", isConforme = true, stationCnpj = cnpj),
            Calibration(date = "2026-07-03", referenceName = "Bico 02", nominalVolume = 20.0, measuredVolume = 19.85, errorPercent = -0.75, inspector = "Carlos Silva", laudo = "Bico fora do limite legal de tolerância (max ±0.5%). Necessita ajuste mecânico imediato.", isConforme = false, stationCnpj = cnpj),
            Calibration(date = "2026-07-02", referenceName = "Bico 03", nominalVolume = 20.0, measuredVolume = 20.02, errorPercent = 0.1, inspector = "Patrícia Lima", laudo = "Medição dentro das margens permitidas.", isConforme = true, stationCnpj = cnpj)
        )
        initialCalibrations.forEach { repository.insertCalibration(it) }

        // 8. Seed audit logs
        val initialAuditLogs = listOf(
            AuditLogEntry(
                date = "2026-07-04",
                time = "08:15",
                actionType = "Aferição Física (INMETRO)",
                target = "Bico 01",
                details = "Aferição de rotina para verificação de vazão e bico de teste. Desvio de -0.10% em relação ao volume padrão de 20L. Equipamento considerado CONFORME.",
                operator = "Carlos Silva",
                complianceStatus = "Regular",
                stationCnpj = cnpj
            ),
            AuditLogEntry(
                date = "2026-07-03",
                time = "16:40",
                actionType = "Aferição Física (INMETRO)",
                target = "Bico 02",
                details = "Aferição realizada após suspeita de desvio. Erro calculated de -0.75%, acima da tolerância máxima permitida por lei (±0.50%). Equipamento classificado como REPROVADO. Recomendada interdição para manutenção corretiva.",
                operator = "Carlos Silva",
                complianceStatus = "Irregular",
                stationCnpj = cnpj
            ),
            AuditLogEntry(
                date = "2026-07-02",
                time = "10:20",
                actionType = "Análise de Qualidade (ANP)",
                target = "Gasolina Comum",
                details = "Coleta e análise laboratorial periódica. Densidade medida: 0.742 g/cm³ a 23°C (Densidade corrigida: 0.744 g/cm³). Teor de etanol medido: 27.0%. Todos os parâmetros em estrita conformidade com a regulamentação ANP vigente.",
                operator = "Patrícia Lima",
                complianceStatus = "Regular",
                stationCnpj = cnpj
            ),
            AuditLogEntry(
                date = "2026-07-01",
                time = "09:00",
                actionType = "Manutenção Preventiva",
                target = "Bomba 2 - Ilha Sul",
                details = "Substituição preventiva do filtro prensa e limpeza interna do gabinete eletrônico. Realizados testes de vazão subsequentes sem anomalias registradas.",
                operator = "Marcos Souza",
                complianceStatus = "Regular",
                stationCnpj = cnpj
            ),
            AuditLogEntry(
                date = "2026-06-28",
                time = "14:55",
                actionType = "Fiscalização Federal",
                target = "Geral do Posto",
                details = "Visita fiscal de rotina por agentes delegados do IPEM/INMETRO. Verificação de lacres das bombas, placas de preços e documentação obrigatória. Nenhum auto de infração lavrado.",
                operator = "Agente Fiscal #402",
                complianceStatus = "Regular",
                stationCnpj = cnpj
            )
        )
        initialAuditLogs.forEach { repository.insertAuditLogEntry(it) }

        // 9. Seed conformity records
        val initialConformities = listOf(
            FuelConformityRecord(
                id = 1,
                date = "2026-07-02",
                fuelType = "Gasolina Comum",
                densityMeasured = 0.742,
                temperature = 20.0,
                ethanolPercent = 27.0,
                aspectColor = "Límpido, isento de impurezas",
                isConforme = true,
                technicianName = "Roberto Técnico",
                observation = "Análise físico-química perfeita dentro dos limites da ANP.",
                stationCnpj = cnpj
            ),
            FuelConformityRecord(
                id = 2,
                date = "2026-07-04",
                fuelType = "Diesel S10",
                densityMeasured = 0.840,
                temperature = 22.0,
                ethanolPercent = 0.0,
                aspectColor = "Aspecto límpido, sem sedimentos",
                isConforme = true,
                technicianName = "Patrícia Lima",
                observation = "Diesel S10 em conformidade com as normas ANP.",
                stationCnpj = cnpj
            )
        )
        initialConformities.forEach { repository.insertFuelConformityRecord(it) }

        // 10. Seed fuel deliveries
        val initialDeliveries = listOf(
            FuelDelivery(
                id = 1,
                date = "2026-07-02",
                invoiceNumber = "NF-e 87342",
                fuelType = "Gasolina Comum",
                volume = 10000.0,
                driverName = "Carlos Silveira",
                driverCnh = "123456789-0",
                truckPlate = "ABC-1234",
                conformityRecordId = 1,
                stationCnpj = cnpj
            ),
            FuelDelivery(
                id = 2,
                date = "2026-07-04",
                invoiceNumber = "NF-e 87405",
                fuelType = "Diesel S10",
                volume = 15000.0,
                driverName = "Marcos Paulo",
                driverCnh = "987654321-0",
                truckPlate = "XYZ-9876",
                conformityRecordId = 2,
                stationCnpj = cnpj
            )
        )
        initialDeliveries.forEach { repository.insertFuelDelivery(it) }
    }

    // Actions
    fun login(email: String, password: String): Boolean {
        val formattedEmail = if (email.contains("@")) email.lowercase() else "${email.lowercase()}@posto.com"
        val localAccount = userAccounts.value.find { 
            val accEmail = it.email.lowercase()
            accEmail == email.lowercase() || accEmail == formattedEmail
        }
        
        if (FirebaseHelper.isAvailable) {
            viewModelScope.launch {
                try {
                    val auth = FirebaseHelper.auth
                    val db = FirebaseHelper.firestore
                    if (auth != null && db != null) {
                        auth.signInWithEmailAndPassword(formattedEmail, password)
                            .addOnSuccessListener { authResult ->
                                db.collection("users").document(formattedEmail).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val firebaseUser = FirebaseHelper.mapToUser(document.data ?: emptyMap())
                                            viewModelScope.launch {
                                                repository.insertUserAccount(firebaseUser)
                                                applyLoginState(firebaseUser)
                                                downloadFromFirestore(firebaseUser.stationCnpj)
                                                addToast("Login Firebase efetuado com sucesso!")
                                            }
                                        } else {
                                            if (localAccount != null) {
                                                applyLoginState(localAccount)
                                                db.collection("users").document(formattedEmail).set(FirebaseHelper.userToMap(localAccount))
                                            } else {
                                                _loginError.value = "Usuário não encontrado no Firestore."
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        if (localAccount != null) {
                                            applyLoginState(localAccount)
                                        } else {
                                            _loginError.value = "Erro ao buscar dados no Firestore: ${e.message}"
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                if (localAccount != null) {
                                    applyLoginState(localAccount)
                                    addToast("Login off-line efetuado com sucesso (modo de segurança)")
                                } else {
                                    _loginError.value = "Erro de autenticação Firebase: ${e.message}"
                                    addToast("Erro no login Firebase: ${e.message}")
                                }
                            }
                    } else {
                        if (localAccount != null) {
                            applyLoginState(localAccount)
                        } else {
                            _loginError.value = "Serviço de autenticação indisponível."
                        }
                    }
                } catch (e: Exception) {
                    if (localAccount != null) {
                        applyLoginState(localAccount)
                    } else {
                        _loginError.value = e.message
                    }
                }
            }
        } else {
            if (localAccount != null) {
                applyLoginState(localAccount)
                return true
            } else {
                _loginError.value = "Credenciais inválidas. Verifique seu login e senha."
                return false
            }
        }
        return localAccount != null
    }

    fun registerSimplifiedManager(login: String, pass: String): Boolean {
        if (login.isBlank() || pass.isBlank()) {
            _loginError.value = "Todos os campos obrigatórios devem ser preenchidos!"
            addToast("Preencha usuário e senha!")
            return false
        }
        val formattedEmail = if (login.contains("@")) login.lowercase() else "${login.lowercase()}@posto.com"
        val capitalizedName = login.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val randomDigits = (10..99).random()
        val stationCnpj = "12.345.678/0001-$randomDigits"
        val stationName = "Posto $capitalizedName"
        val stationEndereco = "Avenida Principal, ${(100..999).random()}"

        return registerManager(
            email = formattedEmail,
            name = capitalizedName,
            pass = pass,
            stationName = stationName,
            stationCnpj = stationCnpj,
            stationEndereco = stationEndereco
        )
    }

    private fun applyLoginState(account: UserAccount) {
        _isLoggedIn.value = true
        _currentUser.value = account
        _currentUserRole.value = account.role
        _stationCnpj.value = account.stationCnpj
        _stationRazaoSocial.value = account.stationName
        _stationEndereco.value = account.stationEndereco
        _bankName.value = account.bankName
        _bankAgency.value = account.bankAgency
        _bankAccount.value = account.bankAccount
        _bankPixKey.value = account.bankPixKey
        _loginError.value = null
        addToast("Bem-vindo, ${account.name}! (${account.role})")
        
        // Auto load from Supabase on successful login if credentials exist
        if (isSupabaseAvailable()) {
            downloadFromSupabase(account.stationCnpj)
        }
    }

    fun logout() {
        if (FirebaseHelper.isAvailable) {
            try {
                FirebaseHelper.auth?.signOut()
            } catch (e: Exception) {
                Log.e("PostoViewModel", "Error signing out from Firebase", e)
            }
        }
        _isLoggedIn.value = false
        _currentUser.value = null
        _currentUserRole.value = "Gerente"
        _currentScreen.value = "DASHBOARD"
        _isSystemsUnlocked.value = false
        addToast("Sessão encerrada com sucesso.")
    }

    fun registerManager(
        email: String,
        name: String,
        pass: String,
        stationName: String,
        stationCnpj: String,
        stationEndereco: String
    ): Boolean {
        if (email.isBlank() || name.isBlank() || pass.isBlank() || stationName.isBlank() || stationCnpj.isBlank()) {
            _loginError.value = "Todos os campos obrigatórios devem ser preenchidos!"
            addToast("Preencha todos os campos obrigatórios!")
            return false
        }
        val existing = userAccounts.value.find { it.email.lowercase() == email.lowercase() }
        if (existing != null) {
            _loginError.value = "Este e-mail já está cadastrado!"
            addToast("Este e-mail já está cadastrado!")
            return false
        }
        
        val newManager = UserAccount(
            email = email,
            name = name,
            role = "Gerente",
            password = pass,
            stationName = stationName,
            stationCnpj = stationCnpj,
            stationEndereco = stationEndereco
        )

        viewModelScope.launch {
            repository.insertUserAccount(newManager)
            seedStationTemplateData(stationCnpj)
            addToast("Gerente '$name' cadastrado localmente!")

            if (FirebaseHelper.isAvailable) {
                val auth = FirebaseHelper.auth
                val db = FirebaseHelper.firestore
                if (auth != null && db != null) {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {
                            db.collection("users").document(email.lowercase()).set(FirebaseHelper.userToMap(newManager))
                                .addOnSuccessListener {
                                    addToast("Conta sincronizada com a nuvem Firebase!")
                                    uploadToFirestore(stationCnpj)
                                }
                                .addOnFailureListener { e ->
                                    addToast("Perfil não pôde ser salvo na nuvem: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            addToast("Erro ao criar conta no Firebase Auth: ${e.message}")
                        }
                }
            }
        }
        return true
    }

    fun registerViewer(name: String, email: String, pass: String): Boolean {
        val manager = currentUser.value
        if (manager == null || manager.role != "Gerente") {
            addToast("Apenas gerentes podem cadastrar visualizadores!")
            return false
        }
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            addToast("Preencha todos os campos obrigatórios!")
            return false
        }
        val currentViewers = userAccounts.value.filter { it.parentManagerEmail == manager.email }
        if (currentViewers.size >= 5) {
            addToast("Limite máximo de 5 visualizadores atingido para este posto!")
            return false
        }
        val existing = userAccounts.value.find { it.email.lowercase() == email.lowercase() }
        if (existing != null) {
            addToast("Este e-mail já está cadastrado!")
            return false
        }

        val newViewer = UserAccount(
            email = email,
            name = name,
            role = "Visualizador",
            password = pass,
            stationName = manager.stationName,
            stationCnpj = manager.stationCnpj,
            stationEndereco = manager.stationEndereco,
            parentManagerEmail = manager.email,
            bankName = manager.bankName,
            bankAgency = manager.bankAgency,
            bankAccount = manager.bankAccount,
            bankPixKey = manager.bankPixKey
        )

        viewModelScope.launch {
            repository.insertUserAccount(newViewer)
            addToast("Visualizador '$name' cadastrado localmente!")

            if (FirebaseHelper.isAvailable) {
                val auth = FirebaseHelper.auth
                val db = FirebaseHelper.firestore
                if (auth != null && db != null) {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {
                            db.collection("users").document(email.lowercase()).set(FirebaseHelper.userToMap(newViewer))
                                .addOnSuccessListener {
                                    addToast("Acesso do visualizador salvo na nuvem!")
                                }
                        }
                        .addOnFailureListener { e ->
                            addToast("Erro ao sincronizar visualizador na nuvem: ${e.message}")
                        }
                }
            }
        }
        return true
    }

    fun deleteViewer(email: String) {
        viewModelScope.launch {
            val target = userAccounts.value.find { it.email == email }
            if (target != null && target.role == "Visualizador") {
                repository.deleteUserAccount(target)
                addToast("Acesso do visualizador '${target.name}' revogado localmente.")

                if (FirebaseHelper.isAvailable) {
                    val db = FirebaseHelper.firestore
                    db?.collection("users")?.document(email.lowercase())?.delete()
                        ?.addOnSuccessListener {
                            addToast("Acesso removido da nuvem com sucesso.")
                        }
                }
            }
        }
    }

    fun uploadToFirestore(cnpj: String) {
        if (!FirebaseHelper.isAvailable) {
            addToast("Firebase não está ativo/configurado.")
            return
        }
        val db = FirebaseHelper.firestore ?: return
        
        viewModelScope.launch {
            addToast("Fazendo backup de dados na nuvem...")
            try {
                fuelTanks.value.forEach { tank ->
                    db.collection("stations").document(cnpj)
                        .collection("fuel_tanks").document(tank.id.toString())
                        .set(FirebaseHelper.tankToMap(tank))
                }

                employees.value.forEach { emp ->
                    db.collection("stations").document(cnpj)
                        .collection("employees").document(emp.id.toString())
                        .set(FirebaseHelper.employeeToMap(emp))
                }

                shiftSchedules.value.forEach { sched ->
                    db.collection("stations").document(cnpj)
                        .collection("shift_schedules").document(sched.id.toString())
                        .set(FirebaseHelper.scheduleToMap(sched))
                }

                appointments.value.forEach { appt ->
                    db.collection("stations").document(cnpj)
                        .collection("appointments").document(appt.id.toString())
                        .set(FirebaseHelper.appointmentToMap(appt))
                }

                dailyReports.value.forEach { report ->
                    db.collection("stations").document(cnpj)
                        .collection("daily_reports").document(report.id.toString())
                        .set(FirebaseHelper.dailyReportToMap(report))
                }

                nozzles.value.forEach { noz ->
                    db.collection("stations").document(cnpj)
                        .collection("nozzles").document(noz.id.toString())
                        .set(FirebaseHelper.nozzleToMap(noz))
                }

                calibrations.value.forEach { cal ->
                    db.collection("stations").document(cnpj)
                        .collection("calibrations").document(cal.id.toString())
                        .set(FirebaseHelper.calibrationToMap(cal))
                }

                fuelConformityRecords.value.forEach { rec ->
                    db.collection("stations").document(cnpj)
                        .collection("conformity_records").document(rec.id.toString())
                        .set(FirebaseHelper.conformityToMap(rec))
                }

                auditLogEntries.value.forEach { log ->
                    db.collection("stations").document(cnpj)
                        .collection("audit_logs").document(log.id.toString())
                        .set(FirebaseHelper.auditToMap(log))
                }

                addToast("Sincronização de backup enviada para a nuvem!")
            } catch (e: Exception) {
                addToast("Erro no backup: ${e.message}")
            }
        }
    }

    fun downloadFromFirestore(cnpj: String) {
        if (!FirebaseHelper.isAvailable) return
        val db = FirebaseHelper.firestore ?: return

        viewModelScope.launch {
            try {
                db.collection("stations").document(cnpj).collection("fuel_tanks").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val tank = FirebaseHelper.mapToTank(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertFuelTank(tank) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("employees").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val emp = FirebaseHelper.mapToEmployee(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertEmployee(emp) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("shift_schedules").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val sched = FirebaseHelper.mapToSchedule(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertShiftSchedule(sched) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("appointments").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val appt = FirebaseHelper.mapToAppointment(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertAppointment(appt) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("daily_reports").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val report = FirebaseHelper.mapToDailyReport(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertDailyReport(report) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("nozzles").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val noz = FirebaseHelper.mapToNozzle(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertNozzle(noz) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("calibrations").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val cal = FirebaseHelper.mapToCalibration(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertCalibration(cal) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("conformity_records").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val rec = FirebaseHelper.mapToConformity(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertFuelConformityRecord(rec) }
                        }
                    }

                db.collection("stations").document(cnpj).collection("audit_logs").get()
                    .addOnSuccessListener { snap ->
                        snap.documents.forEach { doc ->
                            val log = FirebaseHelper.mapToAudit(doc.data ?: emptyMap())
                            viewModelScope.launch { repository.insertAuditLogEntry(log) }
                        }
                    }
                addToast("Dados sincronizados da nuvem com sucesso!")
            } catch (e: Exception) {
                Log.e("PostoViewModel", "Error downloading from Firestore", e)
            }
        }
    }

    private suspend fun seedStationTemplateData(cnpj: String) {
        // Seed fuel tanks
        val tanks = listOf(
            FuelTank(name = "Gasolina Comum", capacity = 20000.0, currentLevel = 14500.0, threshold = 4000.0, pricePerLiter = 5.89, stationCnpj = cnpj),
            FuelTank(name = "Gasolina Aditivada", capacity = 15000.0, currentLevel = 2400.0, threshold = 3000.0, pricePerLiter = 6.09, stationCnpj = cnpj),
            FuelTank(name = "Etanol Comum", capacity = 20000.0, currentLevel = 11000.0, threshold = 4000.0, pricePerLiter = 3.99, stationCnpj = cnpj),
            FuelTank(name = "Diesel S10", capacity = 25000.0, currentLevel = 22000.0, threshold = 5000.0, pricePerLiter = 5.79, stationCnpj = cnpj)
        )
        tanks.forEach { repository.insertFuelTank(it) }

        // Seed employees
        val initialEmployees = listOf(
            Employee(name = "Carlos Silva", role = "Frentista", phone = "(11) 98765-4321", activeShift = "Manhã", stationCnpj = cnpj),
            Employee(name = "Ana Oliveira", role = "Caixa", phone = "(11) 97654-3210", activeShift = "Tarde", stationCnpj = cnpj),
            Employee(name = "Patrícia Lima", role = "Frentista", phone = "(11) 95432-1098", activeShift = "Noite", stationCnpj = cnpj)
        )
        initialEmployees.forEach { repository.insertEmployee(it) }

        // Seed shift schedules
        val initialSchedules = listOf(
            ShiftSchedule(employeeId = 1, employeeName = "Carlos Silva", dayOfWeek = "Dia 01", shift = "Manhã (06h - 14h)", stationCnpj = cnpj),
            ShiftSchedule(employeeId = 2, employeeName = "Ana Oliveira", dayOfWeek = "Dia 01", shift = "Tarde (14h - 22h)", stationCnpj = cnpj)
        )
        initialSchedules.forEach { repository.insertShiftSchedule(it) }

        // Seed calendar appointments
        val initialAppointments = listOf(
            Appointment(title = "Limpeza de Filtros", date = "2026-07-06", time = "09:00", description = "Manutenção preventiva das bombas de etanol.", stationCnpj = cnpj),
            Appointment(title = "Vistoria da ANP", date = "2026-07-10", time = "11:00", description = "Verificação anual de conformidade e aferição das bombas.", stationCnpj = cnpj)
        )
        initialAppointments.forEach { repository.insertAppointment(it) }

        // Seed daily reports
        val initialReports = listOf(
            DailyReport(date = "2026-07-01", totalSales = 12450.0, litersSold = 2150.0, transactionsCount = 142, observation = "Operação de teste inicial.", stationCnpj = cnpj)
        )
        initialReports.forEach { repository.insertDailyReport(it) }

        // Seed nozzles
        val initialNozzles = listOf(
            Nozzle(nozzleNumber = "01", pumpName = "Bomba 1 - Ilha Norte", tankId = 1, tankName = "Gasolina Comum", fuelType = "Gasolina Comum", status = "Ativo", stationCnpj = cnpj),
            Nozzle(nozzleNumber = "02", pumpName = "Bomba 1 - Ilha Norte", tankId = 2, tankName = "Gasolina Aditivada", fuelType = "Gasolina Aditivada", status = "Ativo", stationCnpj = cnpj)
        )
        initialNozzles.forEach { repository.insertNozzle(it) }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun addToast(message: String) {
        _notifications.update { current ->
            (current + message).takeLast(5) // keep last 5 toasts
        }
    }

    fun dismissNotification(message: String) {
        _notifications.update { current -> current.filter { it != message } }
    }

    fun getSavedFirebaseConfig(): Triple<String, String, String> {
        val prefs = getApplication<Application>().getSharedPreferences("FirebasePrefs", android.content.Context.MODE_PRIVATE)
        return Triple(
            prefs.getString("apiKey", "") ?: "",
            prefs.getString("projectId", "") ?: "",
            prefs.getString("appId", "") ?: ""
        )
    }

    fun saveFirebaseConfig(apiKey: String, projectId: String, appId: String): Boolean {
        val context = getApplication<Application>()
        val success = FirebaseHelper.saveAndInitialize(context, apiKey.trim(), projectId.trim(), appId.trim())
        if (success) {
            addToast("Firebase configurado e ativado com sucesso!")
        } else {
            addToast("Erro ao inicializar Firebase. Verifique as credenciais.")
        }
        return success
    }

    fun clearFirebaseConfig() {
        val context = getApplication<Application>()
        FirebaseHelper.clearFirebaseConfig(context)
        addToast("Configuração do Firebase apagada.")
    }

    fun getBackupJsonInternal(): String {
        val backup = org.json.JSONObject()
        
        val tanksArray = org.json.JSONArray()
        fuelTanks.value.forEach { t ->
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
        employees.value.forEach { e ->
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
        shiftSchedules.value.forEach { s ->
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
        appointments.value.forEach { a ->
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
        dailyReports.value.forEach { r ->
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
        nozzles.value.forEach { n ->
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
        calibrations.value.forEach { c ->
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
        fuelConformityRecords.value.forEach { cf ->
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
        auditLogEntries.value.forEach { ad ->
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
        systemCredentials.value.forEach { cr ->
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
        userAccounts.value.forEach { u ->
            val obj = org.json.JSONObject().apply {
                put("email", u.email)
                put("name", u.name)
                put("role", u.role)
                put("password", u.password)
                put("stationName", u.stationName)
                put("stationCnpj", u.stationCnpj)
                put("stationEndereco", u.stationEndereco)
                put("parentManagerEmail", u.parentManagerEmail ?: "")
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

    fun restoreFromJson(backupJson: String) {
        viewModelScope.launch {
            try {
                val backup = org.json.JSONObject(backupJson)
                
                val tanksArray = backup.optJSONArray("fuelTanks")
                tanksArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val t = FuelTank(
                            id = obj.optInt("id", 0),
                            name = obj.optString("name", ""),
                            capacity = obj.optDouble("capacity", 0.0),
                            currentLevel = obj.optDouble("currentLevel", 0.0),
                            threshold = obj.optDouble("threshold", 0.0),
                            pricePerLiter = obj.optDouble("pricePerLiter", 0.0),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99"),
                            color = obj.optString("color", "#005AC1")
                        )
                        repository.insertFuelTank(t)
                    }
                }

                val empArray = backup.optJSONArray("employees")
                empArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val e = Employee(
                            id = obj.optInt("id", 0),
                            name = obj.optString("name", ""),
                            role = obj.optString("role", ""),
                            phone = obj.optString("phone", ""),
                            activeShift = obj.optString("activeShift", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertEmployee(e)
                    }
                }

                val shiftArray = backup.optJSONArray("shiftSchedules")
                shiftArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val s = ShiftSchedule(
                            id = obj.optInt("id", 0),
                            employeeId = obj.optInt("employeeId", 0),
                            employeeName = obj.optString("employeeName", ""),
                            dayOfWeek = obj.optString("dayOfWeek", ""),
                            shift = obj.optString("shift", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertShiftSchedule(s)
                    }
                }

                val appArray = backup.optJSONArray("appointments")
                appArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val a = Appointment(
                            id = obj.optInt("id", 0),
                            title = obj.optString("title", ""),
                            date = obj.optString("date", ""),
                            time = obj.optString("time", ""),
                            description = obj.optString("description", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertAppointment(a)
                    }
                }

                val repArray = backup.optJSONArray("dailyReports")
                repArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val r = DailyReport(
                            id = obj.optInt("id", 0),
                            date = obj.optString("date", ""),
                            fuelName = obj.optString("fuelName", ""),
                            openingStock = obj.optDouble("openingStock", 0.0),
                            receivedVolume = obj.optDouble("receivedVolume", 0.0),
                            litersSold = obj.optDouble("litersSold", 0.0),
                            closingStock = obj.optDouble("closingStock", 0.0),
                            totalSales = obj.optDouble("totalSales", 0.0),
                            transactionsCount = obj.optInt("transactionsCount", 0),
                            observation = obj.optString("observation", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertDailyReport(r)
                    }
                }

                val nozArray = backup.optJSONArray("nozzles")
                nozArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val n = Nozzle(
                            id = obj.optInt("id", 0),
                            nozzleNumber = obj.optString("nozzleNumber", ""),
                            pumpName = obj.optString("pumpName", ""),
                            tankId = obj.optInt("tankId", 0),
                            tankName = obj.optString("tankName", ""),
                            fuelType = obj.optString("fuelType", ""),
                            status = obj.optString("status", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99"),
                            color = obj.optString("color", "#005AC1")
                        )
                        repository.insertNozzle(n)
                    }
                }

                val calArray = backup.optJSONArray("calibrations")
                calArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val c = Calibration(
                            id = obj.optInt("id", 0),
                            date = obj.optString("date", ""),
                            referenceName = obj.optString("referenceName", ""),
                            nominalVolume = obj.optDouble("nominalVolume", 0.0),
                            measuredVolume = obj.optDouble("measuredVolume", 0.0),
                            errorPercent = obj.optDouble("errorPercent", 0.0),
                            inspector = obj.optString("inspector", ""),
                            laudo = obj.optString("laudo", ""),
                            isConforme = obj.optBoolean("isConforme", true),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertCalibration(c)
                    }
                }

                val confArray = backup.optJSONArray("fuelConformityRecords")
                confArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val cf = FuelConformityRecord(
                            id = obj.optInt("id", 0),
                            date = obj.optString("date", ""),
                            fuelType = obj.optString("fuelType", ""),
                            densityMeasured = obj.optDouble("densityMeasured", 0.0),
                            temperature = obj.optDouble("temperature", 0.0),
                            ethanolPercent = obj.optDouble("ethanolPercent", 0.0),
                            aspectColor = obj.optString("aspectColor", ""),
                            isConforme = obj.optBoolean("isConforme", true),
                            technicianName = obj.optString("technicianName", ""),
                            observation = obj.optString("observation", ""),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertFuelConformityRecord(cf)
                    }
                }

                val audArray = backup.optJSONArray("auditLogEntries")
                audArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val ad = AuditLogEntry(
                            id = obj.optInt("id", 0),
                            date = obj.optString("date", ""),
                            time = obj.optString("time", ""),
                            actionType = obj.optString("actionType", ""),
                            target = obj.optString("target", ""),
                            details = obj.optString("details", ""),
                            operator = obj.optString("operator", ""),
                            complianceStatus = obj.optString("complianceStatus", "Regular"),
                            stationCnpj = obj.optString("stationCnpj", "12.345.678/0001-99")
                        )
                        repository.insertAuditLogEntry(ad)
                    }
                }

                val userArray = backup.optJSONArray("userAccounts")
                userArray?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val u = UserAccount(
                            email = obj.optString("email", ""),
                            name = obj.optString("name", ""),
                            role = obj.optString("role", ""),
                            password = obj.optString("password", ""),
                            stationName = obj.optString("stationName", ""),
                            stationCnpj = obj.optString("stationCnpj", ""),
                            stationEndereco = obj.optString("stationEndereco", ""),
                            parentManagerEmail = if (obj.isNull("parentManagerEmail") || obj.optString("parentManagerEmail", "").isEmpty()) null else obj.optString("parentManagerEmail"),
                            bankName = obj.optString("bankName", "Banco do Brasil"),
                            bankAgency = obj.optString("bankAgency", ""),
                            bankAccount = obj.optString("bankAccount", ""),
                            bankPixKey = obj.optString("bankPixKey", "")
                        )
                        repository.insertUserAccount(u)
                    }
                }

                val credArray = backup.optJSONArray("systemCredentials")
                credArray?.let { arr ->
                    val creds = mutableListOf<SystemCredential>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        creds.add(SystemCredential(
                            id = obj.optInt("id", 0),
                            systemName = obj.optString("systemName", ""),
                            category = obj.optString("category", ""),
                            login = obj.optString("login", ""),
                            password = obj.optString("password", ""),
                            description = obj.optString("description", "")
                        ))
                    }
                    if (creds.isNotEmpty()) {
                        _systemCredentials.value = creds
                    }
                }

                addToast("Sincronização / Restauração concluída com sucesso! ⚡")
            } catch (e: Exception) {
                addToast("Erro ao restaurar dados: ${e.message}")
            }
        }
    }

    fun getSavedSupabaseConfig(): Pair<String, String> {
        return SupabaseHelper.getCredentials(getApplication())
    }

    fun isSupabaseAvailable(): Boolean {
        return SupabaseHelper.isConfigured(getApplication())
    }

    fun saveSupabaseConfig(url: String, key: String): Boolean {
        if (url.isEmpty() || key.isEmpty()) return false
        SupabaseHelper.saveCredentials(getApplication(), url, key)
        addToast("Supabase configurado com sucesso!")
        return true
    }

    fun clearSupabaseConfig() {
        SupabaseHelper.clearCredentials(getApplication())
        addToast("Credenciais Supabase removidas.")
    }

    fun uploadToSupabase(cnpj: String) {
        viewModelScope.launch {
            addToast("Enviando backup para o Supabase...")
            val backupJson = getBackupJsonInternal()
            val result = SupabaseHelper.uploadBackup(getApplication(), cnpj, backupJson)
            result.onSuccess {
                addToast("Backup enviado com sucesso para a nuvem Supabase! ☁️")
            }
            result.onFailure { e ->
                addToast("Erro ao enviar backup: ${e.message}")
            }
        }
    }

    fun downloadFromSupabase(cnpj: String) {
        viewModelScope.launch {
            addToast("Buscando backup no Supabase...")
            val result = SupabaseHelper.downloadBackup(getApplication(), cnpj)
            result.onSuccess { backupJson ->
                if (backupJson != null) {
                    restoreFromJson(backupJson)
                } else {
                    addToast("Nenhum backup encontrado para este CNPJ no Supabase.")
                }
            }
            result.onFailure { e ->
                addToast("Erro ao baixar backup: ${e.message}")
            }
        }
    }

    // Interactive operations
    fun addFuelTank(name: String, capacity: Double, currentLevel: Double, threshold: Double, pricePerLiter: Double, color: String = "#005AC1") {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (name.isNotBlank()) {
                repository.insertFuelTank(
                    FuelTank(
                        name = name,
                        capacity = capacity,
                        currentLevel = currentLevel,
                        threshold = threshold,
                        pricePerLiter = pricePerLiter,
                        stationCnpj = _stationCnpj.value,
                        color = color
                    )
                )
                addToast("Tanque '$name' cadastrado com sucesso!")
            }
        }
    }

    fun deleteFuelTank(tank: FuelTank) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteFuelTank(tank)
            addToast("Tanque '${tank.name}' removido.")
        }
    }

    fun updateFuelTank(tank: FuelTank) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.updateFuelTank(tank)
            addToast("Tanque '${tank.name}' atualizado.")
        }
    }

    fun refuelTank(id: Int, liters: Double) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            val tankList = fuelTanks.value
            val tank = tankList.find { it.id == id }
            if (tank != null) {
                val newLevel = (tank.currentLevel + liters).coerceAtMost(tank.capacity)
                repository.updateFuelTankLevel(id, newLevel)
                addToast("Tanque '${tank.name}' abastecido com sucesso! +${liters}L")
            }
        }
    }

    fun simulateSale(id: Int, liters: Double) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            val tankList = fuelTanks.value
            val tank = tankList.find { it.id == id }
            if (tank != null) {
                if (tank.currentLevel >= liters) {
                    val newLevel = tank.currentLevel - liters
                    repository.updateFuelTankLevel(id, newLevel)
                    addToast("Venda simulada de ${liters}L no tanque '${tank.name}'!")

                    // Check if it crossed the low level threshold now
                    if (newLevel <= tank.threshold && tank.currentLevel > tank.threshold) {
                        addToast("⚠️ ALERTA CRÍTICO: Tanque '${tank.name}' atingiu nível baixo!")
                    }
                } else {
                    addToast("Erro: Combustível insuficiente no tanque '${tank.name}'!")
                }
            }
        }
    }

    fun addEmployee(name: String, role: String, phone: String, shift: String) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (name.isNotBlank() && role.isNotBlank()) {
                repository.insertEmployee(
                    Employee(
                        name = name,
                        role = role,
                        phone = phone,
                        activeShift = shift,
                        stationCnpj = _stationCnpj.value
                    )
                )
                addToast("Funcionário '$name' cadastrado com sucesso!")
            }
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteEmployee(employee)
            addToast("Funcionário '${employee.name}' removido.")
        }
    }

    fun addShiftSchedule(employeeId: Int, employeeName: String, dayOfWeek: String, shift: String) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.insertShiftSchedule(
                ShiftSchedule(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    dayOfWeek = dayOfWeek,
                    shift = shift,
                    stationCnpj = _stationCnpj.value
                )
            )
            addToast("Escala definida para '$employeeName' na $dayOfWeek.")
        }
    }

    fun autoFillShiftSchedules(monthName: String, totalDays: Int) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            val employeeList = employees.value
            if (employeeList.isEmpty()) {
                addToast("Erro: Cadastre frentistas antes de auto-preencher!")
                return@launch
            }

            // Clear previous schedules
            repository.deleteShiftSchedulesByStation(_stationCnpj.value)

            val shifts = listOf("Manhã (06h - 14h)", "Tarde (14h - 22h)", "Noite (22h - 06h)")
            val schedulesToInsert = mutableListOf<ShiftSchedule>()
            var employeeIndex = 0

            for (day in 1..totalDays) {
                val dayStr = "Dia %02d".format(day)
                val scheduledOnDay = mutableSetOf<Int>()

                // 1. Assign to shifts
                for (shift in shifts) {
                    val emp = employeeList[employeeIndex]
                    schedulesToInsert.add(
                        ShiftSchedule(
                            employeeId = emp.id,
                            employeeName = emp.name,
                            dayOfWeek = dayStr,
                            shift = shift,
                            stationCnpj = _stationCnpj.value
                        )
                    )
                    scheduledOnDay.add(emp.id)
                    employeeIndex = (employeeIndex + 1) % employeeList.size
                }

                // 2. Assign "Folga (Descanso)" to other frentistas not scheduled today
                employeeList.forEach { emp ->
                    if (!scheduledOnDay.contains(emp.id)) {
                        schedulesToInsert.add(
                            ShiftSchedule(
                                employeeId = emp.id,
                                employeeName = emp.name,
                                dayOfWeek = dayStr,
                                shift = "Folga (Descanso)",
                                stationCnpj = _stationCnpj.value
                            )
                        )
                    }
                }
            }

            repository.insertShiftSchedules(schedulesToInsert)
            addToast("Escala de $monthName auto-preenchida com sucesso! ⚡")
        }
    }

    fun deleteShiftSchedule(schedule: ShiftSchedule) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteShiftSchedule(schedule)
            addToast("Escala removida.")
        }
    }

    fun addAppointment(title: String, date: String, time: String, description: String) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (title.isNotBlank() && date.isNotBlank()) {
                repository.insertAppointment(
                    Appointment(
                        title = title,
                        date = date,
                        time = time,
                        description = description,
                        stationCnpj = _stationCnpj.value
                    )
                )
                addToast("Compromisso '$title' agendado para $date.")
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteAppointment(appointment)
            addToast("Compromisso '${appointment.title}' removido.")
        }
    }

    fun addDailyReport(
        date: String,
        fuelName: String,
        openingStock: Double,
        receivedVolume: Double,
        litersSold: Double,
        closingStock: Double,
        totalSales: Double,
        transactionsCount: Int,
        observation: String
    ) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (date.isNotBlank()) {
                repository.insertDailyReport(
                    DailyReport(
                        date = date,
                        fuelName = fuelName,
                        openingStock = openingStock,
                        receivedVolume = receivedVolume,
                        litersSold = litersSold,
                        closingStock = closingStock,
                        totalSales = totalSales,
                        transactionsCount = transactionsCount,
                        observation = observation,
                        stationCnpj = _stationCnpj.value
                    )
                )
                addToast("Registro de LMC de $date lançado com sucesso!")
            }
        }
    }

    fun deleteDailyReport(report: DailyReport) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteDailyReport(report)
            addToast("Registro de LMC de ${report.date} removido.")
        }
    }

    fun addFuelConformityRecord(
        date: String,
        fuelType: String,
        densityMeasured: Double,
        temperature: Double,
        ethanolPercent: Double,
        aspectColor: String,
        isConforme: Boolean,
        technicianName: String,
        observation: String
    ) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (date.isNotBlank()) {
                repository.insertFuelConformityRecord(
                    FuelConformityRecord(
                        date = date,
                        fuelType = fuelType,
                        densityMeasured = densityMeasured,
                        temperature = temperature,
                        ethanolPercent = ethanolPercent,
                        aspectColor = aspectColor,
                        isConforme = isConforme,
                        technicianName = technicianName,
                        observation = observation,
                        stationCnpj = _stationCnpj.value
                    )
                )

                // Auto Audit log entry for fuel quality registration
                val compStat = if (isConforme) "Regular" else "Irregular"
                val ethStr = if (fuelType.contains("Gasolina", ignoreCase = true)) " | Teor Etanol: ${String.format(Locale.getDefault(), "%.1f", ethanolPercent)}%" else ""
                repository.insertAuditLogEntry(
                    AuditLogEntry(
                        date = date,
                        time = "11:00",
                        actionType = "Análise de Qualidade (ANP)",
                        target = fuelType,
                        details = "Ensaio laboratorial de conformidade de produto. Densidade medida: ${String.format(Locale.getDefault(), "%.3f", densityMeasured)} g/cm³ a ${String.format(Locale.getDefault(), "%.1f", temperature)}°C$ethStr. Aspecto/Cor: $aspectColor. Laudo final: ${if (isConforme) "CONFORME ✓" else "REPROVADO ✗"}. Obs: $observation",
                        operator = technicianName,
                        complianceStatus = compStat,
                        stationCnpj = _stationCnpj.value
                    )
                )

                addToast("Análise de conformidade de $fuelType salva!")
            }
        }
    }

    fun deleteFuelConformityRecord(record: FuelConformityRecord) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteFuelConformityRecord(record)

            // Auto Audit log entry for deleting a quality record
            repository.insertAuditLogEntry(
                AuditLogEntry(
                    date = "2026-07-04",
                    time = "11:05",
                    actionType = "Exclusão de Qualidade",
                    target = record.fuelType,
                    details = "Registro de análise laboratorial de conformidade datado de ${record.date} (Densidade original: ${record.densityMeasured}) foi excluído do histórico pelo operador.",
                    operator = "Operador do Sistema",
                    complianceStatus = "Aviso",
                    stationCnpj = _stationCnpj.value
                )
            )

            addToast("Análise de conformidade excluída.")
        }
    }

    fun addFuelDelivery(
        date: String,
        invoiceNumber: String,
        fuelType: String,
        volume: Double,
        driverName: String,
        driverCnh: String,
        truckPlate: String,
        conformityRecordId: Int?
    ) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (invoiceNumber.isBlank() || driverName.isBlank() || volume <= 0) {
                addToast("Erro: Preencha todos os campos obrigatórios corretamente!")
                return@launch
            }
            val delivery = FuelDelivery(
                date = date,
                invoiceNumber = invoiceNumber,
                fuelType = fuelType,
                volume = volume,
                driverName = driverName,
                driverCnh = driverCnh,
                truckPlate = truckPlate,
                conformityRecordId = conformityRecordId,
                stationCnpj = _stationCnpj.value
            )
            repository.insertFuelDelivery(delivery)

            // Auto Audit log entry
            repository.insertAuditLogEntry(
                AuditLogEntry(
                    date = date,
                    time = "12:00",
                    actionType = "Cadastro de Entrega (NF-e)",
                    target = fuelType,
                    details = "Nota Fiscal $invoiceNumber registrada. Volume: $volume L. Motorista: $driverName (CNH: $driverCnh, Placa: $truckPlate). Vinculado a laudo: ${if (conformityRecordId != null) "Laudo #$conformityRecordId" else "Sem laudo"}",
                    operator = "Gerente de Pátio",
                    complianceStatus = "Regular",
                    stationCnpj = _stationCnpj.value
                )
            )
            addToast("Entrega da Nota Fiscal $invoiceNumber registrada!")
        }
    }

    fun deleteFuelDelivery(delivery: FuelDelivery) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteFuelDelivery(delivery)
            
            // Auto Audit log entry
            repository.insertAuditLogEntry(
                AuditLogEntry(
                    date = "2026-07-04",
                    time = "12:05",
                    actionType = "Exclusão de Entrega",
                    target = delivery.fuelType,
                    details = "Registro de Nota Fiscal ${delivery.invoiceNumber} (Volume: ${delivery.volume}L, Motorista: ${delivery.driverName}) foi excluído pelo operador.",
                    operator = "Operador do Sistema",
                    complianceStatus = "Aviso",
                    stationCnpj = _stationCnpj.value
                )
            )
            addToast("Registro de entrega excluído.")
        }
    }

    fun addNozzle(nozzleNumber: String, pumpName: String, tankId: Int, tankName: String, fuelType: String, status: String, color: String = "#005AC1") {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (nozzleNumber.isNotBlank() && pumpName.isNotBlank()) {
                repository.insertNozzle(
                    Nozzle(
                        nozzleNumber = nozzleNumber,
                        pumpName = pumpName,
                        tankId = tankId,
                        tankName = tankName,
                        fuelType = fuelType,
                        status = status,
                        stationCnpj = _stationCnpj.value,
                        color = color
                    )
                )
                addToast("Bico de bomba '$nozzleNumber' adicionado com sucesso!")
            }
        }
    }

    fun deleteNozzle(nozzle: Nozzle) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteNozzle(nozzle)
            addToast("Bico de bomba '${nozzle.nozzleNumber}' removido.")
        }
    }

    fun updateNozzle(nozzle: Nozzle) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.updateNozzle(nozzle)
            addToast("Bico '${nozzle.nozzleNumber}' atualizado.")
        }
    }

    fun addCalibration(date: String, referenceName: String, nominalVolume: Double, measuredVolume: Double, errorPercent: Double, inspector: String, laudo: String, isConforme: Boolean) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (date.isNotBlank() && referenceName.isNotBlank()) {
                repository.insertCalibration(
                    Calibration(
                        date = date,
                        referenceName = referenceName,
                        nominalVolume = nominalVolume,
                        measuredVolume = measuredVolume,
                        errorPercent = errorPercent,
                        inspector = inspector,
                        laudo = laudo,
                        isConforme = isConforme,
                        stationCnpj = _stationCnpj.value
                    )
                )

                // Auto Audit log entry for registering physical calibration
                val compStat = if (isConforme) "Regular" else "Irregular"
                val errSign = if (errorPercent >= 0) "+" else ""
                repository.insertAuditLogEntry(
                    AuditLogEntry(
                        date = date,
                        time = "10:30",
                        actionType = "Aferição Física (INMETRO)",
                        target = referenceName,
                        details = "Aferição física periódica realizada por fiscal. Volume Nominal: ${String.format(Locale.getDefault(), "%.2f", nominalVolume)}L | Volume Real Medido: ${String.format(Locale.getDefault(), "%.2f", measuredVolume)}L | Desvio: $errSign${String.format(Locale.getDefault(), "%.2f", errorPercent)}%. Resultado: ${if (isConforme) "CONFORME ✓" else "REPROVADO (FORA DE LEI) ✗"}. Laudo: $laudo",
                        operator = inspector,
                        complianceStatus = compStat,
                        stationCnpj = _stationCnpj.value
                    )
                )

                addToast("Aferição para '$referenceName' registrada com sucesso!")
            }
        }
    }

    fun deleteCalibration(calibration: Calibration) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteCalibration(calibration)

            // Auto Audit log entry for deleting physical calibration
            repository.insertAuditLogEntry(
                AuditLogEntry(
                    date = "2026-07-04",
                    time = "10:35",
                    actionType = "Exclusão de Aferição",
                    target = calibration.referenceName,
                    details = "Registro de aferição datado de ${calibration.date} (Volume Medido original: ${calibration.measuredVolume}L, Erro original: ${calibration.errorPercent}%) foi excluído permanentemente do histórico.",
                    operator = "Gerente de Pista",
                    complianceStatus = "Aviso",
                    stationCnpj = _stationCnpj.value
                )
            )

            addToast("Aferição removida do histórico.")
        }
    }

    fun addAuditLogEntry(date: String, time: String, actionType: String, target: String, details: String, operator: String, complianceStatus: String) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            if (date.isNotBlank() && actionType.isNotBlank() && target.isNotBlank()) {
                repository.insertAuditLogEntry(
                    AuditLogEntry(
                        date = date,
                        time = time,
                        actionType = actionType,
                        target = target,
                        details = details,
                        operator = operator,
                        complianceStatus = complianceStatus,
                        stationCnpj = _stationCnpj.value
                    )
                )
                addToast("Registro de auditoria manual gravado com sucesso!")
            }
        }
    }

    fun deleteAuditLogEntry(entry: AuditLogEntry) {
        viewModelScope.launch {
            if (isReadOnly.value) {
                addToast("Erro: Visualizadores não têm permissão para alterar dados!")
                return@launch
            }
            repository.deleteAuditLogEntry(entry)
            addToast("Registro de auditoria removido.")
        }
    }
}
