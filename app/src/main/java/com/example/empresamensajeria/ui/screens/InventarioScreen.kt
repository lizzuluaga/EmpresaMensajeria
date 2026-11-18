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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.empresamensajeria.data.local.entity.Articulo
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.components.EtiquetaEstadoContrato
import com.example.empresamensajeria.ui.components.formatearFecha
import com.example.empresamensajeria.ui.components.textoDiasRestantes
import com.example.empresamensajeria.ui.viewmodel.FormularioArticuloState
import com.example.empresamensajeria.ui.viewmodel.InventarioUiState
import com.example.empresamensajeria.ui.viewmodel.InventarioViewModel

@Composable
fun InventarioScreen(
    onAtrasClick: () -> Unit,
    viewModel: InventarioViewModel = viewModel(factory = InventarioViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    var articuloAEliminar by remember { mutableStateOf<Articulo?>(null) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Inventario", onAtrasClick = onAtrasClick) },
        floatingActionButton = {
            if (estado.editable) {
                FloatingActionButton(onClick = viewModel::abrirNuevo) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar artículo")
                }
            }
        }
    ) { paddingValues ->
        val contrato = estado.contrato
        when {
            estado.cargando -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            contrato == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Este contrato ya no existe.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            else -> ListaInventario(
                estado = estado,
                contrato = contrato,
                onBusquedaChange = viewModel::onBusquedaChange,
                onEditar = viewModel::abrirEdicion,
                onEliminar = { articuloAEliminar = it },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (formulario.abierto) {
        HojaArticulo(
            formulario = formulario,
            onNombreChange = viewModel::onNombreChange,
            onDescripcionChange = viewModel::onDescripcionChange,
            onCantidadChange = viewModel::onCantidadChange,
            onGuardar = viewModel::guardar,
            onCerrar = viewModel::cerrarFormulario
        )
    }

    articuloAEliminar?.let { articulo ->
        AlertDialog(
            onDismissRequest = { articuloAEliminar = null },
            title = { Text("Eliminar artículo") },
            text = { Text("¿Seguro que quieres eliminar \"${articulo.nombre}\" del inventario? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(articulo)
                        articuloAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { articuloAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ListaInventario(
    estado: InventarioUiState,
    contrato: ContratoBodega,
    onBusquedaChange: (String) -> Unit,
    onEditar: (Articulo) -> Unit,
    onEliminar: (Articulo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Espacio abajo para que el botón "+" no tape el último artículo
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ResumenContrato(estado, contrato) }

        if (!estado.editable) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "ℹ️ Este contrato está ${contrato.estado.etiqueta.lowercase()}; " +
                            "el inventario es solo de consulta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // La búsqueda solo tiene sentido si hay artículos
        if (estado.totalArticulos > 0) {
            item {
                CampoTexto(
                    valor = estado.busqueda,
                    onValorChange = onBusquedaChange,
                    etiqueta = "Buscar por nombre o descripción",
                    accionTeclado = ImeAction.Search
                )
            }
        }

        when {
            estado.totalArticulos == 0 -> item {
                Text(
                    text = if (estado.editable) {
                        "Aún no tienes artículos guardados. Toca + para agregar el primero."
                    } else {
                        "Este contrato no tiene artículos guardados."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }

            estado.articulos.isEmpty() -> item {
                Text(
                    text = "Ningún artículo coincide con \"${estado.busqueda.trim()}\".",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }

            else -> items(estado.articulos, key = { it.id }) { articulo ->
                TarjetaArticulo(
                    articulo = articulo,
                    editable = estado.editable,
                    onEditar = { onEditar(articulo) },
                    onEliminar = { onEliminar(articulo) }
                )
            }
        }
    }
}

@Composable
private fun ResumenContrato(estado: InventarioUiState, contrato: ContratoBodega) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contrato #${contrato.id}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Espacio ${contrato.espacio.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                EtiquetaEstadoContrato(contrato.estado)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                DatoResumen("Días restantes", textoDiasRestantes(contrato), Modifier.weight(1f))
                DatoResumen("Artículos", estado.totalArticulos.toString(), Modifier.weight(1f))
                DatoResumen("Unidades", estado.totalUnidades.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TarjetaArticulo(
    articulo: Articulo,
    editable: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = articulo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (articulo.descripcion.isNotBlank()) {
                    Text(
                        text = articulo.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Cantidad: ${articulo.cantidad} · Ingresó el ${formatearFecha(articulo.fechaIngreso)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (editable) {
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar ${articulo.nombre}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar ${articulo.nombre}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaArticulo(
    formulario: FormularioArticuloState,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editando = formulario.idEditando != null

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
                text = if (editando) "Editar artículo" else "Nuevo artículo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            CampoTexto(
                valor = formulario.nombre,
                onValorChange = onNombreChange,
                etiqueta = "Nombre",
                error = formulario.errorNombre,
                capitalizacion = KeyboardCapitalization.Sentences
            )
            CampoTexto(
                valor = formulario.descripcion,
                onValorChange = onDescripcionChange,
                etiqueta = "Descripción (opcional)",
                capitalizacion = KeyboardCapitalization.Sentences
            )
            CampoTexto(
                valor = formulario.cantidad,
                onValorChange = onCantidadChange,
                etiqueta = "Cantidad",
                error = formulario.errorCantidad,
                ayuda = "Número de unidades",
                tipoTeclado = KeyboardType.Number,
                accionTeclado = ImeAction.Done
            )

            formulario.errorGuardado?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onGuardar,
                enabled = formulario.formularioValido && !formulario.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (formulario.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (editando) "Guardar cambios" else "Agregar artículo")
                }
            }
        }
    }
}
