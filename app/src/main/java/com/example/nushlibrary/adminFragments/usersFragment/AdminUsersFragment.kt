package com.example.nushlibrary.adminFragments.usersFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.User
import com.example.nushlibrary.userReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminUsersFragment: Fragment() {
    private var allUsers = arrayListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_users, container, false)

        val userRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_users)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        val userAdapter = UserRecyclerAdapter(requireActivity().supportFragmentManager)
        userRecyclerView.adapter = userAdapter

        showUsers(userAdapter)

        val filterButton: ImageButton = view.findViewById(R.id.admin_users_filter_button)
        filterButton.setOnClickListener {
            FilterUserDialogFragment(object: FilterUserDialogFragment.GetCheckedOnDismiss{
                override fun onDismiss(checkedIds: ArrayList<Int>) {
                    val newUsers = arrayListOf<User>()
                    checkedIds.forEach { checkedId ->
                        when(checkedId) {
                            R.id.borrowed_books_check_box -> {
                                allUsers.forEach { user ->
                                    if (user.booksBorrowed.size != 0) newUsers.add(user)
                                }
                            }

                            R.id.no_borrowed_books_check_box -> {
                                allUsers.forEach { user ->
                                    if (user.booksBorrowed.size == 0) newUsers.add(user)
                                }
                            }

                            R.id.overdue_books_check_box -> {
                                allUsers.forEach { user ->
                                    val daysFromDue = getDaysFromDue(user)
                                    if (daysFromDue != null
                                        && daysFromDue < 0
                                        && !newUsers.contains(user)) newUsers.add(user)
                                }
                            }

                            R.id.no_overdue_books_check_box -> {
                                allUsers.forEach { user ->
                                    val daysFromDue = getDaysFromDue(user)
                                    if ((daysFromDue == null
                                        || daysFromDue > 0)
                                        && !newUsers.contains(user)) newUsers.add(user)
                                }
                            }
                        }
                    }

                    userAdapter.users = newUsers
                    userAdapter.notifyDataSetChanged()
                }

            }).show(requireActivity().supportFragmentManager, "Filter users")
        }

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
                allUsers = users
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}