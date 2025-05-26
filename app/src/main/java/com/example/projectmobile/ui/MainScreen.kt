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
    val shoppingItems = remember { mutableStateListOf<Pair<String, Boolean>>() }
    //val showBottomBar = currentDestination != "login"

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = currentRoute !in listOf("splash", "login", "register")

            if (showBottomBar) {
                BottomNavigationBar(
                    selectedScreen = currentRoute ?: "fridge",
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("login") {
                LoginScreen(navController)
            }
            composable("register") {
                RegisterScreen(navController)
            }
            composable("fridge") {
                FridgeScreen(
                    foodList = foodList,
                    onAddButtonClicked = { navController.navigate("add_item") }
                )
            }
            composable("scan") { ScanScreen() }
            composable("shopping") {
                ShoppingScreen(
                    items = shoppingItems,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("profile") { ProfileScreen(navController) }

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


