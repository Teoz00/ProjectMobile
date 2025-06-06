package com.example.projectmobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow



class RecipeViewModel : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes = _recipes.asStateFlow()

    private val _recipeDetail = MutableStateFlow<RecipeDetail?>(null)
    val recipeDetail: StateFlow<RecipeDetail?> = _recipeDetail.asStateFlow()

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.searchRecipes(query)
                _recipes.value = response.results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRecipeDetail(id: Int) {
        viewModelScope.launch {
            try {
                val detail = RetrofitClient.api.getRecipeDetail(id)
                _recipeDetail.value = detail
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
