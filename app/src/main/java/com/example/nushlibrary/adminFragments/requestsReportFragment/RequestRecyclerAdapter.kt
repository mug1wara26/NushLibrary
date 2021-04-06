package com.example.nushlibrary.adminFragments.requestsReportFragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nushlibrary.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RequestRecyclerAdapter(val context: Context): RecyclerView.Adapter<RequestRecyclerAdapter.ViewHolder>() {
    var requests = arrayListOf<Request>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val requestByTextView: TextView = itemView.findViewById(R.id.request_by_text_view)
        val requestTextView: TextView = itemView.findViewById(R.id.request_text_view)
        private val acceptRequestButton: ImageButton = itemView.findViewById(R.id.accept_request_button)
        private val denyRequestButton: ImageButton = itemView.findViewById(R.id.deny_request_button)

        init {
            acceptRequestButton.setOnClickListener {
                val request = requests[adapterPosition]

                userReference.child(request.id).child("displayName").setValue(request.displayName)

                database.child("requests").child(request.id).setValue(null)
                requests.remove(request)
                notifyDataSetChanged()

                Toast.makeText(context, "Accepted request", Toast.LENGTH_SHORT).show()
            }

            denyRequestButton.setOnClickListener {
                val request = requests[adapterPosition]

                requests.remove(request)
                notifyDataSetChanged()

                Toast.makeText(context, "Request denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_requests, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]

        holder.requestByTextView.text = null
        holder.requestTextView.text = null

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                holder.requestByTextView.text = "Request by ${user?.displayName}"

                holder.requestTextView.text = "Change display name to ${request.displayName}"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return requests.size
    }
}