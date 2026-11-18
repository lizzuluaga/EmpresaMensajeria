package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.session.Rol
import com.example.empresamensajeria.data.session.Sesion
import com.example.empresamensajeria.data.session.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    // Todavía se está leyendo (y validando) la sesión guardada
    object Cargando : EstadoSesion

    data class Lista(val sesion: Sesion?) : EstadoSesion
}

/** Sesión de toda la app (vive a nivel de AppNavegacion). */
class SesionViewModel(
    private val sesionRepository: SesionRepository,
    private val bodegaRepository: BodegaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            // Al abrir la app: si el cliente guardado ya no existe o no tiene cuenta
            // (el admin pudo haberlo eliminado), se cierra la sesión antes de mostrar nada
            val guardada = sesionRepository.sesionActual.first()
            if (guardada?.rol == Rol.CLIENTE &&
                !bodegaRepository.clienteTieneCuentaActiva(guardada.identificador)
            ) {
                sesionRepository.cerrarSesion()
            }
            // Desde aquí se sigue cualquier cambio (iniciar o cerrar sesión)
            sesionRepository.sesionActual.collect { _estado.value = EstadoSesion.Lista(it) }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { sesionRepository.cerrarSesion() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                SesionViewModel(app.sesionRepository, app.bodegaRepository)
            }
        }
    }
}
