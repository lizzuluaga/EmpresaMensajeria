package com.example.empresamensajeria.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.entity.ContratoBodega

data class ClienteConContratos(
    @Embedded val cliente: Cliente,
    @Relation(
        parentColumn = "documento",
        entityColumn = "documentoCliente"
    )
    val contratos: List<ContratoBodega>
)
