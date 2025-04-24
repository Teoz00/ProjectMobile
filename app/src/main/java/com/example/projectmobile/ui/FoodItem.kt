package com.example.projectmobile.ui

import android.net.Uri

data class FoodItem(
    val name: String,
    val expirationDate: String,
    val imageUri: Uri?
)
