package com.example.empresamensajeria.data.repository

import androidx.room.withTransaction
import com.example.empresamensajeria.data.local.EnviaYaDatabase
import com.example.empresamensajeria.data.local.dao.ConteoContratos
import com.example.empresamensajeria.data.local.entity.Articulo
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.relation.ClienteConContratos
import com.example.empresamensajeria.data.local.relation.ContratoConArticulos
import kotlinx.coroutines.flow.Flow

sealed class ResultadoRegistro {
    // El documento no está en la tabla de clientes
    object NoHabilitado : ResultadoRegistro()

    // El documento ya tiene contraseña
    object YaTieneCuenta : ResultadoRegistro()

    object Exito : ResultadoRegistro()
}

/** Situación del cliente al contratar un servicio de bodega (en los tres casos se crea el contrato). */
sealed class ResultadoContrato {
    // Se creó el cliente, todavía sin cuenta
    object ClienteNuevo : ResultadoContrato()

    // Ya existía pero aún no se ha registrado
    object ClienteSinCuenta : ResultadoContrato()

    // Ya existía y ya tiene contraseña
    object ClienteConCuenta : ResultadoContrato()
}

/**
 * Clientes, contratos de bodega y artículos.
 * Todas las funciones suspend usan los DAOs de Room, que ya se ejecutan fuera del hilo principal.
 */
class BodegaRepository(private val db: EnviaYaDatabase) {

    private val clienteDao = db.clienteDao()
    private val contratoDao = db.contratoBodegaDao()
    private val articuloDao = db.articuloDao()

    // ---------- Contratar y cuenta del cliente ----------

    /**
     * Crea o reutiliza el cliente por documento (sin cambiar sus datos si ya existía)
     * y le crea un ContratoBodega ACTIVO. El resultado indica en qué situación estaba el cliente.
     */
    suspend fun contratarServicio(
        documento: String,
        nombre: String,
        telefono: String,
        espacio: String,
        dias: Int,
        valor: Int
    ): ResultadoContrato = db.withTransaction {
        val doc = documento.trim()
        val existente = clienteDao.buscarPorDocumento(doc)
        val resultado = when {
            existente == null -> {
                clienteDao.insertar(
                    Cliente(documento = doc, nombre = nombre.trim(), telefono = telefono.trim())
                )
                ResultadoContrato.ClienteNuevo
            }
            existente.contrasena == null -> ResultadoContrato.ClienteSinCuenta
            else -> ResultadoContrato.ClienteConCuenta
        }
        contratoDao.insertar(
            ContratoBodega(
                documentoCliente = doc,
                espacio = espacio,
                dias = dias,
                valor = valor,
                fechaInicio = System.currentTimeMillis(),
                estado = EstadoContrato.ACTIVO
            )
        )
        resultado
    }

    suspend fun registrarCuenta(documento: String, contrasena: String): ResultadoRegistro =
        db.withTransaction {
            val cliente = clienteDao.buscarPorDocumento(documento.trim())
            when {
                cliente == null -> ResultadoRegistro.NoHabilitado
                cliente.contrasena != null -> ResultadoRegistro.YaTieneCuenta
                else -> {
                    clienteDao.actualizarContrasena(cliente.documento, contrasena)
                    ResultadoRegistro.Exito
                }
            }
        }

    suspend fun iniciarSesionCliente(documento: String, contrasena: String): Cliente? {
        val cliente = clienteDao.buscarPorDocumento(documento.trim()) ?: return null
        return if (cliente.contrasena != null && cliente.contrasena == contrasena) cliente else null
    }

    suspend fun buscarCliente(documento: String): Cliente? =
        clienteDao.buscarPorDocumento(documento.trim())

    /**
     * Para validar una sesión guardada al abrir la app: true solo si el cliente
     * todavía existe y tiene cuenta (el admin pudo haberlo eliminado).
     */
    suspend fun clienteTieneCuentaActiva(documento: String): Boolean =
        clienteDao.buscarPorDocumento(documento.trim())?.contrasena != null

    // ---------- Clientes (panel admin) ----------

    fun observarClientes(): Flow<List<Cliente>> = clienteDao.observarTodos()

    fun buscarClientes(texto: String): Flow<List<Cliente>> = clienteDao.buscar(texto.trim())

    fun observarClienteConContratos(documento: String): Flow<ClienteConContratos?> =
        clienteDao.observarClienteConContratos(documento)

    fun observarClientesConContratos(): Flow<List<ClienteConContratos>> =
        clienteDao.observarClientesConContratos()

    suspend fun editarCliente(cliente: Cliente) = clienteDao.actualizar(cliente)

    /** Borra la contraseña: el cliente deberá registrarse de nuevo con su documento. */
    suspend fun restablecerCuenta(documento: String) = clienteDao.borrarContrasena(documento)

    // Borra también sus contratos y artículos (CASCADE)
    suspend fun eliminarCliente(cliente: Cliente) = clienteDao.eliminar(cliente)

    // ---------- Contratos ----------

    fun observarContratosDeCliente(documento: String): Flow<List<ContratoBodega>> =
        contratoDao.observarPorCliente(documento)

    fun observarContratos(): Flow<List<ContratoBodega>> = contratoDao.observarTodos()

    fun observarContratosPorEstado(estado: EstadoContrato): Flow<List<ContratoBodega>> =
        contratoDao.observarPorEstado(estado)

    fun observarContratoConArticulos(id: Long): Flow<ContratoConArticulos?> =
        contratoDao.observarContratoConArticulos(id)

    fun contarContratosPorEstado(): Flow<List<ConteoContratos>> = contratoDao.contarPorEstado()

    suspend fun editarContrato(contrato: ContratoBodega) = contratoDao.actualizar(contrato)

    suspend fun cambiarEstadoContrato(id: Long, estado: EstadoContrato) =
        contratoDao.actualizarEstado(id, estado)

    // Borra también sus artículos (CASCADE)
    suspend fun eliminarContrato(contrato: ContratoBodega) = contratoDao.eliminar(contrato)

    // ---------- Artículos (inventario del cliente) ----------

    fun observarArticulos(contratoId: Long): Flow<List<Articulo>> =
        articuloDao.observarPorContrato(contratoId)

    suspend fun agregarArticulo(
        contratoId: Long,
        nombre: String,
        descripcion: String,
        cantidad: Int
    ): Long = articuloDao.insertar(
        Articulo(
            contratoId = contratoId,
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            cantidad = cantidad,
            fechaIngreso = System.currentTimeMillis()
        )
    )

    suspend fun editarArticulo(articulo: Articulo) = articuloDao.actualizar(articulo)

    suspend fun eliminarArticulo(articulo: Articulo) = articuloDao.eliminar(articulo)
}
