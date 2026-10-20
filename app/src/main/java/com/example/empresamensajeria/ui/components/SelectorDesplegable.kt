package com.example.empresamensajeria.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectorDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente: al tocarla se abre el menú
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expandido = true }
        )
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            for (opcion in opciones) {
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}