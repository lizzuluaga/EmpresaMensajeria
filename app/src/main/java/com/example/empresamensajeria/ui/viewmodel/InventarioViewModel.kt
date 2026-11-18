package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.local.entity.Articulo
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.navigation.Rutas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventarioUiState(
    val cargando: Boolean = true,
    // null después de cargar = el contrato no existe (por ejemplo, el admin lo eliminó)
    val contrato: ContratoBodega? = null,
    val busqueda: String = "",
    // Artículos ya filtrados por la búsqueda
    val articulos: List<Articulo> = emptyList(),
    // Totales de todo el contrato (sin filtrar)
    val totalArticulos: Int = 0,
    val totalUnidades: Int = 0
) {
    // Solo los contratos activos permiten agregar, editar y eliminar
    val editable: Boolean get() = contrato?.estado == EstadoContrato.ACTIVO
}

data class FormularioArticuloState(
    val abierto: Boolean = false,
    // Artículo que se está editando; null si es uno nuevo
    val editando: Articulo? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val cantidad: String = "",
    val guardando: Boolean = false,
    val errorGuardado: String? = null
) {
    val idEditando: Long? get() = editando?.id

    val errorNombre: String?
        get() = if (nombre.isNotEmpty() && nombre.trim().length < 2) "Escribe al menos 2 caracteres" else null

    val errorCantidad: String?
        get() = if (cantidad.isNotEmpty() && (cantidad.toIntOrNull() ?: 0) <= 0) {
            "La cantidad debe ser mayor que 0"
        } else null

    val formularioValido: Boolean
        get() = nombre.trim().length >= 2 && (cantidad.toIntOrNull() ?: 0) > 0
}

class InventarioViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BodegaRepository
) : ViewModel() {

    private val contratoId: Long = checkNotNull(savedStateHandle.get<Long>(Rutas.ARG_CONTRATO_ID))

    private val busqueda = MutableStateFlow("")

    val uiState: StateFlow<InventarioUiState> =
        combine(repository.observarContratoConArticulos(contratoId), busqueda) { datos, texto ->
            val todos = datos?.articulos.orEmpty().sortedByDescending { it.fechaIngreso }
            val filtro = texto.trim()
            val filtrados = if (filtro.isEmpty()) todos else todos.filter {
                it.nombre.contains(filtro, ignoreCase = true) ||
                    it.descripcion.contains(filtro, ignoreCase = true)
            }
            InventarioUiState(
                cargando = false,
                contrato = datos?.contrato,
                busqueda = texto,
                articulos = filtrados,
                totalArticulos = todos.size,
                totalUnidades = todos.sumOf { it.cantidad }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InventarioUiState()
        )

    private val _formulario = MutableStateFlow(FormularioArticuloState())
    val formulario: StateFlow<FormularioArticuloState> = _formulario.asStateFlow()

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto.take(40)
    }

    // ---------- Formulario ----------

    fun abrirNuevo() {
        if (!uiState.value.editable) return
        _formulario.value = FormularioArticuloState(abierto = true)
    }

    fun abrirEdicion(articulo: Articulo) {
        if (!uiState.value.editable) return
        _formulario.value = FormularioArticuloState(
            abierto = true,
            editando = articulo,
            nombre = articulo.nombre,
            descripcion = articulo.descripcion,
            cantidad = articulo.cantidad.toString()
        )
    }

    fun cerrarFormulario() {
        if (_formulario.value.guardando) return
        _formulario.value = FormularioArticuloState()
    }

    fun onNombreChange(valor: String) =
        _formulario.update { it.copy(nombre = valor.take(50), errorGuardado = null) }

    fun onDescripcionChange(valor: String) =
        _formulario.update { it.copy(descripcion = valor.take(120), errorGuardado = null) }

    fun onCantidadChange(valor: String) =
        _formulario.update { it.copy(cantidad = valor.filter(Char::isDigit).take(5), errorGuardado = null) }

    /** Agrega un artículo nuevo o guarda los cambios del que se está editando. */
    fun guardar() {
        val form = _formulario.value
        if (form.guardando || !form.formularioValido || !uiState.value.editable) return
        val cantidad = form.cantidad.toInt()
        _formulario.update { it.copy(guardando = true, errorGuardado = null) }

        viewModelScope.launch {
            try {
                val original = form.editando
                if (original == null) {
                    repository.agregarArticulo(contratoId, form.nombre, form.descripcion, cantidad)
                } else {
                    repository.editarArticulo(
                        original.copy(
                            nombre = form.nombre.trim(),
                            descripcion = form.descripcion.trim(),
                            cantidad = cantidad
                        )
                    )
                }
                _formulario.value = FormularioArticuloState()
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(guardando = false, errorGuardado = "No se pudo guardar el artículo. Intenta de nuevo.")
                }
            }
        }
    }

    fun eliminar(articulo: Articulo) {
        if (!uiState.value.editable) return
        viewModelScope.launch { repository.eliminarArticulo(articulo) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                InventarioViewModel(createSavedStateHandle(), app.bodegaRepository)
            }
        }
    }
}
