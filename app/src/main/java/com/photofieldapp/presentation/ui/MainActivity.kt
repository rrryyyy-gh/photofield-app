package com.photofieldapp.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photofieldapp.R
import com.photofieldapp.presentation.ui.activitydetail.ActivityDetailScreen
import com.photofieldapp.presentation.ui.map.MapScreen
import com.photofieldapp.presentation.ui.pdfpreview.PdfPreviewScreen
import com.photofieldapp.presentation.ui.projectdetail.ProjectDetailScreen
import com.photofieldapp.presentation.ui.projectlist.ProjectListScreen
import com.photofieldapp.presentation.ui.settings.SettingsScreen
import com.photofieldapp.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.photofieldapp.ui.theme.Theme.PhotoFieldApp {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    NavHost(navController, startDestination = "projects") {
        composable("projects") {
            ProjectListScreen()
        }
        composable(
            route = "project/{projectId}",
            arguments = listOf(androidx.navigation.navArgument("projectId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.getLong("projectId")
            ProjectDetailScreen()
        }
        composable(
            route = "project/{projectId}/activity/{activityId}",
            arguments = listOf(
                androidx.navigation.navArgument("projectId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("activityId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val activityId = backStackEntry.getLong("activityId")
            ActivityDetailScreen()
        }
        composable("map") {
            MapScreen()
        }
        composable("pdf-preview") {
            PdfPreviewScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}

@Composable
fun BottomNavBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        BottomNavItem("projects", stringResource(R.string.nav_projects), Icons.Default.Folder),
        BottomNavItem("map", stringResource(R.string.nav_map), Icons.Default.Map),
        BottomNavItem("pdf-preview", "PDF", Icons.Default.PictureAsPdf),
        BottomNavItem("settings", stringResource(R.string.nav_settings), Icons.Default.Settings)
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        items.forEach { item ->
            val isSelected = navController.currentBackStackEntryAsState().value?.destination?.route?.startsWith(item.route) == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.material.icons.filled.Icon
)