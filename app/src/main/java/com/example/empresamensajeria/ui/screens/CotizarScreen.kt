package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.EspacioBodega
import com.example.empresamensajeria.data.ModoCotizacion
import com.example.empresamensajeria.data.OpcionManejo
import com.example.empresamensajeria.data.ResultadoCotizacion
import com.example.empresamensajeria.data.Servicio
import com.example.empresamensajeria.data.TamanoPaquete
import com.example.empresamensajeria.data.calcularCotizacion
import com.example.empresamensajeria.data.listaCiudades
import com.example.empresamensajeria.data.listaEspacios
import com.example.empresamensajeria.data.listaManejos
import com.example.empresamensajeria.data.listaPaises
import com.example.empresamensajeria.data.listaServicios
import com.example.empresamensajeria.data.listaTamanos
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.FilaInterruptor
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.SelectorDesplegable
import com.example.empresamensajeria.ui.components.TarjetaResultado
import com.example.empresamensajeria.ui.components.TarjetaSeleccion
import kotlinx.coroutines.delay

@Composable
fun CotizarScreen(servicioId: Int, onMenuClick: () -> Unit) {
    val servicio = listaServicios.find { it.id == servicioId } ?: listaServicios.first()
    val nombresCiudades = remember { listaCiudades.map { it.nombre } }
    val nombresPaises = remember { listaPaises.map { it.nombre } }

    // Estado de todos los campos del formulario
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var tamano by remember { mutableStateOf<TamanoPaquete?>(null) }
    var peso by remember { mutableStateOf("") }
    var manejo by remember { mutableStateOf<OpcionManejo?>(null) }
    var espacio by remember { mutableStateOf<EspacioBodega?>(null) }
    var dias by remember { mutableIntStateOf(1) }
    var seguro by remember { mutableStateOf(false) }
    var recoleccion by remember { mutableStateOf(false) }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var resultado by remember { mutableStateOf<ResultadoCotizacion?>(null) }
    val estadoScroll = rememberScrollState()

    // Si el usuario cambia cualquier dato, el resultado anterior deja de ser válido
    LaunchedEffect(
        origen, destino, tamano, peso, manejo, espacio, dias, seguro, recoleccion
    ) {
        resultado = null
    }

    // Al calcular, baja hasta el resultado
    LaunchedEffect(resultado) {
        if (resultado != null) {
            delay(150)
            estadoScroll.animateScrollTo(estadoScroll.maxValue)
        }
    }

    val esEnvio = servicio.modo != ModoCotizacion.BODEGA
    val colorError = MaterialTheme.colorScheme.error

    val tamanoActual = tamano
    val pesoIngresado = peso.toDoubleOrNull()
    val maximoKg = tamanoActual?.pesoMaximoKg?.toInt()
    val pesoExcedido = pesoIngresado != null &&
            tamanoActual != null &&
            pesoIngresado > tamanoActual.pesoMaximoKg

    // Lista de lo que todavía falta; si está vacía, el formulario está completo
    val faltantes = buildList<String> {
        when (servicio.modo) {
            ModoCotizacion.DESTINO -> {
                if (origen.isBlank()) add("ciudad de origen")
                if (destino.isBlank()) add("ciudad de destino")
                else if (origen == destino) add("un destino distinto al origen")
            }
            ModoCotizacion.INTERNACIONAL -> {
                if (origen.isBlank()) add("ciudad de origen")
                if (destino.isBlank()) add("país de destino")
            }
            ModoCotizacion.LOCAL -> {
                if (origen.isBlank()) add("ciudad")
            }
            ModoCotizacion.BODEGA -> {
                if (espacio == null) add("espacio de almacenamiento")
            }
        }
        if (esEnvio) {
            if (tamanoActual == null) add("tamaño del paquete")
            if (pesoIngresado == null || pesoIngresado <= 0.0) add("peso")
            else if (pesoExcedido) add("un peso dentro del máximo")
        }
        if (servicio.manejoEspecial && manejo == null) add("tipo de manejo")
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Cotizar servicio", onMenuClick = onMenuClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResumenServicio(servicio)

            // Los campos de arriba cambian según el modo del servicio
            when (servicio.modo) {
                ModoCotizacion.DESTINO -> {
                    SeccionTitulo("Ruta")
                    SelectorDesplegable(
                        etiqueta = "Ciudad de origen",
                        opciones = nombresCiudades,
                        seleccion = origen,
                        onSeleccion = {
                            origen = it
                            errores = errores - "origen"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = errores["origen"]
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val auxiliar = origen
                                origen = destino
                                destino = auxiliar
                                errores = errores - "origen" - "destino"
                            }
                        ) {
                            Text("⇅", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    SelectorDesplegable(
                        etiqueta = "Ciudad de destino",
                        opciones = nombresCiudades,
                        seleccion = destino,
                        onSeleccion = {
                            destino = it
                            errores = errores - "destino"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = errores["destino"]
                            ?: if (origen.isNotBlank() && origen == destino) {
                                "El destino debe ser distinto al origen"
                            } else null
                    )
                }

                ModoCotizacion.INTERNACIONAL -> {
                    SeccionTitulo("Ruta internacional")
                    SelectorDesplegable(
                        etiqueta = "Ciudad de origen",
                        opciones = nombresCiudades,
                        seleccion = origen,
                        onSeleccion = {
                            origen = it
                            errores = errores - "origen"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = errores["origen"]
                    )
                    SelectorDesplegable(
                        etiqueta = "País de destino",
                        opciones = nombresPaises,
                        seleccion = destino,
                        onSeleccion = {
                            destino = it
                            errores = errores - "destino"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = errores["destino"]
                    )
                }

                ModoCotizacion.LOCAL -> {
                    SeccionTitulo("Ubicación")
                    SelectorDesplegable(
                        etiqueta = "Ciudad",
                        opciones = nombresCiudades,
                        seleccion = origen,
                        onSeleccion = {
                            origen = it
                            errores = errores - "origen"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = errores["origen"]
                    )
                }

                ModoCotizacion.BODEGA -> {
                    SeccionTitulo("Espacio de almacenamiento")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (opcion in listaEspacios) {
                            TarjetaSeleccion(
                                emoji = opcion.emoji,
                                titulo = opcion.nombre,
                                detalle = opcion.medidas,
                                seleccionada = espacio == opcion,
                                onClick = {
                                    espacio = opcion
                                    errores = errores - "espacio"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    errores["espacio"]?.let {
                        Text(it, color = colorError, style = MaterialTheme.typography.bodySmall)
                    }

                    SeccionTitulo("Tiempo de almacenamiento")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                    ) {
                        OutlinedButton(onClick = { if (dias > 1) dias-- }) {
                            Text("−")
                        }
                        Text(
                            text = if (dias == 1) "1 día" else "$dias días",
                            style = MaterialTheme.typography.titleLarge
                        )
                        OutlinedButton(onClick = { if (dias < 365) dias++ }) {
                            Text("+")
                        }
                    }
                }
            }

            // Paquete: solo en los servicios que envían algo
            if (esEnvio) {
                SeccionTitulo("Paquete")
                for (fila in listaTamanos.chunked(2)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (opcion in fila) {
                            TarjetaSeleccion(
                                emoji = opcion.emoji,
                                titulo = opcion.nombre,
                                detalle = opcion.medidas,
                                seleccionada = tamano == opcion,
                                onClick = {
                                    tamano = opcion
                                    errores = errores - "tamano" - "peso"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                errores["tamano"]?.let {
                    Text(it, color = colorError, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = peso,
                    onValueChange = { entrada ->
                        if (entrada.isEmpty() || entrada.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) {
                            peso = entrada
                            errores = errores - "peso"
                        }
                    },
                    label = { Text("Peso del paquete (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errores.containsKey("peso") || pesoExcedido,
                    supportingText = {
                        Text(
                            errores["peso"] ?: when {
                                pesoExcedido -> "El máximo para este tamaño es $maximoKg kg"
                                maximoKg != null -> "Máximo $maximoKg kg para este tamaño"
                                else -> "Primero elige el tamaño del paquete"
                            }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Manejo especial: solo en la entrega especial
            if (servicio.manejoEspecial) {
                SeccionTitulo("Tipo de manejo")
                for (opcion in listaManejos) {
                    TarjetaSeleccion(
                        emoji = opcion.emoji,
                        titulo = opcion.nombre,
                        detalle = opcion.descripcion,
                        seleccionada = manejo == opcion,
                        onClick = {
                            manejo = opcion
                            errores = errores - "manejo"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                errores["manejo"]?.let {
                    Text(it, color = colorError, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Opciones adicionales
            if (esEnvio) {
                SeccionTitulo("Opciones")
                FilaInterruptor(
                    titulo = "Seguro de envío",
                    descripcion = "Protege tu paquete ante pérdida o daño",
                    activo = seguro,
                    onCambio = { seguro = it }
                )
                if (servicio.modo == ModoCotizacion.DESTINO ||
                    servicio.modo == ModoCotizacion.INTERNACIONAL
                ) {
                    FilaInterruptor(
                        titulo = "Recolección a domicilio",
                        descripcion = "Pasamos por tu paquete donde estés",
                        activo = recoleccion,
                        onCambio = { recoleccion = it }
                    )
                }
            }

            // Nota con lo que todavía falta
            if (faltantes.isNotEmpty()) {
                Text(
                    text = "Para calcular falta: ${faltantes.joinToString(", ")}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val tamanoElegido = tamano
                        val manejoElegido = manejo
                        val espacioElegido = espacio
                        val pesoNumero = peso.toDoubleOrNull()
                        val nuevos = mutableMapOf<String, String>()

                        when (servicio.modo) {
                            ModoCotizacion.DESTINO -> {
                                if (origen.isBlank()) nuevos["origen"] = "Elige la ciudad de origen"
                                if (destino.isBlank()) nuevos["destino"] = "Elige la ciudad de destino"
                                if (origen.isNotBlank() && origen == destino) {
                                    nuevos["destino"] = "El destino debe ser distinto al origen"
                                }
                            }
                            ModoCotizacion.INTERNACIONAL -> {
                                if (origen.isBlank()) nuevos["origen"] = "Elige la ciudad de origen"
                                if (destino.isBlank()) nuevos["destino"] = "Elige el país de destino"
                            }
                            ModoCotizacion.LOCAL -> {
                                if (origen.isBlank()) nuevos["origen"] = "Elige la ciudad"
                            }
                            ModoCotizacion.BODEGA -> {
                                if (espacioElegido == null) {
                                    nuevos["espacio"] = "Elige el espacio que necesitas"
                                }
                            }
                        }

                        if (esEnvio) {
                            if (tamanoElegido == null) {
                                nuevos["tamano"] = "Elige el tamaño del paquete"
                            }
                            if (pesoNumero == null || pesoNumero <= 0.0) {
                                nuevos["peso"] = "Escribe un peso mayor que 0"
                            } else if (tamanoElegido != null && pesoNumero > tamanoElegido.pesoMaximoKg) {
                                nuevos["peso"] =
                                    "El máximo para este tamaño es ${tamanoElegido.pesoMaximoKg.toInt()} kg"
                            }
                        }

                        if (servicio.manejoEspecial && manejoElegido == null) {
                            nuevos["manejo"] = "Elige el tipo de manejo"
                        }

                        errores = nuevos
                        resultado = if (nuevos.isEmpty()) {
                            calcularCotizacion(
                                servicio = servicio,
                                origen = origen,
                                destino = destino,
                                tamano = tamanoElegido,
                                pesoKg = pesoNumero ?: 0.0,
                                manejo = manejoElegido,
                                espacio = espacioElegido,
                                dias = dias,
                                seguro = seguro,
                                recoleccion = recoleccion
                            )
                        } else {
                            null
                        }
                    },
                    enabled = faltantes.isEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Calcular tarifa")
                }
                OutlinedButton(
                    onClick = {
                        origen = ""
                        destino = ""
                        tamano = null
                        peso = ""
                        manejo = null
                        espacio = null
                        dias = 1
                        seguro = false
                        recoleccion = false
                        errores = emptyMap()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Restablecer")
                }
            }

            // Tarjeta con el desglose (solo aparece después de calcular)
            resultado?.let { TarjetaResultado(resultado = it) }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResumenServicio(servicio: Servicio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = servicio.imagen),
                contentDescription = servicio.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Servicio seleccionado",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = servicio.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = servicio.precioTexto,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}