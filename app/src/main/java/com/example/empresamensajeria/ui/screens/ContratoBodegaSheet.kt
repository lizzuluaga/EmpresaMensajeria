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
import com.example.empresamensajeria.data.repository.ResultadoContrato
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.viewmodel.ContratoBodegaUiState

/** Datos de la cotización de bodega que se muestran arriba del formulario. */
data class ResumenContrato(
    val espacio: String,
    val medidas: String,
    val dias: Int,
    val total: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaContratoBodega(
    estado: ContratoBodegaUiState,
    resumen: ResumenContrato,
    onDocumentoChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Cuando se guarda el contrato, el sheet se cierra y la pantalla muestra el diálogo
    LaunchedEffect(estado.resultado) {
        if (estado.resultado != null) {
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
                text = "Contratar servicio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TarjetaResumenContrato(resumen)

            SeccionTitulo("Tus datos")
            CampoTexto(
                valor = estado.documento,
                onValorChange = onDocumentoChange,
                etiqueta = "Número de documento",
                error = estado.errorDocumento,
                ayuda = "Con él crearás tu cuenta",
                tipoTeclado = KeyboardType.Number
            )
            CampoTexto(
                valor = estado.nombre,
                onValorChange = onNombreChange,
                etiqueta = "Nombre completo",
                error = estado.errorNombre,
                capitalizacion = KeyboardCapitalization.Words
            )
            CampoTexto(
                valor = estado.telefono,
                onValorChange = onTelefonoChange,
                etiqueta = "Teléfono",
                error = estado.errorTelefono,
                ayuda = "10 dígitos",
                tipoTeclado = KeyboardType.Number,
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
                    Text("Confirmar contratación")
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenContrato(resumen: ResumenContrato) {
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
                text = "Bodega · espacio ${resumen.espacio.lowercase()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = resumen.medidas,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (resumen.dias == 1) "Por 1 día" else "Por ${resumen.dias} días",
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
fun DialogoServicioContratado(
    resultado: ResultadoContrato,
    // true si el contrato se hizo con el documento del cliente que ya tiene la sesión iniciada
    conSesionDelCliente: Boolean,
    onIrALogin: () -> Unit,
    onCerrar: () -> Unit
) {
    val mensaje = when {
        conSesionDelCliente ->
            "El servicio se agregó a tu cuenta. Ya puedes verlo en Mi bodega."
        resultado == ResultadoContrato.ClienteConCuenta ->
            "El servicio se agregó a tu cuenta. Inicia sesión para verlo en Mi bodega."
        else ->
            "Ya puedes crear tu cuenta en Iniciar sesión → Registrarse, usando tu número de documento."
    }
    val textoBoton = if (conSesionDelCliente) "Ir a Mi bodega" else "Ir a Iniciar sesión"

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = "¡Servicio contratado!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onIrALogin) { Text(textoBoton) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}
