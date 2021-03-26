package com.example.nushlibrary.adminFragments.addBookDialogFragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.AdminHomeFragment
import com.example.nushlibrary.adminFragments.BOOK_CREATED_RC
import com.example.nushlibrary.database
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import kotlin.collections.ArrayList


class AddBookDialogFragment(private val listener: GetBookOnDismiss): DialogFragment(), OnGenreClick {
    private val selectedGenres: ArrayList<String> = ArrayList()
    var book: Book? = null
    // Callback interface on dismiss to use book object
    interface GetBookOnDismiss {
        fun onDismiss(book: Book?)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.dialog_add_book, null)
            val addBookRadioGroup: RadioGroup = view.findViewById(R.id.add_book_radio_group)

            // Listener when a radio button is changed
            addBookRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                // checkedId is the RadioButton selected
                val isbnCardView: CardView = view.findViewById(R.id.isbn_card_view)
                val manualCardView: CardView = view.findViewById(R.id.manual_card_view)

                // This function disables and greys out the card view that is not selected
                switchCardViews(manualCardView, isbnCardView, checkedId)
            }


            // Initialize the RecyclerView of genres
            val genreRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_genre)
            // Set layout manager to be a grid of column 2
            genreRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val adapter = GenreRecyclerAdapter(this)
            genreRecyclerView.adapter = adapter

            // Show/hide the recycler view on arrow button click
            val arrowButton: ImageButton = view.findViewById(R.id.arrow_button)
            val expandableCardViewGenre: CardView = view.findViewById(R.id.expandable_card_view_genre)

            arrowButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(
                    expandableCardViewGenre,
                    AutoTransition()
                )

                if (genreRecyclerView.visibility == View.VISIBLE) {
                    genreRecyclerView.visibility = View.GONE
                    arrowButton.setImageResource(R.drawable.icon_expand_arrow)
                }
                else {
                    genreRecyclerView.visibility = View.VISIBLE
                    arrowButton.setImageResource(R.drawable.icon_collapse_arrow)
                }
            }

            // We are storing context here because the fragment is removed after the create button is pressed
            // However we need context to use Toast
            val fragmentContext = context

            // Adds create and cancel buttons
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Create") { _, _ ->
                    if (view.findViewById<RadioButton>(R.id.isbn_radio_button).isChecked) {
                        val isbnInput: TextInputEditText = view.findViewById(R.id.isbn_input)
                        val numberInput: TextInputEditText = view.findViewById(R.id.isbn_book_number_input)

                        val isbn = isbnInput.text.toString()
                        val number = numberInput.text.toString()

                        createBookWithISBN(isbn, number, fragmentContext)
                    }
                    else {
                        val titleInput: TextInputEditText = view.findViewById(R.id.manual_title_input)
                        val authorInput: TextInputEditText = view.findViewById(R.id.manual_author_input)
                        val descriptionInput: TextInputEditText = view.findViewById(R.id.manual_description_input)
                        val publisherInput: TextInputEditText = view.findViewById(R.id.manual_publisher_input)
                        val numberInput: TextInputEditText = view.findViewById(R.id.manual_book_number_input)

                        val title = titleInput.text.toString()
                        val author = authorInput.text.toString()
                        val description = descriptionInput.text.toString()
                        val publisher = publisherInput.text.toString()
                        val number = numberInput.text.toString()

                        createBookManually(fragmentContext, title, author, description, publisher, number)
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun switchCardViews(manualCardView: CardView, isbnCardView: CardView, checkedId: Int) {
        var disabledCardView: CardView = manualCardView
        var enabledCardView: CardView = isbnCardView

        // Sets which card view will be enabled / disabled
        when (checkedId) {
            R.id.isbn_radio_button -> {
                disabledCardView = manualCardView
                enabledCardView = isbnCardView
            }

            R.id.manual_radio_button -> {
                disabledCardView = isbnCardView
                enabledCardView = manualCardView
            }
        }

        // Goes through all views in the card view and disables it
        // Get the constraint layout in the card view first, then loop through the children in that
        val disabledConstraintLayout = disabledCardView.getChildAt(0) as ConstraintLayout
        for (i in disabledConstraintLayout.children) {
            i.isEnabled = false
        }
        // Sets opacity to 30%
        disabledCardView.alpha = 0.3F

        val enabledConstraintLayout = enabledCardView.getChildAt(0) as ConstraintLayout
        for (i in enabledConstraintLayout.children) i.isEnabled = true
        // Sets to opacity to 100%
        enabledCardView.alpha = 1F
    }

    private fun createBookWithISBN(isbn: String, number: String, context: Context?) {
        if (isbn.isNotEmpty() && number.isNotEmpty()) {
            GetBookByISBN(
                selectedGenres,
                isbn.toLong(),
                number.toInt(),
                object : GetBookByISBN.AsyncResponse {
                    // onPostExecute result is transferred here
                    // Memory leak pog
                    override fun processFinish(output: Book?) {
                        if (output != null) {
                            Toast.makeText(
                                context,
                                "Success! Book has been created",
                                Toast.LENGTH_SHORT
                            ).show()
                            book = output

                            // Set book to listener
                            listener.onDismiss(book)
                        }
                        else
                            Toast.makeText(
                            context,
                            "Could not get data from ISBN, please use the manual option.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }).execute()
        }
        else {
            Toast.makeText(
                context,
                "Please fill in all required fields",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun createBookManually(context: Context?, title: String, authors: String, description: String, publisher: String, number: String) {
        val requiredFieldsFilled =
            title.isNotEmpty() &&
                    authors.isNotEmpty() &&
                    description.isNotEmpty() &&
                    number.isNotEmpty()

        if (requiredFieldsFilled) {
            // This checks that the number input is not too large, other wise overflow may occur
            if (number.length <= 4) {
                val authorsList: ArrayList<String> = ArrayList()
                authors.split(",").forEach {
                    authorsList.add(it)
                }

                book = Book(
                    authorsList,
                    title,
                    description,
                    // If user did not input publisher, set it to null otherwise it will store empty string in database
                    publisher =
                    if (publisher.isEmpty()) null
                    else publisher
                    ,
                    selectedGenres,
                    null,
                    number.toInt()
                )

                database.child("books").child(UUID.randomUUID().toString()).setValue(book)

                // Add book to intent

                // Set book to listener
                listener.onDismiss(book)

                Toast.makeText(context, "Success! Book has been created", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Number has to be smaller than 9999", Toast.LENGTH_LONG).show()
        }
        else Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show()
    }

    override fun onGenreClick(genre: String) {
        selectedGenres.add(genre)
    }
}