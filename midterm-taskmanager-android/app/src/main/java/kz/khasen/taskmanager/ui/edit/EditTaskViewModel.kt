package kz.khasen.taskmanager.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.khasen.taskmanager.data.TaskRepository
import kz.khasen.taskmanager.model.Priority
import kz.khasen.taskmanager.model.TaskDraft
import kz.khasen.taskmanager.model.TaskKind
import kz.khasen.taskmanager.ui.navigation.Routes

data class EditTaskUiState(
    val isNew: Boolean = true,
    val title: String = "",
    val description: String = "",
    val tagsInput: String = "",
    val kind: TaskKind = TaskKind.FEATURE,
    val priority: Priority = Priority.MEDIUM,
    val suggestedTags: List<String> = emptyList(),
    val saved: Boolean = false
) {
    val titleError: String? get() = if (title.isNotEmpty() && title.isBlank()) "Title cannot be blank" else null
    val canSave: Boolean get() = title.isNotBlank()
    val estimatePreview: Double
        get() = when (kind) {
            TaskKind.BUG -> priority.weight * 1.5
            TaskKind.FEATURE -> priority.weight * 2 * 4.0
            TaskKind.CHORE -> 1.0
        }
}

class EditTaskViewModel(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Int = savedStateHandle[Routes.ARG_TASK_ID] ?: Routes.NEW_TASK_ID
    private val isNew = taskId == Routes.NEW_TASK_ID

    private val title = savedStateHandle.getStateFlow(KEY_TITLE, "")
    private val description = savedStateHandle.getStateFlow(KEY_DESCRIPTION, "")
    private val tagsInput = savedStateHandle.getStateFlow(KEY_TAGS, "")
    private val kind = savedStateHandle.getStateFlow(KEY_KIND, TaskKind.FEATURE.name)
    private val priority = savedStateHandle.getStateFlow(KEY_PRIORITY, Priority.MEDIUM.name)
    private val saved = savedStateHandle.getStateFlow(KEY_SAVED, false)

    init {
        if (!isNew && !savedStateHandle.contains(KEY_LOADED)) {
            repository.tasks.value.firstOrNull { it.id == taskId }?.let { task ->
                savedStateHandle[KEY_TITLE] = task.title
                savedStateHandle[KEY_DESCRIPTION] = task.description
                savedStateHandle[KEY_TAGS] = task.tags.joinToString(", ")
                savedStateHandle[KEY_KIND] = task.kind.name
                savedStateHandle[KEY_PRIORITY] = task.priority.name
            }
            savedStateHandle[KEY_LOADED] = true
        }
    }

    val uiState: StateFlow<EditTaskUiState> = combine(
        title, description, tagsInput, kind, priority, saved, repository.tasks
    ) { values ->
        val currentTags = parseTags(values[2] as String)
        val allTags = (values[6] as List<*>).flatMap { (it as kz.khasen.taskmanager.model.Task).tags }
            .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }
        EditTaskUiState(
            isNew = isNew,
            title = values[0] as String,
            description = values[1] as String,
            tagsInput = values[2] as String,
            kind = TaskKind.valueOf(values[3] as String),
            priority = Priority.valueOf(values[4] as String),
            suggestedTags = allTags.filter { it !in currentTags }.take(6),
            saved = values[5] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditTaskUiState(isNew = isNew))

    fun onTitleChange(value: String) { savedStateHandle[KEY_TITLE] = value }
    fun onDescriptionChange(value: String) { savedStateHandle[KEY_DESCRIPTION] = value }
    fun onTagsChange(value: String) { savedStateHandle[KEY_TAGS] = value }
    fun onKindChange(value: TaskKind) { savedStateHandle[KEY_KIND] = value.name }
    fun onPriorityChange(value: Priority) { savedStateHandle[KEY_PRIORITY] = value.name }

    fun addSuggestedTag(tag: String) {
        val current = tagsInput.value.trim().trimEnd(',')
        savedStateHandle[KEY_TAGS] = if (current.isEmpty()) tag else "$current, $tag"
    }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        val draft = TaskDraft(
            title = state.title.trim(),
            description = state.description.trim(),
            kind = state.kind,
            priority = state.priority,
            tags = parseTags(state.tagsInput)
        )
        if (isNew) repository.add(draft) else repository.update(taskId, draft)
        savedStateHandle[KEY_SAVED] = true
    }

    private fun parseTags(input: String): Set<String> =
        input.split(",").map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotEmpty() }.toSet()

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_DESCRIPTION = "description"
        const val KEY_TAGS = "tags"
        const val KEY_KIND = "kind"
        const val KEY_PRIORITY = "priority"
        const val KEY_SAVED = "saved"
        const val KEY_LOADED = "loaded"
    }
}
