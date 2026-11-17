package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.empresamensajeria.data.local.entity.Articulo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticuloDao {

    @Insert
    suspend fun insertar(articulo: Articulo): Long

    @Update
    suspend fun actualizar(articulo: Articulo)

    @Delete
    suspend fun eliminar(articulo: Articulo)

    @Query("SELECT * FROM articulos WHERE contratoId = :contratoId ORDER BY fechaIngreso DESC")
    fun observarPorContrato(contratoId: Long): Flow<List<Articulo>>
}
