package com.example.ardrawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ardrawing.data.AuthManager
import com.example.ardrawing.data.LocalAuthManager
import com.example.ardrawing.data.repository.TemplateRepository
import com.example.ardrawing.navigation.NavGraph
import com.example.ardrawing.navigation.Screen
import com.example.ardrawing.ui.components.ARFloatingBottomBar
import com.example.ardrawing.ui.theme.ARDrawingTheme
import com.example.ardrawing.utils.ARCorePreferences
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.animation.AnimatedVisibility

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AuthManager
        val authManager = AuthManager(this)
        
        // Check ARCore compatibility on app startup (only once)
        checkARCoreCompatibility()
        
        // Sign in anonymously on app start
        lifecycleScope.launch {
            val result = authManager.signInAnonymously()
            result.onSuccess { userId ->
                Log.d(TAG, "Anonymous sign-in successful: $userId")
            }.onFailure { error ->
                Log.e(TAG, "Anonymous sign-in failed: ${error.message}")
            }
        }
        
        // Set static navigation bar color (black) - never changes
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK
        // Fix for black glitch during navigation transitions
        window.setBackgroundDrawableResource(R.color.main_bg)
        
        enableEdgeToEdge()
        setContent {
            ARDrawingTheme {
                // Provide AuthManager via CompositionLocal
                CompositionLocalProvider(LocalAuthManager provides authManager) {
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
                    AnimatedVisibility(
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
    
    /**
     * Check ARCore compatibility once at app startup and save to preferences
     * This prevents checking on every launch and improves performance
     */
    private fun checkARCoreCompatibility() {
        // Only check if we haven't checked before
        // Note: If cached value is incorrect, user can clear app data to force re-check
        if (ARCorePreferences.hasCheckedARCore(this)) {
            val cachedStatus = ARCorePreferences.isARCoreSupported(this) ?: false
            Log.d(TAG, "ARCore compatibility already checked (cached): $cachedStatus")
            Log.d(TAG, "To force re-check, clear app data or uninstall/reinstall app")
            return
        }
        
        try {
            // Check ARCore availability
            val availability = ArCoreApk.getInstance().checkAvailability(this)
            Log.d(TAG, "ARCore availability check result: $availability")
            
            // STRICT CHECK: Only mark as supported if ARCore is explicitly INSTALLED or needs update
            // SUPPORTED_NOT_INSTALLED is not reliable - it might mean device is checking compatibility
            val isSupported = when (availability) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                    // ARCore is installed and ready - device is definitely compatible
                    Log.d(TAG, "ARCore is INSTALLED - device is compatible")
                    true
                }
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
                    // ARCore is installed but needs update - device is compatible
                    Log.d(TAG, "ARCore is installed but needs update - device is compatible")
                    true
                }
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    // ARCore not installed - can't determine compatibility reliably
                    // Try to actually verify by attempting to create a session
                    Log.d(TAG, "ARCore not installed - attempting to verify compatibility by checking device capabilities")
                    verifyCompatibilityByDeviceCheck()
                }
                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    // Device is explicitly not compatible
                    Log.d(TAG, "ARCore is NOT supported - device not capable")
                    false
                }
                else -> {
                    // Unknown or unsupported status (UNKNOWN_CHECKING, UNKNOWN_ERROR, UNKNOWN_TIMED_OUT, etc.)
                    // Assume not supported to be safe - this includes any other UNSUPPORTED states
                    Log.w(TAG, "ARCore availability unknown/unsupported: $availability - assuming NOT supported")
                    false
                }
            }
            
            // Save the result to preferences
            ARCorePreferences.setARCoreSupported(this, isSupported)
            Log.d(TAG, "ARCore compatibility saved: $isSupported (availability was: $availability)")
            
        } catch (e: UnavailableDeviceNotCompatibleException) {
            // Device not compatible
            Log.d(TAG, "Device not compatible with ARCore (exception)")
            ARCorePreferences.setARCoreSupported(this, false)
        } catch (e: Exception) {
            // Error checking, assume not supported to be safe
            Log.e(TAG, "Error checking ARCore compatibility: ${e.message}", e)
            ARCorePreferences.setARCoreSupported(this, false)
        }
    }
    
    /**
     * Additional verification for devices where ARCore is not installed
     * Checks device hardware capabilities directly
     */
    private fun verifyCompatibilityByDeviceCheck(): Boolean {
        return try {
            // Try to create a minimal AR session to verify compatibility
            // This will throw an exception if device is truly incompatible
            val session = com.google.ar.core.Session(this)
            session.close()
            Log.d(TAG, "Device capability check: Session created successfully - device is compatible")
            true
        } catch (e: com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException) {
            Log.d(TAG, "Device capability check: Device is NOT compatible")
            false
        } catch (e: com.google.ar.core.exceptions.UnavailableException) {
            // ARCore not installed or other issue - can't verify, assume not supported
            Log.d(TAG, "Device capability check: ARCore unavailable - assuming NOT supported")
            false
        } catch (e: Exception) {
            // Other errors - assume not supported to be safe
            Log.w(TAG, "Device capability check: Error - ${e.message}, assuming NOT supported")
            false
        }
    }
}