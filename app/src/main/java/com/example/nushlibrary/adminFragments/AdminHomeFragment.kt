package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.nushlibrary.R


class AdminHomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        val addBookButton: Button = view.findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            activity?.supportFragmentManager?.let { it ->
                AddBookDialogFragment().show(it, "Add Book")
            }
        }
        return view
    }
}