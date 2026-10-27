package kz.khasen.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kz.khasen.taskmanager.model.StatusKind
import kz.khasen.taskmanager.model.TaskStatus

@Composable
fun statusColor(kind: StatusKind): Color = when (kind) {
    StatusKind.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
    StatusKind.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    StatusKind.BLOCKED -> MaterialTheme.colorScheme.error
    StatusKind.DONE -> MaterialTheme.colorScheme.tertiary
}

@Composable
fun StatusLabel(status: TaskStatus, modifier: Modifier = Modifier) {
    Text(
        text = status.label(),
        style = MaterialTheme.typography.bodySmall,
        color = statusColor(status.kind),
        modifier = modifier
    )
}

@Composable
fun TagRow(tags: Set<String>, modifier: Modifier = Modifier, onTagClick: ((String) -> Unit)? = null) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
