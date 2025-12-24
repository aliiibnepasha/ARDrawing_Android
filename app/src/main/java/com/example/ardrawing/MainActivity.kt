package com.example.ardrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ardrawing.navigation.NavGraph
import com.example.ardrawing.navigation.Screen
import com.example.ardrawing.ui.components.BottomNavigationBar
import com.example.ardrawing.ui.theme.ARDrawingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set static navigation bar color (black) - never changes
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK
        
        enableEdgeToEdge()
        setContent {
            ARDrawingTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Determine which bottom nav item should be selected based on current route
                val selectedRoute = when {
                    currentRoute == Screen.Home.route || currentRoute == null -> "home"
                    currentRoute == Screen.LessonList.route -> "lesson_list"
                    currentRoute == Screen.MyCreative.route -> "my_creative"
                    currentRoute == "ar_text" -> "ar_text"
                    else -> null // Don't highlight any tab for other routes
                }
                
                // Show bottom nav ONLY on: Home, Lesson, AR Text, My
                // Hide on all other screens including TemplateDetail, CategoryDetail, Settings, etc.
                val shouldShowBottomNav = currentRoute != null && (
                    currentRoute == Screen.Home.route ||
                    currentRoute == Screen.LessonList.route ||
                    currentRoute == Screen.MyCreative.route ||
                    currentRoute == "ar_text"
                )
                
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Bottom navigation bar overlay - only show on main screens
                    if (shouldShowBottomNav) {
                        BottomNavigationBar(
                            currentRoute = selectedRoute,
                            onItemClick = { route ->
                                when (route) {
                                    "home" -> {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    }
                                    "lesson_list" -> {
                                        navController.navigate(Screen.LessonList.route) {
                                            popUpTo(Screen.Home.route) { inclusive = false }
                                        }
                                    }
                                    "ar_text" -> {
                                        // TODO: Navigate to AR Text screen when implemented
                                    }
                                    "my_creative" -> {
                                        navController.navigate(Screen.MyCreative.route) {
                                            popUpTo(Screen.Home.route) { inclusive = false }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}