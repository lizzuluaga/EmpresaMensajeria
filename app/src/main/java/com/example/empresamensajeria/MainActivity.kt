package com.example.empresamensajeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
data class Servicio(
    val titulo: String,
    val descripcion: String,
    val precio: String,
    val imagen: Int
)

val listaServicios = listOf(
    Servicio(
        titulo = "Envío nacional",
        descripcion = "Entregas a todo el país en 1 a 3 días hábiles",
        precio = "Desde $8.000",
        imagen = R.drawable.envionacional
    ),
    Servicio(
        titulo = "Envío internacional",
        descripcion = "Llevamos tus paquetes a más de 50 países",
        precio = "Desde $60.000",
        imagen = R.drawable.enviointernacional
    ),
    Servicio(
        titulo = "Bodega",
        descripcion = "Almacenamiento seguro para tu mercancía",
        precio = "Desde $15.000",
        imagen = R.drawable.bodega
    ),
    Servicio(
        titulo = "Recolección a domicilio",
        descripcion = "Recogemos tu paquete donde estés",
        precio = "Desde $5.000",
        imagen = R.drawable.recoleccion
    ),
    Servicio(
        titulo = "Envío el mismo día",
        descripcion = "Entrega urgente dentro de la ciudad",
        precio = "Desde $12.000",
        imagen = R.drawable.mismodia
    )
)






@Composable
fun EmpresaMensajeria() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "EnviaYa 📦🏃‍♀️",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tus paquetes rápidos y seguros",
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
        for (servicio in listaServicios) {
            ProductCard(servicio = servicio)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProductCard(servicio: Servicio) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = servicio.imagen),
                contentDescription = servicio.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = servicio.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = servicio.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = servicio.precio,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Button(onClick = {}) {
                        Text("Pedir")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun EmpresaMensajeriaPreview(){
    EmpresaMensajeriaTheme {
        EmpresaMensajeria()
    }
}


