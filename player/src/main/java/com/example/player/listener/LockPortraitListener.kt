package com.example.player.listener

/**
 * 屏幕状态监听
 */
interface LockPortraitListener {

    fun onLockScreenMode(type: Int)

    companion object {
        const val FIX_MODE_SMALL = 1
        const val FIX_MODE_FULL = 2
    }
}