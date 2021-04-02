package com.example.nushlibrary.adminFragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.User
import com.example.nushlibrary.adminFragments.bookRecyclerView.DUE_TIME

const val DAYS_IN_MILLIS = 1000 * 60 * 60 * 24
class UserRecyclerAdapter(val supportFragmentManager: FragmentManager): RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>() {
    var users = arrayListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_user, parent, false)
        return ViewHolder(v)
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayName: TextView = itemView.findViewById(R.id.user_display_name)
        val email: TextView = itemView.findViewById(R.id.user_email)
        val booksBorrowed: TextView = itemView.findViewById(R.id.user_borrowed_books)
        val daysFromDue: TextView = itemView.findViewById(R.id.user_days_from_due)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayName.text = users[position].displayName
        holder.email.text = users[position].email
        holder.booksBorrowed.text = "Number of books borrowed: ${users[position].booksBorrowed.size}"

        if (users[position].booksBorrowed.size != 0) {
            // Sorts a list that stores time stamps of when the user borrowed a book and get the earliest one
            val earliestTimeStamp = users[position].booksBorrowedTimeStamp.sortedWith(
                compareBy { it })[0]

            // Get number of milliseconds from the due time stamp
            val dueTimeStamp = earliestTimeStamp + DUE_TIME
            val millisFromDue = dueTimeStamp - System.currentTimeMillis()

            // Check if book is overdue or not
            if (millisFromDue >= 0) {
                val daysFromDue = millisFromDue / DAYS_IN_MILLIS
                holder.daysFromDue.text = "Days from book due: $daysFromDue"
            }
            else {
                val daysOverdue = (millisFromDue * -1) / DAYS_IN_MILLIS
                holder.daysFromDue.text = "Days overdue: $daysOverdue"
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}