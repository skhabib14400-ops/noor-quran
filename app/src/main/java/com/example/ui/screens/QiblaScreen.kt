package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppLanguage
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage
    val location = settings.selectedCity

    // Calculate Qibla bearing from current location to Kaaba (Makkah: 21.4225° N, 39.8262° E)
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLng = Math.toRadians(39.8262)
    val userLat = Math.toRadians(location.latitude)
    val userLng = Math.toRadians(location.longitude)

    val dLng = kaabaLng - userLng
    val y = sin(dLng) * cos(kaabaLat)
    val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(dLng)
    val qiblaBearingFromNorth = ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()

    var deviceAzimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthInRadians = orientation[0]
                    val azimuthInDegrees = ((Math.toDegrees(azimuthInRadians.toDouble()) + 360) % 360).toFloat()
                    deviceAzimuth = azimuthInDegrees
                } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    deviceAzimuth = event.values[0]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotationSensor?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val animatedCompassRotation by animateFloatAsState(
        targetValue = -deviceAzimuth,
        label = "compass_rot"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Location Info Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = AppStrings.get("qibla", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                val cityName = when (lang) {
                    AppLanguage.BENGALI -> location.nameBn
                    AppLanguage.ARABIC -> location.nameAr
                    AppLanguage.ENGLISH -> location.nameEn
                }
                Text(
                    text = "$cityName • ${location.country}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%.1f", qiblaBearingFromNorth)}° from North",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = QuranGold
                )
            }
        }

        // Compass Visual
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(6.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Dial with Compass markings
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedCompassRotation)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 16.dp.toPx()

                    // Cardinal directions
                    for (i in 0 until 360 step 30) {
                        val angleRad = Math.toRadians(i.toDouble() - 90)
                        val start = Offset(
                            center.x + (radius - 12.dp.toPx()) * cos(angleRad).toFloat(),
                            center.y + (radius - 12.dp.toPx()) * sin(angleRad).toFloat()
                        )
                        val end = Offset(
                            center.x + radius * cos(angleRad).toFloat(),
                            center.y + radius * sin(angleRad).toFloat()
                        )
                        drawLine(
                            color = if (i % 90 == 0) Color.Red else Color.Gray,
                            start = start,
                            end = end,
                            strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5f.dp.toPx()
                        )
                    }
                }

                // Kaaba needle inside rotating compass
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(qiblaBearingFromNorth),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Qibla Direction",
                            tint = QuranGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "🕋",
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Center stationary marker
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(QuranGold)
            )
        }

        // Instruction Footer
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (lang == AppLanguage.BENGALI) "ফোনটি সমান অনুভূমিক তলে রাখুন এবং গোল্ডেন কাবা নির্দেশক বরাবর ঘুরুন।"
                else if (lang == AppLanguage.ARABIC) "ضع الهاتف بشكل أفقي مستوٍ واتجه نحو مؤشر الكعبة الذهبي."
                else "Place phone on a flat horizontal surface and align with the golden Kaaba indicator.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}
