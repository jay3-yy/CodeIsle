package com.openisle.android.presentation.features.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.openisle.android.R
import com.openisle.android.databinding.ActivityImageViewerBinding
import kotlinx.coroutines.launch
import java.io.OutputStream

class ImageViewerActivity : AppCompatActivity() {

    // 使用 View Binding 替代 findViewById
    private lateinit var binding: ActivityImageViewerBinding
    private var imageUrl: String? = null

    companion object {
        private const val EXTRA_IMAGE_URL = "extra_image_url"

        fun newIntent(context: Context, imageUrl: String): Intent {
            return Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        if (imageUrl == null) {
            Toast.makeText(this, "图片地址无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadImage()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 为返回按钮添加明确的点击事件
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadImage() {
        // 使用 Coil 的扩展函数简化图片加载
        binding.photoView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image) // 添加占位图
            error(R.drawable.ic_broken_image) // 添加加载失败图
            listener(
                onStart = { binding.progressBar.isVisible = true },
                onSuccess = { _, _ -> binding.progressBar.isVisible = false },
                onError = { _, _ -> binding.progressBar.isVisible = false }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_viewer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // home/up button is handled by toolbar's NavigationOnClickListener
            R.id.action_save_image -> {
                saveImageToGallery()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveImageToGallery() {
        lifecycleScope.launch {
            val drawable = binding.photoView.drawable
            if (drawable !is BitmapDrawable) {
                Toast.makeText(this@ImageViewerActivity, "图片尚未加载完成", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val bitmap = drawable.bitmap
            val imageName = "openisle_${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null

            try {
                // 使用 MediaStore API 保存图片，这是 Android 10 (Q) 及以上版本的标准做法
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // 为图片指定存储路径
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OpenIsle")
                    }
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }

                if (fos != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, fos)
                    Toast.makeText(this@ImageViewerActivity, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                } else {
                    throw Exception("无法创建输出流")
                }
            } catch (e: Exception) {
                Toast.makeText(this@ImageViewerActivity, "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                fos?.close()
            }
        }
    }
}