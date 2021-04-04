package com.example.nushlibrary.booksFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.collections.ArrayList


class BooksFragment: Fragment() {
    // Reference this boolean to check if thread is locked
    var isThreadLocked = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        val booksRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_books)
        booksRecyclerView.layoutManager = LinearLayoutManager(context)
        val booksAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager)
        booksRecyclerView.adapter = booksAdapter

        var searchableBooks = arrayListOf<Book>()

        showBook(booksAdapter, object: OnPostExecute{
            override fun onPostExecute() {
                searchableBooks = booksAdapter.books
            }
        })

        val filterButton: ImageButton = view.findViewById(R.id.filter_button)
        filterButton.setOnClickListener {
            FilterBookDialogFragment(object: FilterBookDialogFragment.GetFilterOnDismiss {
                override fun onDismiss(
                    genreFilter: ArrayList<String>,
                    authorsFilter: ArrayList<String>,
                    isBooksBorrowedChecked: Boolean,
                    isToReadChecked: Boolean) {
                    showBook(booksAdapter, object: OnPostExecute{
                        override fun onPostExecute() {
                            // Filter for users borrowed books or to read
                            val newBooksList = arrayListOf<Book>()
                            booksAdapter.books.forEach { book ->
                                genreFilter.forEach { genre ->
                                    if (book.genre.contains(genre)) newBooksList.add(book)
                                }

                                authorsFilter.forEach { author ->
                                    if (book.authors.contains(author) && !newBooksList.contains(book)) newBooksList.add(book)
                                }

                                // Checks if the checkbox is checked, if user has borrowed the book and the current book list does not already contain the book
                                if (isBooksBorrowedChecked
                                    && mainUser.booksBorrowed.contains(book.id)
                                    && !newBooksList.contains(book)) newBooksList.add(book)

                                // Checks if the checkbox is checked, if user has added the book to their to read list and the current book list does not already contain the book
                                if (isToReadChecked
                                    && mainUser.toReadList.contains(book.id)
                                    && !newBooksList.contains(book)) newBooksList.add(book)
                            }

                            // Sort books by title
                            booksAdapter.books.clear()
                            // Cant type cast for some reason bleh
                            newBooksList.sortedWith(compareBy { it.title }).forEach {
                                booksAdapter.books.add(it)
                            }
                            booksAdapter.notifyDataSetChanged()

                            searchableBooks = booksAdapter.books
                        }
                    })
                }
            }).show(requireActivity().supportFragmentManager, "Filter")
        }

        val searchInput: TextInputEditText = view.findViewById(R.id.input_search)

        val refreshButton: ImageButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            showBook(booksAdapter, object: OnPostExecute{
                override fun onPostExecute() {
                    searchableBooks = booksAdapter.books
                    searchInput.text = null
                }
            })
        }

        val searchButton: ImageButton = view.findViewById(R.id.search_layout_search_button)
        searchButton.setOnClickListener {
            val title = searchInput.text.toString()
            if (title.isNotEmpty()) {
                booksAdapter.books = searchForBook(searchableBooks, title)
                booksAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    private fun showBook(
        booksAdapter: BooksRecyclerAdapter,
        listener: OnPostExecute = object: OnPostExecute{
            override fun onPostExecute() {/*  */}
    }) {
        if (!isThreadLocked) {
            isThreadLocked = true
            val books = arrayListOf<Book>()
            bookReference.orderByChild("title")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                             val book = it.getValue(Book::class.java)

                            if (book != null) {
                                books.add(book)
                            }
                        }
                        booksAdapter.books = books
                        listener.onPostExecute()
                        booksAdapter.notifyDataSetChanged()
                        isThreadLocked = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Database error", Toast.LENGTH_SHORT).show()
                        booksAdapter.notifyDataSetChanged()
                    }
                })
        }
    }

    interface OnPostExecute {
        fun onPostExecute()
    }
}

fun searchForBook(books: ArrayList<Book>, title: String): ArrayList<Book> {
    val titles = arrayListOf<String>()

    for (book in books) {
        if (book.title != null) titles.add(book.title)
    }

    println(FuzzySearch.extractSorted(title, titles))
    val sortedTitles = FuzzySearch.extractSorted(title, titles, 50)
    val sortedBooks = arrayListOf<Book>()
    sortedTitles.forEach {
        sortedBooks.add(books[it.index])
    }

    return sortedBooks
}