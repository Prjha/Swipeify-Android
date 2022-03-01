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
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {

    // ui
    private lateinit var mName: EditText
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mRegister: Button

    // firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // ui
        mName = findViewById(R.id.nameEditText)
        mEmail = findViewById(R.id.emailEditText)
        mPassword = findViewById(R.id.passwordEditText)
        mRegister = findViewById(R.id.registerButton)

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

        mRegister.setOnClickListener{


            val name = mName.text.toString()
            val email = mEmail.text.toString()
            val password = mPassword.text.toString()
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // todo add messages for different errors, i.e. faulty email/password
                    Log.d("Emil", "Sign-up error!")
                }else{
                    val userId = mAuth.currentUser!!.uid
                    Log.d("Emil", userId)
                    val currentUserDb = FirebaseDatabase.getInstance("https://labc-686ef-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Users").child(userId).child("Userame")
                    Log.d("Emil", currentUserDb.toString())
                    currentUserDb.setValue(name)
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

    fun cancelRegistration(view: android.view.View) {
        val intent = Intent(this, ChooseLoginRegistrationActivity::class.java)
        startActivity(intent)
        finish()
        return
    }
}