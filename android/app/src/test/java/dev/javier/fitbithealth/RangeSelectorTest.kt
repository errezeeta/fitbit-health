package dev.javier.fitbithealth

import dev.javier.fitbithealth.ui.metrics.HealthRange
import dev.javier.fitbithealth.ui.metrics.rangeQuery
import dev.javier.fitbithealth.ui.metrics.validateCustomRange
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeSelectorTest {
    private val today = LocalDate.of(2026, 8, 3)

    @Test fun presetsHaveExpectedInclusiveDates() {
        assertEquals("2026-07-28" to "2026-08-03", rangeQuery(HealthRange.SevenDays, today))
        assertEquals("2026-07-05" to "2026-08-03", rangeQuery(HealthRange.ThirtyDays, today))
        assertEquals("2026-05-06" to "2026-08-03", rangeQuery(HealthRange.NinetyDays, today))
    }

    @Test fun customRangeValidationRejectsInvalidDates() {
        assertTrue(validateCustomRange("2026-08-01", "2026-08-03"))
        assertFalse(validateCustomRange("2026-08-03", "2026-08-01"))
        assertFalse(validateCustomRange("bad", "2026-08-01"))
    }

    @Test fun customRangeIsPassedThrough() {
        val range = HealthRange.Custom("2026-01-01", "2026-01-31")
        assertEquals("2026-01-01" to "2026-01-31", rangeQuery(range, today))
    }
}
