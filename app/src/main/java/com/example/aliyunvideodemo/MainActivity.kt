package com.example.aliyunvideodemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.common.InitInterface
import com.example.core.FacadeManager
import com.example.core.ImpInitInterface

class MainActivity : AppCompatActivity() {

    private val manager = FacadeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager.getUploadAuth()
    }
}
