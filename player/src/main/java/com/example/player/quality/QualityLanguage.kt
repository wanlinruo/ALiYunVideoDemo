package com.example.player.quality

import android.content.Context
import android.text.TextUtils
import com.example.player.R

object QualityLanguage {
    fun getSaasLanguage(context: Context, quality: String): String {
        return if ("FD" == quality) {
            context.getString(R.string.alivc_fd_definition)
        } else if ("LD" == quality) {
            context.getString(R.string.alivc_ld_definition)
        } else if ("SD" == quality) {
            context.getString(R.string.alivc_sd_definition)
        } else if ("HD" == quality) {
            context.getString(R.string.alivc_hd_definition)
        } else if ("2K" == quality) {
            context.getString(R.string.alivc_k2_definition)
        } else if ("4K" == quality) {
            context.getString(R.string.alivc_k4_definition)
        } else if ("SQ" == quality) {
            context.getString(R.string.alivc_sq_definition)
        } else if ("HQ" == quality) {
            context.getString(R.string.alivc_hq_definition)
        } else {
            if ("OD" == quality) context.getString(R.string.alivc_od_definition) else context.getString(
                R.string.alivc_od_definition
            )
        }
    }

    fun getMtsLanguage(context: Context, quality: String): String {
        return if (TextUtils.isEmpty(quality)) {
            ""
        } else {
            val xldStr: String
            val item: String
            if (quality.toUpperCase().contains("XLD")) {
                xldStr = context.getString(R.string.alivc_mts_xld_definition)
                if (quality.contains("_")) {
                    item = quality.split("_").toTypedArray()[1]
                    xldStr + "_" + item
                } else {
                    xldStr
                }
            } else if (quality.toUpperCase().contains("LD")) {
                xldStr = context.getString(R.string.alivc_mts_ld_definition)
                if (quality.contains("_")) {
                    item = quality.split("_").toTypedArray()[1]
                    xldStr + "_" + item
                } else {
                    xldStr
                }
            } else if (quality.toUpperCase().contains("SD")) {
                xldStr = context.getString(R.string.alivc_mts_sd_definition)
                if (quality.contains("_")) {
                    item = quality.split("_").toTypedArray()[1]
                    xldStr + "_" + item
                } else {
                    xldStr
                }
            } else if (quality.toUpperCase().contains("FHD")) {
                xldStr = context.getString(R.string.alivc_mts_fhd_definition)
                if (quality.contains("_")) {
                    item = quality.split("_").toTypedArray()[1]
                    xldStr + "_" + item
                } else {
                    xldStr
                }
            } else if (quality.toUpperCase().contains("HD")) {
                xldStr = context.getString(R.string.alivc_mts_hd_definition)
                if (quality.contains("_")) {
                    item = quality.split("_").toTypedArray()[1]
                    xldStr + "_" + item
                } else {
                    xldStr
                }
            } else {
                ""
            }
        }
    }
}