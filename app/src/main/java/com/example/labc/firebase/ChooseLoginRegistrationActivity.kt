package com.example.labc.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.labc.R

class ChooseLoginRegistrationActivity : AppCompatActivity() {

    private lateinit var mLogin: Button
    private lateinit var mRegister: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_login_registration)

        mLogin = findViewById(R.id.loginButton)
        mRegister = findViewById(R.id.registerButton)

        mLogin.setOnClickListener {
            Log.d("Emil", "Login button pressed!")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        mRegister.setOnClickListener{
            Log.d("Emil", "Register button pressed!")
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}