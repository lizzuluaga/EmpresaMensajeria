package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.empresamensajeria.data.aPesos
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.EstadoEnvio
import com.example.empresamensajeria.data.local.relation.EnvioConMovimientos
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.components.EtiquetaEstado
import com.example.empresamensajeria.ui.components.LineaTiempo
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.SelectorDesplegable
import com.example.empresamensajeria.ui.components.formatearFechaHora
import com.example.empresamensajeria.ui.viewmodel.EdicionEnvioState
import com.example.empresamensajeria.ui.viewmodel.EnviosAdminState
import com.example.empresamensajeria.ui.viewmodel.PanelAdminViewModel

// ---------- Pestaña Envíos ----------

@Composable
internal fun PestanaEnvios(viewModel: PanelAdminViewModel) {
    val estado by viewModel.envios.collectAsStateWithLifecycle()
    val guiaSeleccionada by viewModel.guiaSeleccionada.collectAsStateWithLifecycle()
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val edicion by viewModel.edicion.collectAsStateWithLifecycle()
    var envioAEliminar by remember { mutableStateOf<Envio?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CampoTexto(
                valor = estado.busqueda,
                onValorChange = viewModel::onBusquedaChange,
                etiqueta = "Buscar por guía, remitente o destinatario",
                accionTeclado = ImeAction.Search
            )
        }
        FiltrosEstado(estado.filtro, viewModel::onFiltroChange)
        ListaEnvios(estado, onClick = viewModel::seleccionarEnvio)
    }

    if (guiaSeleccionada != null) {
        HojaDetalleEnvio(
            detalle = detalle,
            edicion = edicion,
            viewModel = viewModel,
            onEliminar = { envioAEliminar = it }
        )
    }

    envioAEliminar?.let { envio ->
        AlertDialog(
            onDismissRequest = { envioAEliminar = null },
            title = { Text("Eliminar envío") },
            text = { Text("Se eliminará la guía ${envio.guia} y todo su historial.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarEnvio(envio)
                        envioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { envioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FiltrosEstado(filtro: EstadoEnvio?, onFiltroChange: (EstadoEnvio?) -> Unit) {
    // null = Todos
    val opciones: List<EstadoEnvio?> = listOf(null) + EstadoEnvio.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { opcion ->
            FilterChip(
                selected = filtro == opcion,
                onClick = { onFiltroChange(opcion) },
                label = { Text(opcion?.etiqueta ?: "Todos") }
            )
        }
    }
}

@Composable
private fun ListaEnvios(estado: EnviosAdminState, onClick: (Envio) -> Unit) {
    when {
        estado.cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        estado.envios.isEmpty() -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (estado.hayFiltros) {
                    "Ningún envío coincide con la búsqueda o el filtro."
                } else {
                    "Todavía no hay envíos registrados."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(estado.envios, key = { it.id }) { envio ->
                TarjetaEnvio(envio, onClick = { onClick(envio) })
            }
        }
    }
}

@Composable
private fun TarjetaEnvio(envio: Envio, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = envio.guia,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EtiquetaEstado(envio.estado)
            }
            Text(text = textoRuta(envio), style = MaterialTheme.typography.bodyMedium)
            Text(text = envio.servicio, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatearFechaHora(envio.fecha),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = envio.valor.aPesos(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun textoRuta(envio: Envio) =
    if (envio.origen == envio.destino) "En ${envio.origen}" else "${envio.origen} → ${envio.destino}"

// ---------- Hoja de detalle / edición ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaDetalleEnvio(
    detalle: EnvioConMovimientos?,
    edicion: EdicionEnvioState?,
    viewModel: PanelAdminViewModel,
    onEliminar: (Envio) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = viewModel::cerrarDetalle,
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
            when {
                detalle == null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                edicion != null -> FormularioEdicion(edicion, viewModel)

                else -> DetalleEnvio(
                    datos = detalle,
                    onCambiarEstado = { viewModel.cambiarEstado(detalle.envio, it) },
                    onEditar = { viewModel.abrirEdicion(detalle.envio) },
                    onEliminar = { onEliminar(detalle.envio) }
                )
            }
        }
    }
}

@Composable
private fun DetalleEnvio(
    datos: EnvioConMovimientos,
    onCambiarEstado: (EstadoEnvio) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val envio = datos.envio
    val siguiente = EstadoEnvio.entries.getOrNull(envio.estado.ordinal + 1)

    // a) Datos del envío
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Guía", style = MaterialTheme.typography.labelMedium)
            Text(
                text = envio.guia,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        EtiquetaEstado(envio.estado)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DatoEnvio("Servicio", envio.servicio)
            DatoEnvio("Ruta", textoRuta(envio))
            DatoEnvio("Peso", "${envio.peso} kg")
            DatoEnvio("Valor", envio.valor.aPesos())
            DatoEnvio("Fecha", formatearFechaHora(envio.fecha))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DatoEnvio("Remitente", envio.remitente)
            DatoEnvio("Teléfono", envio.telefonoRemitente)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DatoEnvio("Destinatario", envio.destinatario)
            DatoEnvio("Teléfono", envio.telefonoDestinatario)
            DatoEnvio("Dirección", envio.direccionDestino)
        }
    }

    // b) Cambiar estado
    SeccionTitulo("Cambiar estado")
    if (siguiente != null) {
        Button(
            onClick = { onCambiarEstado(siguiente) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Pasar a ${siguiente.etiqueta.lowercase()}")
        }
    }
    val otros = EstadoEnvio.entries.filter { it != envio.estado }
    SelectorDesplegable(
        etiqueta = "Elegir otro estado",
        opciones = otros.map { it.etiqueta },
        seleccion = "",
        onSeleccion = { etiqueta ->
            otros.find { it.etiqueta == etiqueta }?.let(onCambiarEstado)
        },
        modifier = Modifier.fillMaxWidth()
    )

    // c) Historial (se actualiza solo al cambiar el estado)
    SeccionTitulo("Historial")
    LineaTiempo(eventos = datos.movimientos)

    // d) Editar / Eliminar
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onEditar,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text("Editar")
        }
        Button(
            onClick = onEliminar,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text("Eliminar")
        }
    }
}

@Composable
private fun DatoEnvio(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormularioEdicion(edicion: EdicionEnvioState, viewModel: PanelAdminViewModel) {
    val datos = edicion.datos

    Text(
        text = "Editar envío ${edicion.envio.guia}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = "Origen, destino, servicio, peso y valor no se pueden modificar.",
        style = MaterialTheme.typography.bodySmall
    )

    SeccionTitulo("Remitente")
    CampoTexto(
        valor = datos.remitente,
        onValorChange = viewModel::onRemitenteChange,
        etiqueta = "Nombre completo",
        error = datos.errorRemitente,
        capitalizacion = KeyboardCapitalization.Words
    )
    CampoTexto(
        valor = datos.telefonoRemitente,
        onValorChange = viewModel::onTelefonoRemitenteChange,
        etiqueta = "Teléfono",
        error = datos.errorTelefonoRemitente,
        ayuda = "10 dígitos",
        tipoTeclado = KeyboardType.Number
    )

    SeccionTitulo("Destinatario")
    CampoTexto(
        valor = datos.destinatario,
        onValorChange = viewModel::onDestinatarioChange,
        etiqueta = "Nombre completo",
        error = datos.errorDestinatario,
        capitalizacion = KeyboardCapitalization.Words
    )
    CampoTexto(
        valor = datos.telefonoDestinatario,
        onValorChange = viewModel::onTelefonoDestinatarioChange,
        etiqueta = "Teléfono",
        error = datos.errorTelefonoDestinatario,
        ayuda = "10 dígitos",
        tipoTeclado = KeyboardType.Number
    )
    CampoTexto(
        valor = datos.direccionDestino,
        onValorChange = viewModel::onDireccionChange,
        etiqueta = "Dirección de entrega",
        error = datos.errorDireccion,
        capitalizacion = KeyboardCapitalization.Sentences,
        accionTeclado = ImeAction.Done
    )

    datos.errorGuardado?.let {
        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = viewModel::cancelarEdicion,
            enabled = !datos.guardando,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text("Cancelar")
        }
        Button(
            onClick = viewModel::guardarEdicion,
            enabled = datos.formularioValido && !datos.guardando,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            if (datos.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Guardar cambios")
            }
        }
    }
}
