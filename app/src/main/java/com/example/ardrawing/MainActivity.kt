package com.example.ardrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.ardrawing.navigation.NavGraph
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
                NavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}