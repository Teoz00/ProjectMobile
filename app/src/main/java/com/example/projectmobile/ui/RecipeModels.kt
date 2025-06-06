package com.example.projectmobile.ui

data class RecipeResponse(
    val results: List<Recipe>
)

data class Recipe(
    val id: Int,
    val title: String,
    val image: String
)

data class RecipeDetail(
    val id: Int,
    val title: String,
    val image: String,
    val extendedIngredients: List<Ingredient>,
    val instructions: String
)

data class Ingredient(
    val original: String
)