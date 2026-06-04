package com.example.util

import com.example.data.DemandLevel
import com.example.data.LegalRisk

object Formatting {
    fun formatProbability(prob: Double): String = "${(prob * 100).toInt()}%"
    fun formatConfidence(conf: Double): String = "${(conf * 100).toInt()}% conf."
    fun formatFreshness(minutes: Int): String = if (minutes == 0) "Just now" else "$minutes min ago"
    fun formatPrice(price: Double): String = "€%.2f".format(price)
    fun formatDistance(meters: Int): String = "${meters}m"
    
    fun legalRiskLabel(risk: LegalRisk): String = when (risk) {
        LegalRisk.LOW -> "Low Risk"
        LegalRisk.MEDIUM -> "Medium Risk"
        LegalRisk.HIGH -> "High Risk"
        LegalRisk.RESTRICTED -> "Restricted"
    }

    fun demandLevelLabel(level: DemandLevel): String = level.name.lowercase().replaceFirstChar { it.uppercase() } + " Demand"
}
