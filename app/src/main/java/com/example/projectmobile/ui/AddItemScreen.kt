package com.example.projectmobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.CameraAlt
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onAddItem: (String, String, String, Uri?) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Add New Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF3E6D8E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE6F1F6))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // Riquadro per foto alimento
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3E6D8E))
                    .align(Alignment.CenterHorizontally),

                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    IconButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            tint = Color.White,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Nome alimento
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name", color = Color.Gray)},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF3E6D8E),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Data acquisto
            OutlinedTextField(
                value = purchaseDate,
                onValueChange = { purchaseDate = it },
                label = { Text("Purchase Date (gg/mm/aaaa)", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF3E6D8E),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Data scadenza
            OutlinedTextField(
                value = expirationDate,
                onValueChange = { expirationDate = it },
                label = { Text("Expiration Date (gg/mm/aaaa)", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF3E6D8E),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Bottone di salvataggio
            Button(
                onClick = {
                    onAddItem(name, purchaseDate, expirationDate, imageUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6D8E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add Item",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
