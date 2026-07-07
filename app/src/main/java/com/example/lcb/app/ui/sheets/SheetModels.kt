package com.example.lcb.app.ui.sheets

import kotlin.math.roundToInt

internal enum class WeightUnit(
    val minTenths: Int,
    val maxTenths: Int,
    val wholeRange: IntRange,
) {
    Kg(300, 2500, 30..250),
    Lbs(660, 5510, 66..551);

    fun toKgTenths(valueTenths: Int): Int {
        return if (this == Kg) valueTenths else (valueTenths / 2.20462262185).roundToInt()
    }

    fun fromKgTenths(kgTenths: Int): Int {
        return if (this == Kg) kgTenths else (kgTenths * 2.20462262185).roundToInt()
    }
}
