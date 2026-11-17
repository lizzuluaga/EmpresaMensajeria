package com.example.empresamensajeria.data.local.dao

import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.entity.EstadoEnvio

// Resultados de las consultas GROUP BY para las estadísticas del admin
data class ConteoContratos(
    val estado: EstadoContrato,
    val cantidad: Int
)

data class ConteoEnvios(
    val estado: EstadoEnvio,
    val cantidad: Int
)
