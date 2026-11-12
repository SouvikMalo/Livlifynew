package com.livlify.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.livlify.app.R
import com.livlify.app.ui.theme.SurfaceHigh
import com.livlify.app.ui.theme.TextSecondary

/** Album art with a music-note placeholder when a song has no artwork. */
@Composable
fun ArtImage(
    model: Any?,
    modifier: Modifier = Modifier,
    corner: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(SurfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.fillMaxSize(0.45f)
        )
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
