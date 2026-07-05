package com.example.ui

import java.util.Locale

object ANPUtils {

    // --- TECHNICAL LIMITS FOR COMPLIANCE / CALIBRATIONS ---
    
    // Temperature limit: standard operation is between 5.0°C and 45.0°C
    val MIN_TEMPERATURE = 5.0
    val MAX_TEMPERATURE = 45.0

    // Volume of water phase in Proveta (ABNT NBR 13992): starts at 50ml, max is 100ml
    val MIN_WATER_PHASE_VOL = 50.0
    val MAX_WATER_PHASE_VOL = 100.0

    // Calibration can volume deviation limit: typical 20L graduated can neck limit is ±200ml
    val MIN_CALIB_DEVIATION_ML = -200.0
    val MAX_CALIB_DEVIATION_ML = 200.0

    // Density limits at 20°C per fuel type (for input validation)
    fun getDensityLimits(fuelType: String): ClosedRange<Double> {
        return when {
            fuelType.contains("Gasolina", ignoreCase = true) -> 0.650..0.850
            fuelType.contains("Etanol", ignoreCase = true) -> 0.750..0.850
            fuelType.contains("Diesel", ignoreCase = true) -> 0.780..0.920
            else -> 0.600..1.000
        }
    }

    // --- ANP OFFICIAL CALCULATIONS ---

    /**
     * Calculates the density corrected to the reference temperature of 20°C.
     * Formula:
     * - For Ethanol: corrected = measuredDensity + alpha * measuredDensity * (temperature - 20.0)
     * - For others (Gasoline, Diesel): corrected = measuredDensity + 0.0007 * (temperature - 20.0)
     */
    fun calculateCorrectedDensity(density: Double, temperature: Double, isEthanol: Boolean): Double {
        return if (isEthanol) {
            val alpha = 0.00110
            density + alpha * density * (temperature - 20.0)
        } else {
            val factor = 0.0007
            density + factor * (temperature - 20.0)
        }
    }

    /**
     * Calculates the alcohol content of Hydrated Ethanol (Etanol Hidratado) in INPM (% m/m).
     * Formula based on official tables/limits (Res. ANP 907/2022) for corrected density at 20°C:
     * % INPM = 93.8 - (1.3 / 5.7) * (correctedDensity * 1000.0 - 805.3)
     */
    fun calculateEthanolINPM(correctedDensity: Double): Double {
        val rho20 = correctedDensity * 1000.0
        return 93.8 - (1.3 / 5.7) * (rho20 - 805.3)
    }

    /**
     * Calculates the alcohol content of Hydrated Ethanol (Etanol Hidratado) in GL (% v/v).
     * Formula: % GL = 96.0 - (0.6 / 5.7) * (correctedDensity * 1000.0 - 805.3)
     */
    fun calculateEthanolGL(correctedDensity: Double): Double {
        val rho20 = correctedDensity * 1000.0
        return 96.0 - (0.6 / 5.7) * (rho20 - 805.3)
    }

    /**
     * Calculates the anhydrous ethanol content in Gasoline (Teste da Proveta NBR 13992).
     * Formula: % Ethanol = (waterPhaseFinalVolume - 50) * 2 + 1
     * (+ 1.0% correction factor for contraction of the mixture)
     */
    fun calculateGasolineEthanolPercent(waterPhaseFinalVolume: Double): Double {
        if (waterPhaseFinalVolume < 50.0) return 0.0
        return (waterPhaseFinalVolume - 50.0) * 2.0 + 1.0
    }

    /**
     * Calculates the calibration error percentage in a 20L nominal volume measuring can.
     * Formula: (Deviation in ml / 20000 ml) * 100
     */
    fun calculateVolumeErrorPercent(deviationMl: Double): Double {
        return (deviationMl / 20000.0) * 100.0
    }

    // --- COMPLIANCE / CONFORMITY RULES ---

    /**
     * Checks if the fuel parameters are compliant with the official ANP limits.
     */
    fun isFuelCompliant(
        fuelType: String,
        correctedDensity: Double,
        ethanolPercent: Double
    ): Boolean {
        return when {
            fuelType.contains("Gasolina", ignoreCase = true) -> {
                correctedDensity in 0.715..0.775 && ethanolPercent in 26.0..28.0
            }
            fuelType.contains("Etanol", ignoreCase = true) -> {
                correctedDensity in 0.8053..0.8110 && ethanolPercent in 92.5..93.8
            }
            fuelType.contains("Diesel", ignoreCase = true) -> {
                correctedDensity in 0.820..0.850
            }
            else -> true
        }
    }

    /**
     * Checks if the calibration volume error is compliant (within ±0.5% or ±100ml).
     */
    fun isVolumeErrorCompliant(deviationMl: Double): Boolean {
        return Math.abs(deviationMl) <= 100.0
    }
}
