package com.example.maxverstappen

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.maxverstappen.ui.theme.MaxVerstappenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaxVerstappenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyMediaPlayer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MyMediaPlayer(audioResourceId: Int = R.raw.max_verstappen_song, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPlaying by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        createMediaPlayer(context, audioResourceId) {
            isPlaying = it
        }
    }

    // Side-effect to handle lifecycle and resource management
    DisposableEffect(mediaPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("MediaPlayer", "ON_PAUSE")
                    pauseMediaPlayer(mediaPlayer)
                }

                Lifecycle.Event.ON_RESUME -> {
                    Log.d("MediaPlayer", "ON_RESUME")
                    if (!isPlaying) {
                        startMediaPlayer(mediaPlayer)
                    }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("MediaPlayer", "ON_DESTROY")
                    releaseMediaPlayer(mediaPlayer)
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("MediaPlayer", "onDispose")
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                releaseMediaPlayer(mediaPlayer)
            }
        }
    }
    LaunchedEffect(mediaPlayer) {
        if (!isPlaying) {
            startMediaPlayer(mediaPlayer)
        }
    }
    SixteenByNineImage(R.drawable.max_image)
}

private fun createMediaPlayer(
    context: Context,
    audioResourceId: Int,
    onIsPlayingChanged: (Boolean) -> Unit
): MediaPlayer {
    return MediaPlayer.create(context, audioResourceId).apply {
        isLooping = true
        setOnErrorListener { mp, what, extra ->
            Log.e("MediaPlayer", "Error: what=$what, extra=$extra")
            releaseMediaPlayer(mp)
            true // Indicate that we've handled the error
        }
        setOnPreparedListener {
            Log.d("MediaPlayer", "MediaPlayer is prepared")
            onIsPlayingChanged(true)
        }
        setOnCompletionListener {
            onIsPlayingChanged(false)
        }
    }
}

private fun releaseMediaPlayer(mediaPlayer: MediaPlayer?) {
    if (mediaPlayer?.isPlaying == true) {
        mediaPlayer.stop()
    }
    mediaPlayer?.release()
}

private fun pauseMediaPlayer(mediaPlayer: MediaPlayer?) {
    if (mediaPlayer?.isPlaying == true) {
        mediaPlayer.pause()
    }
}

private fun startMediaPlayer(mediaPlayer: MediaPlayer?) {
    if (mediaPlayer?.isPlaying == false) {
        mediaPlayer.start()
    }
}

@Composable
fun SixteenByNineImage(imageResourceId: Int) {
    Image(
        painter = painterResource(id = imageResourceId),
        contentDescription = "16:9 Image",
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(9f / 16)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}