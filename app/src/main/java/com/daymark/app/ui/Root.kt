package com.daymark.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Filled.Home),
    Timeline("timeline", "Timeline", Icons.AutoMirrored.Filled.List),
    ScreenTime("screentime", "Screen Time", Icons.Filled.Phone),
    Goals("goals", "Goals", Icons.Filled.Star),
    Insights("insights", "Insights", Icons.Filled.Info),
}
private const val SETTINGS = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaymarkRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val title = Tab.entries.firstOrNull { it.route == route }?.label ?: if (route == SETTINGS) "Settings" else "Daymark"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (route != SETTINGS) {
                        IconButton(onClick = { nav.navigate(SETTINGS) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Tab.Today.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Today.route) { TodayScreen() }
            composable(Tab.Timeline.route) { TimelineScreen() }
            composable(Tab.ScreenTime.route) {
                NotBuiltYet("Screen Time", "Android usage access, app durations and reconciliation arrive in Milestone 4. No usage data is read or shown yet.")
            }
            composable(Tab.Goals.route) {
                NotBuiltYet("Goals", "Goals, milestones, habits and recurring tasks arrive in Milestone 6.")
            }
            composable(Tab.Insights.route) {
                NotBuiltYet("Insights", "Reports are built from stored records in Milestone 6. Nothing is shown here until real data backs it.")
            }
            composable(SETTINGS) { SettingsScreen() }
        }
    }
}
