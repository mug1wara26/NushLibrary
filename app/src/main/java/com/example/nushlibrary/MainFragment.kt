package com.example.nushlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

val database = Firebase.database.getReferenceFromUrl("https://nush-library-default-rtdb.firebaseio.com/")
val userReference = database.child("users")
class MainFragment: Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val logInBtn: Button = view.findViewById(R.id.logInButton)
        logInBtn.setOnClickListener {
            val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("microsoft.com")

            FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(activity as MainActivity , provider.build())
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    val loginErrorTextView: TextView = view.findViewById(R.id.loginErrorTextView)

                    if (user != null) {
                        if (user.email.endsWith("@nushigh.edu.sg") || user.email.endsWith("@nus.edu.sg")) {
                            userReference.child(user.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        // Check if user exists in database
                                        if (!snapshot.exists()) {
                                            val userClass = User(
                                                user.email!!,
                                                user.displayName!!,
                                                "[]",
                                                "[]",
                                                false
                                            )
                                            userReference.child(user.uid).setValue(userClass)
                                        }

                                        // If user is admin, navigate to admin fragment, else go to user fragment
                                        if (snapshot.child("admin").getValue(Boolean::class.java)!!) {

                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else loginErrorTextView.text = "Your email must be from nushigh or nus"
                    } else loginErrorTextView.text = "Log in error occurred, try again"
                }
                .addOnFailureListener {e->
                    println("error: ${e.message}")
                }
        }

        return view
    }
}