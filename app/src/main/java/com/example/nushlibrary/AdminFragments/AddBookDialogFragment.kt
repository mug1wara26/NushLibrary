package com.example.nushlibrary.AdminFragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.R
import com.google.android.material.textfield.TextInputEditText
import java.lang.IllegalArgumentException


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

                var disabledCardView: CardView = manualCardView
                var enabledCardView: CardView = isbnCardView

                // Sets which card view will be enabled / disabled
                when (checkedId) {
                    R.id.isbn_radio_button -> {
                        disabledCardView = manualCardView
                        enabledCardView = isbnCardView
                        println("isbn")
                    }

                    R.id.manual_radio_button -> {
                        disabledCardView = isbnCardView
                        enabledCardView = manualCardView
                        println("manual")
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


            // Adds create and cancel buttons
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Create") { _, _ ->
                    if (view.findViewById<RadioButton>(R.id.isbn_radio_button).isChecked) {

                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}