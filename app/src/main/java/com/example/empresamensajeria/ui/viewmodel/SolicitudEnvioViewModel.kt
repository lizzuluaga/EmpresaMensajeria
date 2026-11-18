package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.repository.EnvioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SolicitudEnvioUiState(
    val remitente: String = "",
    val telefonoRemitente: String = "",
    val destinatario: String = "",
    val telefonoDestinatario: String = "",
    val direccionDestino: String = "",
    val guardando: Boolean = false,
    // Guía creada al guardar; mientras no sea null se muestra el diálogo de confirmación
    val guiaGenerada: String? = null,
    val errorGuardado: String? = null
) {
    // Los errores solo se muestran cuando el campo ya tiene algo escrito
    val errorRemitente: String? get() = errorNombre(remitente)
    val errorTelefonoRemitente: String? get() = errorTelefono(telefonoRemitente)
    val errorDestinatario: String? get() = errorNombre(destinatario)
    val errorTelefonoDestinatario: String? get() = errorTelefono(telefonoDestinatario)
    val errorDireccion: String? get() =
        if (direccionDestino.isNotEmpty() && direccionDestino.isBlank()) "Escribe la dirección de entrega" else null

    val formularioValido: Boolean
        get() = nombreValido(remitente) && telefonoValido(telefonoRemitente) &&
            nombreValido(destinatario) && telefonoValido(telefonoDestinatario) &&
            direccionDestino.isNotBlank()
}

private fun nombreValido(nombre: String) = nombre.count { it.isLetter() } >= 3

private fun telefonoValido(telefono: String) = telefono.length == 10 && telefono.all { it.isDigit() }

private fun errorNombre(nombre: String): String? =
    if (nombre.isNotEmpty() && !nombreValido(nombre)) "Escribe al menos 3 letras" else null

private fun errorTelefono(telefono: String): String? =
    if (telefono.isNotEmpty() && !telefonoValido(telefono)) "El teléfono debe tener 10 dígitos" else null

class SolicitudEnvioViewModel(private val repository: EnvioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudEnvioUiState())
    val uiState: StateFlow<SolicitudEnvioUiState> = _uiState.asStateFlow()

    fun onRemitenteChange(valor: String) =
        _uiState.update { it.copy(remitente = valor.take(60), errorGuardado = null) }

    fun onTelefonoRemitenteChange(valor: String) =
        _uiState.update { it.copy(telefonoRemitente = soloDigitos(valor), errorGuardado = null) }

    fun onDestinatarioChange(valor: String) =
        _uiState.update { it.copy(destinatario = valor.take(60), errorGuardado = null) }

    fun onTelefonoDestinatarioChange(valor: String) =
        _uiState.update { it.copy(telefonoDestinatario = soloDigitos(valor), errorGuardado = null) }

    fun onDireccionChange(valor: String) =
        _uiState.update { it.copy(direccionDestino = valor.take(100), errorGuardado = null) }

    private fun soloDigitos(valor: String) = valor.filter { it.isDigit() }.take(10)

    fun solicitar(origen: String, destino: String, servicio: String, peso: Double, valor: Int) {
        val estado = _uiState.value
        // Evita el doble toque y los envíos con datos incompletos
        if (estado.guardando || !estado.formularioValido) return
        _uiState.update { it.copy(guardando = true, errorGuardado = null) }

        viewModelScope.launch {
            try {
                val guia = repository.solicitarEnvio(
                    remitente = estado.remitente,
                    telefonoRemitente = estado.telefonoRemitente,
                    destinatario = estado.destinatario,
                    telefonoDestinatario = estado.telefonoDestinatario,
                    direccionDestino = estado.direccionDestino,
                    origen = origen,
                    destino = destino,
                    servicio = servicio,
                    peso = peso,
                    valor = valor
                )
                _uiState.update { it.copy(guardando = false, guiaGenerada = guia) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGuardado = "No se pudo registrar la solicitud. Intenta de nuevo."
                    )
                }
            }
        }
    }

    /** Deja el formulario vacío (también oculta el diálogo de la guía). */
    fun limpiar() {
        _uiState.value = SolicitudEnvioUiState()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                SolicitudEnvioViewModel(app.envioRepository)
            }
        }
    }
}
