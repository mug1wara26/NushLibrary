package com.example.nushlibrary.adminFragments.usersFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.example.nushlibrary.dataClasses.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.math.roundToInt

class AdminUsersFragment: Fragment() {
    // This variable is used when searching
    private var allUsers = arrayListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_users, container, false)

        val userRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_users)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        val userAdapter = UserRecyclerAdapter(requireActivity().supportFragmentManager, requireContext())
        userRecyclerView.adapter = userAdapter

        val progressBar: ProgressBar = view.findViewById(R.id.load_users)
        showUsers(userAdapter, progressBar)

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
                                        && isOverdue(user)
                                        && !newUsers.contains(user)) newUsers.add(user)
                                }
                            }

                            R.id.no_overdue_books_check_box -> {
                                allUsers.forEach { user ->
                                    val daysFromDue = getDaysFromDue(user)
                                    if ((daysFromDue == null
                                        || !isOverdue(user))
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

        // Set the default checked radio button
        var checkedOrderId = R.id.reorder_display_name
        var checkedDirectionId = R.id.reorder_users_ascending

        val reorderButton: ImageButton = view.findViewById(R.id.admin_users_reorder_button)
        reorderButton.setOnClickListener {
            // I should have made this a function but I'm too lazy
            ReorderUsersDialogFragment(checkedOrderId, checkedDirectionId, object: ReorderUsersDialogFragment.GetOrderOnDismiss{
                override fun onDismiss(orderId: Int, ascending: Boolean) {
                    // Set default checked radio button
                    checkedOrderId = orderId
                    checkedDirectionId =
                        if (ascending) R.id.reorder_users_ascending
                        else R.id.reorder_users_descending

                    // Set whether the books are sorted ascending or descending
                    val comparatorBorrowedBooksNumber =
                        if (ascending) compareBy<User> { it.booksBorrowed.size }
                        else compareByDescending { it.booksBorrowed.size }
                    val comparatorDisplayName =
                        if (ascending) compareBy<User> { it.displayName }
                        else compareByDescending { it.displayName }

                    // Determine the final comparator
                    val userComparator =
                        if (orderId == R.id.reorder_books_borrowed_number) comparatorBorrowedBooksNumber
                        else comparatorDisplayName

                    val newUsers = ArrayList(userAdapter.users.sortedWith(userComparator))

                    // Set book adapter
                    userAdapter.users = newUsers
                    userAdapter.notifyDataSetChanged()
                }
            }).show(requireActivity().supportFragmentManager, "Reorder")
        }

        val searchInput: TextInputEditText = view.findViewById(R.id.input_search)

        val refreshButton: ImageButton = view.findViewById(R.id.admin_users_refresh_button)
        refreshButton.setOnClickListener {
            showUsers(userAdapter, progressBar, object: OnPostExecute{
                override fun onPostExecute() {
                    searchInput.text = null
                }
            })
        }

        val searchButton: ImageButton = view.findViewById(R.id.search_layout_search_button)
        searchButton.setOnClickListener {
            val displayName = searchInput.text.toString()
            if (displayName.isNotEmpty()) {
                userAdapter.users = searchForUser(allUsers, displayName)
                userAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    private fun showUsers(
        userAdapter: UserRecyclerAdapter,
        progressBar: ProgressBar,
        listener: OnPostExecute = object : OnPostExecute {
            override fun onPostExecute() { /* Do nothing */ }
        }) {
        progressBar.visibility = View.VISIBLE

        val newUsers = arrayListOf<User>()
        userReference.orderByChild("displayName").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null) {
                        newUsers.add(user)
                    }
                }
                // Sort users by display name
                userAdapter.users = ArrayList(newUsers.sortedWith(compareBy { it.displayName }))
                allUsers = userAdapter.users

                userAdapter.notifyDataSetChanged()
                listener.onPostExecute()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun searchForUser(users: ArrayList<User>, displayName: String): ArrayList<User> {
        val displayNames = arrayListOf<String>()

        for (user in users) {
            displayNames.add(user.displayName)
        }

        println(FuzzySearch.extractSorted(displayName, displayNames))
        val sortedNames = FuzzySearch.extractSorted(displayName, displayNames, 50)
        val sortedUsers = arrayListOf<User>()

        sortedNames.forEach {
            sortedUsers.add(users[it.index])
        }

        return sortedUsers
    }

    interface OnPostExecute {
        fun onPostExecute()
    }
}