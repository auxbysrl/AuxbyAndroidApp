package com.fivedevs.auxby.domain.utils.extensions

import java.nio.ByteBuffer
import java.text.NumberFormat
import java.util.*

fun Boolean?.toInt(): Int = if (this == true) 1 else 0

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

fun Int.toByteArray(): ByteArray? = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

fun Int?.toBoolean(): Boolean = this == 1

fun Int?.greaterThanOrEqualWith(other: Int?): Boolean =
    this != null && other != null && this >= other

fun Int?.greaterThan(other: Int?): Boolean = this != null && other != null && this > other

fun Long?.orZero(): Long = this ?: 0L

fun Int?.orZero(): Int = this ?: 0

fun Double?.orZero(): Double = this ?: 0.toDouble()

fun <T : Number> T?.orElse(x: T): T = this ?: x

fun String?.orElse(text: String): String = this ?: text

fun MutableList<String>?.orElse(): MutableList<String> = this ?: mutableListOf()

fun Double.usFormat(): String = NumberFormat.getNumberInstance(Locale.US).format(this)

fun Float.usFormat(): String = NumberFormat.getNumberInstance(Locale.US).format(this)