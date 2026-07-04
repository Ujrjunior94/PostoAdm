package com.example.data

import kotlinx.coroutines.flow.Flow

class PostoRepository(private val postoDao: PostoDao) {

    val allFuelTanks: Flow<List<FuelTank>> = postoDao.getAllFuelTanks()
    val allEmployees: Flow<List<Employee>> = postoDao.getAllEmployees()
    val allShiftSchedules: Flow<List<ShiftSchedule>> = postoDao.getAllShiftSchedules()
    val allAppointments: Flow<List<Appointment>> = postoDao.getAllAppointments()
    val allDailyReports: Flow<List<DailyReport>> = postoDao.getAllDailyReports()
    val allNozzles: Flow<List<Nozzle>> = postoDao.getAllNozzles()
    val allCalibrations: Flow<List<Calibration>> = postoDao.getAllCalibrations()
    val allFuelConformityRecords: Flow<List<FuelConformityRecord>> = postoDao.getAllFuelConformityRecords()
    val allAuditLogEntries: Flow<List<AuditLogEntry>> = postoDao.getAllAuditLogEntries()

    // Pre-populate database with default items if empty
    suspend fun checkAndPrepopulate() {
        // We will perform a check inside a coroutine when initializing the view model
    }

    // Fuel Tank actions
    suspend fun insertFuelTank(tank: FuelTank) {
        postoDao.insertFuelTank(tank)
    }

    suspend fun deleteFuelTank(tank: FuelTank) {
        postoDao.deleteFuelTank(tank)
    }

    suspend fun updateFuelTank(tank: FuelTank) {
        postoDao.updateFuelTank(tank)
    }

    suspend fun updateFuelTankLevel(id: Int, newLevel: Double) {
        postoDao.updateFuelTankLevel(id, newLevel)
    }

    // Employee actions
    suspend fun insertEmployee(employee: Employee) {
        postoDao.insertEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        // Cascade delete shifts for this employee
        postoDao.deleteShiftSchedulesByEmployee(employee.id)
        postoDao.deleteEmployee(employee)
    }

    // Shift Schedule actions
    suspend fun insertShiftSchedule(schedule: ShiftSchedule) {
        postoDao.insertShiftSchedule(schedule)
    }

    suspend fun deleteShiftSchedule(schedule: ShiftSchedule) {
        postoDao.deleteShiftSchedule(schedule)
    }

    // Appointment actions
    suspend fun insertAppointment(appointment: Appointment) {
        postoDao.insertAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        postoDao.deleteAppointment(appointment)
    }

    // Daily Report actions
    suspend fun insertDailyReport(report: DailyReport) {
        postoDao.insertDailyReport(report)
    }

    suspend fun deleteDailyReport(report: DailyReport) {
        postoDao.deleteDailyReport(report)
    }

    // Fuel Conformity actions
    suspend fun insertFuelConformityRecord(record: FuelConformityRecord) {
        postoDao.insertFuelConformityRecord(record)
    }

    suspend fun deleteFuelConformityRecord(record: FuelConformityRecord) {
        postoDao.deleteFuelConformityRecord(record)
    }

    // Nozzles actions
    suspend fun insertNozzle(nozzle: Nozzle) {
        postoDao.insertNozzle(nozzle)
    }

    suspend fun deleteNozzle(nozzle: Nozzle) {
        postoDao.deleteNozzle(nozzle)
    }

    suspend fun updateNozzle(nozzle: Nozzle) {
        postoDao.updateNozzle(nozzle)
    }

    // Calibrations actions
    suspend fun insertCalibration(calibration: Calibration) {
        postoDao.insertCalibration(calibration)
    }

    suspend fun deleteCalibration(calibration: Calibration) {
        postoDao.deleteCalibration(calibration)
    }

    // Audit Log actions
    suspend fun insertAuditLogEntry(entry: AuditLogEntry) {
        postoDao.insertAuditLogEntry(entry)
    }

    suspend fun deleteAuditLogEntry(entry: AuditLogEntry) {
        postoDao.deleteAuditLogEntry(entry)
    }

    // User Account actions
    val allUserAccounts: Flow<List<UserAccount>> = postoDao.getAllUserAccounts()

    suspend fun insertUserAccount(user: UserAccount) {
        postoDao.insertUserAccount(user)
    }

    suspend fun deleteUserAccount(user: UserAccount) {
        postoDao.deleteUserAccount(user)
    }

    suspend fun getUserAccountByEmail(email: String): UserAccount? {
        return postoDao.getUserAccountByEmail(email)
    }
}
