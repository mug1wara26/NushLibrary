package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.usersFragment.UserRecyclerAdapter
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.dataClasses.User
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// 14 weeks in milliseconds
const val DUE_TIME = 1000 * 60 * 60 * 24 * 14
class BooksRecyclerAdapter(
    val supportFragmentManager: FragmentManager,
    private val user: User? = null,
    private val inBookFragment: Boolean = false,
    private val userAdapter: UserRecyclerAdapter? = null
): RecyclerView.Adapter<BooksRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_book, parent, false)
        return ViewHolder(v)
    }

    var books: ArrayList<Book> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.card_layout_book_title)
        val thumbnail: ImageView = itemView.findViewById(R.id.card_layout_book_thumbnail)
        val authors: TextView = itemView.findViewById(R.id.card_layout_book_authors)
        val dueDate: TextView = itemView.findViewById(R.id.card_layout_book_due_date)
        val editBtn: Button = itemView.findViewById(R.id.edit_button)
        val removeBtn: Button = itemView.findViewById(R.id.remove_button)
        val addBtn: Button = itemView.findViewById(R.id.add_button)

        init {
            itemView.setOnClickListener {
                val book = books[adapterPosition]
                BookDialogFragment(book).show(supportFragmentManager, "Book")
            }
            editBtn.setOnClickListener {
                val book = books[adapterPosition]
                EditBookDialogFragment(book, this@BooksRecyclerAdapter).show(supportFragmentManager, "Edit book")
            }
            removeBtn.setOnClickListener {
                val book = books[adapterPosition]
                // We know user is not null if remove button is visible
                user!!

                // Remove the book from the recycler view
                books.remove(book)
                // Remove the book from the user
                user.booksBorrowed.remove(book.id)

                val borrowedTime = book.borrowedUsers.filter { it.id == user.id }[0].timeStamp
                user.booksBorrowedTimeStamp.remove(borrowedTime)
                // Update user in database
                userReference.child(user.id).setValue(user)

                // Remove the user from the book
                book.borrowedUsers.removeIf {
                    it.id == user.id
                }
                book.number++
                // Update book in database
                bookReference.child(book.id).setValue(book)

                notifyItemRemoved(adapterPosition)
                userAdapter?.notifyDataSetChanged()

                // Notify user
                Toast.makeText(itemView.context, "Removed ${book.title} from ${user.displayName} borrowed books list", Toast.LENGTH_LONG).show()
            }
            addBtn.setOnClickListener {
                AddUserDialogFragment(books[adapterPosition]).show(supportFragmentManager, "Add user to book")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title

        if (mainUser.admin) holder.editBtn.visibility = View.VISIBLE
        // This value is only passed if the recycler view is in a user dialog fragment which only the admin can see
        if (user != null) holder.removeBtn.visibility = View.VISIBLE
        // This value tells us if we are in BookFragment
        if (inBookFragment && mainUser.admin) holder.addBtn.visibility = View.VISIBLE

        // Set it to null first so it doesn't display wrong thumbnail
        holder.thumbnail.setImageBitmap(null)
        val thumbnail = book.thumbnail
        if (thumbnail != null)
            ImageLoadTask(thumbnail, object : ImageLoadTask.AsyncResponse {
                override fun processFinish(output: Bitmap?) {
                    if (output != null)
                        holder.thumbnail.setImageBitmap(output)
                }
            }).execute()

        var authorsString = ""
        var authorNumber = 0
        book.authors.forEach {
            if (authorNumber != 0) {
                authorsString += " and "
            }

            authorsString += it
            authorNumber++
        }
        holder.authors.text = authorsString

        holder.dueDate.text = null
        if (!mainUser.admin && mainUser.booksBorrowed.contains(book.id)) {
            val borrowedUsers = book.borrowedUsers
            val borrowedTime = borrowedUsers.filter { it.id == mainUser.id }[0].timeStamp

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

            val dueDate = Date(borrowedTime + DUE_TIME)
            holder.dueDate.text = "Due on ${sdf.format(dueDate)}"
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }
}

// Async task to get image bitmap
class ImageLoadTask(private val url: String, private val delegate: AsyncResponse?) :
    AsyncTask<Void?, Void?, Bitmap?>() {
    interface AsyncResponse {
        fun processFinish(output: Bitmap?)
    }

    override fun onPostExecute(result: Bitmap?) {
        delegate!!.processFinish(result)
    }

    override fun doInBackground(vararg params: Void?): Bitmap? {
        try {
            return BitmapFactory.decodeStream(URL(url).openStream())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}