package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.relation.ClienteConContratos
import com.example.empresamensajeria.data.repository.BodegaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroCuenta(val etiqueta: String) {
    TODOS("Todos"),
    CON_CUENTA("Con cuenta"),
    SIN_CUENTA("Sin cuenta")
}

data class ClientesAdminState(
    val cargando: Boolean = true,
    val busqueda: String = "",
    val filtro: FiltroCuenta = FiltroCuenta.TODOS,
    val clientes: List<ClienteConContratos> = emptyList()
) {
    val hayFiltros: Boolean get() = busqueda.isNotBlank() || filtro != FiltroCuenta.TODOS
}

/**
 * Formulario de edición del cliente. Reutiliza ContratoBodegaUiState para tener las mismas
 * validaciones (nombre de al menos 3 letras y teléfono de 10 dígitos). El documento no se edita.
 */
data class EdicionClienteState(
    val cliente: Cliente,
    val datos: ContratoBodegaUiState
)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientesAdminViewModel(private val repository: BodegaRepository) : ViewModel() {

    // ---------- Lista con búsqueda y filtro ----------

    private val busqueda = MutableStateFlow("")
    private val filtro = MutableStateFlow(FiltroCuenta.TODOS)

    val lista: StateFlow<ClientesAdminState> = combine(
        repository.observarClientesConContratos(),
        busqueda,
        filtro
    ) { todos, texto, filtroActual ->
        val consulta = texto.trim()
        val filtrados = todos.filter { item ->
            val c = item.cliente
            val coincideTexto = consulta.isEmpty() ||
                c.nombre.contains(consulta, ignoreCase = true) ||
                c.documento.contains(consulta)
            val coincideCuenta = when (filtroActual) {
                FiltroCuenta.TODOS -> true
                FiltroCuenta.CON_CUENTA -> c.contrasena != null
                FiltroCuenta.SIN_CUENTA -> c.contrasena == null
            }
            coincideTexto && coincideCuenta
        }
        ClientesAdminState(cargando = false, busqueda = texto, filtro = filtroActual, clientes = filtrados)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClientesAdminState())

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto.take(40)
    }

    fun onFiltroChange(nuevo: FiltroCuenta) {
        filtro.value = nuevo
    }

    // ---------- Cliente seleccionado ----------

    private val _documentoSeleccionado = MutableStateFlow<String?>(null)
    val documentoSeleccionado: StateFlow<String?> = _documentoSeleccionado.asStateFlow()

    // Observado en Room: se actualiza solo al editar, cambiar estados o eliminar contratos
    val detalle: StateFlow<ClienteConContratos?> = _documentoSeleccionado
        .flatMapLatest { documento ->
            if (documento == null) flowOf(null)
            else repository.observarClienteConContratos(documento).map { datos ->
                datos?.copy(contratos = datos.contratos.sortedByDescending { it.fechaInicio })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _edicion = MutableStateFlow<EdicionClienteState?>(null)
    val edicion: StateFlow<EdicionClienteState?> = _edicion.asStateFlow()

    fun seleccionarCliente(cliente: Cliente) {
        _edicion.value = null
        _documentoSeleccionado.value = cliente.documento
    }

    fun cerrarDetalle() {
        _edicion.value = null
        _documentoSeleccionado.value = null
    }

    // ---------- Edición ----------

    fun abrirEdicion(cliente: Cliente) {
        _edicion.value = EdicionClienteState(
            cliente = cliente,
            datos = ContratoBodegaUiState(
                documento = cliente.documento,
                nombre = cliente.nombre,
                telefono = cliente.telefono
            )
        )
    }

    fun cancelarEdicion() {
        if (_edicion.value?.datos?.guardando == true) return
        _edicion.value = null
    }

    fun onNombreChange(valor: String) = _edicion.update {
        it?.copy(datos = it.datos.copy(nombre = valor.take(60), errorGuardado = null))
    }

    fun onTelefonoChange(valor: String) = _edicion.update {
        it?.copy(datos = it.datos.copy(telefono = valor.filter(Char::isDigit).take(10), errorGuardado = null))
    }

    fun guardarEdicion() {
        val actual = _edicion.value ?: return
        val datos = actual.datos
        if (datos.guardando || !datos.formularioValido) return
        _edicion.update { it?.copy(datos = it.datos.copy(guardando = true, errorGuardado = null)) }

        viewModelScope.launch {
            try {
                // Se parte del cliente más reciente para no pisar su contraseña si cambió mientras tanto
                val base = detalle.value?.cliente?.takeIf { it.documento == actual.cliente.documento }
                    ?: actual.cliente
                repository.editarCliente(base.copy(nombre = datos.nombre.trim(), telefono = datos.telefono))
                _edicion.value = null
            } catch (e: Exception) {
                _edicion.update {
                    it?.copy(
                        datos = it.datos.copy(
                            guardando = false,
                            errorGuardado = "No se pudieron guardar los cambios. Intenta de nuevo."
                        )
                    )
                }
            }
        }
    }

    // ---------- Acciones ----------

    /** Elimina el cliente con sus contratos y artículos (CASCADE). */
    fun eliminarCliente(cliente: Cliente) {
        cerrarDetalle()
        viewModelScope.launch { repository.eliminarCliente(cliente) }
    }

    fun cambiarEstadoContrato(id: Long, estado: EstadoContrato) {
        viewModelScope.launch { repository.cambiarEstadoContrato(id, estado) }
    }

    /** Elimina el contrato con sus artículos (CASCADE). */
    fun eliminarContrato(contrato: ContratoBodega) {
        viewModelScope.launch { repository.eliminarContrato(contrato) }
    }

    /** Borra la contraseña: el cliente deberá registrarse de nuevo. */
    fun restablecerCuenta(cliente: Cliente) {
        viewModelScope.launch { repository.restablecerCuenta(cliente.documento) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                ClientesAdminViewModel(app.bodegaRepository)
            }
        }
    }
}
