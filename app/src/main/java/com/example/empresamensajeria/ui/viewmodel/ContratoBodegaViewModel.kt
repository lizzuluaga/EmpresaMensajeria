package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.repository.ResultadoContrato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContratoBodegaUiState(
    val documento: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val guardando: Boolean = false,
    // Resultado al guardar; mientras no sea null se muestra el diálogo de confirmación
    val resultado: ResultadoContrato? = null,
    val errorGuardado: String? = null
) {
    // Los errores solo se muestran cuando el campo ya tiene algo escrito
    val errorDocumento: String?
        get() = if (documento.isNotEmpty() && !documentoValido(documento)) {
            "El documento debe tener entre 6 y 10 dígitos"
        } else null

    val errorNombre: String?
        get() = if (nombre.isNotEmpty() && !nombreValido(nombre)) "Escribe al menos 3 letras" else null

    val errorTelefono: String?
        get() = if (telefono.isNotEmpty() && !telefonoValido(telefono)) {
            "El teléfono debe tener 10 dígitos"
        } else null

    val formularioValido: Boolean
        get() = documentoValido(documento) && nombreValido(nombre) && telefonoValido(telefono)
}

private fun documentoValido(documento: String) =
    documento.length in 6..10 && documento.all { it.isDigit() }

private fun nombreValido(nombre: String) = nombre.count { it.isLetter() } >= 3

private fun telefonoValido(telefono: String) = telefono.length == 10 && telefono.all { it.isDigit() }

class ContratoBodegaViewModel(private val repository: BodegaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ContratoBodegaUiState())
    val uiState: StateFlow<ContratoBodegaUiState> = _uiState.asStateFlow()

    fun onDocumentoChange(valor: String) =
        _uiState.update { it.copy(documento = valor.filter(Char::isDigit).take(10), errorGuardado = null) }

    fun onNombreChange(valor: String) =
        _uiState.update { it.copy(nombre = valor.take(60), errorGuardado = null) }

    fun onTelefonoChange(valor: String) =
        _uiState.update { it.copy(telefono = valor.filter(Char::isDigit).take(10), errorGuardado = null) }

    /**
     * Con un cliente con sesión iniciada, llena el formulario con sus datos guardados.
     * Solo si el formulario está vacío, para no borrar lo que el usuario ya haya escrito.
     * Los campos siguen siendo editables.
     */
    fun prellenarConCliente(documento: String) {
        val estado = _uiState.value
        val vacio = estado.documento.isEmpty() && estado.nombre.isEmpty() && estado.telefono.isEmpty()
        if (!vacio) return

        viewModelScope.launch {
            val cliente = try {
                repository.buscarCliente(documento)
            } catch (e: Exception) {
                null
            } ?: return@launch
            _uiState.update {
                // Se vuelve a comprobar por si el usuario empezó a escribir mientras se buscaba
                if (it.documento.isEmpty() && it.nombre.isEmpty() && it.telefono.isEmpty()) {
                    it.copy(
                        documento = cliente.documento,
                        nombre = cliente.nombre,
                        telefono = cliente.telefono
                    )
                } else it
            }
        }
    }

    fun contratar(espacio: String, dias: Int, valor: Int) {
        val estado = _uiState.value
        // Evita el doble toque y los contratos con datos incompletos
        if (estado.guardando || !estado.formularioValido) return
        _uiState.update { it.copy(guardando = true, errorGuardado = null) }

        viewModelScope.launch {
            try {
                val resultado = repository.contratarServicio(
                    documento = estado.documento,
                    nombre = estado.nombre,
                    telefono = estado.telefono,
                    espacio = espacio,
                    dias = dias,
                    valor = valor
                )
                _uiState.update { it.copy(guardando = false, resultado = resultado) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGuardado = "No se pudo registrar la contratación. Intenta de nuevo."
                    )
                }
            }
        }
    }

    /** Deja el formulario vacío (también oculta el diálogo de confirmación). */
    fun limpiar() {
        _uiState.value = ContratoBodegaUiState()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                ContratoBodegaViewModel(app.bodegaRepository)
            }
        }
    }
}
