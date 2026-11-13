package com.clemenssielaff.lingo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clemenssielaff.lingo.data.Word
import com.clemenssielaff.lingo.ui.LingoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(viewModel: LingoViewModel, onNavigateBack: () -> Unit) {
    val words by viewModel.practiceWords.collectAsStateWithLifecycle()
    var index by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    val current: Word? = words.getOrNull(index)

    fun advance() {
        if (index < words.size - 1) {
            index++
            flipped = false
        } else {
            done = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Üben") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        when {
            words.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Noch keine Vokabeln vorhanden.", textAlign = TextAlign.Center)
                }
            }

            done || current == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Geschafft!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("${words.size} Vokabeln geübt.")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) { Text("Zurück zur Übersicht") }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Karte ${index + 1} von ${words.size}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { flipped = !flipped },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (flipped) current.translation else current.term,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                if (flipped && current.example.isNotBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = current.example,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (flipped) {
                                        "Tippe, um den Begriff zu sehen"
                                    } else {
                                        "Tippe, um die Übersetzung zu sehen"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.markWrong(current)
                                advance()
                            },
                        ) {
                            Text("Nochmal üben")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.markCorrect(current)
                                advance()
                            },
                        ) {
                            Text("Gewusst")
                        }
                    }
                }
            }
        }
    }
}
