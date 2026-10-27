package kz.khasen.taskmanager.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.khasen.taskmanager.data.TaskRepository
import kz.khasen.taskmanager.model.ProjectReport
import kz.khasen.taskmanager.model.Task

data class StatsUiState(
    val report: ProjectReport = ProjectReport(0, 0, 0.0, 0.0, emptyMap(), emptyMap(), emptyMap(), emptyMap()),
    val topOpen: List<Task> = emptyList(),
    val syncProgress: Int? = null,
    val lastSyncMessage: String? = null
)

class StatsViewModel(private val repository: TaskRepository) : ViewModel() {

    private val syncProgress = MutableStateFlow<Int?>(null)
    private val lastSyncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StatsUiState> = combine(repository.tasks, syncProgress, lastSyncMessage) { tasks, progress, message ->
        StatsUiState(
            report = repository.report(tasks),
            topOpen = tasks.filter { !it.isDone() }.sortedByDescending { it.score() }.take(3),
            syncProgress = progress,
            lastSyncMessage = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun sync() {
        if (syncProgress.value != null) return
        viewModelScope.launch {
            syncProgress.value = 0
            val count = repository.sync { syncProgress.value = it }
            syncProgress.value = null
            lastSyncMessage.value = "Synced $count tasks"
        }
    }
}
