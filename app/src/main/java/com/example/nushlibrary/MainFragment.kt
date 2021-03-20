package com.example.nushlibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider


class MainFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val logInBtn: Button = view.findViewById(R.id.logInButton)
        logInBtn.setOnClickListener {
            val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("microsoft.com")

            FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(activity as MainActivity , provider.build())
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    println(user.email)
                    println(user.displayName)
                }
                .addOnFailureListener {e->
                    println("error: ${e.message}")
                }
        }

        return view
    }
}