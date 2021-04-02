package com.example.nushlibrary.userFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.bookReference
import com.example.nushlibrary.user
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserHomeFragment: Fragment() {
    interface GetBooksOnPostExecute{
        fun onPostExecute(books: ArrayList<Book>)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        val borrowedBooksRecyclerView: RecyclerView = view.findViewById(R.id.user_home_recycler_view_books)
        borrowedBooksRecyclerView.layoutManager = LinearLayoutManager(context)
        val bookAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager)
        borrowedBooksRecyclerView.adapter = bookAdapter

        showBooks(bookAdapter)
        val noBooksTextView: TextView = view.findViewById(R.id.no_books_borrowed_textview)
        if (user.booksBorrowed.size == 0) noBooksTextView.visibility = View.VISIBLE

        return view
    }

    private fun showBooks(bookAdapter: BooksRecyclerAdapter) {
        getBooksById(user.booksBorrowed, object: GetBooksOnPostExecute{
            override fun onPostExecute(books: ArrayList<Book>) {
                books.sortedWith(compareBy { it.borrowedTime }).forEach { book ->
                    bookAdapter.books.add(book)
                }
                bookAdapter.notifyDataSetChanged()
            }
        })

    }

    private fun getBooksById(idArrayList: ArrayList<String>, listener: GetBooksOnPostExecute) {
        val books = arrayListOf<Book>()
        bookReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idArrayList.forEach { id ->
                    val book = snapshot.child(id).getValue(Book::class.java)
                    if (book != null) {
                        books.add(book)
                    }
                }
                listener.onPostExecute(books)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}