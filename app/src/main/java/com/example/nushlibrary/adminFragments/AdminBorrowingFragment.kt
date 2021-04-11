package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.dataClasses.BorrowingUser
import com.example.nushlibrary.dataClasses.User
import com.example.nushlibrary.database
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import me.xdrop.fuzzywuzzy.FuzzySearch

class AdminBorrowingFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_borrowing, container, false)

        val borrowingUsersRecyclerView: RecyclerView = view.findViewById(R.id.borrowing_users_recycler_view)
        borrowingUsersRecyclerView.layoutManager = LinearLayoutManager(context)
        val borrowingUsersRecyclerAdapter = BorrowingUsersRecyclerAdapter(requireContext())
        borrowingUsersRecyclerView.adapter = borrowingUsersRecyclerAdapter

        val borrowingUsers = arrayListOf<BorrowingUser>()
        database.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child("borrowing").children.forEach { snapshotChild ->
                    val user = snapshotChild.getValue(User::class.java)
                    user?.booksBorrowedQueue?.forEach { bookId ->
                        val book = snapshot.child("books").child(bookId).getValue(Book::class.java)

                        if (book != null) {
                            val borrowingUser = BorrowingUser(
                                user.id,
                                bookId,
                                user.displayName,
                                book.title
                            )

                            borrowingUsers.add(borrowingUser)
                        }
                    }
                }

                borrowingUsersRecyclerAdapter.borrowingUsers = borrowingUsers
                borrowingUsersRecyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val searchButton: ImageButton = view.findViewById(R.id.search_layout_search_button)
        searchButton.setOnClickListener {
            val searchDisplayName = view.findViewById<TextInputEditText>(R.id.input_search).text.toString()
            if (searchDisplayName.isNotEmpty()) {
                val displayNames = arrayListOf<String>()

                borrowingUsers.forEach {
                    displayNames.add(it.displayName)
                }

                val sortedNames = FuzzySearch.extractSorted(searchDisplayName, displayNames, 50)
                val sortedBorrowingUsers = arrayListOf<BorrowingUser>()

                sortedNames.forEach {
                    sortedBorrowingUsers.add(borrowingUsers[it.index])
                }

                borrowingUsersRecyclerAdapter.borrowingUsers = sortedBorrowingUsers
                borrowingUsersRecyclerAdapter.notifyDataSetChanged()
            }
        }

        return view
    }
}