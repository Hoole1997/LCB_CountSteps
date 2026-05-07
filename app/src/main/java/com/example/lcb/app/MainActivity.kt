package com.example.lcb.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.lcb.app.ui.LcbStepsApp

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LcbStepsApp()
        }
    }

    override fun onBackPressed() {
        LcbApp.backLaunchActivity()
    }
}
