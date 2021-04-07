package com.example.nushlibrary.adminFragments.usersFragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.dataClasses.User
import com.example.nushlibrary.adminFragments.bookRecyclerView.DUE_TIME


const val DAYS_IN_MILLIS = 1000 * 60 * 60 * 24
class UserRecyclerAdapter(val supportFragmentManager: FragmentManager, val context: Context): RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>() {
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
        private val copyIdBtn: Button = itemView.findViewById(R.id.copy_id_button)

        init {
            itemView.setOnClickListener {
                UserDialogFragment(users[adapterPosition]).show(supportFragmentManager, "User")
            }
            copyIdBtn.setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("User ID", users[adapterPosition].id)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.displayName.text = user.displayName
        holder.email.text = user.email
        holder.booksBorrowed.text = "Number of books borrowed: ${user.booksBorrowed.size}"
        // Reset visibility on daysFromDue
        holder.daysFromDue.visibility = View.GONE

        val daysFromDue = getDaysFromDue(user)
        if (daysFromDue != null) {
            if (daysFromDue >= 0) holder.daysFromDue.text = "Days from book due: $daysFromDue"
            else holder.daysFromDue.text = "Days overdue: ${daysFromDue * -1}"
            holder.daysFromDue.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}

fun getDaysFromDue(user: User): Long? {
    return if (user.booksBorrowed.size != 0) {
        // Sorts a list that stores time stamps of when the user borrowed a book and get the earliest one
        val earliestTimeStamp = user.booksBorrowedTimeStamp.sorted()[0]

        // Get number of milliseconds from the due time stamp
        val dueTimeStamp = earliestTimeStamp + DUE_TIME
        val millisFromDue = dueTimeStamp - System.currentTimeMillis()

        // Check if book is overdue or not
        millisFromDue / DAYS_IN_MILLIS
    }
    else null
}