package com.example.projectapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SingleRecipeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val recipes = mutableListOf<Recipe>()
    val recipesAdapter = RecipeAdapter(recipes)
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        fun loadLikeState(recipeId: String, likeButton: ImageView, likeCountTextView: TextView) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w("SingleRecipeActivity", "User not logged in")
                return
            }

            val userId = currentUser.uid
            val docRef = db.collection("recipes").document(recipeId)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likeCount = document.getLong("likeCount") ?: 0
                    val likedUsers = document.get("likedUsers") as? List<String> ?: listOf()

                    // Update like count text
                    likeCountTextView.text = "Likes: $likeCount"

                    // Update icon based on like status
                    val isLiked = likedUsers.contains(userId)
                    val iconRes = if (isLiked)
                        R.drawable.baseline_favorite_24
                    else
                        R.drawable.favorite_border_24dp_e3e3e3

                    likeButton.setImageResource(iconRes)
                }
            }.addOnFailureListener { e ->
                Log.w("SingleRecipeActivity", "Failed to load like state", e)
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_recipe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }








        val singleTitleText: TextView = findViewById(R.id.singleTitleText)
        val recipeThumbnail: ImageView = findViewById(R.id.recipeThumbnail)
        val ingredientsListView = findViewById<ListView>(R.id.ingredientsListView)
        val recipeId = intent.getStringExtra("recipeId")
        val recipeName = intent.getStringExtra("recipeName")
        val recipePicture = intent.getStringExtra("recipePicture")
        val prepTextView: TextView = findViewById(R.id.prepTextView)
        val likeButton: ImageView = findViewById(R.id.likeButton)
        val likeCountTextView: TextView = findViewById(R.id.likeCountTextView)
        singleTitleText.text = recipeName
        prepTextView.text = recipeName
        Picasso.get().load(recipePicture).into(recipeThumbnail)
        auth = Firebase.auth
        if (recipeId != null) {
            loadLikeState(recipeId, likeButton, likeCountTextView)
        }


        val docRef = db.collection("recipes").document(recipeId.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ingredients = document["ingredients"] as? List<String>
                    if (ingredients != null) {
                        // Set up the ListView with the ingredients
                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredients)
                        ingredientsListView.adapter = adapter
                    } else {
                        Log.d(TAG, "No ingredients found.")
                    }
                } else {
                    Log.d(TAG, "No such document.")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting document: ", exception)
            }

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likeCount = document.getLong("likeCount") ?: 0
                    //likeCountTextView.text = "Likes: $likeCount"
                }
            }

// Step 2: Increment like count when button is clicked
        likeButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w("SingleRecipeActivity", "User not logged in")
                return@setOnClickListener
            }
            val userId = currentUser.uid

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0
                val likedUsers = snapshot.get("likedUsers") as? List<String> ?: listOf()

                val newLikes: Long
                val newLikedUsers: List<String>
                val isLiked = likedUsers.contains(userId)

                if (isLiked) {
                    // Unlike
                    newLikes = (currentLikes - 1).coerceAtLeast(0)
                    newLikedUsers = likedUsers - userId
                } else {
                    // Like
                    newLikes = currentLikes + 1
                    newLikedUsers = likedUsers + userId
                }

                transaction.update(docRef, "likeCount", newLikes)
                transaction.update(docRef, "likedUsers", newLikedUsers)
            }.addOnSuccessListener {
                docRef.get().addOnSuccessListener { document ->
                    val updatedLikes = document.getLong("likeCount") ?: 0
                    likeCountTextView.text = "Likes: $updatedLikes"

                    val updatedLikedUsers = document.get("likedUsers") as? List<String> ?: listOf()
                    val isNowLiked = updatedLikedUsers.contains(userId)

                    // 🔁 Switch icon based on like status
                    val iconRes = if (isNowLiked)
                        R.drawable.baseline_favorite_24
                    else
                        R.drawable.favorite_border_24dp_e3e3e3

                    likeButton.setImageResource(iconRes)
                }
            }.addOnFailureListener { e ->
                Log.w("SingleRecipeActivity", "Transaction failed", e)
            }
        }






    }











    }

