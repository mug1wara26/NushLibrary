package com.example.nushlibrary.adminFragments

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.bookRecyclerView.borrowBook
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.dataClasses.BorrowingUser
import com.example.nushlibrary.dataClasses.User
import com.example.nushlibrary.database
import com.example.nushlibrary.userReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@SuppressLint("SetTextI18n")
class BorrowingUsersRecyclerAdapter(val context: Context): RecyclerView.Adapter<BorrowingUsersRecyclerAdapter.ViewHolder>() {
    var borrowingUsers = arrayListOf<BorrowingUser>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val displayName: TextView = itemView.findViewById(R.id.borrowing_user_name)
        val bookToBorrow: TextView = itemView.findViewById(R.id.book_to_borrow)
        private val acceptBorrowButton: ImageButton = itemView.findViewById(R.id.accept_borrow_button)
        private val denyBorrowButton: ImageButton = itemView.findViewById(R.id.deny_borrow_button)

        init {
            acceptBorrowButton.setOnClickListener {
                val borrowingUser = borrowingUsers[adapterPosition]

                database.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.child("borrowing").child(borrowingUser.userId).getValue(User::class.java)
                        val book = snapshot.child("books").child(borrowingUser.bookId).getValue(Book::class.java)

                        if (user != null && book != null) {
                            val result = borrowBook(book, user)

                            if (result) {
                                // Remove book from user queue
                                removeUserFromQueue(user, borrowingUser)
                                notifyItemRemoved(adapterPosition)

                                Toast.makeText(context, "Accepted user request to borrow book", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(context, "Was unable to add book to user's borrowed book list", Toast.LENGTH_LONG).show()
                            }
                        }
                        else {
                            Toast.makeText(context, "Was unable to retrieve data from database", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            denyBorrowButton.setOnClickListener {
                val borrowingUser = borrowingUsers[adapterPosition]

                userReference.child(borrowingUser.userId).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)

                        if (user != null) {
                            removeUserFromQueue(user, borrowingUser)
                            notifyItemRemoved(adapterPosition)

                            Toast.makeText(context, "Removed user request to borrow book", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    fun removeUserFromQueue(user: User, borrowingUser: BorrowingUser) {
        user.booksBorrowedQueue.remove(borrowingUser.bookId)
        if (user.booksBorrowedQueue.size != 0) database.child("borrowing").child(user.id).setValue(user)
        else database.child("borrowing").child(user.id).removeValue()
        userReference.child(user.id).setValue(user)

        borrowingUsers.remove(borrowingUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_borrowing_user, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayName.text = borrowingUsers[position].displayName
        holder.bookToBorrow.text = "would like to borrow ${borrowingUsers[position].bookTitle}"
    }

    override fun getItemCount(): Int {
        return borrowingUsers.size
    }
}