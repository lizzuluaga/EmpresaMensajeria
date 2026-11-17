package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "envios",
    indices = [Index(value = ["guia"], unique = true)]
)
data class Envio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // "EY" + número, por ejemplo EY1006
    val guia: String,
    val remitente: String,
    val telefonoRemitente: String,
    val destinatario: String,
    val telefonoDestinatario: String,
    val direccionDestino: String,
    val origen: String,
    val destino: String,
    // Título del Servicio cotizado, por ejemplo "Envío nacional"
    val servicio: String,
    // Kilogramos (pesoKg en la cotización)
    val peso: Double,
    // Total de la cotización en pesos
    val valor: Int,
    val estado: EstadoEnvio = EstadoEnvio.PENDIENTE,
    // Milisegundos
    val fecha: Long
)
