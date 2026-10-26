package com.example.havadurumu

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.havadurumu.data.AppContainer
import com.example.havadurumu.ui.CloudHeartApp
import com.example.havadurumu.ui.theme.CloudHeartTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        AppContainer.init(this)
        enableEdgeToEdge()

        setContent {
            CloudHeartTheme {
                CloudHeartApp()
            }
        }
    }

    private companion object {
        const val TAG = "Lifecycle"
    }
}
