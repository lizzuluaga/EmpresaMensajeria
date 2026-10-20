package com.example.empresamensajeria.data

enum class TipoContacto {
    LLAMADA,
    WHATSAPP,
    CORREO,
    MAPA
}

data class MedioContacto(
    val tipo: TipoContacto,
    val emoji: String,
    val titulo: String,
    val detalle: String,
    val accion: String,
    val enlace: String
)

data class HorarioAtencion(
    val dias: String,
    val horas: String
)

val listaContactos = listOf(
    MedioContacto(
        tipo = TipoContacto.LLAMADA,
        emoji = "📞",
        titulo = "Línea de atención",
        detalle = "01 8000 123 456",
        accion = "Llamar",
        enlace = "tel:018000123456"
    ),
    MedioContacto(
        tipo = TipoContacto.WHATSAPP,
        emoji = "💬",
        titulo = "WhatsApp",
        detalle = "+57 300 000 0000",
        accion = "Escribir",
        enlace = "https://wa.me/573000000000"
    ),
    MedioContacto(
        tipo = TipoContacto.CORREO,
        emoji = "✉️",
        titulo = "Correo electrónico",
        detalle = "contacto@enviaya.com",
        accion = "Enviar correo",
        enlace = "mailto:contacto@enviaya.com"
    ),
    MedioContacto(
        tipo = TipoContacto.MAPA,
        emoji = "📍",
        titulo = "Sede principal",
        detalle = "Carrera 0 # 00-00",
        accion = "Ver en el mapa",
        enlace = "geo:0,0?q=Carrera+0+%2300-00"
    )
)

val listaHorarios = listOf(
    HorarioAtencion("Lunes a viernes", "8:00 a. m. – 6:00 p. m."),
    HorarioAtencion("Sábados", "8:00 a. m. – 1:00 p. m."),
    HorarioAtencion("Domingos y festivos", "Cerrado")
)

private val formatoCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun correoValido(correo: String): Boolean = formatoCorreo.matches(correo.trim())