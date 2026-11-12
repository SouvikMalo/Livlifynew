package com.livlify.app.data

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val dateAddedSec: Long,
    val uri: Uri,
    val artUri: Uri
)
