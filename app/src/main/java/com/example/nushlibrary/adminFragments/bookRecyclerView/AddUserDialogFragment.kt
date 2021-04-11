package com.example.nushlibrary.adminFragments.bookRecyclerView

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.dataClasses.Book
import com.example.nushlibrary.R
import com.example.nushlibrary.dataClasses.User
import com.example.nushlibrary.userReference
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AddUserDialogFragment(private val book: Book): DialogFragment() {

    lateinit var dialogContext: Context
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_book_to_user, null)

        dialogContext = requireContext()

        with(builder) {
            setTitle("Add to a user's borrowed book list")
            setView(view)

            setPositiveButton("Add") { _, _ ->
                val userIdInput: TextInputEditText = view.findViewById(R.id.user_id_input)
                val userId = userIdInput.text.toString()

                if (userId.isNotEmpty()) {
                    userReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(User::class.java)

                                // Add book to user
                                if (user != null) {
                                    val result = borrowBook(book, user)

                                    if (result) showToast("Successfully added book to user")
                                    else showToast("Unable to add book to user")
                                }
                                else showToast("Error getting user from ID, please try again", Toast.LENGTH_LONG)
                            }
                            else showToast("Error getting user from ID, check that the user ID is correct", Toast.LENGTH_LONG)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showToast("Database error")
                        }
                    })
                }
            }
            setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }

            return create()
        }
    }

    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(dialogContext, message, length).show()
    }
}