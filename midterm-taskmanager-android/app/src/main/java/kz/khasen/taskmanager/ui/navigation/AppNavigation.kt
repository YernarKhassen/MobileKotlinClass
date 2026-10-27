package kz.khasen.taskmanager.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kz.khasen.taskmanager.ui.detail.TaskDetailScreen
import kz.khasen.taskmanager.ui.edit.EditTaskScreen
import kz.khasen.taskmanager.ui.stats.StatsScreen
import kz.khasen.taskmanager.ui.tasks.TaskListScreen

object Routes {
    const val TASKS = "tasks"
    const val STATS = "stats"
    const val DETAIL = "task/{taskId}"
    const val EDIT = "edit?taskId={taskId}"

    const val ARG_TASK_ID = "taskId"
    const val NEW_TASK_ID = -1

    fun detail(id: Int) = "task/$id"
    fun edit(id: Int? = null) = "edit?taskId=${id ?: NEW_TASK_ID}"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val topLevel = listOf(
    TopLevelDestination(Routes.TASKS, "Tasks", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(Routes.STATS, "Stats", Icons.Default.Info)
)

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevel.any { dest -> currentDestination?.hierarchy?.any { it.route == dest.route } == true }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevel.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TASKS) {
                TaskListScreen(
                    onTaskClick = { id -> navController.navigate(Routes.detail(id)) },
                    onAddClick = { navController.navigate(Routes.edit()) }
                )
            }
            composable(Routes.STATS) {
                StatsScreen(onTagClick = { navController.navigate(Routes.TASKS) })
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument(Routes.ARG_TASK_ID) { type = NavType.IntType })
            ) {
                TaskDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.edit(id)) }
                )
            }
            composable(
                route = Routes.EDIT,
                arguments = listOf(navArgument(Routes.ARG_TASK_ID) {
                    type = NavType.IntType
                    defaultValue = Routes.NEW_TASK_ID
                })
            ) {
                EditTaskScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
