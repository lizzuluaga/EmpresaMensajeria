package com.example.empresamensajeria.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Un solo archivo "sesion" para toda la app
private val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

class SesionRepository(context: Context) {

    private val dataStore = context.applicationContext.sesionDataStore

    private object Claves {
        val ROL = stringPreferencesKey("rol")
        val IDENTIFICADOR = stringPreferencesKey("identificador")
        val NOMBRE = stringPreferencesKey("nombre")
    }

    /** La sesión guardada, o null si no hay ninguna. */
    val sesionActual: Flow<Sesion?> = dataStore.data
        .catch { error ->
            // Si el archivo no se puede leer se trata como "sin sesión"
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias ->
            val rol = preferencias[Claves.ROL]?.let { valor -> Rol.entries.find { it.name == valor } }
            val identificador = preferencias[Claves.IDENTIFICADOR]
            if (rol == null || identificador.isNullOrBlank()) {
                null
            } else {
                Sesion(rol, identificador, preferencias[Claves.NOMBRE].orEmpty())
            }
        }

    suspend fun guardarSesion(sesion: Sesion) {
        dataStore.edit { preferencias ->
            preferencias[Claves.ROL] = sesion.rol.name
            preferencias[Claves.IDENTIFICADOR] = sesion.identificador
            preferencias[Claves.NOMBRE] = sesion.nombre
        }
    }

    suspend fun cerrarSesion() {
        dataStore.edit { it.clear() }
    }
}
