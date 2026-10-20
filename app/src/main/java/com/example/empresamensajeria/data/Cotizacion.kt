package com.example.empresamensajeria.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
//Aca se hacen las cotizaciones de cada producto en especifico ya que algunos servicios requieren cosas diferentres
enum class ModoCotizacion {
    DESTINO,
    INTERNACIONAL,
    LOCAL,
    BODEGA
}

data class Ciudad(
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)

data class Pais(
    val nombre: String,
    val capital: String,
    val latitud: Double,
    val longitud: Double
)

data class TamanoPaquete(
    val nombre: String,
    val emoji: String,
    val medidas: String,
    val pesoMaximoKg: Double,
    val recargo: Int
)

data class OpcionManejo(
    val nombre: String,
    val emoji: String,
    val descripcion: String,
    val recargo: Int
)

data class EspacioBodega(
    val nombre: String,
    val emoji: String,
    val medidas: String,
    val tarifaDia: Int
)

val listaCiudades = listOf(
    Ciudad("Pereira", 4.8133, -75.6961),
    Ciudad("Manizales", 5.0703, -75.5138),
    Ciudad("Armenia", 4.5339, -75.6811),
    Ciudad("Medellín", 6.2442, -75.5812),
    Ciudad("Cali", 3.4516, -76.5320),
    Ciudad("Bogotá", 4.7110, -74.0721),
    Ciudad("Bucaramanga", 7.1193, -73.1227),
    Ciudad("Cartagena", 10.3910, -75.4794),
    Ciudad("Barranquilla", 10.9685, -74.7813),
    Ciudad("Santa Marta", 11.2408, -74.1990)
)

val listaPaises = listOf(
    Pais("Estados Unidos", "Washington D. C.", 38.9072, -77.0369),
    Pais("México", "Ciudad de México", 19.4326, -99.1332),
    Pais("Panamá", "Ciudad de Panamá", 8.9824, -79.5199),
    Pais("Ecuador", "Quito", -0.1807, -78.4678),
    Pais("Perú", "Lima", -12.0464, -77.0428),
    Pais("Brasil", "Brasilia", -15.7939, -47.8828),
    Pais("Argentina", "Buenos Aires", -34.6037, -58.3816),
    Pais("España", "Madrid", 40.4168, -3.7038)
)

val listaTamanos = listOf(
    TamanoPaquete("Documento", "📄", "35 x 25 x 2 cm", 1.0, 0),
    TamanoPaquete("Pequeño", "📦", "30 x 20 x 15 cm", 5.0, 3000),
    TamanoPaquete("Mediano", "🧳", "50 x 40 x 30 cm", 20.0, 8000),
    TamanoPaquete("Grande", "🚚", "100 x 60 x 50 cm", 50.0, 20000)
)

val listaManejos = listOf(
    OpcionManejo("Cadena de frío", "❄️", "Temperatura controlada de 2 a 8 °C", 25000),
    OpcionManejo("Carga delicada", "🥂", "Embalaje y trato especial", 12000),
    OpcionManejo("Entrega supervisada", "🛡️", "Acompañamiento y firma del receptor", 15000)
)

val listaEspacios = listOf(
    EspacioBodega("Pequeño", "🗄️", "1 m³", 3000),
    EspacioBodega("Mediano", "📦", "4 m³", 8000),
    EspacioBodega("Grande", "🏭", "12 m³", 20000)
)

fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radioTierra = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return radioTierra * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun distanciaKm(origen: Ciudad, destino: Ciudad): Double =
    distanciaKm(origen.latitud, origen.longitud, destino.latitud, destino.longitud)

fun distanciaKm(origen: Ciudad, destino: Pais): Double =
    distanciaKm(origen.latitud, origen.longitud, destino.latitud, destino.longitud)

data class LineaDesglose(
    val concepto: String,
    val valor: Int
)

data class ResultadoCotizacion(
    val resumen: String,
    val lineas: List<LineaDesglose>,
    val total: Int
)

fun Int.aPesos(): String {
    val signo = if (this < 0) "-" else ""
    return signo + "\$" + "%,d".format(Locale.US, abs(this)).replace(',', '.')
}

private fun redondear(valor: Double): Int = (valor / 100.0).roundToInt() * 100

fun calcularCotizacion(
    servicio: Servicio,
    origen: String,
    destino: String,
    tamano: TamanoPaquete?,
    pesoKg: Double,
    manejo: OpcionManejo?,
    espacio: EspacioBodega?,
    dias: Int,
    seguro: Boolean,
    recoleccion: Boolean
): ResultadoCotizacion {
    val lineas = mutableListOf<LineaDesglose>()
    val etiquetaBase =
        if (servicio.modo == ModoCotizacion.BODEGA) "Tarifa de ingreso" else "Tarifa base"
    lineas.add(LineaDesglose(etiquetaBase, servicio.precioBase))

    var resumen = ""

    when (servicio.modo) {
        ModoCotizacion.DESTINO -> {
            val ciudadOrigen = listaCiudades.first { it.nombre == origen }
            val ciudadDestino = listaCiudades.first { it.nombre == destino }
            // x1.3 porque la ruta por carretera es más larga que la línea recta
            val km = distanciaKm(ciudadOrigen, ciudadDestino) * 1.3
            lineas.add(
                LineaDesglose("Distancia aprox. ${km.roundToInt()} km", redondear(km * 20))
            )
            resumen = "$origen → $destino"
        }

        ModoCotizacion.INTERNACIONAL -> {
            val ciudadOrigen = listaCiudades.first { it.nombre == origen }
            val paisDestino = listaPaises.first { it.nombre == destino }
            val km = distanciaKm(ciudadOrigen, paisDestino)
            lineas.add(
                LineaDesglose("Distancia aprox. ${km.roundToInt()} km", redondear(km * 8))
            )
            lineas.add(LineaDesglose("Aduana y manejo internacional", 35000))
            resumen = "$origen → $destino"
        }

        ModoCotizacion.LOCAL -> {
            resumen = "Servicio en $origen"
        }

        ModoCotizacion.BODEGA -> {
            if (espacio != null) {
                val textoDias = if (dias == 1) "1 día" else "$dias días"
                lineas.add(
                    LineaDesglose(
                        "Espacio ${espacio.nombre.lowercase()} (${espacio.medidas}) por $textoDias",
                        espacio.tarifaDia * dias
                    )
                )
                resumen = "Almacenamiento por $textoDias"
            }
            val subtotal = lineas.sumOf { it.valor }
            val porcentaje = when {
                dias >= 90 -> 20
                dias >= 30 -> 10
                else -> 0
            }
            if (porcentaje > 0) {
                lineas.add(
                    LineaDesglose(
                        "Descuento por tiempo ($porcentaje %)",
                        -redondear(subtotal * porcentaje / 100.0)
                    )
                )
            }
        }
    }

    if (servicio.modo != ModoCotizacion.BODEGA) {
        if (tamano != null && tamano.recargo > 0) {
            lineas.add(LineaDesglose("Tamaño ${tamano.nombre.lowercase()}", tamano.recargo))
        }
        val tarifaPorKg = when (servicio.modo) {
            ModoCotizacion.INTERNACIONAL -> 6000
            ModoCotizacion.LOCAL -> 800
            else -> 1500
        }
        lineas.add(LineaDesglose("Peso: $pesoKg kg", redondear(pesoKg * tarifaPorKg)))
        if (servicio.manejoEspecial && manejo != null) {
            lineas.add(LineaDesglose("Manejo: ${manejo.nombre.lowercase()}", manejo.recargo))
        }
        val hayViaje = servicio.modo == ModoCotizacion.DESTINO ||
                servicio.modo == ModoCotizacion.INTERNACIONAL
        if (recoleccion && hayViaje) {
            lineas.add(LineaDesglose("Recolección a domicilio", 5000))
        }
        if (seguro) {
            val subtotal = lineas.sumOf { it.valor }
            lineas.add(LineaDesglose("Seguro de envío (3 %)", redondear(subtotal * 0.03)))
        }
    }

    return ResultadoCotizacion(resumen, lineas, lineas.sumOf { it.valor })
}