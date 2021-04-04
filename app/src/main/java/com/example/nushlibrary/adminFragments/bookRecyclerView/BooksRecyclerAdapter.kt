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
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// 14 weeks in milliseconds
const val DUE_TIME = 1000 * 60 * 60 * 24 * 14
class BooksRecyclerAdapter(val supportFragmentManager: FragmentManager, private val user: User? = null): RecyclerView.Adapter<BooksRecyclerAdapter.ViewHolder>() {
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
                // We know   user is not null if remove button is visible
                user!!

                // Remove the book from the recycler view
                books.remove(book)
                notifyDataSetChanged()
                // Remove the book from the user
                user.booksBorrowed.remove(book.id)
                user.booksBorrowedTimeStamp.remove(book.borrowedTime)
                // Update user in database
                userReference.child(user.id).setValue(user)

                // Remove the user from the book
                book.borrowedBy.remove(user.id)
                book.borrowedTime = null
                // Update book in database
                bookReference.child(book.id).setValue(book)

                // Notify user
                Toast.makeText(itemView.context, "Removed ${book.title} from ${user.displayName} borrowed books list", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = books[position].title

        if (mainUser.admin) holder.editBtn.visibility = View.VISIBLE
        // This value is only passed if the recycler view is in a user dialog fragment
        if (user != null) holder.removeBtn.visibility = View.VISIBLE

        // Set it to null first so it doesn't display wrong thumbnail
        holder.thumbnail.setImageBitmap(null)
        val thumbnail = books[position].thumbnail
        if (thumbnail != null)
            ImageLoadTask(thumbnail, object : ImageLoadTask.AsyncResponse {
                override fun processFinish(output: Bitmap?) {
                    if (output != null)
                        holder.thumbnail.setImageBitmap(output)
                }
            }).execute()

        var authorsString = ""
        var authorNumber = 0
        books[position].authors.forEach {
            if (authorNumber != 0) {
                authorsString += " and "
            }

            authorsString += it
            authorNumber++
        }
        
        holder.authors.text = authorsString
        val borrowedTime = books[position].borrowedTime
        if (!mainUser.admin && borrowedTime != null && borrowedTime != 0L) {
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