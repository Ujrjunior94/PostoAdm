package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_tanks")
data class FuelTank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val capacity: Double,
    val currentLevel: Double,
    val threshold: Double,
    val pricePerLiter: Double,
    val stationCnpj: String = "12.345.678/0001-99",
    val color: String = "#005AC1"
) {
    val isLowLevel: Boolean
        get() = currentLevel <= threshold
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val phone: String,
    val activeShift: String, // "Manhã", "Tarde", "Noite"
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "shift_schedules")
data class ShiftSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val dayOfWeek: String, // "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"
    val shift: String, // "Manhã (06h - 14h)", "Tarde (14h - 22h)", "Noite (22h - 06h)"
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val description: String,
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "daily_reports")
data class DailyReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val fuelName: String = "Gasolina Comum",
    val openingStock: Double = 15000.0,
    val receivedVolume: Double = 0.0,
    val litersSold: Double = 2200.0,
    val closingStock: Double = 12800.0,
    val totalSales: Double = 12800.0,
    val transactionsCount: Int = 150,
    val observation: String = "",
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "fuel_conformity_records")
data class FuelConformityRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val fuelType: String, // "Gasolina Comum", "Etanol Hidratado", "Diesel S10"
    val densityMeasured: Double, // in g/cm³ (e.g. 0.745)
    val temperature: Double, // in °C (e.g. 23.5)
    val ethanolPercent: Double, // in % (only for gasoline, e.g. 27.0)
    val aspectColor: String, // e.g., "Límpido e isento de impurezas"
    val isConforme: Boolean,
    val technicianName: String,
    val observation: String = "",
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "nozzles")
data class Nozzle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nozzleNumber: String,
    val pumpName: String,
    val tankId: Int,
    val tankName: String,
    val fuelType: String,
    val status: String, // "Ativo", "Em Manutenção", "Bloqueado"
    val stationCnpj: String = "12.345.678/0001-99",
    val color: String = "#005AC1"
)

@Entity(tableName = "calibrations")
data class Calibration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val referenceName: String, // e.g. "Bico 01" or "Tanque Gasolina"
    val nominalVolume: Double, // nominal liters (usually 20.0L)
    val measuredVolume: Double, // actual measured liters (e.g. 19.98)
    val errorPercent: Double, // error percent e.g. -0.1%
    val inspector: String,
    val laudo: String, // description of calibration
    val isConforme: Boolean, // true if within ±0.5% tolerance
    val stationCnpj: String = "12.345.678/0001-99"
)

@Entity(tableName = "audit_log_entries")
data class AuditLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val actionType: String, // e.g. "Cadastro de Aferição", "Análise de Qualidade", "Exclusão de Registro", "Auditoria Manual"
    val target: String, // e.g. "Bico 01", "Gasolina Comum"
    val details: String, // description of the audit event
    val operator: String, // Inspector / Tech / Operator
    val complianceStatus: String = "Regular", // "Regular", "Aviso", "Irregular"
    val stationCnpj: String = "12.345.678/0001-99"
)

data class SystemCredential(
    val id: Int = 0,
    val systemName: String,
    val category: String, // e.g. "Operacional", "Fiscal", "Equipamentos", "Segurança"
    val login: String,
    val password: String,
    val description: String
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "Gerente" or "Visualizador"
    val password: String,
    val stationName: String,
    val stationCnpj: String,
    val stationEndereco: String,
    val parentManagerEmail: String? = null,
    val bankName: String = "Banco do Brasil",
    val bankAgency: String = "1234-5",
    val bankAccount: String = "98765-4",
    val bankPixKey: String = "12.345.678/0001-99"
)

@Entity(tableName = "fuel_deliveries")
data class FuelDelivery(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val invoiceNumber: String,
    val fuelType: String,
    val volume: Double,
    val driverName: String,
    val driverCnh: String,
    val truckPlate: String,
    val conformityRecordId: Int?, // Linked quality test ID
    val stationCnpj: String = "12.345.678/0001-99"
)

