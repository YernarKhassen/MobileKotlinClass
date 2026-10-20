package kz.khasen.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StateDemoCard(
    viewModelCounter: Int,
    onViewModelIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rememberCounter by remember { mutableIntStateOf(0) }
    var saveableCounter by rememberSaveable { mutableIntStateOf(0) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("State survival demo", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap each counter, then rotate the device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CounterButton(
                    label = "remember",
                    hint = "resets",
                    value = rememberCounter,
                    onClick = { rememberCounter++ },
                    modifier = Modifier.weight(1f)
                )
                CounterButton(
                    label = "rememberSaveable",
                    hint = "survives",
                    value = saveableCounter,
                    onClick = { saveableCounter++ },
                    modifier = Modifier.weight(1f)
                )
                CounterButton(
                    label = "ViewModel",
                    hint = "survives",
                    value = viewModelCounter,
                    onClick = onViewModelIncrement,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CounterButton(
    label: String,
    hint: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = onClick, modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
        Column {
            Text("$value", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 1)
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
