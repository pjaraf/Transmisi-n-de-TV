package com.example

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.AccentRed
import com.example.ui.theme.NavRailBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NavItem(val title: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("Inicio", Icons.Default.PlayArrow),
    NavItem("En Vivo", Icons.Default.Tv),
    NavItem("Series", Icons.Default.PlayCircle),
    NavItem("Películas", Icons.Default.Movie),
    NavItem("Buscar", Icons.Default.Search),
    NavItem("Ajustes", Icons.Default.Settings)
)

@Composable
fun MainScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var liveList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var vodList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var seriesList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 840

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val live = XtreamRepository.getLiveStreams()
                val vod = XtreamRepository.getVodStreams()
                val series = XtreamRepository.getSeries()
                liveList = live
                vodList = vod
                seriesList = series
            } catch (e: Exception) {
                // handle error
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isLargeScreen) {
                    AppBottomNavigation(selectedIndex) { selectedIndex = it }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLargeScreen) {
                    AppNavigationRail(selectedIndex) { selectedIndex = it }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentRed)
                        }
                    } else {
                        when (selectedIndex) {
                            0 -> { // Home
                                val featuredLive = liveList.firstOrNull()
                                val list2026 = (vodList + seriesList).filter { it.title.contains("2026", ignoreCase = true) }.ifEmpty { vodList + seriesList }

                                FeaturedSection(featuredLive, list2026)
                                Spacer(modifier = Modifier.height(32.dp))
                                if (list2026.isNotEmpty()) {
                                    ContentList("Mi Lista y Contenido Destacado", list2026)
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                            1 -> { // Live TV
                                ContentList("Canales en Vivo", liveList)
                            }
                            2 -> { // Shows / Series
                                val series2026 = seriesList.filter { it.title.contains("2026", ignoreCase = true) }.ifEmpty { seriesList }
                                ContentList("Series 2026", series2026)
                            }
                            3 -> { // Movies / VOD
                                val movies2026 = vodList.filter { it.title.contains("2026", ignoreCase = true) }.ifEmpty { vodList }
                                ContentList("Películas 2026", movies2026)
                            }
                            4 -> { // Search / Ajustes etc
                                Text(
                                    text = "Búsqueda en desarrollo",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                            5 -> {
                                Text(
                                    text = "Ajustes de la cuenta (${UserSession.username})",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = "Sección en desarrollo",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AppNavigationRail(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationRail(
        containerColor = NavRailBackground,
        modifier = Modifier.fillMaxHeight(),
        header = { Spacer(modifier = Modifier.height(16.dp)) }
    ) {
        navItems.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(28.dp)
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = TextSecondary,
                    indicatorColor = if (isSelected) AccentRed else Color.Transparent
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun AppBottomNavigation(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = NavRailBackground,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        navItems.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = TextSecondary,
                    indicatorColor = if (isSelected) AccentRed else Color.Transparent
                )
            )
        }
    }
}

@Composable
fun FeaturedSection(liveItem: XtreamItem?, list2026: List<XtreamItem>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Live TV Mini Player
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {
            LiveMiniPlayer(liveItem)
        }

        // Right: Estrenos 2026 Auto-rotating Panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Estrenos 2026",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            RotatingEstrenosPanel(list2026)
        }
    }
}

@Composable
fun LiveMiniPlayer(liveItem: XtreamItem?) {
    val context = LocalContext.current
    val url = liveItem?.streamUrl ?: ""

    val libVLC = remember { LibVLC(context, arrayListOf("--no-drop-late-frames", "--no-skip-frames", "--rtsp-tcp")) }
    val mediaPlayer = remember { MediaPlayer(libVLC) }

    DisposableEffect(url) {
        if (url.isNotBlank()) {
            try {
                val media = Media(libVLC, Uri.parse(url))
                media.setHWDecoderEnabled(true, false)
                mediaPlayer.media = media
                media.release()
                mediaPlayer.play()
            } catch (e: Exception) {
                // fallback
            }
        }
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.detachViews()
                mediaPlayer.release()
                libVLC.release()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .focusableItem(onClick = {
                if (liveItem != null) {
                    VlcPlayerHelper.playUrl(context, liveItem.streamUrl, liveItem.title)
                }
            })
    ) {
        if (url.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    VLCVideoLayout(ctx).apply {
                        mediaPlayer.attachViews(this, null, false, false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = liveItem?.imageUrl ?: "https://image.tmdb.org/t/p/original/mZjZgY6ObiKtVuKVDrnS9VnuNlE.jpg",
                contentDescription = "Live TV",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // "EN VIVO" Badge
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(AccentRed, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "EN VIVO",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Info overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = liveItem?.title ?: "Transmisión en vivo",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RotatingEstrenosPanel(items: List<XtreamItem>) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            while (true) {
                delay(4000)
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    val vod2026Item = items.getOrNull(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .focusableItem(onClick = {
                if (vod2026Item != null) {
                    VlcPlayerHelper.playUrl(context, vod2026Item.streamUrl, vod2026Item.title)
                }
            })
    ) {
        AsyncImage(
            model = vod2026Item?.imageUrl ?: "https://image.tmdb.org/t/p/w500/8c4a8kE7PizaGQQnditMmI1xbRp.jpg",
            contentDescription = "Estrenos 2026",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2026 Badge
        Box(
            modifier = Modifier
                .padding(12.dp)
                .background(AccentRed, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "2026",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Info overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                text = vod2026Item?.title ?: "Estreno 2026",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ContentList(title: String, items: List<XtreamItem>) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(items) { item ->
                MediaCard(item)
            }
        }
    }
}

@Composable
fun MediaCard(item: XtreamItem) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .focusableItem(onClick = {
                VlcPlayerHelper.playUrl(context, item.streamUrl, item.title)
            })
    ) {
        AsyncImage(
            model = item.imageUrl.ifBlank { "https://image.tmdb.org/t/p/w500/8c4a8kE7PizaGQQnditMmI1xbRp.jpg" },
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(8.dp)
        ) {
             Text(
                text = item.title,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

@Composable
fun Modifier.focusableItem(onClick: () -> Unit = {}): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale = if (isFocused) 1.05f else 1.0f

    return this
        .scale(scale)
        .zIndex(if (isFocused) 1f else 0f)
        .focusable(interactionSource = interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            onClick()
        }
        .then(
            if (isFocused) {
                Modifier.background(Color.White.copy(alpha = 0.1f))
            } else {
                Modifier
            }
        )
}
