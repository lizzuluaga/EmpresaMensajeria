package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.entity.EstadoEnvio
import com.example.empresamensajeria.data.local.relation.EnvioConMovimientos
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.repository.EnvioRepository
import com.example.empresamensajeria.data.session.SesionRepository
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

data class ResumenAdminState(
    val cargando: Boolean = true,
    val totalEnvios: Int = 0,
    // Todos los estados, con 0 en los que no tienen envíos
    val enviosPorEstado: Map<EstadoEnvio, Int> = EstadoEnvio.entries.associateWith { 0 },
    val valorTotal: Int = 0,
    val clientes: Int = 0,
    val clientesConCuenta: Int = 0,
    val contratosPorEstado: Map<EstadoContrato, Int> = EstadoContrato.entries.associateWith { 0 }
) {
    val contratosActivos: Int get() = contratosPorEstado[EstadoContrato.ACTIVO] ?: 0
}

data class EnviosAdminState(
    val cargando: Boolean = true,
    val busqueda: String = "",
    // null = todos los estados
    val filtro: EstadoEnvio? = null,
    val envios: List<Envio> = emptyList()
) {
    val hayFiltros: Boolean get() = busqueda.isNotBlank() || filtro != null
}

/**
 * Formulario de edición de un envío. Reutiliza SolicitudEnvioUiState para tener exactamente
 * las mismas validaciones que al solicitar un envío (nombres, teléfonos y dirección).
 * Origen, destino, servicio, peso y valor no se editan.
 */
data class EdicionEnvioState(
    val envio: Envio,
    val datos: SolicitudEnvioUiState
)

@OptIn(ExperimentalCoroutinesApi::class)
class PanelAdminViewModel(
    private val envioRepository: EnvioRepository,
    bodegaRepository: BodegaRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    // ---------- Resumen ----------

    val resumen: StateFlow<ResumenAdminState> = combine(
        envioRepository.contarEnviosPorEstado(),
        envioRepository.observarValorTotal(),
        bodegaRepository.observarClientes(),
        bodegaRepository.contarContratosPorEstado()
    ) { conteoEnvios, valorTotal, clientes, conteoContratos ->
        val porEstado = EstadoEnvio.entries.associateWith { estado ->
            conteoEnvios.find { it.estado == estado }?.cantidad ?: 0
        }
        ResumenAdminState(
            cargando = false,
            totalEnvios = porEstado.values.sum(),
            enviosPorEstado = porEstado,
            valorTotal = valorTotal,
            clientes = clientes.size,
            clientesConCuenta = clientes.count { it.contrasena != null },
            contratosPorEstado = EstadoContrato.entries.associateWith { estado ->
                conteoContratos.find { it.estado == estado }?.cantidad ?: 0
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenAdminState())

    // ---------- Envíos: búsqueda + filtro ----------

    private val busqueda = MutableStateFlow("")
    private val filtro = MutableStateFlow<EstadoEnvio?>(null)

    val envios: StateFlow<EnviosAdminState> = combine(busqueda, filtro) { texto, estado -> texto to estado }
        .flatMapLatest { (texto, estado) ->
            val consulta = texto.trim()
            val base = when {
                // Con texto se busca en guía, remitente o destinatario y luego se aplica el estado
                consulta.isNotEmpty() -> envioRepository.buscarEnvios(consulta).map { lista ->
                    if (estado == null) lista else lista.filter { it.estado == estado }
                }
                estado != null -> envioRepository.observarEnviosPorEstado(estado)
                else -> envioRepository.observarEnvios()
            }
            base.map { EnviosAdminState(cargando = false, busqueda = texto, filtro = estado, envios = it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EnviosAdminState())

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto.take(40)
    }

    fun onFiltroChange(estado: EstadoEnvio?) {
        filtro.value = estado
    }

    // ---------- Envío seleccionado (hoja de detalle) ----------

    private val _guiaSeleccionada = MutableStateFlow<String?>(null)
    val guiaSeleccionada: StateFlow<String?> = _guiaSeleccionada.asStateFlow()

    // Se observa en Room: la línea de tiempo se actualiza sola al cambiar el estado
    val detalle: StateFlow<EnvioConMovimientos?> = _guiaSeleccionada
        .flatMapLatest { guia -> if (guia == null) flowOf(null) else envioRepository.rastrear(guia) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun seleccionarEnvio(envio: Envio) {
        _edicion.value = null
        _guiaSeleccionada.value = envio.guia
    }

    fun cerrarDetalle() {
        _edicion.value = null
        _guiaSeleccionada.value = null
    }

    fun cambiarEstado(envio: Envio, estado: EstadoEnvio) {
        viewModelScope.launch { envioRepository.cambiarEstado(envio, estado) }
    }

    fun eliminarEnvio(envio: Envio) {
        cerrarDetalle()
        viewModelScope.launch { envioRepository.eliminarEnvio(envio) }
    }

    // ---------- Edición ----------

    private val _edicion = MutableStateFlow<EdicionEnvioState?>(null)
    val edicion: StateFlow<EdicionEnvioState?> = _edicion.asStateFlow()

    fun abrirEdicion(envio: Envio) {
        _edicion.value = EdicionEnvioState(
            envio = envio,
            datos = SolicitudEnvioUiState(
                remitente = envio.remitente,
                telefonoRemitente = envio.telefonoRemitente,
                destinatario = envio.destinatario,
                telefonoDestinatario = envio.telefonoDestinatario,
                direccionDestino = envio.direccionDestino
            )
        )
    }

    fun cancelarEdicion() {
        if (_edicion.value?.datos?.guardando == true) return
        _edicion.value = null
    }

    private fun editarDatos(cambio: (SolicitudEnvioUiState) -> SolicitudEnvioUiState) =
        _edicion.update { it?.copy(datos = cambio(it.datos).copy(errorGuardado = null)) }

    private fun soloDigitos(valor: String) = valor.filter(Char::isDigit).take(10)

    fun onRemitenteChange(valor: String) = editarDatos { it.copy(remitente = valor.take(60)) }
    fun onTelefonoRemitenteChange(valor: String) = editarDatos { it.copy(telefonoRemitente = soloDigitos(valor)) }
    fun onDestinatarioChange(valor: String) = editarDatos { it.copy(destinatario = valor.take(60)) }
    fun onTelefonoDestinatarioChange(valor: String) = editarDatos { it.copy(telefonoDestinatario = soloDigitos(valor)) }
    fun onDireccionChange(valor: String) = editarDatos { it.copy(direccionDestino = valor.take(100)) }

    fun guardarEdicion() {
        val actual = _edicion.value ?: return
        val datos = actual.datos
        if (datos.guardando || !datos.formularioValido) return
        _edicion.update { it?.copy(datos = it.datos.copy(guardando = true, errorGuardado = null)) }

        viewModelScope.launch {
            try {
                // Se parte del envío más reciente para no pisar un cambio de estado hecho mientras tanto
                val base = detalle.value?.envio?.takeIf { it.id == actual.envio.id } ?: actual.envio
                envioRepository.editarEnvio(
                    base.copy(
                        remitente = datos.remitente.trim(),
                        telefonoRemitente = datos.telefonoRemitente,
                        destinatario = datos.destinatario.trim(),
                        telefonoDestinatario = datos.telefonoDestinatario,
                        direccionDestino = datos.direccionDestino.trim()
                    )
                )
                // Vuelve al detalle
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

    // ---------- Sesión ----------

    fun cerrarSesion() {
        viewModelScope.launch { sesionRepository.cerrarSesion() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                PanelAdminViewModel(app.envioRepository, app.bodegaRepository, app.sesionRepository)
            }
        }
    }
}
