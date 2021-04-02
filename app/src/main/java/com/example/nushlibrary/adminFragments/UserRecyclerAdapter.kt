package com.example.nushlibrary.adminFragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.User
import com.example.nushlibrary.userFragments.GetBooksOnPostExecute
import com.example.nushlibrary.userFragments.getBooksById

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
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayName.text = users[position].displayName
        holder.email.text = users[position].email
        holder.booksBorrowed.text = "Number of books borrowed: ${users[position].booksBorrowed.size}"
    }

    override fun getItemCount(): Int {
        return users.size
    }
}