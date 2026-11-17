package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articulos",
    foreignKeys = [
        ForeignKey(
            entity = ContratoBodega::class,
            parentColumns = ["id"],
            childColumns = ["contratoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contratoId")]
)
data class Articulo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contratoId: Long,
    val nombre: String,
    val descripcion: String,
    val cantidad: Int,
    // Milisegundos
    val fechaIngreso: Long
)
