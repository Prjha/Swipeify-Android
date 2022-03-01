package com.example.labc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.labc.firebase.ChooseLoginRegistrationActivity
import com.example.labc.spotify.PlayerActivity
import com.example.labc.spotify.SpotifyService
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    private lateinit var mConnectButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        mConnectButton = findViewById(R.id.connectButton)

        mInfoTextView = findViewById(R.id.moreInfoTextView)

        mConnectButton.setOnClickListener{
            SpotifyService.connect(this){
                if(it){
                    val intent = Intent(this, PlayerActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.d("Emil", "User does not have spotify installed!")
                    mInfoTextView.text = "Spotify was not found.\nPlease check that you have Spotify installed on this device."
                    Handler().postDelayed({
                        mInfoTextView.text = resources.getString(R.string.moreInfoText)
                    }, 5000)
                }
            }
        }
    }

    fun signOutUser(view: android.view.View) {
        mAuth.signOut()
        val intent = Intent(this, ChooseLoginRegistrationActivity::class.java)
        startActivity(intent)
        finish()
        return
    }
}