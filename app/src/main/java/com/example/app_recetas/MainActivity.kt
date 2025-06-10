package com.example.app_recetas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_recetas.src.Features.Navigate.AppNavigate
import com.example.app_recetas.ui.theme.AppRecetasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRecetasTheme {
                AppNavigate()
            }
        }
    }
}