package com.example.openisle

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenIsleApplication : Application(), ImageLoaderFactory {

    /**
     * 重写这个方法来提供一个自定义的、支持 SVG 的 ImageLoader 实例。
     * Coil 会在整个应用中自动使用这个我们提供的 ImageLoader。
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // ▼▼▼ 关键：在这里添加 SVG 解码器 ▼▼▼
                add(SvgDecoder.Factory())
            }
            .build()
    }
}