package com.example.empresamensajeria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.empresamensajeria.EnviaYaApplication
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.session.Rol
import com.example.empresamensajeria.data.session.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MiBodegaUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val contratos: List<ContratoBodega> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MiBodegaViewModel(
    sesionRepository: SesionRepository,
    bodegaRepository: BodegaRepository
) : ViewModel() {

    // El documento sale de la sesión guardada; los contratos se observan en Room
    val uiState: StateFlow<MiBodegaUiState> = sesionRepository.sesionActual
        .flatMapLatest { sesion ->
            if (sesion == null || sesion.rol != Rol.CLIENTE) {
                flowOf(MiBodegaUiState(cargando = false))
            } else {
                bodegaRepository.observarContratosDeCliente(sesion.identificador).map { contratos ->
                    MiBodegaUiState(cargando = false, nombre = sesion.nombre, contratos = contratos)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MiBodegaUiState()
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnviaYaApplication
                MiBodegaViewModel(app.sesionRepository, app.bodegaRepository)
            }
        }
    }
}
