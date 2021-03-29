package com.example.nushlibrary.adminFragments.addBookDialogFragment

import android.os.AsyncTask
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.nushlibrary.Book
import com.example.nushlibrary.database
import java.net.HttpURLConnection
import java.net.URL

// Test ISBNs:
// 9781451648546 - Steve Jobs
// 9780751565355 - Some Harry Potter Book (Multiple Authors)
open class GetBookByISBN(
    private val genre: ArrayList<String>,
    private val isbn: Long,
    private val number: Int,
    private val listener: AsyncResponse
): AsyncTask<Void, Void, Book?>() {
    var book: Book? = null

    // Create an interface here and override the interface method in the AddBookDialogFragment class
    // This way, we can pass data from AsyncTask to the fragment class
    // Full details here https://stackoverflow.com/a/12575319/14403601
    interface AsyncResponse {
        fun processFinish(output: Book?)
    }

    override fun onPostExecute(result: Book?) {
        listener.processFinish(result)
    }

    override fun doInBackground(vararg params: Void?): Book? {
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
            val thumbnail = getValueFromPath(jsonObject, "items.volumeInfo.imageLinks.thumbnail") as String?

            // Create book object and add it to database
            book = Book(isbn.toString(), authors, title, description, publisher, genre, thumbnail, number)
            database.child("books").child(isbn.toString()).setValue(book)
        }

        // Returns result
        return book
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