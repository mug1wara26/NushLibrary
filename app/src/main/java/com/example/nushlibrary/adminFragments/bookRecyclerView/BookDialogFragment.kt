package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView

class BookDialogFragment(val book: Book): DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{ fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)

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
                if (getStringFromArrayList(book.genre).isNullOrEmpty()) "No genres"
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

            val arrowButton: ImageButton = view.findViewById(R.id.arrow_button_description)
            val expandableCardView: CardView = view.findViewById(R.id.dialog_book_expandable_description)
            val descriptionTextView: TextView = view.findViewById(R.id.dialog_book_description)

            setExpandableView(arrowButton, expandableCardView, descriptionTextView)

            builder.setView(view)
                .setNeutralButton("Back") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}