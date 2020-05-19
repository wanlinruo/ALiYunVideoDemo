package com.example.player.listener

/**
 * 清晰度切换监听
 */
interface OnChangeQualityListener {

    fun onChangeQualitySuccess(quality: String?)

    fun onChangeQualityFail(code: Int, msg: String?)
}