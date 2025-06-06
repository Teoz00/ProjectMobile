package com.example.projectmobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodItem: FoodItem,
    navController: NavController,
    onSave: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf(foodItem.name) }
    //var purchaseDate by remember { mutableStateOf(foodItem.purchaseDate) }
    var expirationDate by remember { mutableStateOf(foodItem.expirationDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Item Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3E6D8E)
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE6F1F6))
                    .padding(paddingValues)
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.width(50.dp))
                // Box alimento
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = foodItem.imageUri),
                            contentDescription = foodItem.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                text = "Expiration: $expirationDate",
                                color = colorFromDate(expirationDate),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(colorFromDate(expirationDate))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Campo Name
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF3E6D8E),
                        unfocusedIndicatorColor = Color(0xFF3E6D8E),
                        cursorColor = Color(0xFF3E6D8E),
                        focusedLabelColor = Color(0xFF3E6D8E)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF3E6D8E)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Expiration Date
                TextField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it },
                    label = { Text("Expiration Date") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF3E6D8E),
                        unfocusedIndicatorColor = Color(0xFF3E6D8E),
                        cursorColor = Color(0xFF3E6D8E),
                        focusedLabelColor = Color(0xFF3E6D8E)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF3E6D8E)
                        )
                    }
                )


                Spacer(modifier = Modifier.height(35.dp))

                // Bottone Salva
                Button(
                    onClick = {
                        onSave(
                            foodItem.copy(
                                name = name,
                                //purchaseDate = purchaseDate,
                                expirationDate = expirationDate
                            )
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6D8E))
                ) {
                    Text("Save", color = Color.White)
                }

                Spacer(modifier = Modifier.height(160.dp))

                // Sezione ATTENTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.border(2.dp, Color.Red, shape = MaterialTheme.shapes.medium)
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("ATTENTION", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Do you want a recipe for this product?")

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate("recipes/${foodItem.name}")
                                }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Yes", tint = Color.Green)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Yes", color = Color.Green)
                            }


                            /*Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "No", tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("No", color = Color.Red)
                            }*/
                        }
                    }
                }

            }
        }
    )
}
