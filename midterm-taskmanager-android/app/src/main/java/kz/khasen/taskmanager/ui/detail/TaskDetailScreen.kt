package kz.khasen.taskmanager.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.khasen.taskmanager.model.StatusKind
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskStatus
import kz.khasen.taskmanager.ui.AppViewModelProvider
import kz.khasen.taskmanager.ui.components.InfoRow
import kz.khasen.taskmanager.ui.components.PriorityBadge
import kz.khasen.taskmanager.ui.components.SectionTitle
import kz.khasen.taskmanager.ui.components.StatusLabel
import kz.khasen.taskmanager.ui.components.TagRow
import kz.khasen.taskmanager.ui.components.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: TaskDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is TaskDetailUiState.NotFound) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task #${viewModel.taskId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(viewModel.taskId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val s = state) {
            TaskDetailUiState.Loading, TaskDetailUiState.NotFound -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is TaskDetailUiState.Loaded -> TaskDetailContent(
                task = s.task,
                onChangeStatus = viewModel::changeStatus,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete task?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    onChangeStatus: (StatusKind, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityBadge(task.priority)
            Text(task.kind.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Text(task.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        StatusLabel(status = task.status)
        if (task.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            TagRow(tags = task.tags)
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("Description")
        Text(
            text = task.description.ifBlank { "No description" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.description.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(20.dp))
        SectionTitle("Details")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                InfoRow("Estimate", "${task.estimateHours()} h")
                InfoRow("Priority score", "${task.score()}")
                InfoRow("Type", task.kind.label)
                InfoRow("Summary", task.describe())
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("Change status")
        StatusEditor(current = task.status, onApply = onChangeStatus)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatusEditor(
    current: TaskStatus,
    onApply: (StatusKind, String) -> Unit
) {
    var selected by rememberSaveable(current.kind) { mutableStateOf(current.kind) }
    var detail by rememberSaveable(selected) { mutableStateOf(currentDetail(current, selected)) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusKind.entries.forEach { kind ->
                    FilterChip(
                        selected = selected == kind,
                        onClick = { selected = kind },
                        label = { Text(kind.label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            when (selected) {
                StatusKind.TODO -> Text(
                    "Task goes back to the backlog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusKind.IN_PROGRESS -> OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Assignee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                StatusKind.BLOCKED -> OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Blocked by") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                StatusKind.DONE -> OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Hours spent") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onApply(selected, detail) },
                enabled = selected != current.kind || detail != currentDetail(current, selected),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Current: ${current.label()}",
                style = MaterialTheme.typography.labelMedium,
                color = statusColor(current.kind)
            )
        }
    }
}

private fun currentDetail(current: TaskStatus, kind: StatusKind): String =
    if (current.kind != kind) "" else when (current) {
        is TaskStatus.InProgress -> current.assignee
        is TaskStatus.Blocked -> current.reason
        is TaskStatus.Done -> current.hoursSpent.toString()
        TaskStatus.Todo -> ""
    }
