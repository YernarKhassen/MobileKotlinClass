package kz.khasen.taskmanager.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kz.khasen.taskmanager.TaskManagerApp
import kz.khasen.taskmanager.ui.detail.TaskDetailViewModel
import kz.khasen.taskmanager.ui.edit.EditTaskViewModel
import kz.khasen.taskmanager.ui.stats.StatsViewModel
import kz.khasen.taskmanager.ui.tasks.TaskListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { TaskListViewModel(app().repository) }
        initializer { TaskDetailViewModel(app().repository, createSavedStateHandle()) }
        initializer { EditTaskViewModel(app().repository, createSavedStateHandle()) }
        initializer { StatsViewModel(app().repository) }
    }

    private fun CreationExtras.app(): TaskManagerApp =
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TaskManagerApp
}
