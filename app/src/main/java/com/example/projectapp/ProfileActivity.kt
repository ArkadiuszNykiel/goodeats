import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectapp.MainActivity
import com.example.projectapp.R
import com.example.projectapp.Recipe
import com.example.projectapp.RecipeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val recipes = mutableListOf<Recipe>()
    val recipesAdapter = RecipeAdapter(recipes)
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
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        val currentUser = auth.currentUser
        val db = Firebase.firestore
        val signOutButton: Button = findViewById(R.id.signOutButton)

        val recipeRecyclerView: RecyclerView = findViewById(R.id.favRecyclerView)
        recipeRecyclerView.adapter = recipesAdapter
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)


        fun fetchRecipes(name: String) {
            db.collection("recipes").whereGreaterThanOrEqualTo("name", name)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        recipes.add(Recipe( document.id,

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


    }
}