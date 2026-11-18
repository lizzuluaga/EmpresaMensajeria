package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.aPesos
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.viewmodel.SolicitudEnvioUiState

/** Datos de la cotización que se muestran arriba del formulario. */
data class ResumenSolicitud(
    val servicio: String,
    val origen: String,
    val destino: String,
    val peso: String,
    val total: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaSolicitudEnvio(
    estado: SolicitudEnvioUiState,
    resumen: ResumenSolicitud,
    onRemitenteChange: (String) -> Unit,
    onTelefonoRemitenteChange: (String) -> Unit,
    onDestinatarioChange: (String) -> Unit,
    onTelefonoDestinatarioChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Cuando se genera la guía, el sheet se cierra y la pantalla muestra el diálogo
    LaunchedEffect(estado.guiaGenerada) {
        if (estado.guiaGenerada != null) {
            sheetState.hide()
            onCerrar()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState,
        // Los insets se aplican en el contenido para que el formulario suba con el teclado
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Solicitar envío",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TarjetaResumenSolicitud(resumen)

            SeccionTitulo("Remitente")
            CampoTexto(
                valor = estado.remitente,
                onValorChange = onRemitenteChange,
                etiqueta = "Nombre completo",
                error = estado.errorRemitente,
                capitalizacion = KeyboardCapitalization.Words
            )
            CampoTexto(
                valor = estado.telefonoRemitente,
                onValorChange = onTelefonoRemitenteChange,
                etiqueta = "Teléfono",
                error = estado.errorTelefonoRemitente,
                ayuda = "10 dígitos",
                tipoTeclado = KeyboardType.Number
            )

            SeccionTitulo("Destinatario")
            CampoTexto(
                valor = estado.destinatario,
                onValorChange = onDestinatarioChange,
                etiqueta = "Nombre completo",
                error = estado.errorDestinatario,
                capitalizacion = KeyboardCapitalization.Words
            )
            CampoTexto(
                valor = estado.telefonoDestinatario,
                onValorChange = onTelefonoDestinatarioChange,
                etiqueta = "Teléfono",
                error = estado.errorTelefonoDestinatario,
                ayuda = "10 dígitos",
                tipoTeclado = KeyboardType.Number
            )
            CampoTexto(
                valor = estado.direccionDestino,
                onValorChange = onDireccionChange,
                etiqueta = "Dirección de entrega",
                error = estado.errorDireccion,
                capitalizacion = KeyboardCapitalization.Sentences,
                accionTeclado = ImeAction.Done
            )

            estado.errorGuardado?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onConfirmar,
                enabled = estado.formularioValido && !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmar solicitud")
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenSolicitud(resumen: ResumenSolicitud) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = resumen.servicio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (resumen.origen == resumen.destino) {
                    "En ${resumen.origen}"
                } else {
                    "${resumen.origen} → ${resumen.destino}"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Peso: ${resumen.peso} kg",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = resumen.total.aPesos(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DialogoGuiaRegistrada(
    guia: String,
    onRastrear: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = "¡Solicitud registrada!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tu número de guía es",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = guia,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Guárdala para rastrear tu envío",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onRastrear) { Text("Rastrear ahora") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}
