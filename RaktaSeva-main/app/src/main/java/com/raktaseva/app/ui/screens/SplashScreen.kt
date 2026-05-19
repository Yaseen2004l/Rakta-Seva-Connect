package com.raktaseva.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.raktaseva.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF1A0A0A), BgDeep),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.scale(scale.value)
        ) {
            Text("🩸", fontSize = 72.sp)
            Text(
                "Rakta-Seva",
                color      = Blood,
                fontSize   = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Connect",
                color      = TextPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )
            Text(
                "Every second counts",
                color    = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

// need this import for dp inside the file
private val Int.dp get() = androidx.compose.ui.unit.Dp(this.toFloat())