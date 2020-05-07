package com.example.core.http.vo

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/23:17
 *     desc   :
 *     version: 1.0
 * </pre>
 */
data class UploadAuthDTO(
    val requestId: String,
    val videoId: String,
    val uploadAddress: String,
    val uploadAuth: String
)