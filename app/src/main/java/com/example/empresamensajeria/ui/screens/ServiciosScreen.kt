package com.example.empresamensajeria.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.listaServicios
import com.example.empresamensajeria.ui.components.Encabezado
import com.example.empresamensajeria.ui.components.ProductCard
import com.example.empresamensajeria.ui.theme.EmpresaMensajeriaTheme

@Composable
fun ServiciosScreen(
    onMenuClick: () -> Unit,
    onServicioClick: (Int) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    val serviciosFiltrados = listaServicios.filter {
        it.titulo.contains(busqueda, ignoreCase = true) ||
                it.descripcion.contains(busqueda, ignoreCase = true)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Encabezado(
                onMenuClick = onMenuClick,
                onSearchClick = { buscando = !buscando }
            )
        }
        item {
            AnimatedVisibility(visible = buscando) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar servicio") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                busqueda = ""
                                buscando = false
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
            }
        }
        item {
            Text(
                text = "Servicios disponibles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (serviciosFiltrados.isEmpty()) {
            item {
                Text(
                    text = "No encontramos servicios con esa búsqueda",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        items(serviciosFiltrados, key = { it.id }) { servicio ->
            ProductCard(
                servicio = servicio,
                onClick = { onServicioClick(servicio.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
fun ServiciosPreview() {
    EmpresaMensajeriaTheme {
        Surface {
            ServiciosScreen(onMenuClick = {}, onServicioClick = {})
        }
    }
}

@Preview(showBackground = true, heightDp = 1600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ServiciosOscuroPreview() {
    EmpresaMensajeriaTheme {
        Surface {
            ServiciosScreen(onMenuClick = {}, onServicioClick = {})
        }
    }
}