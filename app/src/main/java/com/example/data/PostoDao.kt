package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PostoDao {

    // Fuel Tanks
    @Query("SELECT * FROM fuel_tanks ORDER BY id ASC")
    fun getAllFuelTanks(): Flow<List<FuelTank>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelTank(tank: FuelTank)

    @Delete
    suspend fun deleteFuelTank(tank: FuelTank)

    @Update
    suspend fun updateFuelTank(tank: FuelTank)

    @Query("UPDATE fuel_tanks SET currentLevel = :newLevel WHERE id = :id")
    suspend fun updateFuelTankLevel(id: Int, newLevel: Double)

    // Employees
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // Shift Schedules
    @Query("SELECT * FROM shift_schedules ORDER BY id DESC")
    fun getAllShiftSchedules(): Flow<List<ShiftSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftSchedule(schedule: ShiftSchedule)

    @Delete
    suspend fun deleteShiftSchedule(schedule: ShiftSchedule)

    @Query("DELETE FROM shift_schedules WHERE employeeId = :employeeId")
    suspend fun deleteShiftSchedulesByEmployee(employeeId: Int)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    // Daily Reports
    @Query("SELECT * FROM daily_reports ORDER BY date DESC")
    fun getAllDailyReports(): Flow<List<DailyReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReport(report: DailyReport)

    @Delete
    suspend fun deleteDailyReport(report: DailyReport)

    // Fuel Conformity Records
    @Query("SELECT * FROM fuel_conformity_records ORDER BY date DESC, id DESC")
    fun getAllFuelConformityRecords(): Flow<List<FuelConformityRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelConformityRecord(record: FuelConformityRecord)

    @Delete
    suspend fun deleteFuelConformityRecord(record: FuelConformityRecord)

    // Nozzles
    @Query("SELECT * FROM nozzles ORDER BY nozzleNumber ASC")
    fun getAllNozzles(): Flow<List<Nozzle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNozzle(nozzle: Nozzle)

    @Delete
    suspend fun deleteNozzle(nozzle: Nozzle)

    @Update
    suspend fun updateNozzle(nozzle: Nozzle)

    // Calibrations
    @Query("SELECT * FROM calibrations ORDER BY date DESC, id DESC")
    fun getAllCalibrations(): Flow<List<Calibration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalibration(calibration: Calibration)

    @Delete
    suspend fun deleteCalibration(calibration: Calibration)

    // Audit Log Entries
    @Query("SELECT * FROM audit_log_entries ORDER BY date DESC, time DESC, id DESC")
    fun getAllAuditLogEntries(): Flow<List<AuditLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogEntry(entry: AuditLogEntry)

    @Delete
    suspend fun deleteAuditLogEntry(entry: AuditLogEntry)

    // User Accounts
    @Query("SELECT * FROM user_accounts")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)

    @Delete
    suspend fun deleteUserAccount(user: UserAccount)

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccountByEmail(email: String): UserAccount?
}
