package com.example.nushlibrary.adminFragments.requestsReportFragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReportsRecyclerAdapter(private val context: Context): RecyclerView.Adapter<ReportsRecyclerAdapter.ViewHolder>() {
    var reports = arrayListOf<Report>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val reportTextView: TextView = itemView.findViewById(R.id.reports_text_view)
        val reportByTextView: TextView = itemView.findViewById(R.id.report_by_text_view)
        private val readReportButton: Button = itemView.findViewById(R.id.read_report_button)

        init {
            readReportButton.setOnClickListener {
                val report = reports[adapterPosition]

                database.child("reports").child(report.id).setValue(null)
                reports.remove(report)
                notifyDataSetChanged()

                Toast.makeText(context, "Cleared report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_reports, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]

        holder.reportTextView.text = null
        holder.reportByTextView.text = null

        userReference.child(report.id).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                holder.reportByTextView.text = "Report by ${user?.displayName}"

                holder.reportTextView.text = report.report
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return reports.size
    }
}