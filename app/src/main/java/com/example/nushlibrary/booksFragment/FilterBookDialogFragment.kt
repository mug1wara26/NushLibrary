package com.example.nushlibrary.booksFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.GenreRecyclerAdapter
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.mainUser
import com.google.android.material.textfield.TextInputEditText

class FilterBookDialogFragment(private val listener: GetFilterOnDismiss): DialogFragment() {
    interface GetFilterOnDismiss {
        fun onDismiss(
            genreFilter: ArrayList<String>,
            authorsFilter: ArrayList<String>,
            isBooksBorrowedChecked: Boolean,
            isToReadChecked: Boolean
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_filter_book, null)

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

        if (!mainUser.admin){
            // set the checkbox to visible is user is non admin
            val filterUserLayout: ConstraintLayout = view.findViewById(R.id.layout_filter_user)
            filterUserLayout.visibility = View.VISIBLE
        }

        builder.setTitle("Filter")
        builder.setView(view)
            .setPositiveButton("Filter") { _, _ ->
                val authors: ArrayList<String> = arrayListOf()
                val authorsText = view.findViewById<TextInputEditText>(R.id.search_filter_author).text.toString()

                if (authorsText.isNotEmpty()) {
                    authorsText.split(",").forEach {
                        authors.add(it)
                    }
                }

                val borrowedBooksCheckBox: CheckBox = view.findViewById(R.id.filter_user_borrowed_books)
                val toReadCheckBox: CheckBox = view.findViewById(R.id.filter_user_to_read)

                listener.onDismiss(
                    genreAdapter.selectedGenres,
                    authors,
                    borrowedBooksCheckBox.isChecked,
                    toReadCheckBox.isChecked
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }
}