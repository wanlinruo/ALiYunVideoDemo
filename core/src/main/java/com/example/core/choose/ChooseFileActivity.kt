package com.example.core.choose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.example.common.bo.VodInfoBO
import com.example.core.R
import com.example.upload.UploadManager
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.ImageEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy


class ChooseFileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CHOOSE = 199
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Matisse.from(this@ChooseFileActivity)
            .choose(MimeType.ofAll())
            .countable(true)
            .maxSelectable(1)
            .captureStrategy(CaptureStrategy(true, "com.example.aliyunvideodemo.FileProvider"))
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(Glide4Engine())
            .theme(R.style.Matisse_Zhihu)
            .forResult(REQUEST_CODE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_CHOOSE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val obtainPathResult = Matisse.obtainPathResult(data)
                    if (!obtainPathResult.isNullOrEmpty()) {

                        val vodInfoBO =
                            VodInfoBO(obtainPathResult[0], "Android测试文件", "Android-测试上传文件是否成功")
                        UploadManager.getUploadAuth(vodInfoBO)
                    }
                }
            }
        }
        finish()
    }

    private class Glide4Engine : ImageEngine {

        override fun loadThumbnail(
            context: Context,
            resize: Int,
            placeholder: Drawable,
            imageView: ImageView,
            uri: Uri
        ) {
            Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(
                    RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop()
                )
                .into(imageView)
        }

        override fun loadGifThumbnail(
            context: Context,
            resize: Int,
            placeholder: Drawable,
            imageView: ImageView,
            uri: Uri
        ) {
            Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(
                    RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop()
                )
                .into(imageView)
        }

        override fun loadImage(
            context: Context,
            resizeX: Int,
            resizeY: Int,
            imageView: ImageView,
            uri: Uri
        ) {
            Glide.with(context)
                .load(uri)
                .apply(
                    RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter()
                )
                .into(imageView)
        }

        override fun loadGifImage(
            context: Context,
            resizeX: Int,
            resizeY: Int,
            imageView: ImageView,
            uri: Uri
        ) {
            Glide.with(context)
                .asGif()
                .load(uri)
                .apply(
                    RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter()
                )
                .into(imageView)
        }

        override fun supportAnimatedGif(): Boolean {
            return true
        }
    }

}
