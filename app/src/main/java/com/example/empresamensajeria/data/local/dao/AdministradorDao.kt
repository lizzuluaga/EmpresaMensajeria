package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.empresamensajeria.data.local.entity.Administrador

@Dao
interface AdministradorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(administrador: Administrador)

    @Query("SELECT * FROM administradores WHERE usuario = :usuario AND contrasena = :contrasena LIMIT 1")
    suspend fun validar(usuario: String, contrasena: String): Administrador?
}
