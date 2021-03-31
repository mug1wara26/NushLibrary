package com.example.nushlibrary.booksFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.GenreRecyclerAdapter
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.google.android.material.textfield.TextInputEditText

class FilterDialogFragment(val listener: GetFilterOnDismiss): DialogFragment() {
    interface GetFilterOnDismiss {
        fun onDismiss(genre: ArrayList<String>?, authors: ArrayList<String>)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)

            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_filter, null)

            val genreTextView: TextView = view.findViewById(R.id.genre_text_view)
            genreTextView.text = "Filter by genre"


            // Initialize the RecyclerView of genres
            val genreRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_genre)
            // Set layout manager to be a grid of column 2
            genreRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val genreAdapter = GenreRecyclerAdapter()
            genreRecyclerView.adapter = genreAdapter

            val arrowButton: ImageButton = view.findViewById(R.id.arrow_button_genre)
            val expandableCardViewGenre: CardView = view.findViewById(R.id.expandable_card_view_genre)

            setExpandableView(arrowButton, expandableCardViewGenre, genreRecyclerView)

            builder.setView(view)
                .setPositiveButton("Filter") { _, _ ->
                    val authors: ArrayList<String> = arrayListOf()
                    val authorsInput: TextInputEditText = view.findViewById(R.id.search_filter_author)

                    authorsInput.text.toString().split(",").forEach {
                        authors.add(it)
                    }

                    listener.onDismiss(genreAdapter.selectedGenres, authors)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}