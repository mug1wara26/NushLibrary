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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener


class BooksFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        val booksRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_books)
        booksRecyclerView.layoutManager = LinearLayoutManager(context)
        val booksAdapter = BooksRecyclerAdapter(activity?.supportFragmentManager!!, user.admin)
        booksRecyclerView.adapter = booksAdapter

        var order = "title"
        showBookByOrder(booksAdapter, order)

        val filterButton: ImageButton = view.findViewById(R.id.filter_button)
        filterButton.setOnClickListener {
            activity?.supportFragmentManager?.let {
                FilterDialogFragment(object: FilterDialogFragment.GetFilterOnDismiss {
                    override fun onDismiss(genreFilter: ArrayList<String>, authorsFilter: ArrayList<String>) {
                        showBookByOrder(booksAdapter, order, object: OnPostExecute{
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
                            }
                        })
                    }
                }).show(it, "Filter")
            }
        }

        val refreshButton: ImageButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            showBookByOrder(booksAdapter, order)
        }

        return view
    }

    private fun showBookByOrder(
        booksAdapter: BooksRecyclerAdapter,
        value: String,
        listener: OnPostExecute = object: OnPostExecute{
            override fun onPostExecute() {/* Do Nothing */}
    }) {
        booksAdapter.books.clear()
        database.child("books").orderByChild(value).addListenerForSingleValueEvent(object: ValueEventListener{
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
                    val genericTypeIndicatorArrayListString = object : GenericTypeIndicator<ArrayList<String>>() {}
                    val authors = it.child("authors").getValue(genericTypeIndicatorArrayListString)
                    val genre = it.child("genre").getValue(genericTypeIndicatorArrayListString)

                    val book = Book(
                        id!!,
                        authors = authors ?: arrayListOf<String>(),
                        title, description,
                        publisher,
                        genre = genre ?: arrayListOf<String>(),
                        thumbnail,
                        number!!)

                    booksAdapter.books.add(book)
                }
                booksAdapter.notifyDataSetChanged()
                listener.onPostExecute()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database error", Toast.LENGTH_SHORT).show()
                booksAdapter.notifyDataSetChanged()
            }
        })
    }

    interface OnPostExecute {
        fun onPostExecute()
    }
}