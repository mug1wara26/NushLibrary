package com.example.nushlibrary.adminFragments.usersFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.example.nushlibrary.R

class FilterUserDialogFragment(private val listener: GetCheckedOnDismiss): DialogFragment() {
    interface GetCheckedOnDismiss{
        fun onDismiss(checkedIds: ArrayList<Int>)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_filter_user, null)

        with(builder) {
            setTitle("Filter")
            setView(view)

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Filter") { _, _ ->
                val checkBoxes = arrayListOf<CheckBox>(
                    view.findViewById(R.id.borrowed_books_check_box),
                    view.findViewById(R.id.no_borrowed_books_check_box),
                    view.findViewById(R.id.overdue_books_check_box),
                    view.findViewById(R.id.no_overdue_books_check_box),
                )

                val checkedIds = arrayListOf<Int>()
                checkBoxes.forEach { checkBox ->
                    if (checkBox.isChecked) checkedIds.add(checkBox.id)
                }

                listener.onDismiss(checkedIds)
            }

            return create()
        }
    }
}