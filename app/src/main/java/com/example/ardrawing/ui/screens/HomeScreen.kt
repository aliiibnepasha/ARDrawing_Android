package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.repository.CategoryRepository
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.example.ardrawing.R
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset

/**
 * Creates a nested scroll connection that allows horizontal scrolling to work properly
 * inside a vertical scrollable container (like LazyRow inside LazyColumn).
 * This prevents gesture conflicts between vertical and horizontal scrolling.
 */
@Composable
private fun rememberNestedScrollInteropConnection(): NestedScrollConnection {
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Allow horizontal scrolling to consume the gesture first
                return Offset.Zero
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTemplateSelected: (DrawingTemplate) -> Unit,
    onSeeAll: (Category) -> Unit = {},
    onStartAR: () -> Unit = {},
    onPhotoToSketch: () -> Unit = {},
    onAICreate: () -> Unit = {},
    onProClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val categories = CategoryRepository.getCategories(context)

    // ✅ Vertical scroll state (no snap fling - it's too aggressive for main feeds)
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = colorResource(id = R.color.main_bg),
        topBar = {
            Surface(
                tonalElevation = 0.dp,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // ✅ FIX STATUS BAR
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Ar Drawing",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1C)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF5E8BFF))
                            .clickable { onProClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.crown),
                                contentDescription = "Crown Icon",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                            Text(
                                text = "PRO",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onStartAR() }
                ) {
                    // 🔹 BACKGROUND IMAGE
                    Image(
                        painter = painterResource(id = R.drawable.home_ar_card), // <-- your image
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    // 🔹 CONTENT
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Start AR Tracing",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1C)
                            )

                            Text(
                                text = "Master the art of tracing",
                                fontSize = 13.sp,
                                color = Color(0xFF6F6F6F)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF5E8BFF))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Start",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }


            // ACTION CARDS
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ActionCard2(
                        title = "Photo to\nsketch",
                        subtitle = "Import & trace",
                        iconRes = R.drawable.camera_ic,   // <-- your png later
                        onClick = onPhotoToSketch,
                        modifier = Modifier.weight(1f)
                    )

                    ActionCard2(
                        title = "AI Create\nDrawing",
                        subtitle = "Generate Art",
                        iconRes = R.drawable.ai_icon,  // <-- your png later
                        onClick = onAICreate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // CATEGORY SECTIONS
            items(categories, key = { it.id }) { category ->
                // ✅ Get or create horizontal scroll state for this category (hoisted, stable)
                val rowState = rememberLazyListState()
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.displayName.split(" ").joinToString(" ") { word ->
                                word.lowercase().replaceFirstChar { it.uppercase() }
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { onSeeAll(category) }
                        ) {
                            Text(
                                text = "See all",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = "Arrow",
                                modifier = Modifier.size(12.dp)
                            )

                        }
                    }

                    LazyRow(
                        state = rowState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .nestedScroll(rememberNestedScrollInteropConnection())
                    ) {
                        items(category.templates.take(6)) { template ->
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(
                                        1.dp,
                                        Color(0xFFD9E4FF),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onTemplateSelected(template) },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAssetImagePainter(
                                        template.imageAssetPath
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard2(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(140.dp) // ✅ INCREASED: More height to fit all content properly
            .clip(RoundedCornerShape(18.dp))
            .background(colorResource(R.color.white))
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp), // ✅ EQUAL padding
        verticalArrangement = Arrangement.spacedBy(1.dp) // ✅ REDUCED spacing for better fit
    ) {

        // ✅ Icon circle exactly like Figma
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(50))
                .background(colorResource(R.color.card_color_blue)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
        }

        // ✅ Title with proper overflow handling
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1C),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis, // ✅ Handle overflow gracefully
            lineHeight = 18.sp, // ✅ Better line spacing
            modifier = Modifier.fillMaxWidth() // ✅ Ensure full width
        )

        // ✅ Subtitle with proper overflow handling and ensured visibility
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = Color(0xFF7D7D7D),
            maxLines = 2, // ✅ Allow 2 lines if needed to show full text
            overflow = TextOverflow.Ellipsis, // ✅ Handle overflow gracefully
            modifier = Modifier.fillMaxWidth() // ✅ Ensure full width and visibility
        )
    }
}

