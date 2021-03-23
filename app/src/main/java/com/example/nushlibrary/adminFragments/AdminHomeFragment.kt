package com.example.nushlibrary.adminFragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import com.example.nushlibrary.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.database
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern


class AdminHomeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        val addBookButton: Button = view.findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            activity?.supportFragmentManager?.let { it ->
                AddBookDialogFragment().show(it, "Add Book")
            }
        }
        return view
    }
}

//GetBookByISBN(URL("https://www.googleapis.com/books/v1/volumes?q=isbn:9781451648546")).execute()
class GetBookByISBN(private val genre: String, private val number: Int, private val isbn: Int, private val url: URL): AsyncTask<Void, Void, String>() {
    override fun doInBackground(vararg params: Void?): String {
        var json = ""
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    json += "$line\n"
                }
            }
        }
        // Returns result
        return "$isbn,$json"
    }


    override fun onPostExecute(result: String?) {
        if (result != null){
            // Get substring of first character to first index of ','
            val isbn = result.substring(0, result.indexOf(','))
            // Get substring of the rest of the json, account for the commas in the start index
            val json = result.substring(result.indexOf(',') + 1)
            createBookWithISBN(isbn, json)
        }
    }

    private fun createBookWithISBN(isbn: String, json: String) {
        println(json)

        val pathMatcher = object: PathMatcher {
            // Placeholder fields to be replaced with value
            var placeholder: String = "title,authors,publisher,description"
            override fun pathMatches(path: String)
                    = Pattern.matches(".items.volumeInfo.(title|authors|publisher|description)", path)

            override fun onMatch(path: String, value: Any) {
                val key = path.substringAfterLast('.')
                placeholder.replace(key, value.toString())

                if(!placeholder.contains("title|authors|publisher|description".toRegex())) {
                    println(placeholder)
                    val fields = placeholder.split(",")

                    // Create book using fields
                    val book = Book(fields[0], fields[1], fields[2], fields[3], genre, number)

                    // Add book to database with isbn as identifier
                    database.child("books").child(isbn).setValue(book)
                }
            }

        }

        // Run Patch Matcher
        Klaxon().pathMatcher(pathMatcher).parseJsonObject(StringReader(json))
    }
}