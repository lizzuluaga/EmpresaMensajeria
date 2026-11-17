package com.example.empresamensajeria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "administradores")
data class Administrador(
    @PrimaryKey
    val usuario: String,
    val contrasena: String
)
