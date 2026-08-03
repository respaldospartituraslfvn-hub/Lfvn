package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.MetronomeScreen
import com.example.ui.screens.PercussionLibraryScreen
import com.example.ui.screens.RhythmDetectorScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MetroPulseTheme
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodels.MetronomeViewModel
import com.example.ui.viewmodels.PercussionLibraryViewModel
import com.example.ui.viewmodels.RhythmDetectorViewModel
import com.example.ui.viewmodels.TunerViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    object Metronome : Screen("metronome", "Metrónomo", Icons.Default.Speed, "nav_metronome")
    object Tuner : Screen("tuner", "Afinador", Icons.Default.Tune, "nav_tuner")
    object RhythmDetector : Screen("rhythm", "Detector", Icons.Default.GraphicEq, "nav_rhythm")
    object Percussion : Screen("percussion", "Sonidos", Icons.Default.LibraryMusic, "nav_percussion")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroPulseTheme {
                MetroPulseApp()
            }
        }
    }
}

@Composable
fun MetroPulseApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Metronome,
        Screen.Tuner,
        Screen.RhythmDetector,
        Screen.Percussion
    )

    val metronomeVm: MetronomeViewModel = viewModel()
    val tunerVm: TunerViewModel = viewModel()
    val rhythmVm: RhythmDetectorViewModel = viewModel()
    val percussionVm: PercussionLibraryViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = StudioCard,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = when (screen) {
                                Screen.Metronome -> CyanAccent
                                Screen.Tuner -> EmeraldGreen
                                Screen.RhythmDetector -> AmberAccent
                                Screen.Percussion -> PurpleAccent
                            },
                            selectedTextColor = when (screen) {
                                Screen.Metronome -> CyanAccent
                                Screen.Tuner -> EmeraldGreen
                                Screen.RhythmDetector -> AmberAccent
                                Screen.Percussion -> PurpleAccent
                            },
                            indicatorColor = StudioSurfaceVariant,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Metronome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Metronome.route) {
                MetronomeScreen(viewModel = metronomeVm)
            }
            composable(Screen.Tuner.route) {
                TunerScreen(viewModel = tunerVm)
            }
            composable(Screen.RhythmDetector.route) {
                RhythmDetectorScreen(viewModel = rhythmVm)
            }
            composable(Screen.Percussion.route) {
                PercussionLibraryScreen(viewModel = percussionVm)
            }
        }
    }
}
