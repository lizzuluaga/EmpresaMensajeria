package com.example.empresamensajeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    EmpresaMensajeria()
                }

            }
        }
    }
}

@Composable
fun EmpresaMensajeria(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Mensajeria Fast 📦🏃‍♀️",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tus paquetes rapidos y seguros",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = {
                Text("Nuestros Servicios")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Servicios disponibles",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProductCard(name = "Envio de paquetes Nacionales")
        Spacer(modifier = Modifier.height(12.dp))
        ProductCard(name = "Bodega")
        Spacer(modifier = Modifier.height(12.dp))
        ProductCard(name = "Envio de paquetes Internacionales")
    }
}

@Composable
fun ProductCard( name: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Envio de paquetes",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consulta el precio",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {}
                ) {
                    Text("AQUI")
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun EmpresaMensajeriaPreview(){
    EmpresaMensajeriaTheme {
        EmpresaMensajeria()
    }
}