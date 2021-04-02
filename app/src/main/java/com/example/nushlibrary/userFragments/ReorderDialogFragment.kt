package com.example.nushlibrary.userFragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.R

class ReorderDialogFragment(private val checkedId: Int, private val listener: GetOrderOnDismiss): DialogFragment() {
    interface GetOrderOnDismiss {
        fun onDismiss(orderId: Int)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reorder, null)

        println(checkedId)
        println(R.id.reorder_due_date_ascending)
        view.findViewById<RadioButton>(checkedId).isChecked = true

        builder.setTitle("Order by")
        builder.setView(view)
            .setPositiveButton("Order") { _, _ ->
                val reorderRadioGroup: RadioGroup = view.findViewById(R.id.reorder_radio_group)
                listener.onDismiss(reorderRadioGroup.checkedRadioButtonId)
            }
            .setNeutralButton("Back") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }
}