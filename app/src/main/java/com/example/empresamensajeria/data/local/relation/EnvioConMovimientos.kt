package com.example.empresamensajeria.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.Movimiento

data class EnvioConMovimientos(
    @Embedded val envio: Envio,
    @Relation(
        parentColumn = "id",
        entityColumn = "envioId"
    )
    val movimientos: List<Movimiento>
)
