package com.example.empresamensajeria.navigation

import com.example.empresamensajeria.data.session.Rol

object Rutas {
    const val SERVICIOS = "servicios"

    //esto es para q lea el id por ende el precio base y asi pueda dar con el precio
    const val COTIZAR = "cotizar/{servicioId}"

    // Ruta base (la usa el menú lateral) y ruta con la guía opcional para buscarla al abrir
    const val RASTREAR = "rastrear"
    const val ARG_GUIA = "guia"
    const val RASTREAR_CON_GUIA = "rastrear?$ARG_GUIA={$ARG_GUIA}"
    const val CONTACTO = "contacto"
    // Ruta base (menú lateral) y ruta con la pestaña opcional (CLIENTE o ADMIN) para abrir el login
    const val LOGIN = "login"
    const val ARG_PESTANA = "pestana"
    const val LOGIN_CON_PESTANA = "login?$ARG_PESTANA={$ARG_PESTANA}"

    // Paneles: solo se llega a ellos iniciando sesión (no son opciones fijas del menú)
    const val MI_BODEGA = "mi_bodega"
    const val PANEL_ADMIN = "panel_admin"

    // Inventario de un contrato (se abre desde Mi bodega)
    const val ARG_CONTRATO_ID = "contratoId"
    const val INVENTARIO = "inventario/{$ARG_CONTRATO_ID}"

    fun cotizar(servicioId: Int) = "cotizar/$servicioId"

    fun inventario(contratoId: Long) = "inventario/$contratoId"

    fun rastrear(guia: String) = "rastrear?$ARG_GUIA=$guia"

    fun login(rol: Rol) = "login?$ARG_PESTANA=${rol.name}"

    fun panelDe(rol: Rol) = when (rol) {
        Rol.CLIENTE -> MI_BODEGA
        Rol.ADMIN -> PANEL_ADMIN
    }
}
