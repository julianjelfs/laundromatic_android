package com.julianjelfs.laundromatic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DailyReminderTest {
    private val today = LocalDate.of(2026, 9, 23)

    @Test
    fun `not due before 9am`() {
        assertFalse(DailyReminder.isDue(today.atTime(8, 59), lastNotifiedOn = null))
    }

    @Test
    fun `due at 9am when not yet sent today`() {
        assertTrue(DailyReminder.isDue(today.atTime(9, 0), lastNotifiedOn = today.minusDays(1)))
    }

    @Test
    fun `due later in the day if 9am was missed`() {
        assertTrue(DailyReminder.isDue(today.atTime(14, 30), lastNotifiedOn = null))
    }

    @Test
    fun `not due twice on the same day`() {
        assertFalse(DailyReminder.isDue(today.atTime(9, 30), lastNotifiedOn = today))
    }

    @Test
    fun `next reminder is today at 9am when it is still early`() {
        assertEquals(today.atTime(9, 0), DailyReminder.nextReminderAt(today.atTime(3, 0)))
    }

    @Test
    fun `next reminder is tomorrow once 9am has arrived`() {
        // The alarm re-arms itself when it fires, so firing exactly at 9am must not re-arm for today.
        assertEquals(today.plusDays(1).atTime(9, 0), DailyReminder.nextReminderAt(today.atTime(9, 0)))
    }

    @Test
    fun `next reminder is tomorrow when armed in the evening`() {
        assertEquals(today.plusDays(1).atTime(9, 0), DailyReminder.nextReminderAt(today.atTime(21, 15)))
    }

    @Test
    fun `no reminder when nothing is overdue or due soon`() {
        assertNull(DailyReminder.build(listOf(item("Towels", dueInDays = 2))))
    }

    @Test
    fun `no reminder for an empty list`() {
        assertNull(DailyReminder.build(emptyList()))
    }

    @Test
    fun `paused items are left out`() {
        assertNull(DailyReminder.build(listOf(item("Towels", dueInDays = -3, paused = true))))
    }

    @Test
    fun `groups overdue, due today and due tomorrow in that order`() {
        val reminder = DailyReminder.build(
            listOf(
                item("Jeans", dueInDays = 1),
                item("Gym kit", dueInDays = 0),
                item("Towels", dueInDays = -3),
                item("Sheets", dueInDays = -1),
                item("Coat", dueInDays = 5),
            ),
        )

        assertEquals("2 overdue, 1 due today, 1 due tomorrow", reminder?.title)
        assertEquals(
            listOf(
                "Overdue: Towels, Sheets",
                "Due today: Gym kit",
                "Due tomorrow: Jeans",
            ),
            reminder?.lines,
        )
    }

    @Test
    fun `due tomorrow alone still produces a reminder`() {
        val reminder = DailyReminder.build(listOf(item("Jeans", dueInDays = 1)))

        assertEquals("1 due tomorrow", reminder?.title)
        assertEquals(listOf("Due tomorrow: Jeans"), reminder?.lines)
    }

    /** Builds an item washed long enough ago that it is due in [dueInDays] days from the real today. */
    private fun item(name: String, dueInDays: Int, paused: Boolean = false): LaundryItem {
        val interval = 7
        val zone = ZoneId.systemDefault()
        val lastWashed = LocalDate.now(zone)
            .minusDays((interval - dueInDays).toLong())
            .atTime(12, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        return LaundryItem(
            id = name,
            name = name,
            intervalInDays = interval,
            lastWashed = lastWashed,
            pausedAt = if (paused) 1L else null,
        ).also { check(it.dueInDays == dueInDays) }
    }
}
