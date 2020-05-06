package com.example.core

import com.example.player.PlayerManager
import com.example.upload.UploadManager

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/23:25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object FacadeManager {

    fun init() {
        PlayerManager.initPlayer()
        UploadManager.initUpload()
    }
}