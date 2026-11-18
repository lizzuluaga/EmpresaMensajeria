package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.local.relation.EnvioConMovimientos
import com.example.empresamensajeria.data.repository.EnvioRepository
import com.example.empresamensajeria.navigation.Rutas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ResultadoRastreo {
    object Inicial : ResultadoRastreo
    data class NoEncontrado(val guia: String) : ResultadoRastreo
    data class Encontrado(val envio: EnvioConMovimientos) : ResultadoRastreo
}

data class RastrearUiState(
    val guia: String = "",
    val resultado: ResultadoRastreo = ResultadoRastreo.Inicial
) {
    val guiaValida: Boolean get() = FORMATO_GUIA.matches(guia)
    val formatoIncorrecto: Boolean get() = guia.isNotEmpty() && !guiaValida
}

private val FORMATO_GUIA = Regex("^EY\\d{4}$")

@OptIn(ExperimentalCoroutinesApi::class)
class RastrearViewModel(
    private val repository: EnvioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val guiasEjemplo = listOf("EY1001", "EY1002", "EY1003", "EY1004", "EY1005")

    private val guia = MutableStateFlow("")

    // Guía que se está consultando; null mientras no se haya buscado nada
    private val guiaBuscada = MutableStateFlow<String?>(null)

    // flatMapLatest: cada búsqueda cancela la anterior y queda observando Room,
    // así si el admin cambia el estado del envío la pantalla se actualiza sola
    private val resultado = guiaBuscada.flatMapLatest { buscada ->
        if (buscada == null) {
            flowOf(ResultadoRastreo.Inicial)
        } else {
            repository.rastrear(buscada).map { envio ->
                if (envio == null) ResultadoRastreo.NoEncontrado(buscada)
                else ResultadoRastreo.Encontrado(envio)
            }
        }
    }

    val uiState: StateFlow<RastrearUiState> = combine(guia, resultado) { texto, res ->
        RastrearUiState(guia = texto, resultado = res)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RastrearUiState()
    )

    init {
        // Si se llegó con una guía en la ruta (por ejemplo desde "Rastrear ahora"), se busca de una vez
        val guiaInicial = savedStateHandle.get<String>(Rutas.ARG_GUIA).orEmpty().trim().uppercase()
        if (FORMATO_GUIA.matches(guiaInicial)) buscarEjemplo(guiaInicial)
    }

    fun onGuiaChange(entrada: String) {
        guia.value = entrada.filter { it.isLetterOrDigit() }.take(6).uppercase()
        // Al editar la guía se limpia el resultado anterior
        guiaBuscada.value = null
    }

    fun buscar() {
        val texto = guia.value
        if (FORMATO_GUIA.matches(texto)) guiaBuscada.value = texto
    }

    fun buscarEjemplo(ejemplo: String) {
        guia.value = ejemplo
        guiaBuscada.value = ejemplo
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                RastrearViewModel(app.envioRepository, createSavedStateHandle())
            }
        }
    }
}
