package com.example.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseHelper {
    private const val TAG = "FirebaseHelper"

    val isAvailable: Boolean by lazy {
        try {
            val app = FirebaseApp.getInstance()
            app != null
        } catch (e: Exception) {
            Log.w(TAG, "Firebase is not initialized (google-services.json might be missing): ${e.message}")
            false
        }
    }

    val auth: FirebaseAuth?
        get() = if (isAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error obtaining FirebaseAuth instance", e)
                null
            }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error obtaining FirebaseFirestore instance", e)
                null
            }
        } else null

    // Helper functions to map entities to Maps for Firestore saving
    fun userToMap(user: UserAccount): Map<String, Any?> {
        return mapOf(
            "email" to user.email,
            "name" to user.name,
            "role" to user.role,
            "password" to user.password,
            "stationName" to user.stationName,
            "stationCnpj" to user.stationCnpj,
            "stationEndereco" to user.stationEndereco,
            "parentManagerEmail" to user.parentManagerEmail,
            "bankName" to user.bankName,
            "bankAgency" to user.bankAgency,
            "bankAccount" to user.bankAccount,
            "bankPixKey" to user.bankPixKey
        )
    }

    fun mapToUser(map: Map<String, Any?>): UserAccount {
        return UserAccount(
            email = map["email"] as? String ?: "",
            name = map["name"] as? String ?: "",
            role = map["role"] as? String ?: "Gerente",
            password = map["password"] as? String ?: "",
            stationName = map["stationName"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "",
            stationEndereco = map["stationEndereco"] as? String ?: "",
            parentManagerEmail = map["parentManagerEmail"] as? String,
            bankName = map["bankName"] as? String ?: "Banco do Brasil",
            bankAgency = map["bankAgency"] as? String ?: "1234-5",
            bankAccount = map["bankAccount"] as? String ?: "98765-4",
            bankPixKey = map["bankPixKey"] as? String ?: ""
        )
    }

    fun tankToMap(tank: FuelTank): Map<String, Any?> {
        return mapOf(
            "id" to tank.id,
            "name" to tank.name,
            "capacity" to tank.capacity,
            "currentLevel" to tank.currentLevel,
            "threshold" to tank.threshold,
            "pricePerLiter" to tank.pricePerLiter,
            "stationCnpj" to tank.stationCnpj,
            "color" to tank.color
        )
    }

    fun mapToTank(map: Map<String, Any?>): FuelTank {
        return FuelTank(
            id = (map["id"] as? Long ?: 0L).toInt(),
            name = map["name"] as? String ?: "",
            capacity = map["capacity"] as? Double ?: 0.0,
            currentLevel = map["currentLevel"] as? Double ?: 0.0,
            threshold = map["threshold"] as? Double ?: 0.0,
            pricePerLiter = map["pricePerLiter"] as? Double ?: 0.0,
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99",
            color = map["color"] as? String ?: "#005AC1"
        )
    }

    fun employeeToMap(employee: Employee): Map<String, Any?> {
        return mapOf(
            "id" to employee.id,
            "name" to employee.name,
            "role" to employee.role,
            "phone" to employee.phone,
            "activeShift" to employee.activeShift,
            "stationCnpj" to employee.stationCnpj
        )
    }

    fun mapToEmployee(map: Map<String, Any?>): Employee {
        return Employee(
            id = (map["id"] as? Long ?: 0L).toInt(),
            name = map["name"] as? String ?: "",
            role = map["role"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            activeShift = map["activeShift"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun scheduleToMap(schedule: ShiftSchedule): Map<String, Any?> {
        return mapOf(
            "id" to schedule.id,
            "employeeId" to schedule.employeeId,
            "employeeName" to schedule.employeeName,
            "dayOfWeek" to schedule.dayOfWeek,
            "shift" to schedule.shift,
            "stationCnpj" to schedule.stationCnpj
        )
    }

    fun mapToSchedule(map: Map<String, Any?>): ShiftSchedule {
        return ShiftSchedule(
            id = (map["id"] as? Long ?: 0L).toInt(),
            employeeId = (map["employeeId"] as? Long ?: 0L).toInt(),
            employeeName = map["employeeName"] as? String ?: "",
            dayOfWeek = map["dayOfWeek"] as? String ?: "",
            shift = map["shift"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun appointmentToMap(app: Appointment): Map<String, Any?> {
        return mapOf(
            "id" to app.id,
            "title" to app.title,
            "date" to app.date,
            "time" to app.time,
            "description" to app.description,
            "stationCnpj" to app.stationCnpj
        )
    }

    fun mapToAppointment(map: Map<String, Any?>): Appointment {
        return Appointment(
            id = (map["id"] as? Long ?: 0L).toInt(),
            title = map["title"] as? String ?: "",
            date = map["date"] as? String ?: "",
            time = map["time"] as? String ?: "",
            description = map["description"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun dailyReportToMap(report: DailyReport): Map<String, Any?> {
        return mapOf(
            "id" to report.id,
            "date" to report.date,
            "fuelName" to report.fuelName,
            "openingStock" to report.openingStock,
            "receivedVolume" to report.receivedVolume,
            "litersSold" to report.litersSold,
            "closingStock" to report.closingStock,
            "totalSales" to report.totalSales,
            "transactionsCount" to report.transactionsCount,
            "observation" to report.observation,
            "stationCnpj" to report.stationCnpj
        )
    }

    fun mapToDailyReport(map: Map<String, Any?>): DailyReport {
        return DailyReport(
            id = (map["id"] as? Long ?: 0L).toInt(),
            date = map["date"] as? String ?: "",
            fuelName = map["fuelName"] as? String ?: "Gasolina Comum",
            openingStock = map["openingStock"] as? Double ?: 15000.0,
            receivedVolume = map["receivedVolume"] as? Double ?: 0.0,
            litersSold = map["litersSold"] as? Double ?: 0.0,
            closingStock = map["closingStock"] as? Double ?: 12800.0,
            totalSales = map["totalSales"] as? Double ?: 0.0,
            transactionsCount = (map["transactionsCount"] as? Long ?: 0L).toInt(),
            observation = map["observation"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun nozzleToMap(nozzle: Nozzle): Map<String, Any?> {
        return mapOf(
            "id" to nozzle.id,
            "nozzleNumber" to nozzle.nozzleNumber,
            "pumpName" to nozzle.pumpName,
            "tankId" to nozzle.tankId,
            "tankName" to nozzle.tankName,
            "fuelType" to nozzle.fuelType,
            "status" to nozzle.status,
            "stationCnpj" to nozzle.stationCnpj,
            "color" to nozzle.color
        )
    }

    fun mapToNozzle(map: Map<String, Any?>): Nozzle {
        return Nozzle(
            id = (map["id"] as? Long ?: 0L).toInt(),
            nozzleNumber = map["nozzleNumber"] as? String ?: "",
            pumpName = map["pumpName"] as? String ?: "",
            tankId = (map["tankId"] as? Long ?: 0L).toInt(),
            tankName = map["tankName"] as? String ?: "",
            fuelType = map["fuelType"] as? String ?: "",
            status = map["status"] as? String ?: "Ativo",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99",
            color = map["color"] as? String ?: "#005AC1"
        )
    }

    fun calibrationToMap(cal: Calibration): Map<String, Any?> {
        return mapOf(
            "id" to cal.id,
            "date" to cal.date,
            "referenceName" to cal.referenceName,
            "nominalVolume" to cal.nominalVolume,
            "measuredVolume" to cal.measuredVolume,
            "errorPercent" to cal.errorPercent,
            "inspector" to cal.inspector,
            "laudo" to cal.laudo,
            "isConforme" to cal.isConforme,
            "stationCnpj" to cal.stationCnpj
        )
    }

    fun mapToCalibration(map: Map<String, Any?>): Calibration {
        return Calibration(
            id = (map["id"] as? Long ?: 0L).toInt(),
            date = map["date"] as? String ?: "",
            referenceName = map["referenceName"] as? String ?: "",
            nominalVolume = map["nominalVolume"] as? Double ?: 20.0,
            measuredVolume = map["measuredVolume"] as? Double ?: 0.0,
            errorPercent = map["errorPercent"] as? Double ?: 0.0,
            inspector = map["inspector"] as? String ?: "",
            laudo = map["laudo"] as? String ?: "",
            isConforme = map["isConforme"] as? Boolean ?: true,
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun conformityToMap(rec: FuelConformityRecord): Map<String, Any?> {
        return mapOf(
            "id" to rec.id,
            "date" to rec.date,
            "fuelType" to rec.fuelType,
            "densityMeasured" to rec.densityMeasured,
            "temperature" to rec.temperature,
            "ethanolPercent" to rec.ethanolPercent,
            "aspectColor" to rec.aspectColor,
            "isConforme" to rec.isConforme,
            "technicianName" to rec.technicianName,
            "observation" to rec.observation,
            "stationCnpj" to rec.stationCnpj
        )
    }

    fun mapToConformity(map: Map<String, Any?>): FuelConformityRecord {
        return FuelConformityRecord(
            id = (map["id"] as? Long ?: 0L).toInt(),
            date = map["date"] as? String ?: "",
            fuelType = map["fuelType"] as? String ?: "",
            densityMeasured = map["densityMeasured"] as? Double ?: 0.0,
            temperature = map["temperature"] as? Double ?: 0.0,
            ethanolPercent = map["ethanolPercent"] as? Double ?: 0.0,
            aspectColor = map["aspectColor"] as? String ?: "",
            isConforme = map["isConforme"] as? Boolean ?: true,
            technicianName = map["technicianName"] as? String ?: "",
            observation = map["observation"] as? String ?: "",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }

    fun auditToMap(entry: AuditLogEntry): Map<String, Any?> {
        return mapOf(
            "id" to entry.id,
            "date" to entry.date,
            "time" to entry.time,
            "actionType" to entry.actionType,
            "target" to entry.target,
            "details" to entry.details,
            "operator" to entry.operator,
            "complianceStatus" to entry.complianceStatus,
            "stationCnpj" to entry.stationCnpj
        )
    }

    fun mapToAudit(map: Map<String, Any?>): AuditLogEntry {
        return AuditLogEntry(
            id = (map["id"] as? Long ?: 0L).toInt(),
            date = map["date"] as? String ?: "",
            time = map["time"] as? String ?: "",
            actionType = map["actionType"] as? String ?: "",
            target = map["target"] as? String ?: "",
            details = map["details"] as? String ?: "",
            operator = map["operator"] as? String ?: "",
            complianceStatus = map["complianceStatus"] as? String ?: "Regular",
            stationCnpj = map["stationCnpj"] as? String ?: "12.345.678/0001-99"
        )
    }
}
