package kz.khasen.taskmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kz.khasen.taskmanager.model.Priority

@Composable
fun priorityColors(priority: Priority): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (priority) {
        Priority.LOW -> scheme.secondaryContainer to scheme.onSecondaryContainer
        Priority.MEDIUM -> scheme.primaryContainer to scheme.onPrimaryContainer
        Priority.HIGH -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        Priority.CRITICAL -> scheme.errorContainer to scheme.onErrorContainer
    }
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (bg, fg) = priorityColors(priority)
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = priority.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}
