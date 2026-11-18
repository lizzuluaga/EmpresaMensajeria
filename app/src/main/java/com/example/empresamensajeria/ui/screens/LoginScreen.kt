package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.empresamensajeria.data.session.Rol
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.CampoContrasena
import com.example.empresamensajeria.ui.components.CampoTexto
import com.example.empresamensajeria.ui.viewmodel.LoginUiState
import com.example.empresamensajeria.ui.viewmodel.LoginViewModel
import com.example.empresamensajeria.ui.viewmodel.ModoLogin
import com.example.empresamensajeria.ui.viewmodel.PestanaLogin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onMenuClick: () -> Unit,
    onSesionIniciada: (Rol) -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val alIniciarSesion by rememberUpdatedState(onSesionIniciada)

    // Evento de sesión iniciada: navega según el rol
    LaunchedEffect(viewModel) {
        viewModel.sesionIniciada.collect { rol -> alIniciarSesion(rol) }
    }

    val registrando = estado.modo == ModoLogin.REGISTRARSE

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (registrando) "Crear cuenta" else "Iniciar sesión",
                onMenuClick = onMenuClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (registrando) "📝" else "🔐",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Text(
                text = if (registrando) "Crea tu cuenta" else "Bienvenido a EnviaYa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = when {
                    registrando -> "Usa el documento con el que contrataste tu servicio de bodega."
                    estado.pestana == PestanaLogin.ADMIN -> "Ingresa para administrar clientes, contratos y envíos."
                    else -> "Ingresa para consultar y administrar tu bodega."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (registrando) {
                FormularioRegistro(estado, viewModel)
            } else {
                // Pestañas Cliente / Administrador
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PestanaLogin.entries.forEachIndexed { indice, pestana ->
                        SegmentedButton(
                            selected = estado.pestana == pestana,
                            onClick = { viewModel.cambiarPestana(pestana) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = indice,
                                count = PestanaLogin.entries.size
                            )
                        ) {
                            Text(if (pestana == PestanaLogin.CLIENTE) "Cliente" else "Administrador")
                        }
                    }
                }
                FormularioInicioSesion(estado, viewModel)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FormularioInicioSesion(estado: LoginUiState, viewModel: LoginViewModel) {
    if (estado.pestana == PestanaLogin.CLIENTE) {
        CampoTexto(
            valor = estado.documento,
            onValorChange = viewModel::onDocumentoChange,
            etiqueta = "Número de documento",
            tipoTeclado = KeyboardType.Number,
            habilitado = !estado.cargando
        )
    } else {
        CampoTexto(
            valor = estado.usuario,
            onValorChange = viewModel::onUsuarioChange,
            etiqueta = "Usuario",
            habilitado = !estado.cargando
        )
    }
    CampoContrasena(
        valor = estado.contrasena,
        onValorChange = viewModel::onContrasenaChange,
        etiqueta = "Contraseña",
        habilitado = !estado.cargando,
        onAccionTeclado = viewModel::iniciarSesion
    )

    estado.error?.let { MensajeError(it) }

    BotonPrincipal(
        texto = "Ingresar",
        habilitado = estado.puedeIngresar,
        cargando = estado.cargando,
        onClick = viewModel::iniciarSesion
    )

    // El registro es solo para clientes
    if (estado.pestana == PestanaLogin.CLIENTE) {
        TextButton(onClick = { viewModel.cambiarModo(ModoLogin.REGISTRARSE) }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Composable
private fun FormularioRegistro(estado: LoginUiState, viewModel: LoginViewModel) {
    val verificado = estado.nombreVerificado

    CampoTexto(
        valor = estado.documento,
        onValorChange = viewModel::onDocumentoChange,
        etiqueta = "Número de documento",
        error = estado.errorDocumento,
        tipoTeclado = KeyboardType.Number,
        accionTeclado = if (verificado == null) ImeAction.Done else ImeAction.Next,
        habilitado = !estado.cargando,
        onAccionTeclado = if (verificado == null) viewModel::verificarDocumento else null
    )

    if (verificado == null) {
        // Paso 1: verificar que el documento esté habilitado
        estado.error?.let { MensajeError(it) }
        BotonPrincipal(
            texto = "Verificar documento",
            habilitado = estado.puedeVerificar,
            cargando = estado.cargando,
            onClick = viewModel::verificarDocumento
        )
    } else {
        // Paso 2: crear la contraseña
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "👋 Hola, $verificado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea una contraseña para tu cuenta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        CampoContrasena(
            valor = estado.contrasena,
            onValorChange = viewModel::onContrasenaChange,
            etiqueta = "Contraseña",
            error = estado.errorContrasenaNueva,
            ayuda = "Mínimo 6 caracteres",
            accionTeclado = ImeAction.Next,
            habilitado = !estado.cargando
        )
        CampoContrasena(
            valor = estado.confirmacion,
            onValorChange = viewModel::onConfirmacionChange,
            etiqueta = "Confirmar contraseña",
            error = estado.errorConfirmacion,
            habilitado = !estado.cargando,
            onAccionTeclado = viewModel::crearCuenta
        )
        estado.error?.let { MensajeError(it) }
        BotonPrincipal(
            texto = "Crear cuenta",
            habilitado = estado.puedeCrearCuenta,
            cargando = estado.cargando,
            onClick = viewModel::crearCuenta
        )
    }

    TextButton(onClick = { viewModel.cambiarModo(ModoLogin.INICIAR_SESION) }) {
        Text("¿Ya tienes cuenta? Inicia sesión")
    }
}

@Composable
private fun MensajeError(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = "⚠️ $mensaje",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun BotonPrincipal(
    texto: String,
    habilitado: Boolean,
    cargando: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(texto)
        }
    }
}
