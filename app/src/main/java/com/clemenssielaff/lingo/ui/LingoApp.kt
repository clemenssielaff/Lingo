package com.clemenssielaff.lingo.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clemenssielaff.lingo.ui.screens.AddWordScreen
import com.clemenssielaff.lingo.ui.screens.HomeScreen
import com.clemenssielaff.lingo.ui.screens.PracticeScreen

@Composable
fun LingoApp(viewModel: LingoViewModel = viewModel(factory = LingoViewModel.Factory)) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onPracticeClick = { navController.navigate("practice") },
                onAddClick = { navController.navigate("add") },
            )
        }
        composable("practice") {
            PracticeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("add") {
            AddWordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
