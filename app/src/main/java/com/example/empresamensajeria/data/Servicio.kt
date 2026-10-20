package com.example.empresamensajeria.data

import com.example.empresamensajeria.R
import java.util.Locale

data class Servicio(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val precioBase: Int,
    val imagen: Int,
    val modo: ModoCotizacion = ModoCotizacion.DESTINO,
    val manejoEspecial: Boolean = false
) {
    val precioTexto: String
        get() = "Desde \$" + "%,d".format(Locale.US, precioBase).replace(',', '.')
}

val listaServicios = listOf(
    Servicio(
        id = 1,
        titulo = "Envío nacional",
        descripcion = "Entregas a todo el país en 1 a 3 días hábiles",
        precioBase = 8000,
        imagen = R.drawable.envionacional,
        modo = ModoCotizacion.DESTINO
    ),
    Servicio(
        id = 2,
        titulo = "Envío internacional",
        descripcion = "Llevamos tus paquetes a más de 50 países",
        precioBase = 60000,
        imagen = R.drawable.enviointernacional,
        modo = ModoCotizacion.INTERNACIONAL
    ),
    Servicio(
        id = 3,
        titulo = "Bodega",
        descripcion = "Almacenamiento seguro para tu mercancía",
        precioBase = 15000,
        imagen = R.drawable.bodega,
        modo = ModoCotizacion.BODEGA
    ),
    Servicio(
        id = 4,
        titulo = "Recolección a domicilio",
        descripcion = "Recogemos tu paquete donde estés",
        precioBase = 5000,
        imagen = R.drawable.recoleccion,
        modo = ModoCotizacion.LOCAL
    ),
    Servicio(
        id = 5,
        titulo = "Envío el mismo día",
        descripcion = "Entrega urgente dentro de la ciudad",
        precioBase = 12000,
        imagen = R.drawable.mismodia,
        modo = ModoCotizacion.LOCAL
    ),
    Servicio(
        id = 6,
        titulo = "Entrega especial",
        descripcion = "Cadena de frío y manejo cuidadoso para medicamentos y carga delicada",
        precioBase = 89000,
        imagen = R.drawable.especial,
        modo = ModoCotizacion.DESTINO,
        manejoEspecial = true
    )
)