package kz.khasen.taskmanager.ui.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.khasen.taskmanager.model.Priority
import kz.khasen.taskmanager.model.TaskKind
import kz.khasen.taskmanager.ui.AppViewModelProvider
import kz.khasen.taskmanager.ui.components.PriorityBadge
import kz.khasen.taskmanager.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    onDone: () -> Unit,
    viewModel: EditTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New task" else "Edit task") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        EditTaskForm(
            state = state,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onTagsChange = viewModel::onTagsChange,
            onKindChange = viewModel::onKindChange,
            onPriorityChange = viewModel::onPriorityChange,
            onSuggestedTag = viewModel::addSuggestedTag,
            onSave = viewModel::save,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun EditTaskForm(
    state: EditTaskUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onKindChange: (TaskKind) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onSuggestedTag: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        SectionTitle("Type")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskKind.entries.forEach { k ->
                FilterChip(selected = state.kind == k, onClick = { onKindChange(k) }, label = { Text(k.label) })
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Priority")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.entries.forEach { p ->
                FilterChip(selected = state.priority == p, onClick = { onPriorityChange(p) }, label = { Text(p.label) })
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Tags")
        OutlinedTextField(
            value = state.tagsInput,
            onValueChange = onTagsChange,
            label = { Text("Comma separated") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.suggestedTags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.suggestedTags.forEach { tag ->
                    AssistChip(
                        onClick = { onSuggestedTag(tag) },
                        label = { Text("#$tag") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Estimated effort", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${state.estimatePreview} h · ${state.kind.label}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                PriorityBadge(state.priority)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onSave, enabled = state.canSave, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isNew) "Create task" else "Save changes")
        }
        Spacer(Modifier.height(32.dp))
    }
}
