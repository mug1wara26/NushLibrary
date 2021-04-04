package com.example.nushlibrary.userFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nushlibrary.R
import com.example.nushlibrary.mainUser
import com.example.nushlibrary.userReference
import com.google.android.material.textfield.TextInputEditText

class UserSettingsFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_settings, container, false)

        // Set the text on this to the user display name
        val editNameInput: TextInputEditText = view.findViewById(R.id.edit_display_name_input)
        editNameInput.setText(mainUser.displayName)

        val editNameWarning: TextView = view.findViewById(R.id.edit_display_name_warning)
        val editNameButton: TextView = view.findViewById(R.id.edit_display_name_button)
        val cancelEditNameButton: TextView = view.findViewById(R.id.cancel_edit_display_name_button)

        fun disabledButtons() {
            editNameButton.isEnabled = false
            editNameButton.alpha = 0.3F
            cancelEditNameButton.isEnabled = false
            cancelEditNameButton.alpha = 0.3F
        }

        editNameInput.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.toString() != mainUser.displayName && s.toString().isNotEmpty()) {
                        if (Regex("[a-zA-Z ]*").matches(s) && s.length <= 30) {
                            editNameButton.isEnabled = true
                            editNameButton.alpha = 1F
                            cancelEditNameButton.isEnabled = true
                            cancelEditNameButton.alpha = 1F
                            editNameWarning.visibility = View.GONE
                        }
                        else {
                            editNameWarning.visibility = View.VISIBLE
                            disabledButtons()
                        }
                    } else {
                        editNameWarning.visibility = View.GONE
                        disabledButtons()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {};override fun afterTextChanged(s: Editable?) {}
        })

        editNameButton.setOnClickListener {
            val newDisplayName = editNameInput.text.toString()
            // Change display name for user
            mainUser.displayName = newDisplayName
            userReference.child(mainUser.id).child("displayName").setValue(newDisplayName)
            // Set text in textInputEditText
            editNameInput.setText(newDisplayName)

            Toast.makeText(context, "Successfully edited name", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}