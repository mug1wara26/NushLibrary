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
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.addBookDialogFragment.GenreRecyclerAdapter
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.dataClasses.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EditBookDialogFragment(private val book: Book, private val booksAdapter: BooksRecyclerAdapter): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

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
                // Remove book from database
                val bookId = book.id
                database.child("books").child(bookId).removeValue()

                // Remove book from users
                val borrowedUsers = book.borrowedUsers
                userReference.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        borrowedUsers.forEach{ borrowedUser ->
                            val user = snapshot.child(borrowedUser.id).getValue(User::class.java)

                            user?.booksBorrowed?.remove(bookId)
                            user?.booksBorrowedTimeStamp?.remove(borrowedUser.timeStamp)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

                // Delete book from recycler adapter
                val position = booksAdapter.books.indexOf(book)
                booksAdapter.books.remove(book)
                booksAdapter.notifyItemRemoved(position)


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

                val title = getText(R.id.manual_title_input, book.title) as String

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
                    genre = genreAdapter.selectedGenres,
                    book.thumbnail,
                    number
                )

                // Change book arraylist
                val position = booksAdapter.books.indexOf(book)
                booksAdapter.books[position] = newBook
                booksAdapter.notifyItemChanged(position)

                // Change book in database
                database.child("books").child(book.id).setValue(newBook)

                Toast.makeText(context, "Successfully edited book", Toast.LENGTH_SHORT).show()
            }

        return builder.create()
    }
}