package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contratos_bodega",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["documento"],
            childColumns = ["documentoCliente"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentoCliente")]
)
data class ContratoBodega(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentoCliente: String,
    // Nombre del EspacioBodega cotizado: "Pequeño", "Mediano" o "Grande"
    val espacio: String,
    val dias: Int,
    // Total de la cotización en pesos
    val valor: Int,
    // Milisegundos
    val fechaInicio: Long,
    val estado: EstadoContrato = EstadoContrato.ACTIVO
)
