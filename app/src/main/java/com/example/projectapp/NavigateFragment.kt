package com.example.projectapp



import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton


class NavigateFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigate, container, false)

        val profileImageButton = view.findViewById<ImageButton>(R.id.profileImageButton)
        val addImageButton = view.findViewById<ImageButton>(R.id.addImageButton)

        profileImageButton.setOnClickListener{
            startActivity(Intent(requireContext(), UserActivity::class.java))
        }
        addImageButton.setOnClickListener{
            startActivity(Intent(requireContext(), AddRecipeActivity::class.java))
        }
        return view
    }

}