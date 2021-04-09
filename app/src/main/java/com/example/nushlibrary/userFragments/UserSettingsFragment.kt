package com.example.nushlibrary.userFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.nushlibrary.*
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.adminFragments.bookRecyclerView.DUE_TIME
import com.example.nushlibrary.adminFragments.bookRecyclerView.daysAfterDue
import com.example.nushlibrary.adminFragments.bookRecyclerView.daysBeforeDue
import com.example.nushlibrary.adminFragments.bookRecyclerView.notifyUser
import com.example.nushlibrary.adminFragments.usersFragment.getDaysFromDue
import com.example.nushlibrary.dataClasses.Report
import com.example.nushlibrary.dataClasses.Request
import com.google.android.material.textfield.TextInputEditText

class UserSettingsFragment: Fragment() {
    @SuppressLint("SetTextI18n")
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

        val notificationsView: ConstraintLayout = view.findViewById(R.id.enable_notifications_view)

        // Enable notifications on clicked
        val enabledNotificationsCheckBox: CheckBox = view.findViewById(R.id.enable_notifications_checkbox)
        enabledNotificationsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            notificationsView.alpha =
                if (isChecked) 1F
                else 0.3F
            notificationsView.children.forEach {
                it.isEnabled = isChecked
            }
        }

        // Set default number in text inputs
        val daysBeforeDueInput: TextInputEditText = view.findViewById(R.id.days_before_due_input)
        val daysAfterDueInput: TextInputEditText = view.findViewById(R.id.days_after_due_input)

        daysBeforeDueInput.setText(daysBeforeDue.toString())
        daysAfterDueInput.setText(daysAfterDue.toString())

        // Set buttons to disabled if text input string is null
        val setNotifButton: Button = view.findViewById(R.id.set_notification_button)
        fun setButtonOnStringsEmpty(s1: String, s2: String) {
            val isStringsEmpty = s1.isEmpty() || s2.isEmpty()
            setNotifButton.isEnabled = !isStringsEmpty
            setNotifButton.alpha =
                if (!isStringsEmpty) 1F
                else 0.3F
        }

        fun setButtonOnStringsShort(s1: String, s2: String) {
            val isStringLengthShort = s1.length <= 2 && s2.length <= 2
            setNotifButton.isEnabled = isStringLengthShort
            setNotifButton.alpha =
                if (isStringLengthShort) 1F
                else 0.3F
        }

        daysBeforeDueInput.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val daysAfterDueString = daysAfterDueInput.text.toString()
                // Disable button is inputs are empty
                setButtonOnStringsEmpty(s.toString(), daysAfterDueString)
                // Disable button if inputs are too long
                setButtonOnStringsShort(s.toString(), daysAfterDueString)
            }
            override fun afterTextChanged(s: Editable?) {}; override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        daysAfterDueInput.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val daysBeforeDueString = daysBeforeDueInput.text.toString()
                // Disable button is inputs are empty
                setButtonOnStringsEmpty(s.toString(), daysBeforeDueString)
                // Disable button if inputs are too long
                setButtonOnStringsShort(s.toString(), daysBeforeDueString)
            }
            override fun afterTextChanged(s: Editable?) {}; override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        setNotifButton.setOnClickListener {
            daysBeforeDue = daysBeforeDueInput.text.toString().toInt()
            daysAfterDue = daysAfterDueInput.text.toString().toInt()

            notifyUser(requireContext())
        }

        val cancelSetNotifButton: Button = view.findViewById(R.id.cancel_set_notification_button)
        cancelSetNotifButton.setOnClickListener {
            daysBeforeDueInput.setText(daysBeforeDue.toString())
            daysAfterDueInput.setText(daysAfterDue.toString())
        }

        val setNotifTimeButton: Button = view.findViewById(R.id.set_notif_time_button)
        setNotifTimeButton.setOnClickListener {
            NotifTimePickerDialog().show(requireActivity().supportFragmentManager, "Set notif time")
        }

        return view
    }
}