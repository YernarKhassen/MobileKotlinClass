package kz.khasen.taskmanager

import android.app.Application
import kz.khasen.taskmanager.data.TaskRepository

class TaskManagerApp : Application() {
    val repository: TaskRepository by lazy { TaskRepository() }
}
