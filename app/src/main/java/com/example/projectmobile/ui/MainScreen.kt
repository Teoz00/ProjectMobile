package com.example.projectmobile.ui

import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Lista di alimenti in memoria
    val foodList = remember { mutableStateListOf<FoodItem>() }
    // Lista di shopping (rimane inalterata)
    val shoppingItems = remember { mutableStateListOf<Pair<String, Boolean>>() }

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

            // Schermata Fridge: mostra la lista attuale di foodList
            composable("fridge") {
                FridgeScreen(
                    foodList = foodList,
                    onAddButtonClicked = {
                        // Quando si preme il FAB “+” in FridgeScreen, si va alla schermata di inserimento manuale
                        navController.navigate("add_item")
                    },
                    onFoodItemClicked = { foodItem ->
                        navController.navigate("food_detail/${foodItem.name}")
                    },
                    onDeleteItem = { item ->
                        // Rimuovi l’elemento dalla lista
                        foodList.remove(item)
                    }
                )
            }


            // Schermata Scan: qui passo un callback che aggiunge newItem a foodList
            // adesso navighi esplicitamente a "fridge", rimuovendo "scan" dallo stack
            composable("scan") {
                ScanScreen(
                    onAddScannedItem = { newItem ->
                        // 1) aggiungo alla lista
                        foodList.add(newItem)

                        // 2) navigo a "fridge" cancellando "scan" dallo stack
                        navController.navigate("fridge") {
                            popUpTo("scan") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }


            // Schermata Shopping (rimane invariata)
            composable("shopping") {
                ShoppingScreen(
                    items = shoppingItems,
                    onBack = { navController.popBackStack() }
                )
            }

            // Schermata Profile (rimane invariata)
            composable("profile") {
                ProfileScreen(navController)
            }

            // Schermata di aggiunta manuale alimento
            composable("add_item") {
                AddItemScreen(
                    onAddItem = { name, purchaseDate, expirationDate, imageUri ->
                        foodList.add(FoodItem(name, expirationDate, imageUri))
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Schermata di dettaglio di un alimento
            composable(
                route = "food_detail/{itemName}",
                arguments = listOf(navArgument("itemName") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
                val foodItem = foodList.find { it.name == itemName }

                if (foodItem != null) {
                    FoodDetailScreen(
                        foodItem = foodItem,
                        navController = navController,
                        onSave = { updatedItem ->
                            val index = foodList.indexOfFirst { it.name == foodItem.name }
                            if (index != -1) {
                                // Sostituisco l’elemento modificato
                                foodList[index] = updatedItem
                            }
                        }
                    )
                } else {
                    // Se non trovo l’elemento, mostro un messaggio di errore
                    Text(
                        text = "Item not found",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }

            // Schermata lista ricette (rimane invariata)
            composable("recipes/{foodName}") { backStackEntry ->
                val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
                RecipeListScreen(foodName = foodName, navController = navController)
            }

            // Schermata dettaglio ricetta (rimane invariata)
            composable("recipeDetail/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
                RecipeDetailScreen(recipeId, navController = navController)
            }
        }
    }
}
