package kz.khasen.taskmanager.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kz.khasen.taskmanager.model.Bug
import kz.khasen.taskmanager.model.Chore
import kz.khasen.taskmanager.model.Feature
import kz.khasen.taskmanager.model.Priority
import kz.khasen.taskmanager.model.ProjectReport
import kz.khasen.taskmanager.model.Task
import kz.khasen.taskmanager.model.TaskKind
import kz.khasen.taskmanager.model.TaskStatus

class TaskRepository {
    private val _tasks = MutableStateFlow(seed())
    val tasks: StateFlow<List<Task>> = _tasks

    private var nextId = _tasks.value.size + 1

    fun add(title: String, kind: TaskKind, priority: Priority, tags: Set<String>) {
        val id = nextId++
        val task = when (kind) {
            TaskKind.BUG -> Bug(id, title, priority, tags, severity = priority.weight)
            TaskKind.FEATURE -> Feature(id, title, priority, tags, storyPoints = priority.weight * 2)
            TaskKind.CHORE -> Chore(id, title, priority, tags)
        }
        _tasks.update { it + task }
    }

    fun setStatus(id: Int, status: TaskStatus) {
        _tasks.update { list -> list.map { if (it.id == id) it.withStatus(status) else it } }
    }

    fun remove(id: Int) {
        _tasks.update { list -> list.filter { it.id != id } }
    }

    fun report(list: List<Task>): ProjectReport = ProjectReport(
        total = list.size,
        done = list.count { it.isDone() },
        openHours = list.filter { !it.isDone() }.map { it.estimateHours() }.fold(0.0) { a, b -> a + b },
        byPriority = list.groupingBy { it.priority }.eachCount()
    )

    suspend fun sync(onProgress: (Int) -> Unit) {
        val total = _tasks.value.size.coerceAtLeast(1)
        for (i in 1..total) {
            delay(200)
            onProgress(i * 100 / total)
        }
    }

    private fun seed(): List<Task> = listOf(
        Bug(1, "Crash on login when offline", Priority.CRITICAL, setOf("auth", "mobile"), 4, TaskStatus.InProgress("Yeldar")),
        Feature(2, "Dark theme support", Priority.MEDIUM, setOf("ui", "mobile"), 5),
        Chore(3, "Update CI runner image", Priority.LOW, setOf("devops"), TaskStatus.Done(0.5)),
        Bug(4, "Wrong currency rounding", Priority.HIGH, setOf("payments"), 3, TaskStatus.Blocked("waiting for API spec")),
        Feature(5, "Export tasks to CSV", Priority.LOW, setOf("reports"), 3),
        Chore(6, "Rotate API keys", Priority.HIGH, setOf("devops", "security"))
    )
}
