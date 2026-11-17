package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.relation.ContratoConArticulos
import kotlinx.coroutines.flow.Flow

@Dao
interface ContratoBodegaDao {

    @Insert
    suspend fun insertar(contrato: ContratoBodega): Long

    @Update
    suspend fun actualizar(contrato: ContratoBodega)

    @Delete
    suspend fun eliminar(contrato: ContratoBodega)

    @Query("UPDATE contratos_bodega SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoContrato)

    @Query("SELECT * FROM contratos_bodega WHERE documentoCliente = :documento ORDER BY fechaInicio DESC")
    fun observarPorCliente(documento: String): Flow<List<ContratoBodega>>

    @Query("SELECT * FROM contratos_bodega ORDER BY fechaInicio DESC")
    fun observarTodos(): Flow<List<ContratoBodega>>

    @Query("SELECT * FROM contratos_bodega WHERE estado = :estado ORDER BY fechaInicio DESC")
    fun observarPorEstado(estado: EstadoContrato): Flow<List<ContratoBodega>>

    @Transaction
    @Query("SELECT * FROM contratos_bodega WHERE id = :id")
    fun observarContratoConArticulos(id: Long): Flow<ContratoConArticulos?>

    @Query("SELECT estado, COUNT(*) AS cantidad FROM contratos_bodega GROUP BY estado")
    fun contarPorEstado(): Flow<List<ConteoContratos>>
}
