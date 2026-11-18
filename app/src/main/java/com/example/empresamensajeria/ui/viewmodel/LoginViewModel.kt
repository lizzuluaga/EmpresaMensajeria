package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.repository.AdminRepository
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.repository.ResultadoRegistro
import com.example.empresamensajeria.data.session.Rol
import com.example.empresamensajeria.data.session.Sesion
import com.example.empresamensajeria.data.session.SesionRepository
import com.example.empresamensajeria.navigation.Rutas
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ModoLogin { INICIAR_SESION, REGISTRARSE }

enum class PestanaLogin { CLIENTE, ADMIN }

private const val MINIMO_CONTRASENA = 6
private const val MENSAJE_CREDENCIALES = "Usuario o contraseña inválidos"
private const val MENSAJE_ERROR_GENERAL = "Ocurrió un error. Intenta de nuevo."

data class LoginUiState(
    val modo: ModoLogin = ModoLogin.INICIAR_SESION,
    val pestana: PestanaLogin = PestanaLogin.CLIENTE,
    val documento: String = "",
    val usuario: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    // Registro: nombre del cliente cuando su documento ya fue verificado (habilita las contraseñas)
    val nombreVerificado: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val documentoValido: Boolean get() = documento.length in 6..10

    val errorDocumento: String?
        get() = if (documento.isNotEmpty() && !documentoValido) {
            "El documento debe tener entre 6 y 10 dígitos"
        } else null

    // Solo se validan al crear la cuenta, no al iniciar sesión
    val errorContrasenaNueva: String?
        get() = if (contrasena.isNotEmpty() && contrasena.length < MINIMO_CONTRASENA) {
            "Mínimo $MINIMO_CONTRASENA caracteres"
        } else null

    val errorConfirmacion: String?
        get() = if (confirmacion.isNotEmpty() && confirmacion != contrasena) {
            "Las contraseñas no coinciden"
        } else null

    val puedeIngresar: Boolean
        get() = !cargando && contrasena.isNotEmpty() && when (pestana) {
            PestanaLogin.CLIENTE -> documento.isNotEmpty()
            PestanaLogin.ADMIN -> usuario.isNotBlank()
        }

    val puedeVerificar: Boolean get() = !cargando && documentoValido

    val puedeCrearCuenta: Boolean
        get() = !cargando && nombreVerificado != null &&
            contrasena.length >= MINIMO_CONTRASENA && confirmacion == contrasena
}

class LoginViewModel(
    private val bodegaRepository: BodegaRepository,
    private val adminRepository: AdminRepository,
    private val sesionRepository: SesionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Pestaña inicial según la ruta (por ejemplo, al cerrar sesión como admin se abre "Administrador")
    private val pestanaInicial: PestanaLogin =
        if (savedStateHandle.get<String>(Rutas.ARG_PESTANA) == Rol.ADMIN.name) PestanaLogin.ADMIN
        else PestanaLogin.CLIENTE

    private val _uiState = MutableStateFlow(LoginUiState(pestana = pestanaInicial))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Evento de un solo uso: la pantalla lo recibe y navega según el rol
    private val _sesionIniciada = Channel<Rol>(Channel.BUFFERED)
    val sesionIniciada: Flow<Rol> = _sesionIniciada.receiveAsFlow()

    // ---------- Cambios de modo y pestaña ----------

    fun cambiarPestana(pestana: PestanaLogin) {
        if (_uiState.value.cargando) return
        _uiState.value = LoginUiState(pestana = pestana)
    }

    fun cambiarModo(modo: ModoLogin) {
        if (_uiState.value.cargando) return
        // El registro es solo para clientes; se conserva el documento escrito
        _uiState.update {
            LoginUiState(modo = modo, pestana = PestanaLogin.CLIENTE, documento = it.documento)
        }
    }

    // ---------- Campos ----------

    fun onDocumentoChange(valor: String) = _uiState.update {
        val nuevo = valor.filter(Char::isDigit).take(10)
        // Si cambia el documento ya verificado, hay que volver a verificarlo
        val cambio = nuevo != it.documento
        it.copy(
            documento = nuevo,
            nombreVerificado = if (cambio) null else it.nombreVerificado,
            contrasena = if (cambio && it.modo == ModoLogin.REGISTRARSE) "" else it.contrasena,
            confirmacion = if (cambio) "" else it.confirmacion,
            error = null
        )
    }

    fun onUsuarioChange(valor: String) =
        _uiState.update { it.copy(usuario = valor.take(40), error = null) }

    fun onContrasenaChange(valor: String) =
        _uiState.update { it.copy(contrasena = valor.take(40), error = null) }

    fun onConfirmacionChange(valor: String) =
        _uiState.update { it.copy(confirmacion = valor.take(40), error = null) }

    // ---------- Iniciar sesión ----------

    fun iniciarSesion() {
        val estado = _uiState.value
        if (!estado.puedeIngresar) return
        ejecutar {
            val sesion = when (estado.pestana) {
                PestanaLogin.CLIENTE ->
                    bodegaRepository.iniciarSesionCliente(estado.documento, estado.contrasena)
                        ?.let { Sesion(Rol.CLIENTE, it.documento, it.nombre) }

                PestanaLogin.ADMIN ->
                    if (adminRepository.iniciarSesionAdmin(estado.usuario, estado.contrasena)) {
                        Sesion(Rol.ADMIN, estado.usuario.trim(), "Administrador")
                    } else null
            }
            if (sesion == null) {
                _uiState.update { it.copy(cargando = false, error = MENSAJE_CREDENCIALES) }
            } else {
                entrar(sesion)
            }
        }
    }

    // ---------- Registrarse ----------

    /** Paso 1: comprueba que el documento esté habilitado y aún no tenga cuenta. */
    fun verificarDocumento() {
        val estado = _uiState.value
        if (!estado.puedeVerificar) return
        ejecutar {
            val cliente = bodegaRepository.buscarCliente(estado.documento)
            _uiState.update {
                when {
                    cliente == null -> it.copy(
                        cargando = false,
                        error = "Tu documento no está habilitado. Contrata un servicio de bodega para crear tu cuenta."
                    )
                    cliente.contrasena != null -> it.copy(
                        cargando = false,
                        error = "Este documento ya tiene una cuenta. Inicia sesión."
                    )
                    else -> it.copy(cargando = false, nombreVerificado = cliente.nombre)
                }
            }
        }
    }

    /** Paso 2: crea la contraseña y entra directamente. */
    fun crearCuenta() {
        val estado = _uiState.value
        val nombre = estado.nombreVerificado
        if (!estado.puedeCrearCuenta || nombre == null) return
        ejecutar {
            when (bodegaRepository.registrarCuenta(estado.documento, estado.contrasena)) {
                ResultadoRegistro.Exito -> entrar(Sesion(Rol.CLIENTE, estado.documento, nombre))
                ResultadoRegistro.NoHabilitado -> _uiState.update {
                    it.copy(
                        cargando = false,
                        nombreVerificado = null,
                        error = "Tu documento no está habilitado. Contrata un servicio de bodega para crear tu cuenta."
                    )
                }
                ResultadoRegistro.YaTieneCuenta -> _uiState.update {
                    it.copy(
                        cargando = false,
                        nombreVerificado = null,
                        error = "Este documento ya tiene una cuenta. Inicia sesión."
                    )
                }
            }
        }
    }

    // ---------- Auxiliares ----------

    private suspend fun entrar(sesion: Sesion) {
        sesionRepository.guardarSesion(sesion)
        // Se deja el formulario limpio por si se vuelve a esta pantalla
        _uiState.value = LoginUiState()
        _sesionIniciada.send(sesion.rol)
    }

    // Marca "cargando" (evita el doble toque) y muestra un error genérico si algo falla
    private fun ejecutar(bloque: suspend () -> Unit) {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                bloque()
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = MENSAJE_ERROR_GENERAL) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                LoginViewModel(
                    app.bodegaRepository,
                    app.adminRepository,
                    app.sesionRepository,
                    createSavedStateHandle()
                )
            }
        }
    }
}
