package com.example.nushlibrary.adminFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.User
import com.example.nushlibrary.user
import com.example.nushlibrary.userReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminUsersFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_users, container, false)

        val userRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_users)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        val userAdapter = UserRecyclerAdapter(requireActivity().supportFragmentManager)
        userRecyclerView.adapter = userAdapter

        showUsers(userAdapter)


        return view
    }

    private fun showUsers(userAdapter: UserRecyclerAdapter) {
        val users = arrayListOf<User>()
        userReference.orderByChild("display_name").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                userAdapter.users = users
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}