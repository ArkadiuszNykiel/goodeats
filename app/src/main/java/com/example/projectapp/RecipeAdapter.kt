package com.example.projectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecipeAdapter(var recipes: MutableList<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val textView1: TextView = holder.itemView.findViewById(R.id.titleTextView)
        val imageView1: ImageView = holder.itemView.findViewById(R.id.imageView2)
        textView1.text = recipes[position].name
        Picasso.get().load(recipes[position].picture).into(imageView1)

    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}