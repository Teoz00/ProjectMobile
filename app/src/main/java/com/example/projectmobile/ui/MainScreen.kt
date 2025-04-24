package com.example.projectmobile.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val foodList = remember { mutableStateListOf<FoodItem>() }

    Scaffold(
        bottomBar = {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
            BottomNavigationBar(
                selectedScreen = currentDestination ?: "fridge",
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "fridge",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("fridge") {
                FridgeScreen(
                    foodList = foodList,
                    onAddButtonClicked = { navController.navigate("add_item") }
                )
            }
            composable("scan") { ScanScreen() }
            composable("shopping") { ShoppingScreen() }
            composable("profile") { ProfileScreen() }

            // Schermata di aggiunta alimento
            composable("add_item") {
                AddItemScreen(
                    onAddItem = { name, purchaseDate, expirationDate, imageUri ->
                        foodList.add(FoodItem(name, expirationDate, imageUri))
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}


