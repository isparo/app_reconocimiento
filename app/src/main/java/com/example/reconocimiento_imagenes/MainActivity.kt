package com.example.reconocimiento_imagenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconocimiento_imagenes.ui.theme.ReconocimientoimagenesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReconocimientoimagenesTheme {
                val navController = rememberNavController()
                val viewModel: OpenAIImageAnalysisViewModel = viewModel()

                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "pantalla1",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("pantalla1") {
                            Pantalla1(
                                viewModel = viewModel,
                                onIrAPantalla2 = { navController.navigate("pantalla2") }
                            )
                        }
                        composable("pantalla2") {
                            Pantalla2(
                                viewModel = viewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                        composable("pantalla3") {
                            Pantalla3(onVolver = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}