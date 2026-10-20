package com.example.empresamensajeria.data
//Aca son unos datos provicionales para despues aprender como usar una base de datos :)
enum class EstadoEnvio(val etiqueta: String) {
    RECIBIDO("Recibido"),
    EN_BODEGA("En bodega"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado")
}

data class EventoRastreo(
    val fecha: String,
    val lugar: String,
    val descripcion: String
)

data class Envio(
    val guia: String,
    val servicio: String,
    val origen: String,
    val destino: String,
    val estado: EstadoEnvio,
    val entrega: String,
    val eventos: List<EventoRastreo>
)

val listaEnvios = listOf(
    Envio(
        guia = "EY1001",
        servicio = "Envío nacional",
        origen = "Pereira",
        destino = "Bogotá",
        estado = EstadoEnvio.EN_CAMINO,
        entrega = "Entrega estimada: 23 de octubre",
        eventos = listOf(
            EventoRastreo("20 oct · 3:40 p. m.", "Autopista del Café", "Salió en ruta hacia Bogotá"),
            EventoRastreo("20 oct · 11:05 a. m.", "Bodega Pereira", "Clasificado y cargado en el vehículo"),
            EventoRastreo("19 oct · 4:20 p. m.", "Pereira", "Paquete recibido en la sede")
        )
    ),
    Envio(
        guia = "EY1002",
        servicio = "Envío internacional",
        origen = "Medellín",
        destino = "España",
        estado = EstadoEnvio.EN_BODEGA,
        entrega = "Entrega estimada: 28 de octubre",
        eventos = listOf(
            EventoRastreo("20 oct · 9:10 a. m.", "Bodega Medellín", "En proceso de documentación y aduana"),
            EventoRastreo("19 oct · 6:00 p. m.", "Medellín", "Paquete recibido en la sede")
        )
    ),
    Envio(
        guia = "EY1003",
        servicio = "Entrega especial",
        origen = "Bogotá",
        destino = "Cali",
        estado = EstadoEnvio.ENTREGADO,
        entrega = "Entregado el 18 de octubre",
        eventos = listOf(
            EventoRastreo("18 oct · 2:30 p. m.", "Cali", "Entregado y firmado por el receptor"),
            EventoRastreo("18 oct · 8:00 a. m.", "Cali", "En reparto con cadena de frío"),
            EventoRastreo("17 oct · 5:45 p. m.", "Bodega Bogotá", "Temperatura verificada (2 a 8 °C)"),
            EventoRastreo("17 oct · 3:10 p. m.", "Bogotá", "Paquete recibido con manejo especial")
        )
    ),
    Envio(
        guia = "EY1004",
        servicio = "Recolección a domicilio",
        origen = "Manizales",
        destino = "Manizales",
        estado = EstadoEnvio.RECIBIDO,
        entrega = "Recolección programada: 21 de octubre",
        eventos = listOf(
            EventoRastreo("20 oct · 10:25 a. m.", "Manizales", "Solicitud de recolección registrada")
        )
    ),
    Envio(
        guia = "EY1005",
        servicio = "Envío nacional",
        origen = "Cali",
        destino = "Barranquilla",
        estado = EstadoEnvio.EN_CAMINO,
        entrega = "Entrega estimada: 24 de octubre",
        eventos = listOf(
            EventoRastreo("20 oct · 1:15 p. m.", "Cali", "Salió de la bodega en ruta a Barranquilla"),
            EventoRastreo("19 oct · 5:30 p. m.", "Cali", "Paquete recibido en la sede")
        )
    )
)

private val formatoGuia = Regex("^EY\\d{4}$", RegexOption.IGNORE_CASE)

fun guiaValida(guia: String): Boolean = formatoGuia.matches(guia.trim())

fun buscarEnvio(guia: String): Envio? =
    listaEnvios.find { it.guia.equals(guia.trim(), ignoreCase = true) }