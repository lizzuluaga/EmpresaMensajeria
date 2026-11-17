package com.example.empresamensajeria.data.local.entity

// El orden importa: se usa para la barra de progreso del rastreo
enum class EstadoEnvio(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    RECIBIDO("Recibido"),
    EN_TRANSITO("En tránsito"),
    EN_REPARTO("En reparto"),
    ENTREGADO("Entregado")
}
