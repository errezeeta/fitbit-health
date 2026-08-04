package dev.javier.fitbithealth.ui.metrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface HealthRange {
    data object SevenDays : HealthRange
    data object ThirtyDays : HealthRange
    data object NinetyDays : HealthRange
    data class Custom(val start: String, val end: String) : HealthRange
}

@Composable
fun RangeSelector(
    selected: HealthRange,
    onSelected: (HealthRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ranges = listOf(
        HealthRange.SevenDays to "7 días",
        HealthRange.ThirtyDays to "30 días",
        HealthRange.NinetyDays to "90 días",
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ranges.forEach { (range, label) ->
                FilterChip(
                    selected = selected::class == range::class,
                    onClick = { onSelected(range) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

fun validateCustomRange(start: String, end: String): Boolean {
    if (start.length != 10 || end.length != 10) return false
    return runCatching { java.time.LocalDate.parse(start) <= java.time.LocalDate.parse(end) }.getOrDefault(false)
}

fun rangeQuery(range: HealthRange, today: java.time.LocalDate): Pair<String, String> = when (range) {
    HealthRange.SevenDays -> today.minusDays(6).toString() to today.toString()
    HealthRange.ThirtyDays -> today.minusDays(29).toString() to today.toString()
    HealthRange.NinetyDays -> today.minusDays(89).toString() to today.toString()
    is HealthRange.Custom -> range.start to range.end
}
