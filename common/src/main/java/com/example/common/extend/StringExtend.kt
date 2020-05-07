package com.example.common.extend

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/19:18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

//累加字符串
fun toAddStr(list: List<String>): String {
    if (list.isEmpty()) {
        return ""
    }
    val builder = StringBuilder()
    for (i in list.indices) {
        builder.append(list[i])
    }
    return builder.toString()
}