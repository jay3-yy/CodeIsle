package com.example.openisle

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var photoView: PhotoView
    private lateinit var saveButton: FloatingActionButton
    private var imageUrl: String? = null

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"
    }

    // 注册权限请求的回调
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                saveImageToGallery() // 如果用户同意了权限，则直接保存
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        photoView = findViewById(R.id.photoView)
        saveButton = findViewById(R.id.saveButton)

        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        // 使用 Coil 加载图片到 PhotoView
        imageUrl?.let { url ->
            photoView.load(url)
        }

        // PhotoView 支持点击退出页面
        photoView.setOnPhotoTapListener { _, _, _ ->
            finish()
        }

        saveButton.setOnClickListener {
            if (checkStoragePermission()) {
                saveImageToGallery()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        // Android 10 (Q) 及以上版本，保存到公共目录有新的方式，通常不需要旧的存储权限
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            // 在较新系统上，我们不需要额外权限，可以直接尝试保存
            saveImageToGallery()
        }
    }

    private fun saveImageToGallery() {
        val drawable = photoView.drawable ?: return
        val bitmap = (drawable as BitmapDrawable).bitmap

        val fileName = "OpenIsle_Avatar_${Date().time}.png"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "OpenIsle")
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "OpenIsle"
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val image = File(imagesDir, fileName)
                outputStream = FileOutputStream(image)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this@ImageViewerActivity, "图片已保存到相册", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@ImageViewerActivity, "保存图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}