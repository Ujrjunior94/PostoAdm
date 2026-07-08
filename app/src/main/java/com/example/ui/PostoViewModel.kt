package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import org.json.JSONArray

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

    private fun saveLoggedUser(email: String) {
        val prefs = getApplication<Application>().getSharedPreferences("AuthPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("loggedUserEmail", email).apply()
    }

    private fun getLoggedUserEmail(): String? {
        val prefs = getApplication<Application>().getSharedPreferences("AuthPrefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("loggedUserEmail", null)
    }

    private fun clearLoggedUser() {
        val prefs = getApplication<Application>().getSharedPreferences("AuthPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("loggedUserEmail").apply()
    }

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

    private val _isLoadingAuth = MutableStateFlow(false)
    val isLoadingAuth: StateFlow<Boolean> = _isLoadingAuth.asStateFlow()

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
        viewModelScope.launch {
            // Wait for DB, then seed data if empty
            val tanks = repository.allFuelTanks.first()
            if (tanks.isEmpty()) {
                seedDatabase()
            }
            
            // Check for auto-login
            val savedEmail = getLoggedUserEmail()
            if (savedEmail != null) {
                val accounts = repository.allUserAccounts.first()
                val account = accounts.find { it.email.lowercase() == savedEmail.lowercase() }
                if (account != null) {
                    Log.d("PostoViewModel", "Auto-login for: $savedEmail")
                    applyLoginState(account)
                }
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
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _loginError.value = null
            Log.d("PostoViewModel", "Tentativa de login para: $email")
            val formattedEmail = if (email.contains("@")) email.lowercase() else "${email.lowercase()}@posto.com"
            
            // 1. Tentar encontrar no banco de dados localmente
            val accounts = repository.allUserAccounts.first()
            val localAccount = accounts.find { 
                val accEmail = it.email.lowercase()
                accEmail == email.lowercase() || accEmail == formattedEmail
            }
            
            if (localAccount != null) {
                if (localAccount.password == password) {
                    Log.d("PostoViewModel", "Login local bem-sucedido")
                    applyLoginState(localAccount)
                    _isLoadingAuth.value = false
                    return@launch
                } else {
                    _loginError.value = "Senha incorreta."
                    _isLoadingAuth.value = false
                    return@launch
                }
            }

            // 2. Se não encontrado localmente, o usuário deve informar o CNPJ para buscar na nuvem
            // Para melhorar a autenticação, vamos sugerir que ele crie a conta ou use o CNPJ se já tiver
            _loginError.value = "Usuário não encontrado localmente. Por favor, registre-se ou verifique suas credenciais."
            _isLoadingAuth.value = false
        }
    }

    fun loginWithCnpjSync(email: String, password: String, cnpj: String) {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _loginError.value = null
            
            val result = SupabaseHelper.downloadBackup(getApplication(), cnpj)
            result.onSuccess { json ->
                if (json != null) {
                    try {
                        val stateObj = JSONObject(json)
                        val usersArray = stateObj.optJSONArray("users") ?: JSONArray()
                        var found = false
                        for (i in 0 until usersArray.length()) {
                            val u = usersArray.getJSONObject(i)
                            val uEmail = u.getString("email").lowercase()
                            val uPass = u.getString("password")
                            
                            if (uEmail == email.lowercase() && uPass == password) {
                                // Encontrou na nuvem! Importar dados
                                importFullStateFromJson(json)
                                val accounts = repository.allUserAccounts.first()
                                val importedUser = accounts.find { it.email.lowercase() == email.lowercase() }
                                if (importedUser != null) {
                                    applyLoginState(importedUser)
                                    addToast("Sincronização com Supabase concluída!")
                                    found = true
                                }
                                break
                            }
                        }
                        if (!found) {
                            _loginError.value = "Credenciais não encontradas no backup da nuvem para este CNPJ."
                        }
                    } catch (e: Exception) {
                        _loginError.value = "Erro ao processar dados da nuvem: ${e.message}"
                    }
                } else {
                    _loginError.value = "Nenhum backup encontrado para o CNPJ: $cnpj"
                }
            }.onFailure { e ->
                _loginError.value = "Erro ao conectar com Supabase: ${e.message}"
            }
            _isLoadingAuth.value = false
        }
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
        saveLoggedUser(account.email)
        addToast("Bem-vindo, ${account.name}! (${account.role})")
        
        // Auto load from Supabase on successful login if credentials exist
        if (isSupabaseAvailable()) {
            downloadFromSupabase(account.stationCnpj)
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        _currentUserRole.value = "Gerente"
        _currentScreen.value = "DASHBOARD"
        _isSystemsUnlocked.value = false
        clearLoggedUser()
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
            
            // Auto login after local registration
            applyLoginState(newManager)
            addToast("Conta criada com sucesso!")

            // Permanent integration: sync to Supabase
            syncToSupabase()
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
            syncToSupabase()
        }
        return true
    }

    fun deleteViewer(email: String) {
        viewModelScope.launch {
            val target = userAccounts.value.find { it.email == email }
            if (target != null && target.role == "Visualizador") {
                repository.deleteUserAccount(target)
                addToast("Acesso do visualizador '${target.name}' revogado localmente.")
                syncToSupabase()
            }
        }
    }

    fun syncToSupabase(cnpj: String = _stationCnpj.value) {
        viewModelScope.launch {
            val json = exportFullStateToJson()
            val result = SupabaseHelper.uploadBackup(getApplication(), cnpj, json)
            result.onSuccess {
                addToast("Sincronização com Supabase concluída!")
            }.onFailure { e ->
                addToast("Erro na sincronização: ${e.message}")
            }
        }
    }

    fun downloadFromSupabase(cnpj: String) {
        viewModelScope.launch {
            addToast("Sincronizando com a nuvem...")
            val result = SupabaseHelper.downloadBackup(getApplication(), cnpj)
            result.onSuccess { json ->
                if (json != null) {
                    importFullStateFromJson(json)
                } else {
                    addToast("Nenhum dado encontrado na nuvem para este CNPJ.")
                }
            }.onFailure { e ->
                addToast("Erro ao sincronizar da nuvem: ${e.message}")
            }
        }
    }

    fun exportCalibrationsPdf() {
        viewModelScope.launch {
            val data = calibrations.value
            if (data.isEmpty()) {
                addToast("Não há registros de aferição para exportar.")
                return@launch
            }
            PdfReportGenerator.generateCalibrationReport(
                getApplication(),
                _stationRazaoSocial.value,
                _stationCnpj.value,
                data
            )
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

    fun exportFullStateToJson(): String {
        val backup = JSONObject()
        try {
            val tanksArray = JSONArray()
            fuelTanks.value.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id); put("name", t.name); put("capacity", t.capacity)
                    put("currentLevel", t.currentLevel); put("threshold", t.threshold)
                    put("pricePerLiter", t.pricePerLiter); put("stationCnpj", t.stationCnpj); put("color", t.color)
                }
                tanksArray.put(obj)
            }
            backup.put("fuelTanks", tanksArray)

            val empArray = JSONArray()
            employees.value.forEach { e ->
                val obj = JSONObject().apply {
                    put("id", e.id); put("name", e.name); put("role", e.role)
                    put("phone", e.phone); put("activeShift", e.activeShift); put("stationCnpj", e.stationCnpj)
                }
                empArray.put(obj)
            }
            backup.put("employees", empArray)

            val shiftArray = JSONArray()
            shiftSchedules.value.forEach { s ->
                val obj = JSONObject().apply {
                    put("id", s.id); put("employeeId", s.employeeId); put("employeeName", s.employeeName)
                    put("dayOfWeek", s.dayOfWeek); put("shift", s.shift); put("stationCnpj", s.stationCnpj)
                }
                shiftArray.put(obj)
            }
            backup.put("shiftSchedules", shiftArray)

            val appArray = JSONArray()
            appointments.value.forEach { a ->
                val obj = JSONObject().apply {
                    put("id", a.id); put("title", a.title); put("date", a.date)
                    put("time", a.time); put("description", a.description); put("stationCnpj", a.stationCnpj)
                }
                appArray.put(obj)
            }
            backup.put("appointments", appArray)

            val reportArray = JSONArray()
            dailyReports.value.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id); put("date", r.date); put("fuelName", r.fuelName)
                    put("openingStock", r.openingStock); put("receivedVolume", r.receivedVolume)
                    put("litersSold", r.litersSold); put("closingStock", r.closingStock)
                    put("totalSales", r.totalSales); put("transactionsCount", r.transactionsCount)
                    put("observation", r.observation); put("stationCnpj", r.stationCnpj)
                }
                reportArray.put(obj)
            }
            backup.put("dailyReports", reportArray)

            val nozzleArray = JSONArray()
            nozzles.value.forEach { n ->
                val obj = JSONObject().apply {
                    put("id", n.id); put("nozzleNumber", n.nozzleNumber); put("pumpName", n.pumpName)
                    put("tankId", n.tankId); put("tankName", n.tankName); put("fuelType", n.fuelType)
                    put("status", n.status); put("stationCnpj", n.stationCnpj); put("color", n.color)
                }
                nozzleArray.put(obj)
            }
            backup.put("nozzles", nozzleArray)

            val calArray = JSONArray()
            calibrations.value.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id); put("date", c.date); put("referenceName", c.referenceName)
                    put("nominalVolume", c.nominalVolume); put("measuredVolume", c.measuredVolume)
                    put("errorPercent", c.errorPercent); put("inspector", c.inspector)
                    put("laudo", c.laudo); put("isConforme", c.isConforme); put("stationCnpj", c.stationCnpj)
                }
                calArray.put(obj)
            }
            backup.put("calibrations", calArray)

            val userArray = JSONArray()
            userAccounts.value.forEach { u ->
                val obj = JSONObject().apply {
                    put("email", u.email); put("name", u.name); put("role", u.role); put("password", u.password)
                    put("stationName", u.stationName); put("stationCnpj", u.stationCnpj); put("stationEndereco", u.stationEndereco)
                    put("parentManagerEmail", u.parentManagerEmail ?: ""); put("bankName", u.bankName)
                    put("bankAgency", u.bankAgency); put("bankAccount", u.bankAccount); put("bankPixKey", u.bankPixKey)
                }
                userArray.put(obj)
            }
            backup.put("users", userArray)
            backup.put("userAccounts", userArray)

            val credsArray = JSONArray()
            systemCredentials.value.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id); put("systemName", c.systemName); put("category", c.category)
                    put("login", c.login); put("password", c.password); put("description", c.description)
                }
                credsArray.put(obj)
            }
            backup.put("systemCredentials", credsArray)
        } catch (e: Exception) {
            Log.e("PostoViewModel", "Error exporting state", e)
        }
        return backup.toString()
    }

    fun importFullStateFromJson(json: String) {
        viewModelScope.launch {
            try {
                val backup = JSONObject(json)
                
                backup.optJSONArray("fuelTanks")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertFuelTank(FuelTank(
                            id = o.optInt("id", 0), name = o.optString("name", ""),
                            capacity = o.optDouble("capacity", 0.0), currentLevel = o.optDouble("currentLevel", 0.0),
                            threshold = o.optDouble("threshold", 0.0), pricePerLiter = o.optDouble("pricePerLiter", 0.0),
                            stationCnpj = o.optString("stationCnpj", ""), color = o.optString("color", "#005AC1")
                        ))
                    }
                }

                backup.optJSONArray("employees")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertEmployee(Employee(
                            id = o.optInt("id", 0), name = o.optString("name", ""),
                            role = o.optString("role", ""), phone = o.optString("phone", ""),
                            activeShift = o.optString("activeShift", ""), stationCnpj = o.optString("stationCnpj", "")
                        ))
                    }
                }

                backup.optJSONArray("dailyReports")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertDailyReport(DailyReport(
                            id = o.optInt("id", 0), date = o.optString("date", ""),
                            fuelName = o.optString("fuelName", ""), totalSales = o.optDouble("totalSales", 0.0),
                            stationCnpj = o.optString("stationCnpj", "")
                        ))
                    }
                }

                backup.optJSONArray("nozzles")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertNozzle(Nozzle(
                            id = o.optInt("id", 0), nozzleNumber = o.optString("nozzleNumber", ""),
                            pumpName = o.optString("pumpName", ""), tankId = o.optInt("tankId", 0),
                            tankName = o.optString("tankName", ""), fuelType = o.optString("fuelType", ""),
                            status = o.optString("status", ""), stationCnpj = o.optString("stationCnpj", ""),
                            color = o.optString("color", "#005AC1")
                        ))
                    }
                }

                backup.optJSONArray("calibrations")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertCalibration(Calibration(
                            id = o.optInt("id", 0), date = o.optString("date", ""),
                            referenceName = o.optString("referenceName", ""), nominalVolume = o.optDouble("nominalVolume", 0.0),
                            measuredVolume = o.optDouble("measuredVolume", 0.0), errorPercent = o.optDouble("errorPercent", 0.0),
                            inspector = o.optString("inspector", ""), laudo = o.optString("laudo", ""),
                            isConforme = o.optBoolean("isConforme", false), stationCnpj = o.optString("stationCnpj", "")
                        ))
                    }
                }

                val users = backup.optJSONArray("users") ?: backup.optJSONArray("userAccounts")
                users?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        repository.insertUserAccount(UserAccount(
                            email = o.optString("email", ""), name = o.optString("name", ""),
                            role = o.optString("role", ""), password = o.optString("password", ""),
                            stationName = o.optString("stationName", ""), stationCnpj = o.optString("stationCnpj", ""),
                            stationEndereco = o.optString("stationEndereco", ""),
                            parentManagerEmail = if (o.isNull("parentManagerEmail") || o.optString("parentManagerEmail", "").isEmpty()) null else o.optString("parentManagerEmail"),
                            bankName = o.optString("bankName", "Banco do Brasil"),
                            bankAgency = o.optString("bankAgency", ""), bankAccount = o.optString("bankAccount", ""),
                            bankPixKey = o.optString("bankPixKey", "")
                        ))
                    }
                }

                addToast("Sincronização / Restauração concluída com sucesso! ⚡")
            } catch (e: Exception) {
                Log.e("PostoViewModel", "Error importing state", e)
                addToast("Erro ao restaurar dados: ${e.message}")
            }
        }
    }

    fun isSupabaseAvailable(): Boolean {
        return SupabaseHelper.isConfigured(getApplication())
    }

    fun getSavedSupabaseConfig(): Pair<String, String> {
        return SupabaseHelper.getCredentials(getApplication())
    }

    fun saveSupabaseConfig(url: String, key: String) {
        SupabaseHelper.saveCredentials(getApplication(), url, key)
        addToast("Configuração do Supabase salva!")
    }

    fun clearSupabaseConfig() {
        SupabaseHelper.clearCredentials(getApplication())
        addToast("Configuração do Supabase removida.")
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
