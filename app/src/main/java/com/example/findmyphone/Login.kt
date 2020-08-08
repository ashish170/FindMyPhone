package com.example.findmyphone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*


class Login : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        signInAnon()

    }

    fun signInAnon()
    {
        mAuth!!.signInAnonymously().addOnCompleteListener(this)
        { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(applicationContext, "Authentication Success",Toast.LENGTH_SHORT).show()

                    val user = mAuth!!.currentUser

                } else {
                    // If sign in fails, display a message to the user.

                    Toast.makeText(applicationContext, "Authentication failed.",Toast.LENGTH_SHORT).show()

                }
        }
    }

    fun login(view:View)
    {
        val userinfo= userData(this)
        userinfo.savePhoneNum(editTextPhone.text.toString())
        val df=SimpleDateFormat("yyyy/MM/dd hh:MM:ss" )
        val date=Date()
        val myRef = FirebaseDatabase.getInstance().reference
        myRef.child("Users").child(editTextPhone.text.toString())
            .child("request").setValue(df.format(date).toString())
        myRef.child("Users").child(editTextPhone.text.toString())
            .child("Finders").setValue(df.format(date).toString())


        finish()
    }
}