package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.GenreRecyclerAdapter
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.database
import com.google.android.material.textfield.TextInputEditText

class EditBookDialogFragment(private val book: Book, private val booksAdapter: BooksRecyclerAdapter): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)

            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_edit_book, null)

            // Set text on the expandable card view genre
            val genreTextView: TextView = view.findViewById(R.id.genre_text_view)
            genreTextView.text = resources.getString(R.string.add_genres)

            // Initialize the RecyclerView of genres
            val genreRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_genre)
            // Set layout manager to be a grid of column 2
            genreRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val genreAdapter = GenreRecyclerAdapter()

            // Set default genres selected
            book.genre.forEach {
                genreAdapter.selectedGenres.add(it as String)
            }
            genreRecyclerView.adapter = genreAdapter

            // Show/hide the recycler view on arrow button click
            val arrowButton: ImageButton = view.findViewById(R.id.arrow_button_genre)
            val expandableCardViewGenre: CardView = view.findViewById(R.id.expandable_card_view_genre)

            setExpandableView(arrowButton, expandableCardViewGenre, genreRecyclerView)

            builder.setTitle("Edit Book")
            builder.setView(view)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Delete") { _, _ ->
                    // Delete book from all recycler adapter
                    val id = book.id
                    booksAdapter.books.removeIf {
                        it.id == book.id
                    }
                    booksAdapter.notifyDataSetChanged()

                    database.child("books").child(id).removeValue()

                    Toast.makeText(context, "Successfully deleted book", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Edit") { _, _ ->
                    // Function to make my life easier
                    fun getText(textInputId: Int, defaultValue: Any?): Any? {
                        // If text is empty, return default value, else return text
                        val text = view.findViewById<TextInputEditText>(textInputId).text
                        return if (text.isNullOrEmpty()) defaultValue
                        else text.toString()
                    }

                    val title = getText(R.id.manual_title_input, book.title) as String?

                    var authors = getText(R.id.manual_author_input, book.authors)

                    if (authors is String) {
                        val authorsList: ArrayList<String> = ArrayList()
                        authors.split(",").forEach {
                            authorsList.add(it)
                        }
                        authors = authorsList
                    }

                    val description = getText(R.id.manual_description_input, book.description) as String?
                    val publisher = getText(R.id.manual_publisher_input, book.publisher) as String?
                    val number = getText(R.id.manual_book_number_input, book.number).toString().toInt()

                    val newBook = Book(
                        book.id,
                        authors as ArrayList<*>,
                        title,
                        description,
                        publisher,
                        genreAdapter.selectedGenres,
                        book.thumbnail,
                        number,
                        0
                    )

                    // Change book arraylist
                    booksAdapter.books[booksAdapter.books.indexOf(book)] = newBook
                    booksAdapter.notifyDataSetChanged()

                    // Change book in database
                    database.child("books").child(book.id).setValue(newBook)

                    Toast.makeText(context, "Successfully edited book", Toast.LENGTH_SHORT).show()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}