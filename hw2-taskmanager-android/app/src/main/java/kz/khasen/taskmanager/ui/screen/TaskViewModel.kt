package kz.khasen.taskmanager.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.khasen.taskmanager.data.TaskRepository
import kz.khasen.taskmanager.model.Priority
import kz.khasen.taskmanager.model.ProjectReport
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskKind
import kz.khasen.taskmanager.model.TaskStatus

enum class TaskFilter(val label: String) {
    ALL("All"), OPEN("Open"), DONE("Done"), URGENT("Urgent")
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val report: ProjectReport = ProjectReport(0, 0, 0.0, emptyMap()),
    val syncProgress: Int? = null,
    val viewModelCounter: Int = 0
)

class TaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val syncProgress = MutableStateFlow<Int?>(null)
    private val counter = MutableStateFlow(0)

    val uiState: StateFlow<TaskUiState> = combine(
        repository.tasks, query, filter, syncProgress, counter
    ) { tasks, q, f, progress, count ->
        val filtered = tasks
            .filter { matchesFilter(it, f) }
            .filter { q.isBlank() || it.title.contains(q, ignoreCase = true) || it.tags.any { t -> t.contains(q, ignoreCase = true) } }
            .sortedByDescending { it.score() }
        TaskUiState(
            tasks = filtered,
            query = q,
            filter = f,
            report = repository.report(tasks),
            syncProgress = progress,
            viewModelCounter = count
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: TaskFilter) {
        filter.value = value
    }

    fun addTask(title: String, kind: TaskKind, priority: Priority, tags: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val tagSet = tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        repository.add(cleanTitle, kind, priority, tagSet)
    }

    fun toggleDone(task: Task) {
        val next = if (task.isDone()) TaskStatus.Todo else TaskStatus.Done(task.estimateHours())
        repository.setStatus(task.id, next)
    }

    fun remove(task: Task) {
        repository.remove(task.id)
    }

    fun incrementCounter() {
        counter.update { it + 1 }
    }

    fun syncWithServer() {
        if (syncProgress.value != null) return
        viewModelScope.launch {
            syncProgress.value = 0
            repository.sync { syncProgress.value = it }
            syncProgress.value = null
        }
    }

    private fun matchesFilter(task: Task, f: TaskFilter): Boolean = when (f) {
        TaskFilter.ALL -> true
        TaskFilter.OPEN -> !task.isDone()
        TaskFilter.DONE -> task.isDone()
        TaskFilter.URGENT -> !task.isDone() && task.priority.weight >= Priority.HIGH.weight
    }
}
