package com.example.authenticationguide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectapp.Comment
import com.example.projectapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommentAdapter(val comments: MutableList<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    val db = Firebase.firestore
    var auth: FirebaseAuth = Firebase.auth
    val user = auth.currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val dateTextView: TextView = holder.itemView.findViewById(R.id.dateTextView)
        val userTextView: TextView = holder.itemView.findViewById(R.id.userTextView)
        val contentTextView: TextView = holder.itemView.findViewById(R.id.contentTextView)


        dateTextView.text = comments[position].timestamp.toDate().toString()
        contentTextView.text = comments[position].commentText
//        userTextView.text = comments[position].userId







        db.collection("users").document(comments[position].userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userTextView.text = document.data!!["name"].toString()
                }
            }
            .addOnFailureListener { exception ->
                userTextView.text = "Error"
            }
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}