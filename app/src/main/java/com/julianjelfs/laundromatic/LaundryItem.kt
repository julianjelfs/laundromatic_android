package com.julianjelfs.laundromatic

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class LaundryItem(
    val id: String,
    val name: String,
    val intervalInDays: Int,
    val lastWashed: Long,
    val pausedAt: Long?,
) {
    val dueInDays: Int = Companion.dueInDays(lastWashed, intervalInDays)

    val isPaused: Boolean = pausedAt != null

    val status: LaundryStatus =
        when {
            dueInDays < 0 -> LaundryStatus.Overdue
            dueInDays == 0 -> LaundryStatus.Due
            else -> LaundryStatus.UnderControl
        }

    val dueLabel: String =
        when (dueInDays) {
            0 -> "Due today"
            1 -> "Due tomorrow"
            -1 -> "Due yesterday"
            else ->
                if (dueInDays < 0) {
                    "Overdue by ${-dueInDays} days"
                } else {
                    "Due in $dueInDays days"
                }
        }

    val intervalLabel: String = "Every $intervalInDays day" + if (intervalInDays == 1) "" else "s"

    companion object {
        private val zoneId: ZoneId = ZoneId.systemDefault()

        fun dueInDays(lastWashed: Long, intervalInDays: Int): Int {
            val lastWashedDate = Instant.ofEpochMilli(lastWashed).atZone(zoneId).toLocalDate()
            val dueDate = lastWashedDate.plusDays(intervalInDays.toLong())
            return ChronoUnit.DAYS.between(LocalDate.now(zoneId), dueDate).toInt()
        }
    }
}

enum class LaundryStatus {
    UnderControl,
    Due,
    Overdue,
}
