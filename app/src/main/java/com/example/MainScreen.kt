package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NavRailBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

data class MediaItem(val title: String, val imageUrl: String)

val featuredMedia = listOf(
    MediaItem("Conexiones Perdidas", "https://image.tmdb.org/t/p/w500/8c4a8kE7PizaGQQnditMmI1xbRp.jpg"), // Example poster URLs
    MediaItem("Batman Inicia", "https://image.tmdb.org/t/p/w500/aAwwqG1n5eGj483v29u6Q8YmYyX.jpg"),
    MediaItem("Arsenal Military", "https://image.tmdb.org/t/p/w500/6yK2bO2sO4jC9V46jI7jRz9q7E5.jpg"),
    MediaItem("Ted", "https://image.tmdb.org/t/p/w500/y6wG1z8r1yO22sR2M459pQZcQzZ.jpg"),
    MediaItem("Code Name: The", "https://image.tmdb.org/t/p/w500/1X7vow16X7CnCoexXh4H4F2yDJv.jpg"),
    MediaItem("Chicha tu madre", "https://image.tmdb.org/t/p/w500/8Gxv8gP5dEqZFAEQQnGIKqQ8nS2.jpg")
)

@Composable
fun NetflixBackground() {
    NetflixCollageBackground()
}

@Composable
fun MainScreen() {
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp > 600
    var selectedIndex by remember { mutableIntStateOf(1) } // Default to 'Live' to match screenshot roughly

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
                FeaturedSection(isLargeScreen)
                Spacer(modifier = Modifier.height(32.dp))
                ContentList("Mi Lista y Contenido Destacado", featuredMedia)
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
fun FeaturedSection(isLargeScreen: Boolean = false) {
    // Determine arrangement based on layout
    // In a real app we might use a FlowRow or BoxWithConstraints, but since we are handling isLargeScreen we can adapt.
    // The screenshot has a side-by-side layout (60/40 split roughly)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live TV Panel
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .focusableItem()
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/original/mZjZgY6ObiKtVuKVDrnS9VnuNlE.jpg", // Sports backdrop
                contentDescription = "Live TV",
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
                    text = "EN VIVO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            // Channel/Info overlay (bottom left)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CICLISMO | LA VUELTA 1.Tadej Pogacar (Eslovenia)",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Premieres Panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .focusableItem()
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/original/y6wG1z8r1yO22sR2M459pQZcQzZ.jpg", // Ted poster
                contentDescription = "Estrenos 2026",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Top Title
            Text(
                text = "Estrenos 2026",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    // Added a shadow layer for readability if background is bright
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Bottom Title
            Text(
                text = "ted",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun ContentList(title: String, items: List<MediaItem>) {
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
fun MediaCard(item: MediaItem) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .focusableItem()
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Gradient or dark overlay could be added here for better text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
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

// Custom modifier to handle focus states cleanly for TV (D-pad) and Touch
@Composable
fun Modifier.focusableItem(): Modifier {
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
        ) {}
        .then(
            if (isFocused) {
                Modifier.background(Color.White.copy(alpha = 0.1f))
            } else {
                Modifier
            }
        )
}
