package com.example.player.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.widget.ImageView
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 * 简单的图片加载器
 */
class ImageLoader(target: ImageView) {
    private val mLoadImgHandler: LoadImgHandler
    fun loadAsync(url: String) {
        Thread(Runnable {
            val bitmap = getImageBitmap(url)
            val msg = mLoadImgHandler.obtainMessage()
            msg.obj = bitmap
            mLoadImgHandler.sendMessage(msg)
        }).start()
    }

    /**
     * 获取bitmap
     */
    private fun getImageBitmap(url: String): Bitmap? {
        var imgUrl: URL? = null
        var bitmap: Bitmap? = null
        try {
            imgUrl = URL(url)
            val conn = imgUrl
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doInput = true
            conn.connect()
            val `is` = conn.inputStream
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: MalformedURLException) {
        } catch (e: IOException) {
        }
        return bitmap
    }

    /**
     * 异步加载图片
     */
    private class LoadImgHandler internal constructor(imageView: ImageView) :
        Handler() {
        private val imageViewWeakReference: WeakReference<ImageView>
        override fun handleMessage(msg: Message) {
            val targetView = imageViewWeakReference.get() ?: return
            val bitmap = msg.obj as Bitmap
            targetView.setImageBitmap(bitmap)
            super.handleMessage(msg)
        }

        init {
            imageViewWeakReference =
                WeakReference(imageView)
        }
    }

    init {
        mLoadImgHandler = LoadImgHandler(target)
    }
}