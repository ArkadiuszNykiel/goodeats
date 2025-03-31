package com.example.projectapp
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var loadingdialog: Dialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth

        val currentUser = auth.currentUser
        val db = Firebase.firestore

        val emailEdit: EditText = findViewById(R.id.registerName)
        val passwordEdit: EditText = findViewById(R.id.registerPass)
        val registerButton: Button = findViewById(R.id.registerButton)
        val movetoLogin: TextView = findViewById(R.id.loginTextView)
        loadingdialog = createLoadingDialog(this)






        registerButton.setOnClickListener {
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

                        val newUser = hashMapOf(
                            "name" to user.email
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

