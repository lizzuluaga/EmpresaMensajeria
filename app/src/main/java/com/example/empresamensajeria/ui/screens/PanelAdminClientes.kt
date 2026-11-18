package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.empresamensajeria.data.aPesos
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.relation.ClienteConContratos
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.components.EtiquetaEstadoContrato
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.SelectorDesplegable
import com.example.empresamensajeria.ui.components.formatearFecha
import com.example.empresamensajeria.ui.components.textoDias
import com.example.empresamensajeria.ui.viewmodel.ClientesAdminState
import com.example.empresamensajeria.ui.viewmodel.ClientesAdminViewModel
import com.example.empresamensajeria.ui.viewmodel.EdicionClienteState
import com.example.empresamensajeria.ui.viewmodel.FiltroCuenta

/** Acciones que piden confirmación antes de ejecutarse. */
private sealed interface Confirmacion {
    data class EliminarContrato(val contrato: ContratoBodega) : Confirmacion
    data class RestablecerCuenta(val cliente: Cliente) : Confirmacion
    data class EliminarCliente(val cliente: Cliente, val contratos: Int) : Confirmacion
}

// ---------- Pestaña Clientes ----------

@Composable
internal fun PestanaClientes(
    onVerInventario: (Long) -> Unit,
    viewModel: ClientesAdminViewModel = viewModel(factory = ClientesAdminViewModel.Factory)
) {
    val estado by viewModel.lista.collectAsStateWithLifecycle()
    val documentoSeleccionado by viewModel.documentoSeleccionado.collectAsStateWithLifecycle()
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val edicion by viewModel.edicion.collectAsStateWithLifecycle()
    var confirmacion by remember { mutableStateOf<Confirmacion?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            CampoTexto(
                valor = estado.busqueda,
                onValorChange = viewModel::onBusquedaChange,
                etiqueta = "Buscar por nombre o documento",
                accionTeclado = ImeAction.Search
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FiltroCuenta.entries) { opcion ->
                FilterChip(
                    selected = estado.filtro == opcion,
                    onClick = { viewModel.onFiltroChange(opcion) },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }
        ListaClientes(estado, onClick = viewModel::seleccionarCliente)
    }

    if (documentoSeleccionado != null) {
        HojaDetalleCliente(
            detalle = detalle,
            edicion = edicion,
            viewModel = viewModel,
            onVerInventario = onVerInventario,
            onConfirmar = { confirmacion = it }
        )
    }

    confirmacion?.let { accion ->
        val (titulo, mensaje, boton) = when (accion) {
            is Confirmacion.EliminarContrato -> Triple(
                "Eliminar contrato",
                "Se eliminará el contrato y todos sus artículos.",
                "Eliminar"
            )
            is Confirmacion.RestablecerCuenta -> Triple(
                "Restablecer cuenta",
                "El cliente deberá registrarse de nuevo con su documento para crear otra contraseña.",
                "Restablecer"
            )
            is Confirmacion.EliminarCliente -> Triple(
                "Eliminar cliente",
                "Se eliminarán sus ${accion.contratos} contratos y todos sus artículos. Ya no podrá iniciar sesión.",
                "Eliminar"
            )
        }
        AlertDialog(
            onDismissRequest = { confirmacion = null },
            title = { Text(titulo) },
            text = { Text(mensaje) },
            confirmButton = {
                Button(
                    onClick = {
                        when (accion) {
                            is Confirmacion.EliminarContrato -> viewModel.eliminarContrato(accion.contrato)
                            is Confirmacion.RestablecerCuenta -> viewModel.restablecerCuenta(accion.cliente)
                            is Confirmacion.EliminarCliente -> viewModel.eliminarCliente(accion.cliente)
                        }
                        confirmacion = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text(boton) }
            },
            dismissButton = {
                TextButton(onClick = { confirmacion = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ListaClientes(estado: ClientesAdminState, onClick: (Cliente) -> Unit) {
    when {
        estado.cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        estado.clientes.isEmpty() -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (estado.hayFiltros) {
                    "Ningún cliente coincide con la búsqueda o el filtro."
                } else {
                    "Todavía no hay clientes registrados."
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
            items(estado.clientes, key = { it.cliente.documento }) { item ->
                TarjetaCliente(item, onClick = { onClick(item.cliente) })
            }
        }
    }
}

@Composable
private fun TarjetaCliente(item: ClienteConContratos, onClick: () -> Unit) {
    val cliente = item.cliente
    val activos = item.contratos.count { it.estado == EstadoContrato.ACTIVO }
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
                    text = cliente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaCuenta(tieneCuenta = cliente.contrasena != null)
            }
            Text(text = "Documento: ${cliente.documento}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Teléfono: ${cliente.telefono}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = textoContratos(item.contratos.size, activos),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun textoContratos(total: Int, activos: Int): String {
    val contratos = if (total == 1) "1 contrato" else "$total contratos"
    return if (total == 0) contratos else "$contratos ($activos ${if (activos == 1) "activo" else "activos"})"
}

/** Chip "Con cuenta" / "Sin cuenta" con el mismo estilo que EtiquetaEstado. */
@Composable
private fun EtiquetaCuenta(tieneCuenta: Boolean) {
    val color = if (tieneCuenta) Color(0xFF43A047) else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (tieneCuenta) "Con cuenta" else "Sin cuenta",
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------- Hoja de detalle / edición ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaDetalleCliente(
    detalle: ClienteConContratos?,
    edicion: EdicionClienteState?,
    viewModel: ClientesAdminViewModel,
    onVerInventario: (Long) -> Unit,
    onConfirmar: (Confirmacion) -> Unit
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

                edicion != null -> FormularioEdicionCliente(edicion, viewModel)

                else -> DetalleCliente(
                    datos = detalle,
                    onEditar = { viewModel.abrirEdicion(detalle.cliente) },
                    onCambiarEstado = viewModel::cambiarEstadoContrato,
                    onVerInventario = onVerInventario,
                    onConfirmar = onConfirmar
                )
            }
        }
    }
}

@Composable
private fun DetalleCliente(
    datos: ClienteConContratos,
    onEditar: () -> Unit,
    onCambiarEstado: (Long, EstadoContrato) -> Unit,
    onVerInventario: (Long) -> Unit,
    onConfirmar: (Confirmacion) -> Unit
) {
    val cliente = datos.cliente
    val tieneCuenta = cliente.contrasena != null

    // a) Datos del cliente
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Cliente", style = MaterialTheme.typography.labelMedium)
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        EtiquetaCuenta(tieneCuenta)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DatoCliente("Documento", cliente.documento)
            DatoCliente("Teléfono", cliente.telefono)
            DatoCliente("Cuenta", if (tieneCuenta) "Registrada" else "Aún no se ha registrado")
        }
    }

    // b) Editar
    OutlinedButton(
        onClick = onEditar,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("Editar")
    }

    // c) Contratos
    SeccionTitulo("Contratos")
    if (datos.contratos.isEmpty()) {
        Text(
            text = "Este cliente no tiene contratos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
    datos.contratos.forEach { contrato ->
        TarjetaContratoAdmin(
            contrato = contrato,
            onCambiarEstado = { onCambiarEstado(contrato.id, it) },
            onVerInventario = { onVerInventario(contrato.id) },
            onEliminar = { onConfirmar(Confirmacion.EliminarContrato(contrato)) }
        )
    }

    // d) Restablecer cuenta (solo si tiene)
    if (tieneCuenta) {
        OutlinedButton(
            onClick = { onConfirmar(Confirmacion.RestablecerCuenta(cliente)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(50.dp)
        ) {
            Text("Restablecer cuenta")
        }
    }

    // e) Eliminar cliente
    Button(
        onClick = { onConfirmar(Confirmacion.EliminarCliente(cliente, datos.contratos.size)) },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (tieneCuenta) 0.dp else 12.dp)
            .height(50.dp)
    ) {
        Text("Eliminar cliente")
    }
}

@Composable
private fun TarjetaContratoAdmin(
    contrato: ContratoBodega,
    onCambiarEstado: (EstadoContrato) -> Unit,
    onVerInventario: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Contrato #${contrato.id}", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "Espacio ${contrato.espacio.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                EtiquetaEstadoContrato(contrato.estado)
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar contrato ${contrato.id}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(modifier = Modifier.padding(end = 8.dp)) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DatoCliente("Días", textoDias(contrato.dias))
                DatoCliente("Valor", contrato.valor.aPesos())
                DatoCliente("Fecha de inicio", formatearFecha(contrato.fechaInicio))

                val otros = EstadoContrato.entries.filter { it != contrato.estado }
                SelectorDesplegable(
                    etiqueta = "Cambiar estado",
                    opciones = otros.map { it.etiqueta },
                    seleccion = "",
                    onSeleccion = { etiqueta -> otros.find { it.etiqueta == etiqueta }?.let(onCambiarEstado) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                TextButton(
                    onClick = onVerInventario,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Ver inventario →")
                }
            }
        }
    }
}

@Composable
private fun DatoCliente(etiqueta: String, valor: String) {
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
private fun FormularioEdicionCliente(edicion: EdicionClienteState, viewModel: ClientesAdminViewModel) {
    val datos = edicion.datos

    Text(
        text = "Editar cliente",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = "Documento ${edicion.cliente.documento} (no se puede modificar).",
        style = MaterialTheme.typography.bodySmall
    )
    CampoTexto(
        valor = datos.nombre,
        onValorChange = viewModel::onNombreChange,
        etiqueta = "Nombre completo",
        error = datos.errorNombre,
        capitalizacion = KeyboardCapitalization.Words
    )
    CampoTexto(
        valor = datos.telefono,
        onValorChange = viewModel::onTelefonoChange,
        etiqueta = "Teléfono",
        error = datos.errorTelefono,
        ayuda = "10 dígitos",
        tipoTeclado = KeyboardType.Number,
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
