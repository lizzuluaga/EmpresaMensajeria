package com.example.empresamensajeria.data.repository

import androidx.room.withTransaction
import com.example.empresamensajeria.data.local.EnviaYaDatabase
import com.example.empresamensajeria.data.local.dao.ConteoEnvios
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.EstadoEnvio
import com.example.empresamensajeria.data.local.entity.Movimiento
import com.example.empresamensajeria.data.local.relation.EnvioConMovimientos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Envíos y sus movimientos.
 * Todas las funciones suspend usan los DAOs de Room, que ya se ejecutan fuera del hilo principal.
 */
class EnvioRepository(private val db: EnviaYaDatabase) {

    private val envioDao = db.envioDao()
    private val movimientoDao = db.movimientoDao()

    // ---------- Solicitar envío ----------

    /** Crea el envío PENDIENTE con su primer movimiento y devuelve la guía generada. */
    suspend fun solicitarEnvio(
        remitente: String,
        telefonoRemitente: String,
        destinatario: String,
        telefonoDestinatario: String,
        direccionDestino: String,
        origen: String,
        destino: String,
        servicio: String,
        peso: Double,
        valor: Int
    ): String = db.withTransaction {
        val guia = generarGuia()
        val ahora = System.currentTimeMillis()
        val envioId = envioDao.insertar(
            Envio(
                guia = guia,
                remitente = remitente.trim(),
                telefonoRemitente = telefonoRemitente.trim(),
                destinatario = destinatario.trim(),
                telefonoDestinatario = telefonoDestinatario.trim(),
                direccionDestino = direccionDestino.trim(),
                origen = origen,
                destino = destino,
                servicio = servicio,
                peso = peso,
                valor = valor,
                estado = EstadoEnvio.PENDIENTE,
                fecha = ahora
            )
        )
        movimientoDao.insertar(
            Movimiento(
                envioId = envioId,
                fecha = ahora,
                ubicacion = origen,
                descripcion = "Solicitud de envío registrada"
            )
        )
        guia
    }

    // EY + (1000 + último id + 1); si por algún motivo ya existe, prueba con el siguiente número
    private suspend fun generarGuia(): String {
        var numero = 1000 + (envioDao.obtenerUltimoId() ?: 0L) + 1
        var guia = "EY$numero"
        while (envioDao.existeGuia(guia)) {
            numero++
            guia = "EY$numero"
        }
        return guia
    }

    // ---------- Cambiar estado (panel admin) ----------

    /** Actualiza el estado y registra automáticamente el movimiento correspondiente. */
    suspend fun cambiarEstado(envio: Envio, nuevoEstado: EstadoEnvio) {
        if (envio.estado == nuevoEstado) return
        db.withTransaction {
            envioDao.actualizarEstado(envio.id, nuevoEstado)
            val (ubicacion, descripcion) = when (nuevoEstado) {
                EstadoEnvio.PENDIENTE -> envio.origen to "Envío marcado como pendiente"
                EstadoEnvio.RECIBIDO -> "Bodega ${envio.origen}" to "Paquete recibido en bodega"
                EstadoEnvio.EN_TRANSITO -> envio.origen to "En tránsito hacia ${envio.destino}"
                EstadoEnvio.EN_REPARTO -> envio.destino to "En reparto en ${envio.destino}"
                EstadoEnvio.ENTREGADO -> envio.destino to "Entregado al destinatario"
            }
            movimientoDao.insertar(
                Movimiento(
                    envioId = envio.id,
                    fecha = System.currentTimeMillis(),
                    ubicacion = ubicacion,
                    descripcion = descripcion
                )
            )
        }
    }

    suspend fun editarEnvio(envio: Envio) = envioDao.actualizar(envio)

    // Borra también sus movimientos (CASCADE)
    suspend fun eliminarEnvio(envio: Envio) = envioDao.eliminar(envio)

    // ---------- Rastrear ----------

    /** El envío con sus movimientos ordenados del más reciente al más antiguo. */
    fun rastrear(guia: String): Flow<EnvioConMovimientos?> =
        envioDao.buscarPorGuia(guia.trim()).map { resultado ->
            resultado?.copy(movimientos = resultado.movimientos.sortedByDescending { it.fecha })
        }

    fun observarMovimientos(envioId: Long): Flow<List<Movimiento>> =
        movimientoDao.observarPorEnvio(envioId)

    // ---------- Panel admin ----------

    fun observarEnvios(): Flow<List<Envio>> = envioDao.observarTodos()

    fun observarEnviosPorEstado(estado: EstadoEnvio): Flow<List<Envio>> =
        envioDao.observarPorEstado(estado)

    fun buscarEnvios(texto: String): Flow<List<Envio>> = envioDao.buscar(texto.trim())

    fun contarEnviosPorEstado(): Flow<List<ConteoEnvios>> = envioDao.contarPorEstado()

    fun observarValorTotal(): Flow<Int> = envioDao.totalValor().map { it ?: 0 }
}
