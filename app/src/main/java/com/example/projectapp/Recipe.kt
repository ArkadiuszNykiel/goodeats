package com.example.projectapp

import android.widget.ImageView

data class Recipe(val id: String, val name: String, val picture: String, val ingredients: List<String> = emptyList())

