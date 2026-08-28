package com.example

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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.AccentRed
import com.example.ui.theme.NavRailBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NavItem(val title: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("Home", Icons.Filled.PlayArrow),
    NavItem("Live", Icons.Filled.PlayCircle),
    NavItem("Shows", Icons.Filled.Tv),
    NavItem("Movies", Icons.Filled.Movie),
    NavItem("History", Icons.Filled.History),
    NavItem("Settings", Icons.Filled.Settings),
    NavItem("Search", Icons.Filled.Search)
)

@Composable
fun NetflixBackground() {
    NetflixCollageBackground()
}

@Composable
fun MainScreen() {
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp > 600
    var selectedIndex by remember { mutableIntStateOf(1) } // Default to 'Live'

    val scope = rememberCoroutineScope()
    var liveList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var vodList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var seriesList by remember { mutableStateOf<List<XtreamItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            liveList = XtreamRepository.getLiveStreams()
            vodList = XtreamRepository.getVodStreams()
            seriesList = XtreamRepository.getSeries()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NetflixBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
                    TopBar()
                    Spacer(modifier = Modifier.height(24.dp))

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
                                FeaturedSection(liveList.firstOrNull())
                                Spacer(modifier = Modifier.height(32.dp))
                                if (liveList.isNotEmpty()) {
                                    ContentList("Canales en Vivo", liveList.take(20))
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                                if (vodList.isNotEmpty()) {
                                    ContentList("Películas (VOD)", vodList.take(20))
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                                if (seriesList.isNotEmpty()) {
                                    ContentList("Series", seriesList.take(20))
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                            1 -> { // Live TV
                                ContentList("Canales en Vivo", liveList)
                            }
                            2 -> { // Shows / Series
                                ContentList("Series de TV", seriesList)
                            }
                            3 -> { // Movies / VOD
                                ContentList("Películas", vodList)
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
fun TopBar() {
    val dateFormat = SimpleDateFormat("HH:mm • dd/MM/yyyy", Locale.getDefault())
    val currentTime = dateFormat.format(Date())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTime,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FeaturedSection(featuredItem: XtreamItem?) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Featured Live / Stream Panel
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .focusableItem(onClick = {
                    if (featuredItem != null) {
                        VlcPlayerHelper.playUrl(context, featuredItem.streamUrl, featuredItem.title)
                    }
                })
        ) {
            AsyncImage(
                model = featuredItem?.imageUrl ?: "https://image.tmdb.org/t/p/original/mZjZgY6ObiKtVuKVDrnS9VnuNlE.jpg",
                contentDescription = "Featured",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // "EN VIVO" Badge
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(AccentRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "DESTACADO IPTV",
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
                    text = featuredItem?.title ?: "Transmisión en vivo",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
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
