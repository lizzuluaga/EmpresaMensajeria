package com.example.empresamensajeria.data.local.entity

// Room guarda los enums como texto (el nombre de la constante), no hace falta TypeConverter
enum class EstadoContrato(val etiqueta: String) {
    ACTIVO("Activo"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado")
}
