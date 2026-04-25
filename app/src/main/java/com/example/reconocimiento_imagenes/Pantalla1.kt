package com.example.reconocimiento_imagenes

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Pantalla1(onIrAPantalla2: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón de Configuración (Superior Derecha)
        OutlinedButton(
            onClick = { /* Acción Conf */ },
            modifier = Modifier.align(Alignment.TopEnd),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Conf")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp), // Espacio para no chocar con el botón Conf
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Contenedor de Imagen Vista Previa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .border(2.dp, Color.Gray, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Vista Previa",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Fila de botones Seleccionar y Tomar Foto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { /* Acción Seleccionar */ },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Boton Seleccionar")
                }

                OutlinedButton(
                    onClick = { /* Acción Tomar foto */ },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Boton Tomar\nfoto", textAlign = TextAlign.Center)
                }
            }

            // Descripción de la APP
            Text(
                text = "Descripcion de la APP",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1.0f))

            // Botón Analizar (Inferior Derecha)
            OutlinedButton(
                onClick = onIrAPantalla2,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text("Boton Analizar")
            }
        }
    }
}