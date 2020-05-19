package com.example.player.constants

/**
 * 播放参数, 包含:
 * vid, vidSts, akId, akSecre, scuToken
 */
object PlayParameter {
    /**
     * type, 用于区分播放类型, 默认为vidsts播放
     * vidsts: vid类型
     * localSource: url类型
     */
    var PLAY_PARAM_TYPE = "vidsts"
    private const val PLAY_PARAM_VID_DEFAULT = "9fb028c29acb421cb634c77cf4ebe078"

    /**
     * vid, 初始为: 9fb028c29acb421cb634c77cf4ebe078
     */
    var PLAY_PARAM_VID = ""
    var PLAY_PARAM_REGION = "cn-shanghai"

    /**
     * akId
     */
    var PLAY_PARAM_AK_ID = ""

    /**
     * akSecre
     */
    var PLAY_PARAM_AK_SECRE = ""

    /**
     * scuToken
     */
    var PLAY_PARAM_SCU_TOKEN = ""

    /**
     * url类型的播放地址, 初始为:http://player.alicdn.com/video/aliyunmedia.mp4
     */
    var PLAY_PARAM_URL = "http://player.alicdn.com/video/aliyunmedia.mp4"
}