package com.example.aliyunvideodemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.core.FacadeManager
import com.example.core.choose.ChooseFileActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //选择文件，添加到上传列表
        btn_choose.setOnClickListener {
//            if (PermissionUtils.getPhotoOrGalleryPermission(this)) {
            val intent = Intent()
            intent.setClass(this, ChooseFileActivity::class.java)
            startActivity(intent)
//            }
        }

        //上传控制-开始上传
        btn_upload_start.setOnClickListener {
            FacadeManager.startUpload()
        }
    }
}
