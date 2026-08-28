package com.example

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.AccentRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

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

    // Full screen expansion for Live TV
    var isFullScreenLive by remember { mutableStateOf(false) }
    var currentLiveIndex by remember { mutableIntStateOf(0) }

    // Overlays when in full screen Live TV
    var showChannelsSidebar by remember { mutableStateOf(false) }
    var showCategoriesSidebar by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        if (isFullScreenLive) {
            val activeLive = liveList.getOrNull(currentLiveIndex) ?: liveList.firstOrNull()
            Box(modifier = Modifier.fillMaxSize()) {
                // Expanded Fullscreen Video Player
                LiveFullScreenPlayer(
                    liveItem = activeLive,
                    onToggleChannels = { showChannelsSidebar = !showChannelsSidebar },
                    onToggleCategories = { showCategoriesSidebar = !showCategoriesSidebar },
                    onExitFullScreen = { isFullScreenLive = false }
                )

                // Left Floating Sidebar: Channels
                if (showChannelsSidebar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)
                            .width(320.dp)
                            .fillMaxHeight(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Canales en Vivo",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(liveList.size) { index ->
                                    val channel = liveList[index]
                                    val isCurrent = index == currentLiveIndex
                                    Surface(
                                        color = if (isCurrent) AccentRed else Color(0xFF222222),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusableItem(onClick = {
                                                currentLiveIndex = index
                                                showChannelsSidebar = false
                                            })
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = channel.title,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Floating Sidebar: Categories / Quick Navigation
                if (showCategoriesSidebar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                            .width(280.dp)
                            .fillMaxHeight(0.7f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Categorías",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(navItems.size) { index ->
                                    val item = navItems[index]
                                    Surface(
                                        color = Color(0xFF222222),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusableItem(onClick = {
                                                showCategoriesSidebar = false
                                                isFullScreenLive = false
                                                selectedIndex = index
                                            })
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(item.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Floating Floating Sidebar (Left)
                FloatingNavigationSidebar(selectedIndex) { selectedIndex = it }

                // Main Content Area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp, vertical = 24.dp)
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
                                val activeLive = liveList.getOrNull(currentLiveIndex) ?: liveList.firstOrNull()
                                val list2026 = (vodList + seriesList).filter { it.title.contains("2026", ignoreCase = true) }.ifEmpty { vodList + seriesList }

                                FeaturedSection(
                                    liveItem = activeLive,
                                    list2026 = list2026,
                                    onExpandLive = { isFullScreenLive = true },
                                    onLiveChanged = { currentLiveIndex = it },
                                    liveList = liveList
                                )
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
                            4 -> { // Search
                                Text(
                                    text = "Búsqueda en desarrollo",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                            5 -> { // Settings
                                Text(
                                    text = "Ajustes de la cuenta (${UserSession.username})",
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
fun FloatingNavigationSidebar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .width(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AccentRed else Color.Transparent)
                        .focusableItem(onClick = { onItemSelected(index) }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedSection(
    liveItem: XtreamItem?,
    list2026: List<XtreamItem>,
    onExpandLive: () -> Unit,
    onLiveChanged: (Int) -> Unit,
    liveList: List<XtreamItem>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Live TV Mini Player with Channel Carousel below it
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LiveMiniPlayer(liveItem, onExpandLive)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Channel Carousel directly below the Mini Player
            if (liveList.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(liveList.size) { index ->
                        val channel = liveList[index]
                        val isSelected = channel.streamUrl == liveItem?.streamUrl
                        Surface(
                            color = if (isSelected) AccentRed else Color(0xFF222222),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .focusableItem(onClick = { onLiveChanged(index) })
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = channel.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
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
fun LiveMiniPlayer(liveItem: XtreamItem?, onExpandLive: () -> Unit) {
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
            } catch (e: Exception) {}
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
                onExpandLive()
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
fun LiveFullScreenPlayer(
    liveItem: XtreamItem?,
    onToggleChannels: () -> Unit,
    onToggleCategories: () -> Unit,
    onExitFullScreen: () -> Unit
) {
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
            } catch (e: Exception) {}
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
            .background(Color.Black)
    ) {
        if (url.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    VLCVideoLayout(ctx).apply {
                        mediaPlayer.attachViews(this, null, false, false)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        // Toggle or show UI options
                    }
            )
        }

        // Floating Action Buttons on FullScreen (Channels & Categories & Exit)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onToggleChannels() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Icon(Icons.Default.Tv, contentDescription = "Canales")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Canales")
            }

            Button(
                onClick = { onToggleCategories() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Icon(Icons.Default.List, contentDescription = "Categorías")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Categorías")
            }

            Button(
                onClick = { onExitFullScreen() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
            ) {
                Text("Salir")
            }
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
