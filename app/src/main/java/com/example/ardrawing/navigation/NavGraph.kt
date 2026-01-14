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
import com.example.ardrawing.ui.screens.MyAlbumScreen
import com.example.ardrawing.ui.screens.PaperTraceScreen
import com.example.ardrawing.ui.screens.SettingsScreen
import com.example.ardrawing.ui.screens.TemplateListScreen
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel
import com.example.ardrawing.LaunchActivity
import com.example.ardrawing.data.repository.TemplateRepository
import com.example.ardrawing.data.utils.AssetUtils
import com.example.ardrawing.ui.screens.HomeScreenNew

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TemplateList : Screen("template_list")
    object DrawingModeSelection : Screen("drawing_mode_selection/{id}/{type}") {
        fun createRoute(id: String, type: String) = "drawing_mode_selection/$id/$type"
    }
    // Updated route to include type
    object CameraPreview : Screen("camera_preview/{id}/{type}") {
        fun createRoute(id: String, type: String) = "camera_preview/$id/$type"
    }
    // Updated route to include type
    object PaperTrace : Screen("paper_trace/{id}/{type}") {
        fun createRoute(id: String, type: String) = "paper_trace/$id/$type"
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
    object PhotoToSketch : Screen("photo_to_sketch")
    object CreateWithAI : Screen("create_with_ai")
    object MyAlbum : Screen("my_album")
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
                        Screen.DrawingModeSelection.createRoute(template.id, "template")
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
                
                onTextToImage = {
                     navController.navigate("text_to_image")
                },
                
                onCustomText = {
                    navController.navigate("ar_text")
                },

                onProClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onExplore = {
                    // Open Google in browser
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))
                    context.startActivity(intent)
                },
                onAddIllustration = { _ ->
                    // Navigate to DrawingModeSelectionScreen with gallery image
                    // URI is already stored in LaunchActivity.galleryImageUri
                    navController.navigate(Screen.DrawingModeSelection.createRoute("gallery", "gallery"))
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
        
        composable("text_to_image") { // New Route
             com.example.ardrawing.ui.screens.TextToImageScreen(
                 onBackClick = { navController.popBackStack() },
                 onUseToDraw = {
                     // TODO: Navigate to drawing canvas
                 }
             )
        }
        
        // This is the "Text" tab route (Now Favorites)
        composable("favorite") {
             com.example.ardrawing.ui.screens.FavoriteScreen()
        }
        
        // Kept for direct access from Home
        composable("ar_text") {
            com.example.ardrawing.ui.screens.CustomTextScreen(
                onBackClick = { navController.navigate(Screen.Home.route) }, 
                onDrawClick = {
                    // TODO: Navigate to drawing with text
                }
            )
        }

        composable(Screen.CategoryDetail.route) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryDetailScreen(
                categoryId = categoryId,
                onTemplateSelected = { template ->
                    navController.navigate(Screen.DrawingModeSelection.createRoute(template.id, "template"))
                },
                onBackClick = { navController.popBackStack() },
                onAddIllustration = { _ ->
                    // Navigate to DrawingModeSelectionScreen with gallery image
                    // URI is already stored in LaunchActivity.galleryImageUri
                    navController.navigate(Screen.DrawingModeSelection.createRoute("gallery", "gallery"))
                }
            )
        }
        
        composable(Screen.TemplateList.route) {
            val context = LocalContext.current
            TemplateListScreen(
                onTemplateSelected = { template ->
                    navController.navigate(Screen.DrawingModeSelection.createRoute(template.id, "template"))
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
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "template"
            
            val template = if (type == "template") {
                com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, id)
            } else null
            
            val lesson = if (type == "lesson") {
                LessonRepository.getLessonById(context, id)
            } else null
            
            // Handle gallery image
            if (type == "gallery") {
                // Get URI from LaunchActivity (stored when gallery was opened)
                val imageUriString = LaunchActivity.galleryImageUri
                if (imageUriString == null) {
                    android.util.Log.e("NavGraph", "Gallery image URI not found")
                    android.widget.Toast.makeText(context, "Error loading image", android.widget.Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    return@composable
                }
                
                val imageUri = android.net.Uri.parse(imageUriString)
                
                // Load bitmap from gallery URI
                val bitmap = try {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bmp
                } catch (e: Exception) {
                    android.util.Log.e("NavGraph", "Error loading gallery image: ${e.message}", e)
                    android.widget.Toast.makeText(context, "Error loading image: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    return@composable
                }
                
                if (bitmap != null) {
                    // Set as selected overlay bitmap for AR
                    LaunchActivity.selectedOverlayBitmap = bitmap
                    android.util.Log.d("NavGraph", "Gallery image loaded: ${bitmap.width}x${bitmap.height}")
                }
                
                DrawingModeSelectionScreen(
                    template = null,
                    lesson = null,
                    onBackClick = { navController.popBackStack() },
                    onDrawSketchClick = {
                        // Navigate to camera preview with gallery image
                        navController.navigate(Screen.CameraPreview.createRoute(id, "gallery"))
                    },
                    onTraceImageClick = {
                        // Navigate to paper trace with gallery image
                        navController.navigate(Screen.PaperTrace.createRoute(id, "gallery"))
                    },
                    onStartAR = {
                        // Image is already set in LaunchActivity.selectedOverlayBitmap
                        val intent = Intent(context, LaunchActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            } else if (template != null) {
                DrawingModeSelectionScreen(
                    template = template,
                    lesson = null,
                    onBackClick = { navController.popBackStack() },
                    onDrawSketchClick = {
                        navController.navigate(Screen.CameraPreview.createRoute(id, "template"))
                    },
                    onTraceImageClick = {
                        navController.navigate(Screen.PaperTrace.createRoute(id, "template"))
                    },
                    onStartAR = {
                        // If we have a template, load its image for overlay
                        if (template != null) {
                            android.util.Log.d("NavGraph", "Loading template image from: ${template.imageAssetPath}")
                            val bitmap = com.example.ardrawing.data.utils.AssetUtils.getBitmapFromAsset(context, template.imageAssetPath)
                            if (bitmap != null) {
                                android.util.Log.d("NavGraph", "Template bitmap loaded successfully: ${bitmap.width}x${bitmap.height}")
                                LaunchActivity.selectedOverlayBitmap = bitmap
                            } else {
                                android.util.Log.e("NavGraph", "Failed to load template bitmap")
                                LaunchActivity.selectedOverlayBitmap = null
                            }
                        } else {
                            // Clear previous overlay if not using a template
                            LaunchActivity.selectedOverlayBitmap = null
                        }
                        
                        val intent = Intent(context, LaunchActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            } else if (lesson != null) {
                DrawingModeSelectionScreen(
                    template = null,
                    lesson = lesson,
                    onBackClick = { navController.popBackStack() },
                    onDrawSketchClick = {
                        navController.navigate(Screen.CameraPreview.createRoute(id, "lesson"))
                    },
                    onTraceImageClick = {
                        navController.navigate(Screen.PaperTrace.createRoute(id, "lesson"))
                    },
                    onStartAR = {
                         // If we have a lesson, load its image (using first step or main image if available)
                        // For now assuming lesson also has imageAssetPath or similar logic
                        if (lesson != null && lesson.steps.isNotEmpty()) {
                             android.util.Log.d("NavGraph", "Loading lesson image from: ${lesson.steps[0].imageAssetPath}")
                             val bitmap = com.example.ardrawing.data.utils.AssetUtils.getBitmapFromAsset(context, lesson.steps[0].imageAssetPath)
                             if (bitmap != null) {
                                 android.util.Log.d("NavGraph", "Lesson bitmap loaded successfully: ${bitmap.width}x${bitmap.height}")
                                 LaunchActivity.selectedOverlayBitmap = bitmap
                             } else {
                                 android.util.Log.e("NavGraph", "Failed to load lesson bitmap")
                                 LaunchActivity.selectedOverlayBitmap = null
                             }
                        } else {
                             LaunchActivity.selectedOverlayBitmap = null
                        }
                        
                        val intent = Intent(context, LaunchActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        composable(Screen.CameraPreview.route) { backStackEntry ->
            val context = LocalContext.current
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "template"
            
            val template = if (type == "template") {
                com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, id)
            } else null
            
            val lesson = if (type == "lesson") {
                LessonRepository.getLessonById(context, id)
            } else null
            
            // For gallery type, get URI from LaunchActivity
            val galleryImageUri = if (type == "gallery") {
                LaunchActivity.galleryImageUri
            } else null
            
            // For gallery type, also load the image and set it for AR
            if (type == "gallery" && galleryImageUri != null) {
                try {
                    val imageUri = android.net.Uri.parse(galleryImageUri)
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        LaunchActivity.selectedOverlayBitmap = bitmap
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NavGraph", "Error loading gallery image for camera: ${e.message}", e)
                }
            }

            if (template != null || lesson != null || type == "gallery") {
                CameraPreviewScreen(
                    template = template,
                    lesson = lesson,
                    galleryImageUri = galleryImageUri,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.PaperTrace.route) { backStackEntry ->
            val context = LocalContext.current
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "template"
            
            val template = if (type == "template") {
                com.example.ardrawing.data.repository.TemplateRepository.getTemplateById(context, id)
            } else null
            
            val lesson = if (type == "lesson") {
                LessonRepository.getLessonById(context, id)
            } else null
            
            // For gallery type, get URI from LaunchActivity
            val galleryImageUri = if (type == "gallery") {
                LaunchActivity.galleryImageUri
            } else null
            
            if (template != null || lesson != null || type == "gallery") {
                PaperTraceScreen(
                    template = template,
                    lesson = lesson,
                    galleryImageUri = galleryImageUri,
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
                },
                onSeeAllAlbumClick = {
                    navController.navigate(Screen.MyAlbum.route)
                }
            )
        }
        
        composable(Screen.MyAlbum.route) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = SavedDrawingRepository(database.savedDrawingDao())
            val viewModel: MyCreativeViewModel = viewModel(
                factory = MyCreativeViewModel.provideFactory(repository)
            )
            
            MyAlbumScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onImageClick = { imageUri ->
                    // TODO: Handle image click (maybe navigate to full screen view)
                }
            )
        }
        
        composable(Screen.LessonList.route) {
            LessonScreen(
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.DrawingModeSelection.createRoute(lessonId, "lesson"))
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
                         // TODO: Remove this legacy route later
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

