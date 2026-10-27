package kz.khasen.taskmanager.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kz.khasen.taskmanager.data.TaskRepository
import kz.khasen.taskmanager.model.StatusKind
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskStatus
import kz.khasen.taskmanager.ui.navigation.Routes

sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data object NotFound : TaskDetailUiState
    data class Loaded(val task: Task) : TaskDetailUiState
}

class TaskDetailViewModel(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: Int = checkNotNull(savedStateHandle[Routes.ARG_TASK_ID])

    val uiState: StateFlow<TaskDetailUiState> = repository.task(taskId)
        .map { task -> if (task == null) TaskDetailUiState.NotFound else TaskDetailUiState.Loaded(task) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskDetailUiState.Loading)

    fun changeStatus(kind: StatusKind, detail: String) {
        val current = (uiState.value as? TaskDetailUiState.Loaded)?.task ?: return
        val status: TaskStatus = when (kind) {
            StatusKind.TODO -> TaskStatus.Todo
            StatusKind.IN_PROGRESS -> TaskStatus.InProgress(detail.ifBlank { "unassigned" })
            StatusKind.BLOCKED -> TaskStatus.Blocked(detail.ifBlank { "no reason given" })
            StatusKind.DONE -> TaskStatus.Done(detail.toDoubleOrNull() ?: current.estimateHours())
        }
        repository.setStatus(taskId, status)
    }

    fun delete() {
        repository.remove(taskId)
    }
}
