package com.example.player.view.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.player.R

/**
 * 缩略图View
 */
class ThumbnailView : LinearLayout {
    /**
     * 缩略图 time
     */
    private var mPositionTextView: TextView? = null

    /**
     * 缩略图 picture
     */
    private var mThumbnailImageView: ImageView = findViewById(R.id.iv_thumbnail)

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        //Inflate布局
        LayoutInflater.from(context).inflate(R.layout.alivc_view_thumbnail, this, true)
        findAllViews()
    }

    private fun findAllViews() {
        mPositionTextView = findViewById(R.id.tv_position)
    }

    fun setTime(time: String?) {
        mPositionTextView!!.text = time
    }

    fun setThumbnailPicture(bitmap: Bitmap?) {
        mThumbnailImageView.setImageBitmap(bitmap)
    }

    fun showThumbnailView() {
        visibility = View.VISIBLE
    }

    fun hideThumbnailView() {
        visibility = View.GONE
    }

    fun getThumbnailImageView(): ImageView {
        return mThumbnailImageView
    }

}