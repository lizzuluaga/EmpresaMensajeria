package com.example.empresamensajeria.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.correoValido
import com.example.empresamensajeria.data.listaContactos
import com.example.empresamensajeria.data.listaHorarios
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.TarjetaContacto

private data class MensajeEnviado(
    val nombre: String,
    val correo: String,
    val mensaje: String
)

private fun abrirEnlace(contexto: Context, enlace: String) {
    val accion = when {
        enlace.startsWith("tel:") -> Intent.ACTION_DIAL
        enlace.startsWith("mailto:") -> Intent.ACTION_SENDTO
        else -> Intent.ACTION_VIEW
    }
    try {
        contexto.startActivity(Intent(accion, Uri.parse(enlace)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            contexto,
            "No hay una aplicación disponible para esta acción",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun ContactoScreen(onMenuClick: () -> Unit) {
    val contexto = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf<MensajeEnviado?>(null) }

    val nombreInvalido = nombre.isNotEmpty() && nombre.trim().length < 3
    val correoInvalido = correo.isNotEmpty() && !correoValido(correo)
    val mensajeCorto = mensaje.isNotEmpty() && mensaje.trim().length < 10
    val formularioValido = nombre.trim().length >= 3 &&
            correoValido(correo) &&
            mensaje.trim().length >= 10

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Contáctanos", onMenuClick = onMenuClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Estamos para ayudarte",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Elige el canal que prefieras o escríbenos un mensaje.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SeccionTitulo("Canales de atención")
            for (medio in listaContactos) {
                TarjetaContacto(
                    medio = medio,
                    onClick = { abrirEnlace(contexto, medio.enlace) }
                )
            }

            SeccionTitulo("Horario de atención")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    listaHorarios.forEachIndexed { indice, horario ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = horario.dias,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = horario.horas,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (indice != listaHorarios.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }

            SeccionTitulo("Escríbenos")

            val confirmacion = enviado
            if (confirmacion == null) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nombreInvalido,
                    supportingText = {
                        if (nombreInvalido) Text("Escribe al menos 3 caracteres")
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = correoInvalido,
                    supportingText = {
                        if (correoInvalido) Text("Escribe un correo válido, como nombre@correo.com")
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { if (it.length <= 300) mensaje = it },
                    label = { Text("Mensaje") },
                    minLines = 4,
                    maxLines = 6,
                    isError = mensajeCorto,
                    supportingText = {
                        Text(
                            if (mensajeCorto) {
                                "Escribe al menos 10 caracteres (${mensaje.length}/300)"
                            } else {
                                "${mensaje.length}/300"
                            }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        enviado = MensajeEnviado(nombre.trim(), correo.trim(), mensaje.trim())
                        nombre = ""
                        correo = ""
                        mensaje = ""
                    },
                    enabled = formularioValido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Enviar mensaje")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "✅ ¡Mensaje enviado!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gracias, ${confirmacion.nombre}. " +
                                    "Te responderemos a ${confirmacion.correo} lo antes posible.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tu mensaje:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = confirmacion.mensaje,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { enviado = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar otro mensaje")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}