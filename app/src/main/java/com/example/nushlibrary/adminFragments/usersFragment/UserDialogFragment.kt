package com.example.nushlibrary.adminFragments.usersFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.User
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.userFragments.GetBooksOnPostExecute
import com.example.nushlibrary.userFragments.getBooksById

class UserDialogFragment(val user: User): DialogFragment() {
    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_user, null)

        view.findViewById<TextView>(R.id.dialog_user_display_name).text = user.displayName
        view.findViewById<TextView>(R.id.dialog_user_email).text = user.email
        view.findViewById<TextView>(R.id.dialog_user_borrowed_books).text = "Number of books borrowed: ${user.booksBorrowed.size}"

        // Make the book list expandable
        val arrowButton: ImageButton = view.findViewById(R.id.arrow_button_borrowed_books)
        val expandableCardView: CardView = view.findViewById(R.id.dialog_user_expandable_books)
        val booksBorrowedRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_borrowed_books)

        setExpandableView(arrowButton, expandableCardView, booksBorrowedRecyclerView)

        // Add to the recycler view
        booksBorrowedRecyclerView.layoutManager = LinearLayoutManager(context)

        val bookAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager)
        getBooksById(user.booksBorrowed, object: GetBooksOnPostExecute{
            override fun onPostExecute(books: ArrayList<Book>) {
                books.sortedWith(compareBy{ it.borrowedTime }).forEach { book ->
                    bookAdapter.books.add(book)
                }
                booksBorrowedRecyclerView.adapter = bookAdapter
            }
        })

        with(builder) {
            setView(view)

            setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }

            return create()
        }
    }
}