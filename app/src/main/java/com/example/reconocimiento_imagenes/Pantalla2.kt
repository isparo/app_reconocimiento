package com.example.reconocimiento_imagenes

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Pantalla2(
    viewModel: OpenAIImageAnalysisViewModel,
    onVolver: () -> Unit
) {
    val images = viewModel.generatedImages

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón Volver (Arriba Izquierda como en la imagen)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila superior: Imagen resultado 1 e Imagen resultado 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContenedorResultado(
                    bitmap = images.getOrNull(0),
                    modifier = Modifier.weight(1f)
                )
                ContenedorResultado(
                    bitmap = images.getOrNull(1),
                    modifier = Modifier.weight(1f)
                )
            }

            // Imagen Resultado 3 (Centrada abajo de las otras dos)
            ContenedorResultado(
                bitmap = images.getOrNull(2),
                modifier = Modifier
                    .fillMaxWidth(0.5f) // Más angosta que la fila superior
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sección de Textos descriptivos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "- Reconstructed piece",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "- Historical coloration",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "- Alternative coloration",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

/**
 * Componente reutilizable para los cuadros de imagen resultado
 */
@Composable
fun ContenedorResultado(bitmap: Bitmap?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Mantiene forma cuadrada
            .border(2.dp, Color.Gray, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
