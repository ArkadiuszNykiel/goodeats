package com.example.projectapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user)
        auth = Firebase.auth
        if (auth.currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val db = Firebase.firestore
        val recipes = mutableListOf<Recipe>()
        val userReyclerView: RecyclerView = findViewById(R.id.faveRecyclerView)
        val adapter = RecipeAdapter(recipes)
        userReyclerView.adapter = adapter
        userReyclerView.layoutManager = LinearLayoutManager(this)

        val logOutButton: Button = findViewById(R.id.logOutButton)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun fetchLikedRecipes(name: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(TAG, "User not logged in")
                return
            }

            val userId = currentUser.uid

            db.collection("recipes")
                .whereGreaterThanOrEqualTo("name", name)
                .get()
                .addOnSuccessListener { result ->
                    recipes.clear() // Clear old list
                    for (document in result) {
                        val likedUsers = document.get("likedUsers") as? List<*>
                        if (likedUsers != null && likedUsers.contains(userId)) {
                            recipes.add(
                                Recipe(
                                    document.id,
                                    document.getString("name") ?: "",
                                    document.getString("image") ?: ""
                                )
                            )
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }
        fetchLikedRecipes("")




    }


}