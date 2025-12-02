package com.example.ardrawing.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.ui.screens.CameraPreviewScreen
import com.example.ardrawing.ui.screens.TemplateDetailScreen
import com.example.ardrawing.ui.screens.TemplateListScreen

sealed class Screen(val route: String) {
    object TemplateList : Screen("template_list")
    object TemplateDetail : Screen("template_detail/{templateId}") {
        fun createRoute(templateId: String) = "template_detail/$templateId"
    }
    object CameraPreview : Screen("camera_preview/{templateId}") {
        fun createRoute(templateId: String) = "camera_preview/$templateId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.TemplateList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.TemplateList.route) {
            TemplateListScreen(
                onTemplateSelected = { template ->
                    navController.navigate(Screen.TemplateDetail.createRoute(template.id))
                }
            )
        }
        
        composable(Screen.TemplateDetail.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)
            
            template?.let {
                TemplateDetailScreen(
                    template = it,
                    onBackClick = { navController.popBackStack() },
                    onCameraSketchClick = {
                        navController.navigate(Screen.CameraPreview.createRoute(templateId))
                    },
                    onPaperTraceClick = {
                        // TODO: Implement Paper Trace feature later
                    }
                )
            }
        }
        
        composable(Screen.CameraPreview.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)
            
            template?.let {
                CameraPreviewScreen(
                    template = it,
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = {
                        navController.popBackStack(
                            route = Screen.TemplateList.route,
                            inclusive = false
                        )
                    }
                )
            }
        }
    }
}

