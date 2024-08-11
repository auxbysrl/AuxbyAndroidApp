package com.fivedevs.auxby.domain.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    val priceDecimalFormat = DecimalFormat("#,###.##")

    fun formatDateStringToHourAndMinute(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date ?: Date())
    }
}