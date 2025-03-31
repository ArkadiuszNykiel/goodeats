package com.example.projectapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddRecipeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recipe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        val currentUser = auth.currentUser
        val nameEdit: EditText = findViewById(R.id.nameEditText)
        val ingredientEdit: EditText = findViewById(R.id.IngredientEditText)
        val listView: ListView = findViewById(R.id.listView)
        val recipeEdit: EditText = findViewById(R.id.recipeEditText)
        val addButton: Button = findViewById(R.id.button2)
        val addIngredientButton: Button = findViewById(R.id.addIngredientButton)
        val db = Firebase.firestore

        val ingredientslist = mutableListOf<String>(

        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredientslist)
        listView.adapter = adapter
        addIngredientButton.setOnClickListener{
            ingredientslist.add(ingredientEdit.text.toString())
            adapter.notifyDataSetChanged()
        }
        val name = nameEdit.text.toString()


        addButton.setOnClickListener{
            val recipe = hashMapOf(
                "name" to nameEdit.text.toString(),
                "ingredients" to ingredientslist,
                "instructions" to recipeEdit.text.toString(),
                "userid" to currentUser?.uid

            )
            db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
        }




    }




}