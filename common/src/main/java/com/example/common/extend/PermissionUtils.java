package com.example.common.extend;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * <pre>
 *     author : LinRuo
 *     time   : 2018/01/26
 *     desc   : Android 6.0 运行时权限
 *     version: 1.0
 * </pre>
 */

public class PermissionUtils {

    public static final int REQUEST_CODE = 100;

    /**
     * 检查当前系统版本是否大于Android6.0
     *
     * @return
     */
    public static boolean checkAndroidVersion() {
        //判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }

    /**
     * 检查运行时权限
     *
     * @param activity
     * @param permission
     * @return 返回boolean true说明有权限,false说明没有权限
     */
    public static boolean checkPermission(Context activity, String permission) {
        //包名管理器
        PackageManager pkgManager = activity.getPackageManager();
        //返回请求权限的结果
        return pkgManager.checkPermission(permission, activity.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 去请求权限
     */
    public static void requestPermission(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_CODE);
    }

    /**
     * 检查获取拍照或者图库的权限
     *
     * @return
     */
    public static boolean getPhotoOrGalleryPermission(Activity activity) {
        if (PermissionUtils.checkAndroidVersion()) {
            if (!PermissionUtils.checkPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    || !PermissionUtils.checkPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || !PermissionUtils.checkPermission(activity, android.Manifest.permission.CAMERA)) {
                PermissionUtils.requestPermission(
                        activity,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA});
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
}
