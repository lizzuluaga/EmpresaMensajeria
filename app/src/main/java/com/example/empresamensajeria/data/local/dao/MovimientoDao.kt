package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.empresamensajeria.data.local.entity.Movimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    // Del más reciente al más antiguo, como la línea de tiempo de Rastrear
    @Query("SELECT * FROM movimientos WHERE envioId = :envioId ORDER BY fecha DESC")
    fun observarPorEnvio(envioId: Long): Flow<List<Movimiento>>
}
