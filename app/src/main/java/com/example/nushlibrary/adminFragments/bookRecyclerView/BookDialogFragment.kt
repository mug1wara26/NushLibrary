package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.adminFragments.usersFragment.UserRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@SuppressLint("SetTextI18n")
class BookDialogFragment(val book: Book): DialogFragment() {
    var isBorrowBookThreadLocked = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_book, null)

        fun setText(text: String?, textViewId: Int) {
            view.findViewById<TextView>(textViewId).text = text
        }

        fun getStringFromArrayList(arrayList: ArrayList<*>): String {
            var returnString = ""
            var authorNumber = 0
            arrayList.forEach {
                if (authorNumber != 0) {
                    returnString += " and "
                }

                returnString += it
                authorNumber++
            }
            return returnString
        }

        val genreString =
            if (getStringFromArrayList(book.genre).isEmpty()) "No genres"
            else "Genres: ${getStringFromArrayList(book.genre)}"
        val publisherString =
            if (book.publisher.isNullOrEmpty()) "No publisher"
            else "Published by ${book.publisher}"


        val textArrayList: ArrayList<String?> = arrayListOf(
            book.title,
            "By ${getStringFromArrayList(book.authors)}",
            "Number of books left: ${book.number}",
            genreString,
            publisherString,
            book.description
        )
        val textViewIdArrayList: ArrayList<Int> = arrayListOf(
            R.id.dialog_book_title,
            R.id.dialog_book_authors,
            R.id.dialog_book_number,
            R.id.dialog_book_genres,
            R.id.dialog_book_publisher,
            R.id.dialog_book_description
        )

        textArrayList.forEachIndexed { index, s ->
            setText(s, textViewIdArrayList[index])
        }

        val thumbnailImageView: ImageView = view.findViewById(R.id.dialog_book_thumbnail)
        if (book.thumbnail != null)
            ImageLoadTask(book.thumbnail, object : ImageLoadTask.AsyncResponse {
                override fun processFinish(output: Bitmap?) {
                    if (output != null)
                        thumbnailImageView.setImageBitmap(output)
                }
            }).execute()

        val arrowButtonDescription: ImageButton = view.findViewById(R.id.arrow_button_description)
        val expandableDescriptionCardView: CardView = view.findViewById(R.id.dialog_book_expandable_description)
        val descriptionTextView: TextView = view.findViewById(R.id.dialog_book_description)

        setExpandableView(arrowButtonDescription, expandableDescriptionCardView, descriptionTextView)

        if (user.admin) {
            // Show a recycler view of users that borrowed this book
            val arrowButtonUsers: ImageButton = view.findViewById(R.id.arrow_button_users)
            val expandableUsersCardView: CardView = view.findViewById(R.id.dialog_book_expandable_users)
            val borrowedByRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_borrowed_by)

            expandableUsersCardView.visibility = View.VISIBLE
            setExpandableView(arrowButtonUsers, expandableUsersCardView, borrowedByRecyclerView)

            // Set recycler view users
            borrowedByRecyclerView.layoutManager = LinearLayoutManager(context)
            val usersAdapter = UserRecyclerAdapter(requireActivity().supportFragmentManager)

            getUsersById(book.borrowedBy, object: GetUsersOnPostExecute{
                override fun onPostExecute(users: ArrayList<User>) {
                    usersAdapter.users = users
                    borrowedByRecyclerView.adapter = usersAdapter
                }
            })
        }
        else {
            // Show borrow and to read button to user
            val borrowButton: Button = view.findViewById(R.id.borrow_book_button)
            val toReadButton: Button = view.findViewById(R.id.to_read_button)

            borrowButton.visibility = View.VISIBLE
            toReadButton.visibility = View.VISIBLE

            // Disable the button if user has already borrowed the book
            if (user.booksBorrowed.contains(book.id)) {
                borrowButton.isEnabled = false
                borrowButton.alpha = 0.3F
            }

            borrowButton.setOnClickListener {
                borrowButtonOnClick(view, borrowButton)
            }
            toReadButton.setOnClickListener {
                if (!user.toReadList.contains(book.id)) {
                    user.toReadList.add(book.id)
                    toReadButton.alpha = 0.3F
                    Toast.makeText(context, "Added to your to read list", Toast.LENGTH_SHORT).show()
                }
                else {
                    user.toReadList.remove(book.id)
                    toReadButton.alpha = 1F
                    Toast.makeText(context, "Removed from your to read list", Toast.LENGTH_SHORT).show()
                }


                userReference.child(user.id).child("toReadList").setValue(user.toReadList)
            }
        }

        builder.setView(view)
            .setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun borrowButtonOnClick(view: View, borrowButton: Button) {
        // Lock thread to avoid race condition
        if (!isBorrowBookThreadLocked) {
            isBorrowBookThreadLocked = true
            if (book.number > 0) {
                if (!user.booksBorrowed.contains(book.id)) {
                    val timeStamp = System.currentTimeMillis()
                    // Add book to user books borrowed
                    user.booksBorrowed.add(book.id)
                    userReference.child(user.id).child("booksBorrowed")
                        .setValue(user.booksBorrowed)
                    // Add book borrowed timestamp to user
                    user.booksBorrowedTimeStamp.add(timeStamp)
                    userReference.child(user.id).child("booksBorrowedTimeStamp").setValue(user.booksBorrowedTimeStamp)

                    // Decrease book number
                    book.number--
                    view.findViewById<TextView>(R.id.dialog_book_number).text =
                        "Number of books left: ${book.number}"
                    bookReference.child(book.id).child("number").setValue(book.number)

                    // Set book timestamp
                    book.borrowedTime = timeStamp
                    bookReference.child(book.id).child("borrowedTime").setValue(book.borrowedTime)

                    // Add user to borrowedBy field in user
                    book.borrowedBy.add(user.id)
                    bookReference.child(book.id).child("borrowedBy").setValue(book.borrowedBy)

                    Toast.makeText(context, "Successfully borrowed book", Toast.LENGTH_SHORT)
                        .show()

                    // Disable the button
                    borrowButton.isEnabled = false
                    borrowButton.alpha = 0.3F
                }
            } else Toast.makeText(context, "No books left", Toast.LENGTH_SHORT).show()
        }
        isBorrowBookThreadLocked = false
    }

    interface GetUsersOnPostExecute {
        fun onPostExecute(users: ArrayList<User>)
    }
    private fun getUsersById(usersId: ArrayList<String>, listener: GetUsersOnPostExecute) {
        val users = arrayListOf<User>()
        userReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersId.forEach { id ->
                    val user = snapshot.child(id).getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                listener.onPostExecute(users)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}