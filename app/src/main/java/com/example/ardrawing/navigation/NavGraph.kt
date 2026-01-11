package com.example.ardrawing.navigation

import android.content.Intent
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
import com.example.ardrawing.ui.viewmodel.ARViewModel
import com.example.ardrawing.ui.screens.CameraPreviewScreen
import com.example.ardrawing.ui.screens.CaptureResultScreen
import com.example.ardrawing.ui.screens.CategoryDetailScreen
import com.example.ardrawing.ui.screens.ColoringImageSelectionScreen
import com.example.ardrawing.ui.screens.ColoringScreen
import com.example.ardrawing.ui.screens.CreateLessonFromImageScreen
import com.example.ardrawing.ui.screens.DrawingModeSelectionScreen
import com.example.ardrawing.ui.screens.LessonDrawingScreen
import com.example.ardrawing.ui.screens.LessonPreviewScreen
import com.example.ardrawing.ui.screens.LessonScreen
import com.example.ardrawing.ui.screens.MyCreativeScreen
import com.example.ardrawing.ui.screens.PaperTraceScreen
import com.example.ardrawing.ui.screens.SettingsScreen
import com.example.ardrawing.ui.screens.TemplateListScreen
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel
import com.example.ardrawing.LaunchActivity
import com.example.ardrawing.data.repository.TemplateRepository
import com.example.ardrawing.ui.screens.HomeScreenNew

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TemplateList : Screen("template_list")
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
    object LessonList : Screen("lesson_list")
    object LessonPreview : Screen("lesson_preview/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_preview/$lessonId"
    }
    object LessonDrawing : Screen("lesson_drawing/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_drawing/$lessonId"
    }
    object CreateLessonFromImage : Screen("create_lesson_from_image")
    object ColoringImageSelection : Screen("coloring_image_selection")
    object Coloring : Screen("coloring/{templateId}") {
        fun createRoute(templateId: String) = "coloring/$templateId"
    }
    object Settings : Screen("settings")
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: String) = "category_detail/$categoryId"
    }
    object DrawingModeSelection : Screen("drawing_mode_selection/{templateId}") {
        fun createRoute(templateId: String) = "drawing_mode_selection/$templateId"
    }
    object PhotoToSketch : Screen("photo_to_sketch")
    object CreateWithAI : Screen("create_with_ai")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
    currentTabRoute: String? = null,
    arViewModel: ARViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val context = LocalContext.current

            HomeScreenNew(
                currentRoute = currentTabRoute,
                onTemplateSelected = { template ->
                    navController.navigate(
                        Screen.DrawingModeSelection.createRoute(template.id)
                    )
                },

                onSeeAll = { category ->
                    navController.navigate(
                        Screen.CategoryDetail.createRoute(category.id)
                    )
                },
                onCategoryClick = { categoryId ->
                    navController.navigate(
                        Screen.CategoryDetail.createRoute(categoryId)
                    )
                },
                onStartAR = {
                    // Start AR tracing flow directly: Camera -> Crop -> AR
                    val intent = Intent(context, LaunchActivity::class.java)
                    context.startActivity(intent)
                },
                onPhotoToSketch = {
                    navController.navigate(Screen.PhotoToSketch.route)
                },

                onAICreate = {
                    navController.navigate(Screen.CreateWithAI.route)
                },

                onProClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.PhotoToSketch.route) {
            com.example.ardrawing.ui.screens.PhotoToSketchScreen(
                onBackClick = { navController.popBackStack() },
                onPhotoSelected = { uri ->
                   // TODO: Handle photo selection (navigate to next step)
                }
            )
        }
        
        composable(Screen.CreateWithAI.route) {
            com.example.ardrawing.ui.screens.CreateWithAIScreen(
                 onBackClick = { navController.popBackStack() },
                 onUseToDraw = {
                     // TODO: Navigate to drawing canvas with generated image
                 }
            )
        }

        composable(Screen.CategoryDetail.route) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryDetailScreen(
                categoryId = categoryId,
                onTemplateSelected = { template ->
                    navController.navigate(Screen.DrawingModeSelection.createRoute(template.id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TemplateList.route) {
            val context = LocalContext.current
            TemplateListScreen(
                onTemplateSelected = { template ->
                    navController.navigate(Screen.DrawingModeSelection.createRoute(template.id))
                },
                onStartLessonClick = {
                    navController.navigate(Screen.LessonList.route)
                },
                onColoringClick = {
                    navController.navigate(Screen.ColoringImageSelection.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.DrawingModeSelection.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)

            template?.let {
                DrawingModeSelectionScreen(
                    template = it,
                    onBackClick = { navController.popBackStack() },
                    onDrawSketchClick = {
                        navController.navigate(Screen.CameraPreview.createRoute(templateId))
                    },
                    onTraceImageClick = {
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
                    onBackClick = { navController.popBackStack() }
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
                            route = Screen.Home.route,
                            inclusive = false
                        )
                    },
                    onSaveClick = {
                        navController.navigate(Screen.MyCreative.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
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
        
        composable(Screen.LessonList.route) {
            LessonScreen()
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
        
        composable(Screen.ColoringImageSelection.route) {
            ColoringImageSelectionScreen(
                onImageSelected = { template ->
                    navController.navigate(Screen.Coloring.createRoute(template.id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Coloring.route) { backStackEntry ->
            val context = LocalContext.current
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            val template = com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, templateId)
            
            template?.let {
                ColoringScreen(
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
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

