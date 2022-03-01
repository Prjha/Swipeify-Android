package com.example.labc.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.labc.MainActivity
import com.example.labc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    // ui
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mLogin: Button

    // firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ui
        mEmail = findViewById(R.id.emailEditText)
        mPassword = findViewById(R.id.passwordEditText)
        mLogin = findViewById(R.id.loginButton)

        // firebase
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthStateListener = FirebaseAuth.AuthStateListener{
            val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if(user!=null){
                Log.d("Emil", "Register auth state changed!")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        mLogin.setOnClickListener{
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // todo add different messages for different errors, i.e. incorrect email/password
                    Log.d("Emil", "Login error!")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(firebaseAuthStateListener)
    }

    fun cancelLogin(view: android.view.View) {
        val intent = Intent(this, ChooseLoginRegistrationActivity::class.java)
        startActivity(intent)
        finish()
        return
    }

}