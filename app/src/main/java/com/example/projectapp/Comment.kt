package com.example.projectapp

data class Comment(val commentId: String, val userId: String, val recipeId: String, val commentText: String, val timestamp: com.google.firebase.Timestamp)
