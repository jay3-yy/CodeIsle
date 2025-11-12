package com.openisle.android

import android.app.Application
import android.graphics.Bitmap
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenIsleApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // 如果后续要恢复 Firebase，这里再初始化即可
        // FirebaseApp.initializeApp(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // ★ 关键：禁止硬件位图，避免 BlurView 软件绘制时崩溃
            .allowHardware(false)
            // 建议使用 ARGB_8888，配合禁用硬件位图保证兼容性与显示质量
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            // 你项目里已有的 SVG 支持，保留
            .components(
                ComponentRegistry.Builder()
                    .add(SvgDecoder.Factory())
                    .build()
            )
            .build()
    }
}
