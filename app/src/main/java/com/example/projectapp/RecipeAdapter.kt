package com.example.projectapp

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class RecipeAdapter(var recipes: MutableList<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    private var auth = Firebase.auth
    val db = Firebase.firestore


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val textView1: TextView = holder.itemView.findViewById(R.id.titleTextView)
        val imageView1: ImageView = holder.itemView.findViewById(R.id.imageView2)
        var doesExist = false
        val likeButton: ImageButton = holder.itemView.findViewById(R.id.likeButton)
        val docRef = db.collection("follows").document(auth.currentUser!!.uid+":"+recipes[position].id)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("TAG", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
        textView1.text = recipes[position].name
        Picasso.get().load(recipes[position].picture).into(imageView1)
        likeButton.setOnClickListener {

            if(!doesExist) {
                val follow = hashMapOf(
                    "uid" to auth.currentUser!!.uid,
                    "pid" to recipes[position].id
                )

                db.collection("follows").document(auth.currentUser!!.uid+":"+recipes[position].id)
                    .set(follow)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written with ID:")
                        doesExist = true
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                    }
            }
            else {
                db.collection("follows").document(auth.currentUser!!.uid+":"+recipes[position].id)
                    .delete()
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!")

                        doesExist = false
                    }
                    .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
            }

        }



    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}