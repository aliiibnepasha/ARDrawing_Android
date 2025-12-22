package com.example.ardrawing.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ardrawing.data.local.database.AppDatabase
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.repository.SavedDrawingRepository
import com.example.ardrawing.data.repository.LessonRepository
import com.example.ardrawing.ui.screens.CameraPreviewScreen
import com.example.ardrawing.ui.screens.CaptureResultScreen
import com.example.ardrawing.ui.screens.CreateLessonFromImageScreen
import com.example.ardrawing.ui.screens.LessonDrawingScreen
import com.example.ardrawing.ui.screens.LessonPreviewScreen
import com.example.ardrawing.ui.screens.MyCreativeScreen
import com.example.ardrawing.ui.screens.PaperTraceScreen
import com.example.ardrawing.ui.screens.TemplateDetailScreen
import com.example.ardrawing.ui.screens.TemplateListScreen
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel

sealed class Screen(val route: String) {
    object TemplateList : Screen("template_list")
    object TemplateDetail : Screen("template_detail/{templateId}") {
        fun createRoute(templateId: String) = "template_detail/$templateId"
    }
    object CameraPreview : Screen("camera_preview/{templateId}") {
        fun createRoute(templateId: String) = "camera_preview/$templateId"
    }
    object PaperTrace : Screen("paper_trace/{templateId}") {
        fun createRoute(templateId: String) = "paper_trace/$templateId"
    }
    object CaptureResult : Screen("capture_result/{templateId}/{sourceType}") {
        fun createRoute(templateId: String, sourceType: String) = "capture_result/$templateId/$sourceType"
    }
    object MyCreative : Screen("my_creative")
    object LessonPreview : Screen("lesson_preview/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_preview/$lessonId"
    }
    object LessonDrawing : Screen("lesson_drawing/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_drawing/$lessonId"
    }
    object CreateLessonFromImage : Screen("create_lesson_from_image")
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
            val context = LocalContext.current
            TemplateListScreen(
                onTemplateSelected = { template ->
                    navController.navigate(Screen.TemplateDetail.createRoute(template.id))
                },
                onStartLessonClick = {
                    navController.navigate(Screen.CreateLessonFromImage.route)
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
                        navController.navigate(Screen.PaperTrace.createRoute(templateId))
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
        
        composable(Screen.PaperTrace.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)
            
            template?.let {
                PaperTraceScreen(
                    template = it,
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = {
                        navController.popBackStack(
                            route = Screen.TemplateList.route,
                            inclusive = false
                        )
                    },
                    onCaptureClick = {
                        navController.navigate(Screen.CaptureResult.createRoute(templateId, "Paper Trace"))
                    }
                )
            }
        }
        
        composable(Screen.CaptureResult.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val sourceType = backStackEntry.arguments?.getString("sourceType") ?: "Camera"
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)
            
            template?.let {
                CaptureResultScreen(
                    template = it,
                    sourceType = sourceType,
                    onCloseClick = { navController.popBackStack() },
                    onHomeClick = {
                        navController.popBackStack(
                            route = Screen.TemplateList.route,
                            inclusive = false
                        )
                    },
                    onSaveClick = {
                        navController.navigate(Screen.MyCreative.route) {
                            popUpTo(Screen.TemplateList.route) { inclusive = false }
                        }
                    }
                )
            }
        }
        
        composable(Screen.MyCreative.route) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = SavedDrawingRepository(database.savedDrawingDao())
            val viewModel: MyCreativeViewModel = viewModel(
                factory = MyCreativeViewModel.provideFactory(repository)
            )
            
            MyCreativeScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onDrawingClick = { drawing ->
                    // TODO: Navigate to drawing detail if needed
                }
            )
        }
        
        composable(Screen.LessonPreview.route) { backStackEntry ->
            val context = LocalContext.current
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val lesson = LessonRepository.getLessonById(context, lessonId)
            
            lesson?.let {
                LessonPreviewScreen(
                    lesson = it,
                    onBackClick = { navController.popBackStack() },
                    onDrawWithCameraClick = {
                        navController.navigate(Screen.LessonDrawing.createRoute(lessonId))
                    },
                    onTraceImageClick = {
                        // Navigate to PaperTrace with lesson step
                        val templates = com.example.ardrawing.data.repository.TemplateRepository.getTemplates(context)
                        if (templates.isNotEmpty()) {
                            navController.navigate(Screen.PaperTrace.createRoute(templates.first().id))
                        }
                    }
                )
            }
        }
        
        composable(Screen.LessonDrawing.route) { backStackEntry ->
            val context = LocalContext.current
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val lesson = LessonRepository.getLessonById(context, lessonId)
            
            lesson?.let {
                LessonDrawingScreen(
                    lesson = it,
                    onBackClick = { navController.popBackStack() },
                    onCaptureClick = {
                        // Navigate to capture result
                        val templates = com.example.ardrawing.data.repository.TemplateRepository.getTemplates(context)
                        if (templates.isNotEmpty()) {
                            navController.navigate(Screen.CaptureResult.createRoute(templates.first().id, "Camera"))
                        }
                    },
                    onHomeClick = {
                        navController.popBackStack(
                            route = Screen.TemplateList.route,
                            inclusive = false
                        )
                    }
                )
            }
        }
        
        composable(Screen.CreateLessonFromImage.route) {
            CreateLessonFromImageScreen(
                onBackClick = { navController.popBackStack() },
                onLessonCreated = { lesson ->
                    // Save lesson and navigate to preview
                    navController.navigate(Screen.LessonPreview.createRoute(lesson.id)) {
                        popUpTo(Screen.TemplateList.route) { inclusive = false }
                    }
                }
            )
        }
    }
}

