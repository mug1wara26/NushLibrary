package com.example.nushlibrary.userFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nushlibrary.*
import com.example.nushlibrary.dataClasses.Report
import com.example.nushlibrary.dataClasses.Request
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

        fun disabledEditButtons() {
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
                            disabledEditButtons()
                        }
                    } else {
                        editNameWarning.visibility = View.GONE
                        disabledEditButtons()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {};override fun afterTextChanged(s: Editable?) {}
        })

        editNameButton.setOnClickListener {
            val newDisplayName = editNameInput.text.toString()

            val timeStamp = System.currentTimeMillis()
            val request = Request(mainUser.id, newDisplayName, timeStamp)
            database.child("requests").child(mainUser.id).setValue(request)

            Toast.makeText(context, "Edit name request has been made", Toast.LENGTH_SHORT).show()
        }

        cancelEditNameButton.setOnClickListener {
            editNameInput.setText(mainUser.displayName)
        }

        // Code to handle reports
        val reportButton: Button = view.findViewById(R.id.submit_report_button)
        val reportInput: TextInputEditText = view.findViewById(R.id.report_input)

        reportInput.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val characterLimit = s.toString().length in 100..500

                reportButton.isEnabled = characterLimit
                if (characterLimit) reportButton.alpha = 1F
                else reportButton.alpha = 0.3F
            }

            override fun afterTextChanged(s: Editable?) {}; override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        reportButton.setOnClickListener {
            val reportString = reportInput.text.toString()

            val timeStamp = System.currentTimeMillis()
            val report = Report(mainUser.id, reportString, timeStamp)
            database.child("reports").child(mainUser.id).setValue(report)
            reportInput.text = null

            Toast.makeText(context, "Sent report to admin", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}