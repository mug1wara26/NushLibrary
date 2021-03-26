package com.example.nushlibrary.adminFragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.AddBookDialogFragment

const val BOOK_CREATED_RC = 1
class AdminHomeFragment: Fragment() {
    private val adapter = RecentlyAddedRecyclerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)


        // Open dialog on button click
        val addBookButton: Button = view.findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            activity?.supportFragmentManager?.let {
                AddBookDialogFragment(object: AddBookDialogFragment.GetBookOnDismiss {
                    override fun onDismiss(book: Book?) {
                        if (book != null) {
                            adapter.books.add(book)
                            adapter.notifyDataSetChanged()
                        }
                    }

                }).show(it, "Add Book")

            }
        }

        val recentlyAddedRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_recently_added)
        recentlyAddedRecyclerView.layoutManager = LinearLayoutManager(context)
        recentlyAddedRecyclerView.adapter = adapter
        return view
    }
}