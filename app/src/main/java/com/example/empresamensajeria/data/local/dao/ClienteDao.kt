package com.example.empresamensajeria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.relation.ClienteConContratos
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(cliente: Cliente)

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Delete
    suspend fun eliminar(cliente: Cliente)

    @Query("SELECT * FROM clientes WHERE documento = :documento LIMIT 1")
    suspend fun buscarPorDocumento(documento: String): Cliente?

    @Query("UPDATE clientes SET contrasena = :contrasena WHERE documento = :documento")
    suspend fun actualizarContrasena(documento: String, contrasena: String)

    @Query("SELECT * FROM clientes ORDER BY nombre COLLATE NOCASE")
    fun observarTodos(): Flow<List<Cliente>>

    @Query(
        "SELECT * FROM clientes " +
            "WHERE nombre LIKE '%' || :texto || '%' OR documento LIKE '%' || :texto || '%' " +
            "ORDER BY nombre COLLATE NOCASE"
    )
    fun buscar(texto: String): Flow<List<Cliente>>

    @Transaction
    @Query("SELECT * FROM clientes WHERE documento = :documento")
    fun observarClienteConContratos(documento: String): Flow<ClienteConContratos?>

    // Panel admin: todos los clientes con sus contratos
    @Transaction
    @Query("SELECT * FROM clientes ORDER BY nombre COLLATE NOCASE")
    fun observarClientesConContratos(): Flow<List<ClienteConContratos>>

    // Restablecer cuenta: el cliente vuelve a quedar habilitado sin contraseña
    @Query("UPDATE clientes SET contrasena = NULL WHERE documento = :documento")
    suspend fun borrarContrasena(documento: String)
}
