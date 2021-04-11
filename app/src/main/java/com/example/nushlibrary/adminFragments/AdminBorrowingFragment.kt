package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.dataClasses.BorrowingUser
import com.example.nushlibrary.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminBorrowingFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        val borrowingUsersRecyclerView: RecyclerView = view.findViewById(R.id.borrowing_users_recycler_view)
        borrowingUsersRecyclerView.layoutManager = LinearLayoutManager(context)
        val borrowingUsersRecyclerAdapter = BorrowingUsersRecyclerAdapter(requireContext())
        borrowingUsersRecyclerView.adapter = borrowingUsersRecyclerAdapter

        val borrowingUsers = arrayListOf<BorrowingUser>()
        database.child("borrowing").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { snapshotChild ->
                    val borrowingUser = snapshotChild.getValue(BorrowingUser::class.java)
                    if (borrowingUser != null) {
                        borrowingUsers.add(borrowingUser)
                    }
                }

                borrowingUsersRecyclerAdapter.borrowingUsers = borrowingUsers
                borrowingUsersRecyclerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}