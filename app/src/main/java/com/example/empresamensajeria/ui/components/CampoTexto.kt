package com.example.empresamensajeria.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto con el estilo de la app: bordes redondeados, mensaje de error
 * debajo (o un texto de ayuda si no hay error) y teclado configurable.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    accionTeclado: ImeAction = ImeAction.Next,
    habilitado: Boolean = true,
    // Para contraseñas u otros campos especiales
    transformacionVisual: VisualTransformation = VisualTransformation.None,
    iconoFinal: (@Composable () -> Unit)? = null,
    // Qué hacer al tocar la tecla de acción (por ejemplo "Listo"); si es null, el comportamiento normal
    onAccionTeclado: (() -> Unit)? = null
) {
    val textoInferior = error ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
        isError = error != null,
        supportingText = textoInferior?.let { { Text(it) } },
        visualTransformation = transformacionVisual,
        trailingIcon = iconoFinal,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalizacion,
            keyboardType = tipoTeclado,
            imeAction = accionTeclado
        ),
        keyboardActions = onAccionTeclado?.let { accion -> KeyboardActions(onAny = { accion() }) }
            ?: KeyboardActions.Default,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}
