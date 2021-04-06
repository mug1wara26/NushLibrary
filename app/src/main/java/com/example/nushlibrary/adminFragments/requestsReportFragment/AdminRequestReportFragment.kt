package com.example.nushlibrary.adminFragments.requestsReportFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.R
import com.example.nushlibrary.Report
import com.example.nushlibrary.Request
import com.example.nushlibrary.adminFragments.addBookDialogFragment.setExpandableView
import com.example.nushlibrary.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminRequestReportFragment: Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_request_report, container, false)

        val expandRequestsLayout: ConstraintLayout = view.findViewById(R.id.expand_requests_layout)
        val expandReportsLayout: ConstraintLayout = view.findViewById(R.id.expand_reports_layout)

        expandRequestsLayout.findViewById<TextView>(R.id.expandable_card_view_title)
            .text = "Requests"
        expandReportsLayout.findViewById<TextView>(R.id.expandable_card_view_title)
            .text = "Reports"

        // Set requests and reports to be expandable
        val expandRequestsCardView: CardView = view.findViewById(R.id.expandable_card_view_requests)
        val requestsRecyclerView: RecyclerView = view.findViewById(R.id.request_recycler_view)
        val requestsArrowButton: ImageButton = expandRequestsLayout.findViewById(R.id.arrow_button)
        setExpandableView(requestsArrowButton, expandRequestsCardView, requestsRecyclerView)

        val expandReportsCardView: CardView = view.findViewById(R.id.expandable_card_view_reports)
        val reportsRecyclerView: RecyclerView = view.findViewById(R.id.reports_recycler_view)
        val reportsArrowButton: ImageButton = expandReportsLayout.findViewById(R.id.arrow_button)
        setExpandableView(reportsArrowButton, expandReportsCardView, reportsRecyclerView)

        // Set requests recycler view
        requestsRecyclerView.layoutManager = LinearLayoutManager(context)
        val requestsAdapter = RequestRecyclerAdapter(requireContext())
        requestsRecyclerView.adapter = requestsAdapter

        database.child("requests").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = arrayListOf<Request>()
                snapshot.children.forEach { snapshotChild ->
                    val request = snapshotChild.getValue(Request::class.java)
                    if (request != null) requests.add(request)
                }

                requestsAdapter.requests = ArrayList(requests.sortedWith(compareBy { it.timeStamp }))
                requestsAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Set reports recycler view
        reportsRecyclerView.layoutManager = LinearLayoutManager(context)
        val reportsAdapter = ReportsRecyclerAdapter(requireContext())
        reportsRecyclerView.adapter = reportsAdapter

        database.child("reports").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = arrayListOf<Report>()
                snapshot.children.forEach { snapshotChild ->
                    val report = snapshotChild.getValue(Report::class.java)
                    if (report != null) reports.add(report)
                }

                reportsAdapter.reports = ArrayList(reports.sortedWith(compareBy { it.timeStamp }))
                reportsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}