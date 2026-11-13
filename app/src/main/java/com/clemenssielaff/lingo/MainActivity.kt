package com.clemenssielaff.lingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.clemenssielaff.lingo.ui.LingoApp
import com.clemenssielaff.lingo.ui.theme.LingoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LingoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LingoApp()
                }
            }
        }
    }
}
