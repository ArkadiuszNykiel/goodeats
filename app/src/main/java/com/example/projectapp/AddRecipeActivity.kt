package com.example.projectapp


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class AddRecipeActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var imageURL: String = ""
    private lateinit var imageUri2: Uri
    private var imagePicked = false
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK)
        {
            val data: Intent? = it.data
            val imageUri = data?.data
            imageUri2 = imageUri!!
            imageView.setImageURI(imageUri)
            imagePicked = true
        }
    }
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
        imageView = findViewById(R.id.recipeImageView)

        val ingredientslist = mutableListOf<String>(

        )
        Picasso.get().load("https://i.imgur.com/DvpvklR.png").into(imageView)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }


        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredientslist)
        listView.adapter = adapter
        addIngredientButton.setOnClickListener{
            ingredientslist.add(ingredientEdit.text.toString())
            adapter.notifyDataSetChanged()
        }
        val name = nameEdit.text.toString()


        addButton.setOnClickListener{

            if(!imagePicked){
                imageURL = "https://i.imgur.com/DvpvklR.png"
            }
            else{
                val storageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageReference.child("images/" + UUID.randomUUID().toString())

                imageRef.putFile(imageUri2)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            imageURL = uri.toString()
                            val recipe = hashMapOf(
                                "name" to nameEdit.text.toString(),
                                "image" to imageURL,
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



        }




    }




}