package com.example.nushlibrary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

val database = Firebase.database.getReferenceFromUrl("https://nush-library-default-rtdb.firebaseio.com/")
val userReference = database.child("users")
class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Start log in on click
        val logInBtn: Button = findViewById(R.id.logInButton)
        logInBtn.setOnClickListener {
            logInBtn.isEnabled = false
            logInBtn.alpha = 0.6F

            // Set the provider to microsoft
            val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("microsoft.com")

            // Starts microsoft log in
            FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user != null) {
                        if (user.email!!.endsWith("@nushigh.edu.sg") || user.email!!.endsWith("@nus.edu.sg")) {
                            userReference.child(user.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        // Check if user exists in database
                                        if (!snapshot.exists()) {
                                            val userClass = User(
                                                user.email!!,
                                                user.displayName!!,
                                                null,
                                                null,
                                                false
                                            )
                                            userReference.child(user.uid).setValue(userClass)
                                        }

                                        // If user is admin, start AdminActivity, else start UserActivity
                                        if (snapshot.child("admin").getValue(Boolean::class.java)!!) {
                                            // Re-enables the log in button in case user presses back button
                                            logInBtn.isEnabled = true
                                            logInBtn.alpha = 1F

                                            val intent = Intent(applicationContext, AdminActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else Toast.makeText(this, "Your email must be from nushigh or nus", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "Log in error occurred, try again", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {e ->
                    Toast.makeText(this, "An unknown error occurred, try again", Toast.LENGTH_SHORT).show()
                    println("error: ${e.message}")
                    logInBtn.isEnabled = true
                    logInBtn.alpha = 1F
                }
        }
    }
}