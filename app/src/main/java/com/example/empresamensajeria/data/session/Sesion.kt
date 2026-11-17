package com.example.empresamensajeria.data.session

/**
 * Sesión iniciada que se recuerda al cerrar y abrir la app.
 * identificador: documento del cliente o usuario del administrador.
 */
data class Sesion(
    val rol: Rol,
    val identificador: String,
    val nombre: String
)
