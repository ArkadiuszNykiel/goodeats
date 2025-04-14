package com.example.projectapp

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val recipes = mutableListOf<Recipe>()
    val recipesAdapter = RecipeAdapter(recipes)
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingdialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = Firebase.auth
        val signOutButton: Button = findViewById(R.id.signoutButton)
        val currentUser = auth.currentUser
        val addRecipeButton: Button = findViewById(R.id.addrecipeButton)
        loadingdialog = createLoadingDialog(this)

        val recipeRecyclerView: RecyclerView = findViewById(R.id.recipeRecyclerView)
        recipeRecyclerView.adapter = recipesAdapter
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)



        fun fetchRecipes(name: String) {
            db.collection("recipes").whereGreaterThanOrEqualTo("name", name)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        recipes.add(Recipe(

                            document.data["name"].toString(),
                            document.data["image"].toString()
                            ))

                    }
                    recipesAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }
        fetchRecipes("")





        signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        addRecipeButton.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    private fun createLoadingDialog(context: Context): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.loadingdialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog


    }

}