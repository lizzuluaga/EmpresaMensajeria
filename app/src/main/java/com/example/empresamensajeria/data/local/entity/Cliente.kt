package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey
    val documento: String,
    val nombre: String,
    val telefono: String,
    // null hasta que el cliente cree su cuenta
    val contrasena: String? = null
)
