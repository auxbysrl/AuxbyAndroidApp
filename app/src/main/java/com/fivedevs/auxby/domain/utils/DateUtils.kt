package com.fivedevs.auxby.domain.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class DateUtils {

    private val utcTimeZone: TimeZone = TimeZone.getTimeZone("UTC")

    fun formatToUTCDate(dateToFormat: String, outputFormat: String): String {
        val inputFormatter = SimpleDateFormat(DATE_TIME_FORMAT_DEFAULT, Locale.getDefault())
        inputFormatter.timeZone = utcTimeZone
        val date = inputFormatter.parse(dateToFormat)

        val outputFormatter = SimpleDateFormat(outputFormat, currentLocale)
        // Do not set the time zone on the outputFormatter to let it use the device's local time zone

        return date?.let { outputFormatter.format(it) }.toString().lowercase()
    }

    // "2019-12-11T11:40:49.000+00:00"
    fun getRemainingTimeForAuction(dateToFormat: String?, dateFormat: String = DATE_TIME_FORMAT_DEFAULT): String {
        if (dateToFormat.orEmpty().isEmpty()) return ""
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = utcTimeZone
        val date = formatter.parse(dateToFormat.toString())
        return date?.let { formatDuration(Date(), it, currentLocale) }.orEmpty()
    }

    private fun formatDuration(start: Date, end: Date, locale: Locale): String {
        val daysInMillis = 1000 * 60 * 60 * 24
        val hoursInMillis = 1000 * 60 * 60
        val minutesInMillis = 1000 * 60

        val durationInMillis = kotlin.math.abs(end.time - start.time)
        val days = durationInMillis / daysInMillis
        val hours = (durationInMillis % daysInMillis) / hoursInMillis
        val minutes = (durationInMillis % hoursInMillis) / minutesInMillis

        // Format the duration as "Xd Xh | DayOfWeek, X:XXam/pm"
        val dateFormat = SimpleDateFormat("EEEE, h:mm a", locale)
        val endDateFormatted = dateFormat.format(end)

        // Convert "AM" and "PM" to lowercase
        val formattedAMPM = endDateFormatted.replace("AM", "am").replace("PM", "pm")

        return when {
            days > 0 -> "${days}d ${hours}h | $formattedAMPM"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun getFormattedDateForPreview(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        val formatter = SimpleDateFormat(FORMAT_YYYY_MM_DD, Locale.getDefault())
        formatter.timeZone = utcTimeZone
        val date = formatter.parse(dateToFormat)
        return date?.let { SimpleDateFormat(FORMAT_DD_MMM_YYYY, currentLocale).format(it) }.toString()
            .lowercase()
    }

    fun getFormattedDateForAuctionPreview(dateToFormat: String): String {
        val formatter = SimpleDateFormat(FORMAT_YYYY_MM_DD, Locale.getDefault()).parse(dateToFormat)
        return formatter?.let { SimpleDateFormat(DATE_TIME_FORMAT_AUCTION, currentLocale).format(it) }.toString()
    }

    fun getFormattedDateForOffer(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        return formatToUTCDate(dateToFormat, FORMAT_DD_MMM_YYYY)
    }

    fun getFormattedDateForUserActive(dateToFormat: String, outputFormat: String = FORMAT_HH_MM): String {
        if (dateToFormat.isEmpty()) return ""
        return formatToUTCDate(dateToFormat, outputFormat)
    }

    fun getFormatOfferEndDate(dateToFormat: String): String {
        val formatter = SimpleDateFormat(FORMAT_DD_MMM_YYYY, Locale.getDefault()).parse(dateToFormat)
        return formatter?.let { SimpleDateFormat(DATE_FORMAT_SEVER_END_OFFER, currentLocale).format(it) }
            .toString().lowercase()
    }

    fun getFormatChatDividerDate(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        return formatToUTCDate(dateToFormat, FORMAT_DD_MM_YYYY_DOTS)
    }

    fun getFormattedDateForBidHistory(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        return formatToUTCDate(dateToFormat, FORMAT_BID_HISTORY_DATE)
    }

    fun getFormattedDateYearMonthDay(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        val formatter = SimpleDateFormat(FORMAT_YYYY_MM_DD, Locale.getDefault())
        formatter.timeZone = utcTimeZone
        val date = formatter.parse(dateToFormat)
        return date?.let { SimpleDateFormat(FORMAT_DD_MMM_YYYY, currentLocale).format(it) }.toString()
            .lowercase()
    }

    fun getFormattedNotificationDate(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        val formatter = SimpleDateFormat(FORMAT_YYYY_MM_DD, Locale.getDefault())
        formatter.timeZone = utcTimeZone
        val date = formatter.parse(dateToFormat)
        return date?.let { SimpleDateFormat(FORMAT_NOTIFICATION_DATE, currentLocale).format(it) }.toString()
            .lowercase()
    }

    fun getFormattedDateYearMonthDayFromServer(dateToFormat: String): String {
        if (dateToFormat.isEmpty()) return ""
        val formatter = SimpleDateFormat(DATE_TIME_FORMAT_DEFAULT, Locale.getDefault())
        formatter.timeZone = utcTimeZone
        val date = formatter.parse(dateToFormat)
        return date?.let { SimpleDateFormat(FORMAT_YYYY_MM_DD, currentLocale).format(it) }.toString()
            .lowercase()
    }

    fun isNewer(dateTime1: String, dateTime2: String): Boolean {
        val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val parsedDateTime1: OffsetDateTime = OffsetDateTime.parse(dateTime1, formatter)
        val parsedDateTime2: OffsetDateTime = OffsetDateTime.parse(dateTime2, formatter)
        return parsedDateTime1.isAfter(parsedDateTime2)
    }

    fun isToday(dateString: String): Boolean {
        return try {
            val today = LocalDate.now(ZoneOffset.UTC).toString()
            //val messageDate = LocalDate.parse(getFormattedDateYearMonthDayFromServer(dateString))
            dateString.equals(today, true)
        } catch (exception: Exception) {
            false
        }
    }

    fun isYesterday(dateString: String): Boolean {
        return try {
            val today = LocalDate.now(ZoneOffset.UTC)
            val messageDate = LocalDate.parse(dateString)
            return messageDate.isEqual(today.minusDays(1))
        } catch (exception: Exception) {
            false
        }
    }

    fun calculateDaysLeft(endDate: String): Long {
        // Get the current date
        val currentDate = Calendar.getInstance().time

        // Parse the end date string into a Date object
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDateDate: Date = sdf.parse(endDate) ?: currentDate

        // Calculate the difference in milliseconds
        val timeDifference = endDateDate.time - currentDate.time

        // Convert milliseconds to days
        return TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS)
    }

    fun addDaysToDate(currentDate: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    companion object {
        var currentLocale = Locale("ro", "RO")

        const val FORMAT_DD_MMM_YYYY = "dd MMM yyyy"
        const val FORMAT_yyyyMMdd_HH = "yyyyMMdd_HHmmss"

        private const val FORMAT_HH_MM = "HH:mm"
        const val FORMAT_YYYY_MM_DD = "yyyy-MM-dd"
        private const val FORMAT_DD_MM_YYYY_DOTS = "dd.MM.yyyy"
        private const val DATE_FORMAT_SEVER_END_OFFER = "yyyy-MM-dd"
        private const val DATE_TIME_FORMAT_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss"
        private const val FORMAT_BID_HISTORY_DATE = "dd MMM, yyyy 'la' HH:mm"
        private const val FORMAT_NOTIFICATION_DATE = "dd MMM yyyy, HH:mm"
        private const val DATE_TIME_FORMAT_AUCTION = "dd'd' HH'h |' EEEE',' HH:mmaa"
    }
}