package com.example.fitorquit

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class VideoTextActivity : AppCompatActivity() {
    private lateinit var videoPlayer: VideoView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_text)

        videoPlayer = findViewById(R.id.videoView2)


        findViewById<Button>(R.id.btn).setOnClickListener {
            openGallery(60)
        }
    }

    private fun openGallery(limit: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "video/*"
        galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, limit)
        galleryIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 104857600L);
        videoGalleryLauncher.launch(galleryIntent)
    }

    private fun retrieveDimensions(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, uri)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)

        Log.d("test", "retrieveDimensions: $width x $height")
    }

    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    retrieveDimensions(fileUri)
                    videoPlayer.setVideoURI(fileUri)
                    videoPlayer.start()
                }
                else -> {
                    Log.e("test", "videoGallery: cancelled")
                }
            }
        }

}