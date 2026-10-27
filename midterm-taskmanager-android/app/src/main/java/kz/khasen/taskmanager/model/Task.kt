package kz.khasen.taskmanager.model

enum class Priority(val weight: Int, val label: String) {
    LOW(1, "Low"), MEDIUM(2, "Medium"), HIGH(3, "High"), CRITICAL(5, "Critical")
}

enum class TaskKind(val label: String) {
    BUG("Bug"), FEATURE("Feature"), CHORE("Chore")
}

enum class StatusKind(val label: String) {
    TODO("To do"), IN_PROGRESS("In progress"), BLOCKED("Blocked"), DONE("Done")
}

sealed class TaskStatus {
    data object Todo : TaskStatus()
    data class InProgress(val assignee: String) : TaskStatus()
    data class Blocked(val reason: String) : TaskStatus()
    data class Done(val hoursSpent: Double) : TaskStatus()

    val kind: StatusKind
        get() = when (this) {
            is Todo -> StatusKind.TODO
            is InProgress -> StatusKind.IN_PROGRESS
            is Blocked -> StatusKind.BLOCKED
            is Done -> StatusKind.DONE
        }

    fun label(): String = when (this) {
        is Todo -> "To do"
        is InProgress -> "In progress · $assignee"
        is Blocked -> "Blocked · $reason"
        is Done -> "Done · ${hoursSpent}h"
    }
}

interface Describable {
    fun describe(): String
}

interface Estimable {
    fun estimateHours(): Double
}

abstract class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Priority,
    val tags: Set<String>,
    val status: TaskStatus
) : Describable, Estimable {

    abstract val kind: TaskKind

    abstract fun copyWith(
        title: String = this.title,
        description: String = this.description,
        priority: Priority = this.priority,
        tags: Set<String> = this.tags,
        status: TaskStatus = this.status
    ): Task

    override fun describe(): String = "${kind.label} · ${priority.label} · ${status.label()}"

    fun isDone(): Boolean = status is TaskStatus.Done

    fun isUrgent(): Boolean = !isDone() && priority.weight >= Priority.HIGH.weight

    fun score(): Int = if (isDone()) 0 else priority.weight * 10 + bonus()

    protected open fun bonus(): Int = 0
}

class Bug(
    id: Int,
    title: String,
    description: String,
    priority: Priority,
    tags: Set<String>,
    val severity: Int,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, description, priority, tags, status) {
    override val kind = TaskKind.BUG
    override fun estimateHours(): Double = severity * 1.5
    override fun bonus(): Int = severity * 3
    override fun copyWith(title: String, description: String, priority: Priority, tags: Set<String>, status: TaskStatus): Task =
        Bug(id, title, description, priority, tags, priority.weight, status)
}

class Feature(
    id: Int,
    title: String,
    description: String,
    priority: Priority,
    tags: Set<String>,
    val storyPoints: Int,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, description, priority, tags, status) {
    override val kind = TaskKind.FEATURE
    override fun estimateHours(): Double = storyPoints * 4.0
    override fun bonus(): Int = storyPoints
    override fun copyWith(title: String, description: String, priority: Priority, tags: Set<String>, status: TaskStatus): Task =
        Feature(id, title, description, priority, tags, storyPoints, status)
}

class Chore(
    id: Int,
    title: String,
    description: String,
    priority: Priority,
    tags: Set<String>,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, description, priority, tags, status) {
    override val kind = TaskKind.CHORE
    override fun estimateHours(): Double = 1.0
    override fun copyWith(title: String, description: String, priority: Priority, tags: Set<String>, status: TaskStatus): Task =
        Chore(id, title, description, priority, tags, status)
}

data class TaskDraft(
    val title: String = "",
    val description: String = "",
    val kind: TaskKind = TaskKind.FEATURE,
    val priority: Priority = Priority.MEDIUM,
    val tags: Set<String> = emptySet()
)

data class ProjectReport(
    val total: Int,
    val done: Int,
    val openHours: Double,
    val doneHours: Double,
    val byPriority: Map<Priority, Int>,
    val byKind: Map<TaskKind, Int>,
    val byStatus: Map<StatusKind, Int>,
    val byTag: Map<String, Int>
) {
    val completionPercent: Int
        get() = if (total == 0) 0 else done * 100 / total
}
