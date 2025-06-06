package com.example.projectmobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    viewModel: RecipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController
) {
    val recipeDetail by viewModel.recipeDetail.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.getRecipeDetail(recipeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recipe Details",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3E6D8E))
            )
        },
        containerColor = Color(0xFFE6F1F6)
    ) { padding ->
        recipeDetail?.let { detail ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item {
                    Text(detail.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(detail.image),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
                    detail.extendedIngredients.forEach {
                        Text("- ${it.original}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Instructions:", style = MaterialTheme.typography.titleMedium)
                    Text(detail.instructions)
                }
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}
