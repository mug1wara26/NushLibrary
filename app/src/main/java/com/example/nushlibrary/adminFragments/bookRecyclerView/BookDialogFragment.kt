package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
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
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.dataClasses.BorrowedUser
import com.example.nushlibrary.dataClasses.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
var isBorrowBookThreadLocked = false
const val NOTIFY_ID = 100
var daysBeforeDue = 3
var daysAfterDue = 5
var pendingIntent: PendingIntent? = null
class BookDialogFragment(val book: Book): DialogFragment() {
    @SuppressLint("SetTextI18n")
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

        if (mainUser.admin) {
            // Show a recycler view of users that borrowed this book
            val arrowButtonUsers: ImageButton = view.findViewById(R.id.arrow_button_users)
            val expandableUsersCardView: CardView = view.findViewById(R.id.dialog_book_expandable_users)
            val borrowedByRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_borrowed_by)

            expandableUsersCardView.visibility = View.VISIBLE
            setExpandableView(arrowButtonUsers, expandableUsersCardView, borrowedByRecyclerView)

            // Set recycler view users
            borrowedByRecyclerView.layoutManager = LinearLayoutManager(context)
            val usersAdapter = UserRecyclerAdapter(requireActivity().supportFragmentManager, requireContext())

            getUsersById(book.borrowedUsers, object: GetUsersOnPostExecute{
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
            if (mainUser.booksBorrowed.contains(book.id)) {
                borrowButton.isEnabled = false
                borrowButton.alpha = 0.3F
            }

            borrowButton.setOnClickListener {
                val result = borrowBook(book, mainUser, requireContext())
                if (result) {
                    Toast.makeText(context, "Successfully borrowed book", Toast.LENGTH_SHORT).show()

                    // Update number of books left and borrow button
                    view.findViewById<TextView>(R.id.dialog_book_number).text =
                        "Number of books left: ${book.number}"
                    borrowButton.isEnabled = false
                    borrowButton.alpha = 0.3F
                }
                else {
                    Toast.makeText(context, "Unable to borrow book", Toast.LENGTH_SHORT).show()
                }
            }
            toReadButton.setOnClickListener {
                if (!mainUser.toReadList.contains(book.id)) {
                    mainUser.toReadList.add(book.id)
                    toReadButton.alpha = 0.3F
                    Toast.makeText(context, "Added to your to read list", Toast.LENGTH_SHORT).show()
                }
                else {
                    mainUser.toReadList.remove(book.id)
                    toReadButton.alpha = 1F
                    Toast.makeText(context, "Removed from your to read list", Toast.LENGTH_SHORT).show()
                }


                userReference.child(mainUser.id).child("toReadList").setValue(mainUser.toReadList)
            }
        }

        builder.setView(view)
            .setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    interface GetUsersOnPostExecute {
        fun onPostExecute(users: ArrayList<User>)
    }
    private fun getUsersById(borrowedUsers: ArrayList<BorrowedUser>, listener: GetUsersOnPostExecute) {
        val users = arrayListOf<User>()
        userReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                borrowedUsers.forEach { borrowedUser ->
                    val user = snapshot.child(borrowedUser.id).getValue(User::class.java)

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

@SuppressLint("SetTextI18n")
fun borrowBook(book: Book, user: User, context: Context): Boolean {
    // Lock thread to avoid race condition
    if (!isBorrowBookThreadLocked) {
        isBorrowBookThreadLocked = true
        if (book.number > 0) {
            if (!user.booksBorrowed.contains(book.id)) {
                val timeStamp = System.currentTimeMillis()

                // Add book to user
                user.booksBorrowed.add(book.id)
                user.booksBorrowedTimeStamp.add(timeStamp)
                // Update user in database
                userReference.child(user.id).setValue(user)

                // Add user to book
                book.number--
                book.borrowedUsers.add(BorrowedUser(user.id, timeStamp))
                // Update book in database
                bookReference.child(book.id).setValue(book)

                // Update notifications
                notifyUser(context)

                // Unlock thread
                isBorrowBookThreadLocked = false

                return true
            }
        }
    }
    return false
}

// Notify user only for the book that is due the earliest
// This function is called everytime the user borrows a book just to update it
fun notifyUser(
    context: Context,
    calendar: Calendar = Calendar.getInstance().apply {
        // 7 am
        timeInMillis = System.currentTimeMillis()
        timeZone = TimeZone.getTimeZone("Asia/Singapore")
        set(Calendar.HOUR_OF_DAY, 7)
    }) {
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    val notifyIntent = Intent(context, NotificationPublisher::class.java)
    notifyIntent.putExtra("user-id", mainUser.id)

    pendingIntent = PendingIntent.getBroadcast(context, 0, notifyIntent, 0)

    // setRepeating() lets you specify a precise custom interval--in this case, every day
    alarmMgr.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        1000 * 60 * 60 * 24,
        pendingIntent
    )
}