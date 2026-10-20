package com.example.empresamensajeria.navigation

object Rutas {
    const val SERVICIOS = "servicios"

    //esto es para q lea el id por ende el precio base y asi pueda dar con el precio
    const val COTIZAR = "cotizar/{servicioId}"

    const val RASTREAR = "rastrear"
    const val CONTACTO = "contacto"
    const val LOGIN = "login"

    fun cotizar(servicioId: Int) = "cotizar/$servicioId"
}
