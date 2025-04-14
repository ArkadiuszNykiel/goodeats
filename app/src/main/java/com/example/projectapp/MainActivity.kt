package com.example.projectapp
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


import org.w3c.dom.Text
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var imageURL: String = ""
    private lateinit var imageUri2: Uri
    private var imagePicked = false
    private lateinit var loadingdialog: Dialog
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
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        storage = Firebase.storage

        val currentUser = auth.currentUser
        val db = Firebase.firestore

        val emailEdit: EditText = findViewById(R.id.registerName)
        val passwordEdit: EditText = findViewById(R.id.registerPass)
        val registerButton: Button = findViewById(R.id.registerButton)
        val movetoLogin: TextView = findViewById(R.id.loginTextView)
        imageView = findViewById(R.id.imageView)
        loadingdialog = createLoadingDialog(this)

        Picasso.get().load("https://i.imgur.com/DvpvklR.png").into(imageView)


        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }



        registerButton.setOnClickListener {
            if(emailEdit.text.isEmpty() || passwordEdit.text.isEmpty()){
                Toast.makeText(
                    baseContext,
                    "Fields cannot be empty",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(emailEdit.text.toString(), passwordEdit.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = auth.currentUser
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Hello:"+user!!.email.toString(),
                            Toast.LENGTH_SHORT,
                        ).show()
                        if(!imagePicked){
                            imageURL = "https://i.imgur.com/DvpvklR.png"
                        }
                        else{
                            val storageReference = FirebaseStorage.getInstance().reference
                            val imageRef = storageReference.child("images/" + UUID.randomUUID().toString())

                            imageRef.putFile(imageUri2)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    imageURL = uri.toString()}
                                }
                        }


                        val newUser = hashMapOf(
                            "name" to user.email,
                            "avatar" to imageURL
                        )

                        db.collection("users").document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully written!")
                                loadingdialog.show()
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

        }
        movetoLogin.setOnClickListener{
            loadingdialog.show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("Main", "Nie zalogowany")
            Toast.makeText(
                baseContext,
                "Niezalogowany",

                Toast.LENGTH_SHORT,
            ).show()
        }
        else {
            Log.d("Main", "Zalogowany")
            Toast.makeText(
                baseContext,
                "Zalogowany",
                Toast.LENGTH_SHORT,
            ).show()
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
        }
    }
    private fun createLoadingDialog(context: Context): Dialog{
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

