package com.example.reconocimiento_imagenes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Pantalla3(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón Volver (Arriba Izquierda)
        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text("<-")
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Contenedor de Imagen Ampliada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Lo hace cuadrado según la imagen
                    .border(2.dp, Color.Gray, RoundedCornerShape(48.dp)), // Bordes más redondeados
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "imagen Ampliada",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Botón Descargar (Alineado a la izquierda según la imagen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(
                    onClick = { /* Acción descargar de momento vacía */ },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Boton Descargar")
                }
            }
        }
    }
}