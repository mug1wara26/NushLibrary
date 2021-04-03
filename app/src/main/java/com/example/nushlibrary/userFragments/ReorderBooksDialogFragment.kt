package com.example.nushlibrary.userFragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.R

class ReorderBooksDialogFragment(private val checkedOrderId: Int, private val checkedDirectionId: Int, private val listener: GetOrderOnDismiss): DialogFragment() {
    interface GetOrderOnDismiss {
        fun onDismiss(orderId: Int, ascending: Boolean)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reorder_book, null)

        view.findViewById<RadioButton>(checkedOrderId).isChecked = true
        view.findViewById<RadioButton>(checkedDirectionId).isChecked = true

        builder.setTitle("Order by")
        builder.setView(view)
            .setPositiveButton("Order") { _, _ ->
                val reorderRadioGroup: RadioGroup = view.findViewById(R.id.reorder_book_radio_group)
                val directionRadioGroup: RadioGroup = view.findViewById(R.id.reorder_direction_radio_group)
                listener.onDismiss(
                    reorderRadioGroup.checkedRadioButtonId,
                    reorderRadioGroup.checkedRadioButtonId == R.id.reorder_book_ascending
                )
            }
            .setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }
}