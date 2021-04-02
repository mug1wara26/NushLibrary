package com.example.nushlibrary.userFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.bookReference
import com.example.nushlibrary.booksFragment.searchForBook
import com.example.nushlibrary.user
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserHomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        val borrowedBooksRecyclerView: RecyclerView = view.findViewById(R.id.user_home_recycler_view_books)
        borrowedBooksRecyclerView.layoutManager = LinearLayoutManager(context)
        val bookAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager)
        borrowedBooksRecyclerView.adapter = bookAdapter

        showBooks(bookAdapter)
        val noBooksTextView: TextView = view.findViewById(R.id.no_books_borrowed_textview)
        if (user.booksBorrowed.size == 0) noBooksTextView.visibility = View.VISIBLE

        val reorderButton: ImageButton = view.findViewById(R.id.user_home_reorder_button)
        // Set the default checked radio button
        var checkedId = R.id.reorder_due_date_ascending
        reorderButton.setOnClickListener {
            ReorderDialogFragment(checkedId, object: ReorderDialogFragment.GetOrderOnDismiss{
                override fun onDismiss(orderId: Int) {
                    val newBooks = arrayListOf<Book>()
                    checkedId = orderId
                    when (orderId) {
                        R.id.reorder_due_date_ascending -> {
                            bookAdapter.books.sortedWith(compareBy { it.borrowedTime }).forEach { book ->
                                newBooks.add(book)
                            }
                        }

                        R.id.reorder_due_date_descending -> {
                            bookAdapter.books.sortedWith(compareByDescending { it.borrowedTime }).forEach { book ->
                                newBooks.add(book)
                            }
                        }

                        R.id.reorder_title -> {
                            bookAdapter.books.sortedWith(compareBy { it.title }).forEach { book ->
                                newBooks.add(book)
                            }
                        }
                    }
                    bookAdapter.books = newBooks
                    bookAdapter.notifyDataSetChanged()
                }
            }).show(requireActivity().supportFragmentManager, "Reorder")
        }

        val searchInput: TextInputEditText = view.findViewById(R.id.input_search)
        val refreshButton: TextInputEditText =  view.findViewById(R.id.user_home_refresh_button)
        refreshButton.setOnClickListener {
            bookAdapter.books.clear()
            showBooks(bookAdapter)
            checkedId = R.id.reorder_due_date_ascending
            searchInput.text = null
        }

        // Implement search
        val searchButton: ImageButton = view.findViewById(R.id.search_layout_search_button)
        searchButton.setOnClickListener {
            val title = searchInput.text.toString()
            if (title.isNotEmpty()) {
                bookAdapter.books = searchForBook(bookAdapter.books, title)
                bookAdapter.notifyDataSetChanged()
            }
        }

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

}
interface GetBooksOnPostExecute{
    fun onPostExecute(books: ArrayList<Book>)
}

fun getBooksById(idArrayList: ArrayList<String>, listener: GetBooksOnPostExecute) {
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