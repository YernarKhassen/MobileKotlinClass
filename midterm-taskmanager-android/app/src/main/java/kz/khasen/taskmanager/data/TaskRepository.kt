package kz.khasen.taskmanager.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kz.khasen.taskmanager.model.Bug
import kz.khasen.taskmanager.model.Chore
import kz.khasen.taskmanager.model.Feature
import kz.khasen.taskmanager.model.Priority
import kz.khasen.taskmanager.model.ProjectReport
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskDraft
import kz.khasen.taskmanager.model.TaskKind
import kz.khasen.taskmanager.model.TaskStatus

class TaskRepository {
    private val _tasks = MutableStateFlow(seed())
    val tasks: StateFlow<List<Task>> = _tasks

    private var nextId = _tasks.value.maxOf { it.id } + 1

    fun task(id: Int): Flow<Task?> = _tasks.map { list -> list.firstOrNull { it.id == id } }

    fun add(draft: TaskDraft): Int {
        val id = nextId++
        val task = when (draft.kind) {
            TaskKind.BUG -> Bug(id, draft.title, draft.description, draft.priority, draft.tags, severity = draft.priority.weight)
            TaskKind.FEATURE -> Feature(id, draft.title, draft.description, draft.priority, draft.tags, storyPoints = draft.priority.weight * 2)
            TaskKind.CHORE -> Chore(id, draft.title, draft.description, draft.priority, draft.tags)
        }
        _tasks.update { it + task }
        return id
    }

    fun update(id: Int, draft: TaskDraft) {
        _tasks.update { list ->
            list.map { existing ->
                if (existing.id != id) return@map existing
                if (existing.kind == draft.kind) {
                    existing.copyWith(draft.title, draft.description, draft.priority, draft.tags)
                } else {
                    when (draft.kind) {
                        TaskKind.BUG -> Bug(id, draft.title, draft.description, draft.priority, draft.tags, draft.priority.weight, existing.status)
                        TaskKind.FEATURE -> Feature(id, draft.title, draft.description, draft.priority, draft.tags, draft.priority.weight * 2, existing.status)
                        TaskKind.CHORE -> Chore(id, draft.title, draft.description, draft.priority, draft.tags, existing.status)
                    }
                }
            }
        }
    }

    fun setStatus(id: Int, status: TaskStatus) {
        _tasks.update { list -> list.map { if (it.id == id) it.copyWith(status = status) else it } }
    }

    fun remove(id: Int) {
        _tasks.update { list -> list.filter { it.id != id } }
    }

    fun report(list: List<Task>): ProjectReport {
        val (done, open) = list.partition { it.isDone() }
        return ProjectReport(
            total = list.size,
            done = done.size,
            openHours = open.map { it.estimateHours() }.fold(0.0) { a, b -> a + b },
            doneHours = done.map { (it.status as TaskStatus.Done).hoursSpent }.fold(0.0) { a, b -> a + b },
            byPriority = list.groupingBy { it.priority }.eachCount(),
            byKind = list.groupingBy { it.kind }.eachCount(),
            byStatus = list.groupingBy { it.status.kind }.eachCount(),
            byTag = list.flatMap { it.tags }.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }.associate { it.key to it.value }
        )
    }

    suspend fun sync(onProgress: (Int) -> Unit): Int {
        val total = _tasks.value.size.coerceAtLeast(1)
        for (i in 1..total) {
            delay(200)
            onProgress(i * 100 / total)
        }
        return total
    }

    private fun seed(): List<Task> = listOf(
        Bug(1, "Crash on login when offline", "App crashes with NPE when the device has no network during sign-in. Reproduces on Android 12+.", Priority.CRITICAL, setOf("auth", "mobile"), 4, TaskStatus.InProgress("Yeldar")),
        Feature(2, "Dark theme support", "Add a dark color scheme and follow the system setting.", Priority.MEDIUM, setOf("ui", "mobile"), 5),
        Chore(3, "Update CI runner image", "Move the pipeline to the ubuntu-24.04 image.", Priority.LOW, setOf("devops"), TaskStatus.Done(0.5)),
        Bug(4, "Wrong currency rounding", "KZT amounts are rounded half-down instead of half-even on the receipt screen.", Priority.HIGH, setOf("payments"), 3, TaskStatus.Blocked("waiting for API spec")),
        Feature(5, "Export tasks to CSV", "Allow exporting the task list with filters applied.", Priority.LOW, setOf("reports"), 3),
        Chore(6, "Rotate API keys", "Quarterly rotation of the payment gateway keys.", Priority.HIGH, setOf("devops", "security")),
        Feature(7, "Push notifications for blocked tasks", "Notify the assignee when a task they depend on becomes blocked.", Priority.HIGH, setOf("notifications", "mobile"), 8)
    )
}
