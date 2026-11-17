package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.EstadoEnvio
import com.example.empresamensajeria.data.local.relation.EnvioConMovimientos
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvioDao {

    @Insert
    suspend fun insertar(envio: Envio): Long

    @Update
    suspend fun actualizar(envio: Envio)

    @Delete
    suspend fun eliminar(envio: Envio)

    @Query("UPDATE envios SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoEnvio)

    // Para Rastrear: la guía se compara sin importar mayúsculas
    @Transaction
    @Query("SELECT * FROM envios WHERE guia = :guia COLLATE NOCASE LIMIT 1")
    fun buscarPorGuia(guia: String): Flow<EnvioConMovimientos?>

    @Query("SELECT EXISTS(SELECT 1 FROM envios WHERE guia = :guia COLLATE NOCASE)")
    suspend fun existeGuia(guia: String): Boolean

    @Query("SELECT MAX(id) FROM envios")
    suspend fun obtenerUltimoId(): Long?

    @Query("SELECT * FROM envios ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE estado = :estado ORDER BY fecha DESC")
    fun observarPorEstado(estado: EstadoEnvio): Flow<List<Envio>>

    @Query(
        "SELECT * FROM envios " +
            "WHERE guia LIKE '%' || :texto || '%' " +
            "OR remitente LIKE '%' || :texto || '%' " +
            "OR destinatario LIKE '%' || :texto || '%' " +
            "ORDER BY fecha DESC"
    )
    fun buscar(texto: String): Flow<List<Envio>>

    @Query("SELECT estado, COUNT(*) AS cantidad FROM envios GROUP BY estado")
    fun contarPorEstado(): Flow<List<ConteoEnvios>>

    @Query("SELECT SUM(valor) FROM envios")
    fun totalValor(): Flow<Int?>
}
