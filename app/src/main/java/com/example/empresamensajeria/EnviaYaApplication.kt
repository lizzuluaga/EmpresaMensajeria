package com.example.empresamensajeria

import android.app.Application
import com.example.empresamensajeria.data.local.EnviaYaDatabase
import com.example.empresamensajeria.data.repository.AdminRepository
import com.example.empresamensajeria.data.repository.BodegaRepository
import com.example.empresamensajeria.data.repository.EnvioRepository
import com.example.empresamensajeria.data.session.SesionRepository

/**
 * Contenedor sencillo de dependencias (sin Hilt).
 * Todo se crea la primera vez que se usa y vive mientras viva la app.
 */
class EnviaYaApplication : Application() {

    val database: EnviaYaDatabase by lazy { EnviaYaDatabase.getDatabase(this) }

    val bodegaRepository: BodegaRepository by lazy { BodegaRepository(database) }

    val envioRepository: EnvioRepository by lazy { EnvioRepository(database) }

    val adminRepository: AdminRepository by lazy { AdminRepository(database.administradorDao()) }

    val sesionRepository: SesionRepository by lazy { SesionRepository(this) }
}
