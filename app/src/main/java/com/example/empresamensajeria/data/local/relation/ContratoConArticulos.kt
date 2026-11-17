package com.example.empresamensajeria.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.empresamensajeria.data.local.entity.Articulo
import com.example.empresamensajeria.data.local.entity.ContratoBodega

data class ContratoConArticulos(
    @Embedded val contrato: ContratoBodega,
    @Relation(
        parentColumn = "id",
        entityColumn = "contratoId"
    )
    val articulos: List<Articulo>
)
