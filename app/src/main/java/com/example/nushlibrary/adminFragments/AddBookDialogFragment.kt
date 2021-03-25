package com.example.nushlibrary.adminFragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.database
import com.google.android.material.textfield.TextInputEditText
import java.net.HttpURLConnection
import java.net.URL


class AddBookDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.dialog_add_book, null)
            val addBookRadioGroup: RadioGroup = view.findViewById(R.id.add_book_radio_group)

            // Listener when a radio button is changed
            addBookRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                // checkedId is the RadioButton selected
                val isbnCardView: CardView = view.findViewById(R.id.isbn_card_view)
                val manualCardView: CardView = view.findViewById(R.id.manual_card_view)

                // This function disables and greys out the card view that is not selected
                switchCardViews(manualCardView, isbnCardView, checkedId)
            }


            // Initialize the RecyclerView of genres
            val genreRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_genre)
            // Set layout manager to be a grid of column 2
            genreRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val adapter = GenreRecyclerAdapter()
            genreRecyclerView.adapter = adapter

            // Show/hide the recycler view on arrow button click
            val arrowButton: ImageButton = view.findViewById(R.id.arrow_button)
            val expandableCardViewGenre: CardView = view.findViewById(R.id.expandable_card_view_genre)

            arrowButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(
                    expandableCardViewGenre,
                    AutoTransition()
                )

                if (genreRecyclerView.visibility == View.VISIBLE) {
                    genreRecyclerView.visibility = View.GONE
                    arrowButton.setImageResource(R.drawable.icon_expand_arrow)
                }
                else {
                    genreRecyclerView.visibility = View.VISIBLE
                    arrowButton.setImageResource(R.drawable.icon_collapse_arrow)
                }
            }

            // We are storing context here because the fragment is removed after the create button is pressed
            // However we need context to use Toast
            val fragmentContext = context

            // Adds create and cancel buttons
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Create") { _, _ ->
                    if (view.findViewById<RadioButton>(R.id.isbn_radio_button).isChecked) {
                        val isbnInput: TextInputEditText = view.findViewById(R.id.isbn_input)
                        val numberInput: TextInputEditText = view.findViewById(R.id.isbn_book_number_input)

                        val isbn = isbnInput.text.toString().toLong()
                        val number: Int = numberInput.text.toString().toInt()

                        val genre = ArrayList<String>()
                        genre.add("Placeholder")
                        GetBookByISBN(genre, isbn, number, object: GetBookByISBN.AsyncResponse {
                            // onPostExecute result is transferred here
                            // Memory leak pog
                            override fun processFinish(output: Int) {
                                if (output == 1) Toast.makeText(fragmentContext, "Success! Book has been created", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(fragmentContext, "Could not get data from ISBN, please use the manual option.", Toast.LENGTH_LONG).show()
                            }
                        }).execute()
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun switchCardViews(manualCardView: CardView, isbnCardView: CardView, checkedId: Int) {
        var disabledCardView: CardView = manualCardView
        var enabledCardView: CardView = isbnCardView

        // Sets which card view will be enabled / disabled
        when (checkedId) {
            R.id.isbn_radio_button -> {
                disabledCardView = manualCardView
                enabledCardView = isbnCardView
            }

            R.id.manual_radio_button -> {
                disabledCardView = isbnCardView
                enabledCardView = manualCardView
            }
        }

        // Goes through all views in the card view and disables it
        // Get the constraint layout in the card view first, then loop through the children in that
        val disabledConstraintLayout = disabledCardView.getChildAt(0) as ConstraintLayout
        for (i in disabledConstraintLayout.children) {
            i.isEnabled = false
        }
        // Sets opacity to 30%
        disabledCardView.alpha = 0.3F

        val enabledConstraintLayout = enabledCardView.getChildAt(0) as ConstraintLayout
        for (i in enabledConstraintLayout.children) i.isEnabled = true
        // Sets to opacity to 100%
        enabledCardView.alpha = 1F
    }
}

// Test ISBNs:
// 9781451648546 - Steve Jobs
// 9780751565355 - Some Harry Potter Book (Multiple Authors)
open class GetBookByISBN(
    private val genre: ArrayList<String>,
    private val isbn: Long,
    private val number: Int,
    private val delegate: AsyncResponse?
    ): AsyncTask<Void, Void, Int>() {

    // Create an interface here and override the interface method in the AddBookDialogFragment class
    // This way, we can pass data from AsyncTask to the fragment class
    // Full details here https://stackoverflow.com/a/12575319/14403601
    interface AsyncResponse {
        fun processFinish(output: Int)
    }

    override fun onPostExecute(result: Int) {
        delegate!!.processFinish(result)
    }

    override fun doInBackground(vararg params: Void?): Int {
        var json = ""
        val url = URL("https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn")
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    json += "$line\n"
                }
            }
        }

        val parser = Parser()
        // Turns json string into json object
        val jsonObject = parser.parse(StringBuilder(json)) as JsonObject

        // Total items will either be 1 or 0 as there is only 1 book for every valid isbn
        if(jsonObject["totalItems"] == 1) {
            val authors = getValueFromPath(jsonObject, "items.volumeInfo.authors") as ArrayList<*>
            val title = getValueFromPath(jsonObject, "items.volumeInfo.title") as String?
            val description = getValueFromPath(jsonObject, "items.volumeInfo.description") as String?
            val publisher = getValueFromPath(jsonObject, "items.volumeInfo.publisher") as String?

            // Create book object and add it to database
            val book = Book(authors, title, description, publisher, genre, number)
            database.child("books").child(isbn.toString()).setValue(book)
        }

        // Returns result
        return jsonObject["totalItems"] as Int
    }

    private fun getValueFromPath(jsonObject: JsonObject, path: String): Any? {
        val pathList = path.split(".")
        // Initialize jsonArray
        var jsonArray: JsonArray<*> = jsonObject[pathList[0]] as JsonArray<*>

        // Remove the first directory in path
        pathList.subList(1, pathList.size).forEach {
            jsonArray = jsonArray[it]
        }

        // There may be multiple authors so this helps to make the data cleaner
        if (path.endsWith("authors")) {
            val authors: ArrayList<Any> = ArrayList()
            (jsonArray[0] as JsonArray<*>).forEach {
                if (it != null) {
                    authors.add(it)
                }
            }
            return authors
        }
        return jsonArray[0]
    }
}