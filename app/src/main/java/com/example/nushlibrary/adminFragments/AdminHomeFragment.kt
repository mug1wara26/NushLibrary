package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.AddBookDialogFragment
import com.example.nushlibrary.adminFragments.bookRecyclerView.BooksRecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminHomeFragment: Fragment() {
    lateinit var recentlyAddedBooksAdapter: BooksRecyclerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        if (!::recentlyAddedBooksAdapter.isInitialized)
            recentlyAddedBooksAdapter = BooksRecyclerAdapter(requireActivity().supportFragmentManager)

        // Open dialog on button click
        val addBookButton: FloatingActionButton = view.findViewById(R.id.add_book_fab)
        val progressBar: ProgressBar = view.findViewById(R.id.load_add_book)

        addBookButton.setOnClickListener {
            AddBookDialogFragment(progressBar, object: AddBookDialogFragment.GetBookOnDismiss {
                override fun onDismiss(book: Book?) {
                    if (book != null) {
                        recentlyAddedBooksAdapter.books.add(book)
                        recentlyAddedBooksAdapter.notifyItemChanged(recentlyAddedBooksAdapter.itemCount - 1)
                    }
                }

            }).show(requireActivity().supportFragmentManager, "Add Book")
        }

        val recentlyAddedRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_recently_added)
        recentlyAddedRecyclerView.layoutManager = LinearLayoutManager(context)
        recentlyAddedRecyclerView.adapter = recentlyAddedBooksAdapter
        return view
    }
}