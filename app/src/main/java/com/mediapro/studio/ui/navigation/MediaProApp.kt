package com.mediapro.studio.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mediapro.studio.domain.model.MediaKind
import com.mediapro.studio.ui.screens.HomeScreen
import com.mediapro.studio.ui.screens.ImageEditorScreen
import com.mediapro.studio.ui.screens.ProjectsScreen
import com.mediapro.studio.ui.screens.SettingsScreen
import com.mediapro.studio.ui.screens.VideoEditorScreen
import com.mediapro.studio.ui.viewmodel.HomeViewModel

private data class Destination(val route: String, val label: String)

@Composable
fun MediaProApp(viewModel: HomeViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val destinations = listOf(
        Destination("home", "الرئيسية"),
        Destination("projects", "المشاريع"),
        Destination("settings", "الإعدادات")
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute != "image-editor/{uri}" && currentRoute != "video-editor/{uri}"

    Scaffold(bottomBar = {
        if (showBottomBar) NavigationBar {
            destinations.forEach { destination ->
                val icon = when (destination.route) {
                    "projects" -> Icons.Filled.VideoLibrary
                    "settings" -> Icons.Filled.Settings
                    else -> Icons.Filled.Home
                }
                NavigationBarItem(
                    selected = currentRoute == destination.route,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(icon, contentDescription = destination.label) },
                    label = { Text(destination.label) }
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "home", modifier = androidx.compose.ui.Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(projects) { uri, mime, name ->
                    viewModel.addMedia(uri, mime, name)
                    val route = if (mime?.startsWith("video/") == true) "video-editor" else "image-editor"
                    navController.navigate("$route/${Uri.encode(uri.toString())}")
                }
            }
            composable("projects") {
                ProjectsScreen(projects) { project ->
                    val route = if (project.kind == MediaKind.VIDEO) "video-editor" else "image-editor"
                    navController.navigate("$route/${Uri.encode(project.uri)}")
                }
            }
            composable(
                route = "image-editor/{uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { entry ->
                val uri = Uri.parse(entry.arguments?.getString("uri").orEmpty())
                ImageEditorScreen(uri) { navController.popBackStack() }
            }
            composable(
                route = "video-editor/{uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { entry ->
                val uri = Uri.parse(entry.arguments?.getString("uri").orEmpty())
                VideoEditorScreen(uri) { navController.popBackStack() }
            }
            composable("settings") { SettingsScreen() }
        }
    }
}
