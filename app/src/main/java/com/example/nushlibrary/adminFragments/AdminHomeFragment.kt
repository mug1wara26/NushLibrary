package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.AddBookDialogFragment
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.example.nushlibrary.user

class AdminHomeFragment: Fragment() {
    lateinit var recentlyAddedBooksAdapter: BooksRecyclerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        if (!::recentlyAddedBooksAdapter.isInitialized) recentlyAddedBooksAdapter = BooksRecyclerAdapter(activity?.supportFragmentManager!!, user.admin)

        // Open dialog on button click
        val addBookButton: Button = view.findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            activity?.supportFragmentManager?.let {
                AddBookDialogFragment(object: AddBookDialogFragment.GetBookOnDismiss {
                    override fun onDismiss(book: Book?) {
                        if (book != null) {
                            recentlyAddedBooksAdapter.books.add(book)
                            recentlyAddedBooksAdapter.notifyDataSetChanged()
                        }
                    }

                }).show(it, "Add Book")
            }
        }

        val recentlyAddedRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_recently_added)
        recentlyAddedRecyclerView.layoutManager = LinearLayoutManager(context)
        recentlyAddedRecyclerView.adapter = recentlyAddedBooksAdapter
        return view
    }
}