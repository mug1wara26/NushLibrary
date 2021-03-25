package com.example.nushlibrary.adminFragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R


// Callback interface to be inplemented in dialog
interface OnGenreClick {
    fun onGenreClick(genre: String)
}

class GenreRecyclerAdapter(listener: OnGenreClick) : RecyclerView.Adapter<GenreRecyclerAdapter.ViewHolder>() {
    val callback: OnGenreClick = listener

    private val genres = arrayOf(
        "Action and adventure",
        "Alternate history",
        "Anthology",
        "Art/architecture",
        "Autobiography",
        "Biography",
        "Business/economics",
        "Children's",
        "Classic",
        "Crafts/hobbies",
        "Comic book",
        "Coming-of-age",
        "Cookbook",
        "Crime",
        "Diary",
        "Dictionary",
        "Drama",
        "Encyclopedia",
        "Fairytale",
        "Fantasy",
        "Graphic novel",
        "Guide",
        "Health/fitness",
        "Historical fiction",
        "History",
        "Home and garden",
        "Horror",
        "Humor",
        "Journal",
        "Math",
        "Memoir",
        "Mystery",
        "Paranormal romance",
        "Philosophy",
        "Picture book",
        "Political thriller",
        "Prayer",
        "Religion, spirituality, and new age",
        "Romance",
        "Review",
        "Satire",
        "Science",
        "Science fiction",
        "Self help",
        "Short story",
        "Sports and leisure",
        "Suspense",
        "Textbook",
        "Thriller",
        "Travel",
        "True crime",
        "Western",
        "Young adult"
    )

    val selectedGenres = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.genre.text = genres[position]
    }

    override fun getItemCount(): Int {
        return genres.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var genre: TextView = itemView.findViewById(R.id.genre)

        init {
            itemView.setOnClickListener { view ->
                val position = adapterPosition

                val genre = genres[position]
                val message: String
                if (selectedGenres.contains(genre)) {
                    // Set opacity of item to 100%, remove it from selected genres
                    itemView.alpha = 1F
                    message = "Removed $genre"
                }
                else {
                    // Set opacity to 30%, add it to selected genres
                    itemView.alpha = 0.3F
                    message = "Added $genre"
                }

                // Add selectedGenres to onClick in interface
                callback.onGenreClick(genre)

                Toast.makeText(view.context!!, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}