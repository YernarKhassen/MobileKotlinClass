package kz.khasen.taskmanager.model

enum class Priority(val weight: Int, val label: String) {
    LOW(1, "Low"), MEDIUM(2, "Medium"), HIGH(3, "High"), CRITICAL(5, "Critical")
}

enum class TaskKind(val label: String) {
    BUG("Bug"), FEATURE("Feature"), CHORE("Chore")
}

sealed class TaskStatus {
    data object Todo : TaskStatus()
    data class InProgress(val assignee: String) : TaskStatus()
    data class Done(val hoursSpent: Double) : TaskStatus()
    data class Blocked(val reason: String) : TaskStatus()

    fun label(): String = when (this) {
        is Todo -> "To do"
        is InProgress -> "In progress · $assignee"
        is Done -> "Done · ${hoursSpent}h"
        is Blocked -> "Blocked · $reason"
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
    val priority: Priority,
    val tags: Set<String>,
    val status: TaskStatus
) : Describable, Estimable {

    abstract val kind: TaskKind

    abstract fun withStatus(newStatus: TaskStatus): Task

    override fun describe(): String = "${kind.label} · ${priority.label} · ${status.label()}"

    fun isDone(): Boolean = status is TaskStatus.Done

    fun score(): Int = if (isDone()) 0 else priority.weight * 10 + bonus()

    protected open fun bonus(): Int = 0
}

class Bug(
    id: Int,
    title: String,
    priority: Priority,
    tags: Set<String>,
    val severity: Int,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, priority, tags, status) {
    override val kind = TaskKind.BUG
    override fun estimateHours(): Double = severity * 1.5
    override fun bonus(): Int = severity * 3
    override fun withStatus(newStatus: TaskStatus): Task = Bug(id, title, priority, tags, severity, newStatus)
}

class Feature(
    id: Int,
    title: String,
    priority: Priority,
    tags: Set<String>,
    val storyPoints: Int,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, priority, tags, status) {
    override val kind = TaskKind.FEATURE
    override fun estimateHours(): Double = storyPoints * 4.0
    override fun bonus(): Int = storyPoints
    override fun withStatus(newStatus: TaskStatus): Task = Feature(id, title, priority, tags, storyPoints, newStatus)
}

class Chore(
    id: Int,
    title: String,
    priority: Priority,
    tags: Set<String>,
    status: TaskStatus = TaskStatus.Todo
) : Task(id, title, priority, tags, status) {
    override val kind = TaskKind.CHORE
    override fun estimateHours(): Double = 1.0
    override fun withStatus(newStatus: TaskStatus): Task = Chore(id, title, priority, tags, newStatus)
}

data class ProjectReport(
    val total: Int,
    val done: Int,
    val openHours: Double,
    val byPriority: Map<Priority, Int>
) {
    val completionPercent: Int
        get() = if (total == 0) 0 else done * 100 / total
}
