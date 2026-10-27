package kz.khasen.taskmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.khasen.taskmanager.data.TaskRepository
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskStatus

enum class TaskFilter(val label: String) {
    ALL("All"), OPEN("Open"), URGENT("Urgent"), BLOCKED("Blocked"), DONE("Done")
}

enum class SortOrder(val label: String) {
    SCORE("Priority"), TITLE("Title"), ESTIMATE("Estimate")
}

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: SortOrder = SortOrder.SCORE,
    val openCount: Int = 0,
    val doneCount: Int = 0
)

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val sort = MutableStateFlow(SortOrder.SCORE)

    val uiState: StateFlow<TaskListUiState> = combine(repository.tasks, query, filter, sort) { tasks, q, f, s ->
        val visible = tasks
            .filter { matchesFilter(it, f) }
            .filter { matchesQuery(it, q) }
            .let { list ->
                when (s) {
                    SortOrder.SCORE -> list.sortedByDescending { it.score() }
                    SortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
                    SortOrder.ESTIMATE -> list.sortedByDescending { it.estimateHours() }
                }
            }
        TaskListUiState(
            tasks = visible,
            query = q,
            filter = f,
            sort = s,
            openCount = tasks.count { !it.isDone() },
            doneCount = tasks.count { it.isDone() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: TaskFilter) {
        filter.value = value
    }

    fun onSortChange(value: SortOrder) {
        sort.value = value
    }

    fun toggleDone(task: Task) {
        val next = if (task.isDone()) TaskStatus.Todo else TaskStatus.Done(task.estimateHours())
        repository.setStatus(task.id, next)
    }

    private fun matchesFilter(task: Task, f: TaskFilter): Boolean = when (f) {
        TaskFilter.ALL -> true
        TaskFilter.OPEN -> !task.isDone()
        TaskFilter.URGENT -> task.isUrgent()
        TaskFilter.BLOCKED -> task.status is TaskStatus.Blocked
        TaskFilter.DONE -> task.isDone()
    }

    private fun matchesQuery(task: Task, q: String): Boolean {
        if (q.isBlank()) return true
        val needle = q.trim().removePrefix("#")
        return task.title.contains(needle, ignoreCase = true) || task.tags.any { it.contains(needle, ignoreCase = true) }
    }
}
