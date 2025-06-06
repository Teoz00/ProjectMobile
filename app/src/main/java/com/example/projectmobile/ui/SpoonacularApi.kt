package com.example.projectmobile.ui

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface SpoonacularApi {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 5,
        @Query("apiKey") apiKey: String = "5a65b4d8076244288e6c755e26582bdd"
    ): RecipeResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetail(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String = "5a65b4d8076244288e6c755e26582bdd"
    ): RecipeDetail

}
