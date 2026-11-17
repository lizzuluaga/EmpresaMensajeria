package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = Envio::class,
            parentColumns = ["id"],
            childColumns = ["envioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("envioId")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val envioId: Long,
    // Milisegundos
    val fecha: Long,
    val ubicacion: String,
    val descripcion: String
)
