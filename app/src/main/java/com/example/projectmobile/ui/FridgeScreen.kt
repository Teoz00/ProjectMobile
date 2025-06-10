package com.example.projectmobile.ui

//import java.time.LocalDateAdd
//import java.time.LocalDateAdd
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.ui.draw.rotate
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FridgeScreen(
    foodList: MutableList<FoodItem>,
    onAddButtonClicked: () -> Unit,
    onFoodItemClicked: (FoodItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    var mediaScadenza by remember { mutableStateOf<Double?>(null) }
            LaunchedEffect(foodList.toList()) {
                calcolaMediaScadenze(foodList) { media ->
                    mediaScadenza = media
                }
            }

    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val oggi = Calendar.getInstance().time

    val prodottiInScadenza by remember(foodList) {
        derivedStateOf {
            foodList.filter {
                try {
                    val scadenza = formatter.parse(it.expirationDate)
                    val diffMillis = scadenza.time - oggi.time
                    val giorniRimanenti = TimeUnit.MILLISECONDS.toDays(diffMillis)
                    giorniRimanenti in 0..3
                } catch (e: Exception) {
                    false
                }
            }
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddButtonClicked,
                containerColor = Color(0xFF3E6D8E),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE6F1F6))
        ) {
            // Parte blu in cima (navbar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF3E6D8E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FridgeScan",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Barra di ricerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search your items...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = " 📊 Calculate average days remaining:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3E6D8E)
                )

                if (mediaScadenza != null) {
                    Text(
                        text = " ${"%.1f".format(mediaScadenza)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E6D8E)
                    )
                }
            }

            if (prodottiInScadenza.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ Products expiring soon:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val oggi = Calendar.getInstance().time

                        prodottiInScadenza.forEach { prodotto ->
                            val scadenzaDate = formatter.parse(prodotto.expirationDate)
                            val diffMillis = scadenzaDate.time - oggi.time
                            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

                            // la clessidra animata
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "- ${prodotto.name} ($diffDays days left)",
                                    color = Color(0xFF856404)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                RotatingHourglass(modifier = Modifier.size(20.dp)) // <<--- CLessidra animata qui
                            }
                        }
                    }
                }
            }

            // Lista degli ingredienti filtrati
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Applica i paddingValues solo alla lista, non alla parte blu
            ) {
                items(foodList.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }) { item ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToStart) {
                                foodList.remove(item)
                            }
                            true
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color.Red),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
                            }
                        },
                        dismissContent = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onFoodItemClicked(item) },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ){
                                Row(
                                    modifier = Modifier.padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item.imageUri?.let {
                                        Image(
                                            painter = rememberAsyncImagePainter(it),
                                            contentDescription = item.name,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(
                                            text = "Expiration: ${item.expirationDate}",
                                            color = colorFromDate(item.expirationDate)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(colorFromDate(item.expirationDate))
                                    )
                                }
                            }
                        }
                    )

                    // Aggiungi uno spacer tra le card
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
@Composable
fun RotatingHourglass(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        )
    )
    Icon(
        imageVector = Icons.Filled.HourglassEmpty,
        contentDescription = "Hourglass",
        modifier = modifier.rotate(rotation)
    )
}

