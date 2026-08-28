package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

val netflixPosters = listOf(
    listOf(
        "https://image.tmdb.org/t/p/w500/x2LSRK2CLDCVq8G3raBcTF9CAfv.jpg",
        "https://image.tmdb.org/t/p/w500/reEMJA1uzscCbkpeRJeTT2bjqUp.jpg",
        "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg",
        "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Jz20NpMygczOkv.jpg",
        "https://image.tmdb.org/t/p/w500/rJZv8lXGkgZgK3R1G66c4rC4c0X.jpg",
        "https://image.tmdb.org/t/p/w500/7vjaCdMw15FEbXyLQTVa04URsPm.jpg"
    ),
    listOf(
        "https://image.tmdb.org/t/p/w500/m73vugp9kU05z8nE25gH7y99t8s.jpg",
        "https://image.tmdb.org/t/p/w500/6KErczPBROQty7QoIsaa6wJYXZi.jpg",
        "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
        "https://image.tmdb.org/t/p/w500/xuWxWlhKIXFIIrNSZWgjICzfnqg.jpg",
        "https://image.tmdb.org/t/p/w500/qNBAXBIQlnOThrVvA6mA2B5ggV6.jpg",
        "https://image.tmdb.org/t/p/w500/8c4a8kE7PizaGQQnditMmI1xbRp.jpg"
    ),
    listOf(
        "https://image.tmdb.org/t/p/w500/to0sphuRuKGXRd52ZkUDD.jpg",
        "https://image.tmdb.org/t/p/w500/f496cm9enuEsZkSPzCwnTESEK5s.jpg",
        "https://image.tmdb.org/t/p/w500/3xnWaBz9kL7WjK4y6j212d1F8k8.jpg",
        "https://image.tmdb.org/t/p/w500/1X7vow16X7CnCoexXh4H4F2yDJv.jpg",
        "https://image.tmdb.org/t/p/w500/8Gxv8gP5dEqZFAEQQnGIKqQ8nS2.jpg",
        "https://image.tmdb.org/t/p/w500/suopoADq0k8YZr4dQXcU6pToj6s.jpg"
    )
)

@Composable
fun NetflixCollageBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f),
            verticalArrangement = Arrangement.spacedBy((-20).dp)
        ) {
            netflixPosters.forEach { rowList ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy((-10).dp)
                ) {
                    rowList.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

        // Deep dark cinematic overlay matching Netflix login / main screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
        )
    }
}
