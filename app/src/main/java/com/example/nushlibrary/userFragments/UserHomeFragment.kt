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
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.bookReference
import com.example.nushlibrary.booksFragment.searchForBook
import com.example.nushlibrary.mainUser
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
        if (mainUser.booksBorrowed.size == 0) noBooksTextView.visibility = View.VISIBLE

        // Set the default checked radio button
        var checkedOrderId = R.id.reorder_book_due_date
        var checkedDirectionId = R.id.reorder_book_ascending

        val reorderButton: ImageButton = view.findViewById(R.id.user_home_reorder_button)
        reorderButton.setOnClickListener {
            ReorderBooksDialogFragment(checkedOrderId, checkedDirectionId, object: ReorderBooksDialogFragment.GetOrderOnDismiss{
                override fun onDismiss(orderId: Int, ascending: Boolean) {
                    // Set default checked radio button
                    checkedOrderId = orderId
                    checkedDirectionId =
                        if (ascending) R.id.reorder_book_ascending
                        else R.id.reorder_book_descending

                    // Set whether the books are sorted ascending or descending
                    val comparatorDueDate =
                        if (ascending) compareBy<Book> {
                            it.borrowedUsers.filter { borrowedUser ->
                                borrowedUser.id == mainUser.id
                            }[0].timeStamp
                        }
                        else compareByDescending {
                            it.borrowedUsers.filter { borrowedUser ->
                                borrowedUser.id == mainUser.id
                            }[0].timeStamp
                        }
                    val comparatorTitle =
                        if (ascending) compareBy<Book> { it.title }
                        else compareByDescending { it.title }

                    // Determine the final comparator
                    val bookComparator =
                        if (orderId == R.id.reorder_book_due_date) comparatorDueDate
                        else comparatorTitle

                    val newBooks = ArrayList(bookAdapter.books.sortedWith(bookComparator))

                    // Set book adapter
                    bookAdapter.books = newBooks
                    bookAdapter.notifyDataSetChanged()
                }
            }).show(requireActivity().supportFragmentManager, "Reorder")
        }

        val searchInput: TextInputEditText = view.findViewById(R.id.input_search)
        val refreshButton: ImageButton =  view.findViewById(R.id.user_home_refresh_button)
        refreshButton.setOnClickListener {
            showBooks(bookAdapter)
            // Set default checked radio buttons
            checkedOrderId = R.id.reorder_book_due_date
            checkedDirectionId = R.id.reorder_book_ascending
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
        bookAdapter.books.clear()
        getBooksById(mainUser.booksBorrowed, object: GetBooksOnPostExecute{
            override fun onPostExecute(books: ArrayList<Book>) {
                bookAdapter.books = ArrayList(books.sortedWith(compareBy {
                    it.borrowedUsers.filter { borrowedUser ->
                        borrowedUser.id == mainUser.id
                    }[0].timeStamp
                }))

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