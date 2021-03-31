package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import java.net.URL


class BooksRecyclerAdapter(val supportFragmentManager: FragmentManager): RecyclerView.Adapter<BooksRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_book, parent, false)
        return ViewHolder(v)
    }

    val books: ArrayList<Book> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.card_layout_book_title)
        val thumbnail: ImageView = itemView.findViewById(R.id.card_layout_book_thumbnail)
        val authors: TextView = itemView.findViewById(R.id.card_layout_book_authors)
        private val editBtn: Button = itemView.findViewById(R.id.edit_button)

        init {
            itemView.setOnClickListener {
                val book = books[adapterPosition]
                BookDialogFragment(book).show(supportFragmentManager, "Book")
            }
            editBtn.setOnClickListener {
                val book = books[adapterPosition]
                EditBookDialogFragment(book).show(supportFragmentManager, "Edit book")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = books[position].title

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