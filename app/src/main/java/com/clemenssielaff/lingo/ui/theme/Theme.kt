package com.clemenssielaff.lingo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LingoColorScheme = lightColorScheme(
    primary = Color(0xFF3D5AFE),
    onPrimary = Color.White,
    secondary = Color(0xFF00BFA5),
    background = Color(0xFFF5F7FB),
    surface = Color.White,
)

@Composable
fun LingoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LingoColorScheme,
        content = content,
    )
}
