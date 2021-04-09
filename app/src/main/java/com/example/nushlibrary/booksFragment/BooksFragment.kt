package com.example.nushlibrary.booksFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.dataClasses.Book
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
        val booksAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager, inBookFragment = true)
        booksRecyclerView.adapter = booksAdapter

        var searchableBooks = arrayListOf<Book>()
        val progressBar: ProgressBar = view.findViewById(R.id.load_fragment_books)

        showBook(booksAdapter, progressBar, object: OnPostExecute{
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
                    showBook(booksAdapter, progressBar, object: OnPostExecute{
                        override fun onPostExecute() {
                            // Filter for users borrowed books or to read
                            val newBooksList = booksAdapter.books.filter { book ->
                                var genreFilterBoolean = false
                                var authorFilterBoolean = false
                                val isBookBorrowed = isBooksBorrowedChecked && mainUser.booksBorrowed.contains(book.id)
                                val isToRead = isToReadChecked && mainUser.toReadList.contains(book.id)

                                genreFilter.forEach genre@{ genre ->
                                    genreFilterBoolean = book.genre.contains(genre)
                                    if (genreFilterBoolean) return@genre
                                }

                                authorsFilter.forEach genre@{ author ->
                                    authorFilterBoolean = book.genre.contains(author)
                                    if (authorFilterBoolean) return@genre
                                }

                                genreFilterBoolean || authorFilterBoolean || isBookBorrowed || isToRead
                            }

                            booksAdapter.books = ArrayList(newBooksList.sortedWith(compareBy { it.title }))
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
            showBook(booksAdapter, progressBar, object: OnPostExecute{
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
                booksAdapter.books = searchForBook(searchableBooks, title, progressBar)
                booksAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    private fun showBook(
        booksAdapter: BooksRecyclerAdapter,
        progressBar: ProgressBar,
        listener: OnPostExecute = object: OnPostExecute{
            override fun onPostExecute() {/*  */}
    }) {
        if (!isThreadLocked) {
            isThreadLocked = true
            progressBar.visibility = View.VISIBLE

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
                        progressBar.visibility = View.GONE
                        isThreadLocked = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Database error", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    interface OnPostExecute {
        fun onPostExecute()
    }
}

fun searchForBook(books: ArrayList<Book>, title: String, progressBar: ProgressBar): ArrayList<Book> {
    progressBar.visibility = View.VISIBLE
    val titles = arrayListOf<String>()

    for (book in books) {
        if (book.title != null) titles.add(book.title)
    }

    val sortedTitles = FuzzySearch.extractSorted(title, titles, 50)
    val sortedBooks = arrayListOf<Book>()
    sortedTitles.forEach {
        sortedBooks.add(books[it.index])
    }

    progressBar.visibility = View.GONE
    return sortedBooks
}