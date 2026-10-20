package com.example.empresamensajeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.empresamensajeria.navigation.AppNavegacion
import com.example.empresamensajeria.ui.theme.EmpresaMensajeriaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmpresaMensajeriaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavegacion()
                }

            }
        }
    }
}



