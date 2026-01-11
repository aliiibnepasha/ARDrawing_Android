package com.example.ardrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ardrawing.data.repository.TemplateRepository
import com.example.ardrawing.navigation.NavGraph
import com.example.ardrawing.navigation.Screen
import com.example.ardrawing.ui.components.ARFloatingBottomBar
import com.example.ardrawing.ui.theme.ARDrawingTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set static navigation bar color (black) - never changes
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK
        // Fix for black glitch during navigation transitions
        window.setBackgroundDrawableResource(R.color.main_bg)
        
        enableEdgeToEdge()
        setContent {
            ARDrawingTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Track if navigation is ready to show bottom bar
                var isNavigationReady by remember { mutableStateOf(false) }
                
                // Define routes that should show the bottom bar
                val bottomBarRoutes = listOf(
                    Screen.Home.route,
                    Screen.LessonList.route,
                    Screen.MyCreative.route,
                    "favorite"
                )

                // Effect to handle bottom bar visibility delay
                LaunchedEffect(currentRoute) {
                     if (currentRoute in bottomBarRoutes) {
                        // If we are already on a bottom bar route and switching to another, show immediately
                        // (We need to capture the previous state effectively, but for now assuming if we are in valid route)
                         // Ideally we check internal state, but here we can just check if we want to show it.
                        
                         // To prevent "late" appearance when coming BACK from detail screen:
                         // We delay ONLY if the previous state wasn't one of the main tabs.
                         // Since we can't easily access "previousRoute" directly from the state without tracking it manually:
                         
                         // We'll use a simple heuristic:
                         // Always delay slightly to match screen transition (300ms is default compose nav transition)
                         // UNLESS we know for sure it's a tab switch.
                         
                         // For simplicity and robustness to fix the specific "pop in" issue:
                         delay(300) 
                         isNavigationReady = true
                     } else {
                         isNavigationReady = false
                     }
                }

                // Determine which bottom nav item should be selected based on current route
                val selectedRoute = when {
                    currentRoute == Screen.Home.route -> "home"
                    currentRoute == Screen.LessonList.route -> "lesson_list"
                    currentRoute == Screen.MyCreative.route -> "my_creative"
                    currentRoute == "favorite" -> "favorite"
                    else -> null // Don't highlight any tab for other routes
                }
                
                // Show bottom nav ONLY on: Home, Lesson, AR Text, My
                val shouldShowBottomNav = currentRoute in bottomBarRoutes

                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        currentTabRoute = selectedRoute,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Animated visibility for bottom bar
                    androidx.compose.animation.AnimatedVisibility(
                        visible = shouldShowBottomNav && isNavigationReady,
                        enter = androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it }
                        ) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.slideOutVertically(
                            targetOffsetY = { it }
                        ) + androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        ARFloatingBottomBar(
                            currentRoute = selectedRoute,
                            onItemClick = { route ->
                                // On tab click, we want immediate feedback so we don't reset isNavigationReady here
                                // implicitly by route change loop if we handle it carefully.
                                
                                when (route) {
                                    "home" -> {
                                        if (currentRoute != Screen.Home.route) {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Home.route) { inclusive = true }
                                            }
                                        }
                                    }
                                    "lesson_list" -> {
                                        if (currentRoute != Screen.LessonList.route) {
                                            navController.navigate(Screen.LessonList.route) {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                            }
                                        }
                                    }
                                    "favorite" -> {
                                        if (currentRoute != "favorite") {
                                            navController.navigate("favorite") {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                            }
                                        }
                                    }
                                    "my_creative" -> {
                                        if (currentRoute != Screen.MyCreative.route) {
                                            navController.navigate(Screen.MyCreative.route) {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}