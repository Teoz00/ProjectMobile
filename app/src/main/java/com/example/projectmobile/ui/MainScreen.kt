package com.example.projectmobile.ui

//import okhttp3.OkHttpClientAdd
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val foodList = remember { mutableStateListOf<FoodItem>() }
    val shoppingItems = remember { mutableStateListOf<Pair<String, Boolean>>() }
    //val showBottomBar = currentDestination != "login"

    val mediaScadenza = remember { mutableStateOf<Double?>(null) }

            LaunchedEffect(foodList) {
                calcolaMediaScadenze(foodList) { media ->
                    mediaScadenza.value = media
                }
            }

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
                    onAddButtonClicked = { navController.navigate("add_item") },
                    onFoodItemClicked = { foodItem ->
                        navController.navigate("food_detail/${foodItem.name}")
                    }
                )
            }
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
                                foodList[index] = updatedItem
                            }
                        }
                    )
                } else {
                    Text(
                        text = "Item not found",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }

            composable("recipes/{foodName}") { backStackEntry ->
                val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
                RecipeListScreen(foodName = foodName, navController = navController)
            }

            composable("recipeDetail/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
                RecipeDetailScreen(recipeId, navController = navController)
            }

        }
    }
}
val client = OkHttpClient()
val moshi = Moshi.Builder().build()

fun calcolaMediaScadenze(foodList: List<FoodItem>, onResult: (Double?) -> Unit) {
    val alimentiJson = foodList.map {
        mapOf("nome" to it.name, "scadenza" to it.expirationDate)
    }

    val adapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    val jsonBody = adapter.toJson(mapOf("alimenti" to alimentiJson))

    val requestBody = RequestBody.create(
        "application/json".toMediaTypeOrNull(),
        jsonBody
    )

    val request = Request.Builder()
        .url("https://Martinaa9.pythonanywhere.com/scadenza-media")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val json = JSONObject(body)
            val media = json.optDouble("media_scadenza", Double.NaN)
            onResult(if (media.isNaN()) null else media)
        }
    })
}

data class ProdottoInScadenza(
    val nome: String,
    val scadenza: String,
    val giorniRimanenti: Int
)

fun getProdottiInScadenza(
    foodList: List<FoodItem>,
    onResult: (List<ProdottoInScadenza>) -> Unit
) {
    val alimentiJson = foodList.map {
        mapOf("nome" to it.name, "scadenza" to it.expirationDate)
    }

    val adapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )
    val jsonBody = adapter.toJson(mapOf("alimenti" to alimentiJson))

    val requestBody = RequestBody.create(
        "application/json".toMediaTypeOrNull(),
        jsonBody
    )

    val request = Request.Builder()
        .url("https://Martinaa9.pythonanywhere.com/scadenze-imminenti")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(emptyList())
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val json = JSONObject(body)
            val array = json.optJSONArray("in_scadenza") ?: return onResult(emptyList())

            val result = mutableListOf<ProdottoInScadenza>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    ProdottoInScadenza(
                        nome = obj.getString("nome"),
                        scadenza = obj.getString("scadenza"),
                        giorniRimanenti = obj.getInt("giorni_rimanenti")
                    )
                )
            }
            onResult(result)
        }
    })
}

