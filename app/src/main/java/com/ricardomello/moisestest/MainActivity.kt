package com.ricardomello.moisestest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.ricardomello.moisestest.navigation.AppNavGraph
import com.ricardomello.moisestest.ui.theme.MoisesTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoisesTestTheme(darkTheme = true, dynamicColor = false) {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}
