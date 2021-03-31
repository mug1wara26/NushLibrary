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
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.database
import com.example.nushlibrary.user
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
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
        val booksAdapter = BooksRecyclerAdapter(activity?.supportFragmentManager!!, user.admin)
        booksRecyclerView.adapter = booksAdapter

        showBook(booksAdapter)

        var searchableBooks = arrayListOf<Book>()
        val filterButton: ImageButton = view.findViewById(R.id.filter_button)
        filterButton.setOnClickListener {
            FilterDialogFragment(object: FilterDialogFragment.GetFilterOnDismiss {
                override fun onDismiss(genreFilter: ArrayList<String>, authorsFilter: ArrayList<String>) {
                    showBook(booksAdapter, object: OnPostExecute{
                        override fun onPostExecute() {
                            genreFilter.forEach { genre ->
                                booksAdapter.books.removeIf { book ->
                                    !book.genre.contains(genre)
                                }
                            }

                            authorsFilter.forEach { author ->
                                booksAdapter.books.removeIf { book ->
                                    !book.authors.contains(author)
                                }
                            }

                            searchableBooks = booksAdapter.books
                        }
                    })
                }
            }).show(requireActivity().supportFragmentManager, "Filter")
        }

        val searchInput: TextInputEditText = view.findViewById(R.id.book_input_search)

        val refreshButton: ImageButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            showBook(booksAdapter, object: OnPostExecute{
                override fun onPostExecute() {
                    searchableBooks = booksAdapter.books
                    searchInput.text = null
                }
            })
        }

        val searchButton: ImageButton = view.findViewById(R.id.book_fragment_search_button)
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
            database.child("books").orderByChild("title")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            // val book = it.getValue(Book::class.java)
                            // The above line doesn't work for some reason so we have to manually get all values :(
                            val id = it.child("id").getValue(String::class.java)
                            val title = it.child("title").getValue(String::class.java)
                            val description = it.child("description").getValue(String::class.java)
                            val publisher = it.child("publisher").getValue(String::class.java)
                            val thumbnail = it.child("thumbnail").getValue(String::class.java)
                            val number = it.child("number").getValue(Int::class.java)

                            // Firebase just requires this i guess
                            val genericTypeIndicatorArrayListString =
                                object : GenericTypeIndicator<ArrayList<String>>() {}
                            val authors =
                                it.child("authors").getValue(genericTypeIndicatorArrayListString)
                            val genre =
                                it.child("genre").getValue(genericTypeIndicatorArrayListString)

                            val book = Book(
                                id!!,
                                authors = authors ?: arrayListOf<String>(),
                                title, description,
                                publisher,
                                genre = genre ?: arrayListOf<String>(),
                                thumbnail,
                                number!!
                            )

                            books.add(book)
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

    private fun searchForBook(books: ArrayList<Book>, title: String): ArrayList<Book> {
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

    interface OnPostExecute {
        fun onPostExecute()
    }
}