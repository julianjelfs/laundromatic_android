package com.julianjelfs.laundromatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Reminder(
    val title: String,
    val lines: List<String>,
)

object DailyReminder {
    val reminderTime: LocalTime = LocalTime.of(9, 0)

    /** The reminder is due once per day, on the first check at or after [reminderTime]. */
    fun isDue(now: LocalDateTime, lastNotifiedOn: LocalDate?): Boolean =
        !now.toLocalTime().isBefore(reminderTime) && lastNotifiedOn != now.toLocalDate()

    /** Today at [reminderTime] if that is still ahead of [now], otherwise tomorrow at [reminderTime]. */
    fun nextReminderAt(now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate().atTime(reminderTime)
        return if (now.isBefore(today)) today else today.plusDays(1)
    }

    /** Returns null when no active item is overdue, due today or due tomorrow. */
    fun build(items: List<LaundryItem>): Reminder? {
        val active = items.filterNot(LaundryItem::isPaused).sortedBy(LaundryItem::dueInDays)
        val overdue = active.filter { it.dueInDays < 0 }
        val dueToday = active.filter { it.dueInDays == 0 }
        val dueTomorrow = active.filter { it.dueInDays == 1 }

        val groups = listOf(
            "overdue" to overdue,
            "due today" to dueToday,
            "due tomorrow" to dueTomorrow,
        ).filter { (_, group) -> group.isNotEmpty() }

        if (groups.isEmpty()) return null

        return Reminder(
            title = groups.joinToString(", ") { (label, group) -> "${group.size} $label" },
            lines = groups.map { (label, group) ->
                "${label.replaceFirstChar(Char::uppercase)}: ${group.joinToString(", ", transform = LaundryItem::name)}"
            },
        )
    }
}
