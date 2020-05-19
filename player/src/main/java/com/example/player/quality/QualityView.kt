package com.example.player.quality

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.aliyun.player.nativeclass.TrackInfo
import com.example.player.R
import com.example.player.listener.QualityValue
import com.example.player.quality.QualityItem.Companion.getItem
import com.example.player.theme.ITheme
import com.example.player.widget.AliyunVodPlayerView
import java.util.*

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 清晰度列表view。用于显示不同的清晰度列表。
 * 在[AliyunVodPlayerView]中使用。
 */
class QualityView : FrameLayout, ITheme {
    //显示清晰度的列表
    private var mListView: ListView? = null
    private var mAdapter: BaseAdapter? = null

    //adapter的数据源
    private var mQualityItems: List<TrackInfo>? = null

    //当前播放的清晰度，高亮显示
    private var currentQuality: String? = null

    //清晰度项的点击事件
    private var mOnQualityClickListener: OnQualityClickListener? = null

    //是否是mts源
    private var isMtsSource = false

    //默认的主题色
    private var themeColorResId = R.color.alivc_player_theme_blue

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        //初始化布局
        LayoutInflater.from(context).inflate(R.layout.alivc_view_quality, this, true)
        mListView = findViewById<View>(R.id.quality_view) as ListView
        mListView!!.choiceMode = ListView.CHOICE_MODE_SINGLE
        //不显示滚动条，保证全部被显示
        mListView!!.isVerticalScrollBarEnabled = false
        mListView!!.isHorizontalScrollBarEnabled = false
        mAdapter = QualityAdapter()
        mListView!!.adapter = mAdapter
        mListView!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id -> //点击之后就隐藏
                hide()
                //回调监听
                if (mOnQualityClickListener != null && mQualityItems != null) {
                    mOnQualityClickListener!!.onQualityClick(mQualityItems!![position])
                }
            }
        hide()
    }

    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        //更新主题
        themeColorResId = if (theme === AliyunVodPlayerView.Theme.Blue) {
            R.color.alivc_player_theme_blue
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            R.color.alivc_player_theme_green
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            R.color.alivc_player_theme_orange
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            R.color.alivc_player_theme_red
        } else {
            R.color.alivc_player_theme_blue
        }
        if (mAdapter != null) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 设置清晰度点击监听
     *
     * @param l 点击监听
     */
    fun setOnQualityClickListener(l: OnQualityClickListener?) {
        mOnQualityClickListener = l
    }

    /**
     * 设置清晰度
     *
     * @param qualities      所有支持的清晰度
     * @param currentQuality 当前的清晰度
     */
    fun setQuality(
        qualities: List<TrackInfo>,
        currentQuality: String
    ) {
        //排序之后显示出来
        mQualityItems = sortQuality(qualities)
        this.currentQuality = currentQuality
        if (mAdapter != null) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 设置是否是MTS的源，因为清晰度的文字显示与其他的不一样
     *
     * @param isMts 是否是MTS的源
     */
    fun setIsMtsSource(isMts: Boolean) {
        isMtsSource = isMts
    }

    /**
     * SQ，HQ，FD，LD，SD，HD，2K，4K，OD
     */
    private fun sortQuality(qualities: List<TrackInfo>): List<TrackInfo> {

        //MTS的源不需要排序
        if (isMtsSource) {
            return qualities
        }
        var ld: TrackInfo? = null
        var sd: TrackInfo? = null
        var hd: TrackInfo? = null
        var fd: TrackInfo? = null
        var k2: TrackInfo? = null
        var k4: TrackInfo? = null
        var od: TrackInfo? = null
        var sq: TrackInfo? = null
        var hq: TrackInfo? = null
        for (quality in qualities) {
            if (QualityValue.QUALITY_FLUENT == quality.vodDefinition) {
//                fd = QualityValue.QUALITY_FLUENT;
                fd = quality
            } else if (QualityValue.QUALITY_LOW == quality.vodDefinition) {
//                ld = QualityValue.QUALITY_LOW;
                ld = quality
            } else if (QualityValue.QUALITY_STAND == quality.vodDefinition) {
//                sd = QualityValue.QUALITY_STAND;
                sd = quality
            } else if (QualityValue.QUALITY_HIGH == quality.vodDefinition) {
//                hd = QualityValue.QUALITY_HIGH;
                hd = quality
            } else if (QualityValue.QUALITY_2K == quality.vodDefinition) {
//                k2 = QualityValue.QUALITY_2K;
                k2 = quality
            } else if (QualityValue.QUALITY_4K == quality.vodDefinition) {
//                k4 = QualityValue.QUALITY_4K;
                k4 = quality
            } else if (QualityValue.QUALITY_ORIGINAL == quality.vodDefinition) {
//                od = QualityValue.QUALITY_ORIGINAL;
                od = quality
            } else if (QualityValue.QUALITY_SQ == quality.vodDefinition) {
                sq = quality
            } else if (QualityValue.QUALITY_HQ == quality.vodDefinition) {
                hq = quality
            }
        }

        //清晰度按照fd,ld,sd,hd,2k,4k,od排序
        val sortedQuality: MutableList<TrackInfo> =
            LinkedList()
        //        if (!TextUtils.isEmpty(fd)) {
//            sortedQuality.add(fd);
//        }
//
//        if (!TextUtils.isEmpty(ld)) {
//            sortedQuality.add(ld);
//        }
//        if (!TextUtils.isEmpty(sd)) {
//            sortedQuality.add(sd);
//        }
//        if (!TextUtils.isEmpty(hd)) {
//            sortedQuality.add(hd);
//        }
//
//        if (!TextUtils.isEmpty(k2)) {
//            sortedQuality.add(k2);
//        }
//        if (!TextUtils.isEmpty(k4)) {
//            sortedQuality.add(k4);
//        }
//        if (!TextUtils.isEmpty(od)) {
//            sortedQuality.add(od);
//        }
        if (sq != null) {
            sortedQuality.add(sq)
        }
        if (hq != null) {
            sortedQuality.add(hq)
        }
        if (fd != null) {
            sortedQuality.add(fd)
        }
        if (ld != null) {
            sortedQuality.add(ld)
        }
        if (sd != null) {
            sortedQuality.add(sd)
        }
        if (hd != null) {
            sortedQuality.add(hd)
        }
        if (k2 != null) {
            sortedQuality.add(k2)
        }
        if (k4 != null) {
            sortedQuality.add(k4)
        }
        if (od != null) {
            sortedQuality.add(od)
        }
        return sortedQuality
    }

    /**
     * 在某个控件的上方显示
     *
     * @param anchor 控件
     */
    fun showAtTop(anchor: View) {
        val listViewParam =
            mListView!!.layoutParams as LayoutParams
        listViewParam.width = anchor.width
        listViewParam.height =
            resources.getDimensionPixelSize(R.dimen.alivc_player_rate_item_height) * mQualityItems!!.size
        val location = IntArray(2)
        anchor.getLocationInWindow(location)
        listViewParam.leftMargin = location[0]
        listViewParam.topMargin = height - listViewParam.height - anchor.height - 20
        mListView!!.layoutParams = listViewParam
        mListView!!.visibility = View.VISIBLE
    }

    /**
     * 隐藏
     */
    fun hide() {
        if (mListView != null && mListView!!.visibility == View.VISIBLE) {
            mListView!!.visibility = View.GONE
        }
    }

    /**
     * 触摸之后就隐藏
     *
     * @param event 事件
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mListView!!.visibility == View.VISIBLE) {
            hide()
            return true
        }
        return super.onTouchEvent(event)
    }

    interface OnQualityClickListener {
        /**
         * 清晰度点击事件
         *
         * @param qualityTrackInfo 点中的清晰度
         */
        fun onQualityClick(qualityTrackInfo: TrackInfo)
    }

    /**
     * 清晰度列表的适配器
     */
    private inner class QualityAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return if (mQualityItems != null) {
                mQualityItems!!.size
            } else 0
        }

        override fun getItem(position: Int): Any {
            return mQualityItems!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View,
            parent: ViewGroup
        ): View {
            val view =
                LayoutInflater.from(context).inflate(R.layout.ratetype_item, null) as TextView
            if (mQualityItems != null) {
                val trackInfo = mQualityItems!![position]
                val quality = trackInfo.vodDefinition
                view.text = getItem(context, quality, isMtsSource).name
                //默认白色，当前清晰度为主题色。
                if (quality == currentQuality) {
                    view.setTextColor(ContextCompat.getColor(context, themeColorResId))
                } else {
                    view.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.alivc_common_font_white_light
                        )
                    )
                }
            }
            return view
        }
    }
}