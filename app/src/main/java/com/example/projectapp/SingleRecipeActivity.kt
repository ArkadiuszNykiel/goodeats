package com.example.projectapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticationguide.CommentAdapter
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
        val comments = mutableListOf<Comment>()
        val addCommentButton: androidx.appcompat.widget.AppCompatButton =
            findViewById(R.id.addCommentButton)
        val commentEdit: EditText = findViewById(R.id.commentEdit)

        singleTitleText.text = recipeName
        prepTextView.text = recipeName
        Picasso.get().load(recipePicture).into(recipeThumbnail)
        auth = Firebase.auth
        if (recipeId != null) {
            loadLikeState(recipeId, likeButton, likeCountTextView)
        }
        val adapter = CommentAdapter(comments)
        val commentRecyclerView = findViewById<RecyclerView>(R.id.commentRecyclerView)
        commentRecyclerView.adapter = adapter
        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        val deleteButton: ImageView = findViewById(R.id.deleteButton)




        val docRef = db.collection("recipes").document(recipeId.toString())

        docRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val createdBy = document.getString("userid")
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (createdBy == currentUserId) {
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.setOnClickListener {
                        docRef.delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "Recipe deleted successfully")
                                finish() // Optional: close activity
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error deleting recipe", e)
                            }
                    }
                } else {
                    deleteButton.visibility = View.GONE
                }
            }
        }



        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ingredients = document["ingredients"] as? List<String>
                    if (ingredients != null) {
                        // Set up the ListView with the ingredients
                        val adapter =
                            ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredients)
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

                    newLikes = (currentLikes - 1).coerceAtLeast(0)
                    newLikedUsers = likedUsers - userId
                } else {

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
        db.collection("comments").whereEqualTo("recipeId", recipeId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("SinglePost", "${document.id} => ${document.data}")
                    comments.add(
                        Comment(
                            document.id,
                            document.data["uid"].toString(),
                            document.data["postId"].toString(),
                            document.data["text"].toString(),
                            document.data["timestamp"] as com.google.firebase.Timestamp
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("SinglePost", "Error getting documents: ", exception)
            }

        addCommentButton.setOnClickListener {
            val comment = hashMapOf(
                "text" to commentEdit.text.toString(),
                "uid" to auth.currentUser!!.uid,
                "recipeId" to recipeId,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("comments")
                .add(comment)
                .addOnSuccessListener { documentReference ->
                    Log.d("Comment", "DocumentSnapshot written with ID: ${documentReference.id}")
                    commentEdit.text.clear()
                    commentEdit.onEditorAction(0)
                    db.collection("comments").whereEqualTo("postId", recipeId)
                        .get()
                        .addOnSuccessListener { result ->
                            comments.clear()
                            for (document in result) {
                                Log.d("SinglePost", "${document.id} => ${document.data}")
                                comments.add(
                                    Comment(
                                        document.id,
                                        document.data["uid"].toString(),
                                        document.data["recipeId"].toString(),
                                        document.data["text"].toString(),
                                        document.data["timestamp"] as com.google.firebase.Timestamp
                                    )
                                )
                            }
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("SinglePost", "Error getting documents: ", exception)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("Comment", "Error adding document", e)
                }


        }
        fun deleteRecipeIfOwner(recipeId: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return
            val docRef = db.collection("recipes").document(recipeId)

            docRef.get().addOnSuccessListener { document ->
                val createdBy = document.getString("userid")
                if (createdBy == currentUser.uid) {
                    docRef.delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "Recipe deleted successfully")

                        }

                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error deleting recipe", e)
                        }
                } else {
                    Log.w(TAG, "User not authorized to delete this recipe")
                }
            }
        }
        deleteButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            deleteRecipeIfOwner(recipeId.toString())
        }

    }


    }

