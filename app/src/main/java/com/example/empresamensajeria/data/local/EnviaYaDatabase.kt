package com.example.empresamensajeria.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.empresamensajeria.data.local.dao.AdministradorDao
import com.example.empresamensajeria.data.local.dao.ArticuloDao
import com.example.empresamensajeria.data.local.dao.ClienteDao
import com.example.empresamensajeria.data.local.dao.ContratoBodegaDao
import com.example.empresamensajeria.data.local.dao.EnvioDao
import com.example.empresamensajeria.data.local.dao.MovimientoDao
import com.example.empresamensajeria.data.local.entity.Administrador
import com.example.empresamensajeria.data.local.entity.Articulo
import com.example.empresamensajeria.data.local.entity.Cliente
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.Envio
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import com.example.empresamensajeria.data.local.entity.EstadoEnvio
import com.example.empresamensajeria.data.local.entity.Movimiento
import java.util.Calendar
import java.util.TimeZone

@Database(
    entities = [
        Cliente::class,
        ContratoBodega::class,
        Articulo::class,
        Envio::class,
        Movimiento::class,
        Administrador::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EnviaYaDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun contratoBodegaDao(): ContratoBodegaDao
    abstract fun articuloDao(): ArticuloDao
    abstract fun envioDao(): EnvioDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun administradorDao(): AdministradorDao

    companion object {
        @Volatile
        private var INSTANCE: EnviaYaDatabase? = null

        fun getDatabase(context: Context): EnviaYaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EnviaYaDatabase::class.java,
                    "enviaya.db"
                )
                    .addCallback(PrecargaCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

/**
 * Inserta los datos iniciales SOLO cuando se crea el archivo de la base de datos.
 *
 * Se usa execSQL sobre el SupportSQLiteDatabase que recibe onCreate, así la precarga
 * no depende de INSTANCE ni de los DAOs (que todavía no existen en ese momento) y
 * queda dentro de la misma creación: cuando la app hace su primera consulta los datos ya están.
 */
private object PrecargaCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.beginTransaction()
        try {
            insertarAdministrador(db)
            insertarClientePrueba(db)
            insertarEnviosEjemplo(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertarAdministrador(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO administradores (usuario, contrasena) VALUES (?, ?)",
            arrayOf<Any?>("admin", "admin123")
        )
    }

    // Cliente habilitado sin contraseña, para probar "Registrarse" sin contratar primero
    private fun insertarClientePrueba(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO clientes (documento, nombre, telefono, contrasena) VALUES (?, ?, ?, NULL)",
            arrayOf<Any?>("1001234567", "Cliente Prueba", "3001234567")
        )
        // Mediano por 30 días: 15.000 de ingreso + 8.000 x 30 - 10 % de descuento = 229.500
        db.execSQL(
            "INSERT INTO contratos_bodega (documentoCliente, espacio, dias, valor, fechaInicio, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            arrayOf<Any?>(
                "1001234567", "Mediano", 30, 229500,
                System.currentTimeMillis(), EstadoContrato.ACTIVO.name
            )
        )
    }

    private data class MovimientoInicial(val fecha: Long, val ubicacion: String, val descripcion: String)

    private fun insertarEnviosEjemplo(db: SupportSQLiteDatabase) {
        insertarEnvio(
            db, id = 1, guia = "EY1001",
            remitente = "Laura Gómez", telRemitente = "3104567890",
            destinatario = "Andrés Ramírez", telDestinatario = "3157894561",
            direccion = "Calle 72 # 10-34, Bogotá",
            origen = "Pereira", destino = "Bogotá", servicio = "Envío nacional",
            peso = 3.5, valor = 21000, estado = EstadoEnvio.EN_TRANSITO,
            movimientos = listOf(
                MovimientoInicial(fecha(19, 16, 20), "Pereira", "Paquete recibido en la sede"),
                MovimientoInicial(fecha(20, 11, 5), "Bodega Pereira", "Clasificado y cargado en el vehículo"),
                MovimientoInicial(fecha(20, 15, 40), "Autopista del Café", "Salió en ruta hacia Bogotá")
            )
        )
        insertarEnvio(
            db, id = 2, guia = "EY1002",
            remitente = "Carlos Restrepo", telRemitente = "3012345678",
            destinatario = "María Fernández", telDestinatario = "+34 612 345 678",
            direccion = "Calle de Alcalá 45, Madrid",
            origen = "Medellín", destino = "España", servicio = "Envío internacional",
            peso = 2.0, valor = 174000, estado = EstadoEnvio.RECIBIDO,
            movimientos = listOf(
                MovimientoInicial(fecha(19, 18, 0), "Medellín", "Paquete recibido en la sede"),
                MovimientoInicial(fecha(20, 9, 10), "Bodega Medellín", "En proceso de documentación y aduana")
            )
        )
        insertarEnvio(
            db, id = 3, guia = "EY1003",
            remitente = "Laboratorio Andino S.A.S.", telRemitente = "6014567890",
            destinatario = "Clínica Valle Salud", telDestinatario = "6023456789",
            direccion = "Carrera 38 # 5A-100, Cali",
            origen = "Bogotá", destino = "Cali", servicio = "Entrega especial",
            peso = 8.0, valor = 141800, estado = EstadoEnvio.ENTREGADO,
            movimientos = listOf(
                MovimientoInicial(fecha(17, 15, 10), "Bogotá", "Paquete recibido con manejo especial"),
                MovimientoInicial(fecha(17, 17, 45), "Bodega Bogotá", "Temperatura verificada (2 a 8 °C)"),
                MovimientoInicial(fecha(18, 8, 0), "Cali", "En reparto con cadena de frío"),
                MovimientoInicial(fecha(18, 14, 30), "Cali", "Entregado y firmado por el receptor")
            )
        )
        // Antes figuraba como "Recibido", pero su único evento es la solicitud de recolección:
        // con los nuevos estados corresponde a PENDIENTE
        insertarEnvio(
            db, id = 4, guia = "EY1004",
            remitente = "Sofía Castaño", telRemitente = "3206549870",
            destinatario = "Julián Ospina", telDestinatario = "3123216547",
            direccion = "Avenida Santander # 65-20, Manizales",
            origen = "Manizales", destino = "Manizales", servicio = "Recolección a domicilio",
            peso = 1.0, valor = 5800, estado = EstadoEnvio.PENDIENTE,
            movimientos = listOf(
                MovimientoInicial(fecha(20, 10, 25), "Manizales", "Solicitud de recolección registrada")
            )
        )
        insertarEnvio(
            db, id = 5, guia = "EY1005",
            remitente = "Diego Valencia", telRemitente = "3168529637",
            destinatario = "Camila Herrera", telDestinatario = "3009638527",
            direccion = "Carrera 53 # 82-86, Barranquilla",
            origen = "Cali", destino = "Barranquilla", servicio = "Envío nacional",
            peso = 12.0, valor = 54800, estado = EstadoEnvio.EN_TRANSITO,
            movimientos = listOf(
                MovimientoInicial(fecha(19, 17, 30), "Cali", "Paquete recibido en la sede"),
                MovimientoInicial(fecha(20, 13, 15), "Cali", "Salió de la bodega en ruta a Barranquilla")
            )
        )
    }

    private fun insertarEnvio(
        db: SupportSQLiteDatabase,
        id: Long,
        guia: String,
        remitente: String,
        telRemitente: String,
        destinatario: String,
        telDestinatario: String,
        direccion: String,
        origen: String,
        destino: String,
        servicio: String,
        peso: Double,
        valor: Int,
        estado: EstadoEnvio,
        movimientos: List<MovimientoInicial>
    ) {
        // La fecha del envío es la de su primer movimiento
        db.execSQL(
            "INSERT INTO envios (id, guia, remitente, telefonoRemitente, destinatario, " +
                "telefonoDestinatario, direccionDestino, origen, destino, servicio, peso, valor, estado, fecha) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any?>(
                id, guia, remitente, telRemitente, destinatario, telDestinatario, direccion,
                origen, destino, servicio, peso, valor, estado.name, movimientos.first().fecha
            )
        )
        for (movimiento in movimientos) {
            db.execSQL(
                "INSERT INTO movimientos (envioId, fecha, ubicacion, descripcion) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(id, movimiento.fecha, movimiento.ubicacion, movimiento.descripcion)
            )
        }
    }

    // Fechas de ejemplo en octubre de 2026, hora de Colombia
    private fun fecha(dia: Int, hora: Int, minuto: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("America/Bogota")).apply {
            clear()
            set(2026, Calendar.OCTOBER, dia, hora, minuto)
        }.timeInMillis
}
