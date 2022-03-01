package com.example.labc.spotify

import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.labc.*
import com.example.labc.api.ItemJSONResponse
import com.example.labc.api.JsonPlaceHolderApi
import com.example.labc.api.RecommendationJSONResponse
import com.example.labc.firebase.ChooseLoginRegistrationActivity
import com.example.labc.model.cards
import com.google.firebase.auth.FirebaseAuth
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PlayerActivity : AppCompatActivity() {

    // ui
    private lateinit var mPauseButton: Button
    private lateinit var mPlayButton: Button
    private lateinit var mResumeButton: Button
    private lateinit var mReconnectButton: Button
    private lateinit var mLikeImageView: ImageView
    private lateinit var mDislikeImageView: ImageView

    // animations
    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation
    private var likeAnimationCancelled: Boolean = false
    private var dislikeAnimationCancelled: Boolean = false

    // firebase
    private lateinit var mAuth: FirebaseAuth

    // swipecards
    lateinit var mCards: ArrayList<cards>
    private lateinit var mArrayAdapter: arrayAdapter

    // spotify
    private val CLIENT_ID = "df90f4b104214528b01960317c8c60b8"
    private val  REDIRECT_URI = "https://127.0.0.1"
    private val API_BASE_URL = "https://api.spotify.com"
    private var mSpotifyAppRemote: SpotifyAppRemote? = null

    // Token
    private var tokenType = AuthorizationResponse.Type.TOKEN
    private var tokenValue = ""
    val REQUEST_CODE = 1337

    private var topArtistsList: ArrayList<String> = arrayListOf<String>()
    private var topTracksList: ArrayList<String> = arrayListOf<String>()
    private var recommendationList: ArrayList<String> = arrayListOf<String>()

    // Retrofit
    private lateinit var loggingInterceptor: HttpLoggingInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit
    private lateinit var request: JsonPlaceHolderApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // ui
        mPauseButton = findViewById(R.id.pauseButton)
        mPlayButton = findViewById(R.id.playButton)
        mResumeButton = findViewById(R.id.resumeButton)
        mReconnectButton = findViewById(R.id.reconnectButton)
        mLikeImageView = findViewById(R.id.likeImageView)
        mLikeImageView.visibility = View.GONE
        mDislikeImageView = findViewById(R.id.dislikeImageView)
        mDislikeImageView.visibility = View.GONE

        // animations
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        // firebase
        mAuth = FirebaseAuth.getInstance()

        // swipecards
        mCards = ArrayList()
        mArrayAdapter = arrayAdapter(this, R.layout.item, mCards)

        setupButtons()
        setupListeners()
        setUpSwipeCardListeners()

        loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        request = retrofit.create(JsonPlaceHolderApi::class.java)

        val builder =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("streaming", "user-top-read"))
        val request = builder.build()

        AuthorizationClient.clearCookies(this)

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    // sets up the buttons for the app
    private fun setupButtons() {
        mReconnectButton.visibility = View.GONE
        SpotifyService.playingState {
            when (it) {
                PlayingState.PLAYING -> showPauseButton()
                PlayingState.STOPPED -> showPlayButton()
                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }

    // sets up listeners for the app
    private fun setupListeners() {

        mPauseButton.setOnClickListener {
            SpotifyService.pause()
            showResumeButton()
        }

        mPlayButton.setOnClickListener {
            showPauseButton()
        }

        mResumeButton.setOnClickListener {
            SpotifyService.resume()
            showPauseButton()
        }

        mReconnectButton.setOnClickListener {
            SpotifyService.connect(this) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        SpotifyService.subscribeToChanges {
            updateCardView()
            Log.d("Emil", "change detected")
        }
    }

    private fun showPauseButton() {
        mPauseButton.visibility = View.VISIBLE
        mPlayButton.visibility = View.GONE
        mResumeButton.visibility = View.GONE
        mReconnectButton.visibility = View.GONE
    }

    private fun showPlayButton() {
        mPauseButton.visibility = View.GONE
        mPlayButton.visibility = View.VISIBLE
        mResumeButton.visibility = View.GONE
        mReconnectButton.visibility = View.GONE
    }

    private fun showResumeButton() {
        mPauseButton.visibility = View.GONE
        mPlayButton.visibility = View.GONE
        mResumeButton.visibility = View.VISIBLE
        mReconnectButton.visibility = View.GONE
    }

    private fun showReconnectButton() {
        mPauseButton.visibility = View.GONE
        mPlayButton.visibility = View.GONE
        mResumeButton.visibility = View.GONE
        mReconnectButton.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i("test123", "vi är inne")

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Log.i("test123", "Success: " + response.accessToken)
                    tokenType = AuthorizationResponse.Type.TOKEN
                    tokenValue = response.accessToken

                    Log.i("test123", "Time left: ${response.expiresIn}")

                    getUserTopArtists()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.i("test123", "Failed: " + response.toString())
                }
                AuthorizationResponse.Type.EMPTY -> {
                    Log.i("test123", "Failed i empty: ${response.error}")

                }
                else -> {
                    Log.i("test123", "Failed i else: ${response.accessToken}")
                }
            }
        }
    }

    private fun getRecommendation() {

        val artistArr =
            arrayOf(topArtistsList[0], topArtistsList[1], topArtistsList[2])
        val tracksArr = arrayOf(topTracksList[0], topTracksList[1])

        /*val artistArr =
            arrayOf("4NHQUGzhtTLFvgF5SZesLK", "C2CIMQHirSU0MQqyYHq0eOx", "C57dN52uHvrHOxijzpIgu3E")
        val tracksArr = arrayOf("0c6xIDDpzE81m2q797ordA", "C1p7939nftudJfauI7fpa04")*/

        val call = request.getRecommendations(
            "Bearer $tokenValue",
            60,
            "${topArtistsList[0]}%2C${topArtistsList[1]}%2C${topArtistsList[2]}",
            "${topTracksList[0]}%2C${topTracksList[1]}"
        )

        call.enqueue(object : Callback<RecommendationJSONResponse> {
            override fun onResponse(
                call: Call<RecommendationJSONResponse>,
                response: Response<RecommendationJSONResponse>
            ) {
                if (!response.isSuccessful) {
                    return
                } else {
                    val artists: RecommendationJSONResponse? = response.body()

                    val data = artists?.tracks

                    for(item in data!!) {
                        recommendationList.add(item.uri)
                    }

                    Log.i("Umar", recommendationList[0])

                }
            }

            override fun onFailure(call: Call<RecommendationJSONResponse>, t: Throwable) {
                Log.d("Retrofit", t.message.toString())
            }
        })
    }

    fun getUserTopArtists() {
        val call = request.getUserTopArtists("Bearer $tokenValue")

        call.enqueue(object : Callback<ItemJSONResponse> {
            override fun onResponse(
                call: Call<ItemJSONResponse>,
                response: Response<ItemJSONResponse>
            ) {
                if (!response.isSuccessful) {
                    return
                } else {
                    val artists: ItemJSONResponse? = response.body()

                    val data = artists?.items

                    for (item in data!!) {
                        topArtistsList.add(item.id)
                    }

                    getUserTopTracks()

                    /*for (item: Item in artists!!) {
                        var content = ""
                        content += "ID: " + track.url

                        textViewResult.append(content)
                    }

                    for (i in data.size) {
                        topArtistsList.add(data?.get(i).id)
                    }*/
                }
            }

            override fun onFailure(call: Call<ItemJSONResponse>, t: Throwable) {
                Log.d("Retrofit", t.message.toString())

            }
        })
    }

    fun getUserTopTracks() {
        val call = request.getUserTopTracks("Bearer $tokenValue")

        call.enqueue(object : Callback<ItemJSONResponse> {
            override fun onResponse(
                call: Call<ItemJSONResponse>,
                response: Response<ItemJSONResponse>
            ) {
                if (!response.isSuccessful) {
                    return
                } else {
                    val tracks: ItemJSONResponse? = response.body()

                    val data = tracks?.items

                    for (item in data!!) {
                        topTracksList.add(item.id)
                    }

                    /*for(i in 0..data?.size!!) {
                        topTracksList.add(data?.get(i).id)
                    }*/

                    getRecommendation()

                    /*for (track: Track in tracks!!) {
                        var content = ""
                        content += "ID: " + track.url

                        textViewResult.append(content)
                    }*/
                }
            }

            override fun onFailure(call: Call<ItemJSONResponse>, t: Throwable) {
                Log.d("Retrofit", t.message.toString())

            }
        })
    }

    override fun onStop() {
        super.onStop()

        showReconnectButton()
        SpotifyService.disconnect()
    }

    fun signOutUser(view: android.view.View) {
        SpotifyService.pause()
        mAuth.signOut()
        val intent = Intent(this, ChooseLoginRegistrationActivity::class.java)
        startActivity(intent)
        finish()
        return
    }

    // sets up the swipecard listeners
    private fun setUpSwipeCardListeners() {
        val flingContainer: SwipeFlingAdapterView = findViewById<SwipeFlingAdapterView>(R.id.frame)
        flingContainer.adapter = mArrayAdapter
        flingContainer.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            // when the user swipes
            override fun removeFirstObjectInAdapter() {
                removeCurrentCardView()
            }

            // when user swipes left
            override fun onLeftCardExit(dataObject: Any?) {
                dislikeSong()
            }

            // when user swipes right
            override fun onRightCardExit(dataObject: Any?) {
                likeSong()
            }

            // when the adapter is about be empty
            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                //updateCardView()
            }

            // when the user scrolls
            override fun onScroll(scrollProgressPercent: Float) {
            }

        })

        // when the user clicks the cardView
        flingContainer.setOnItemClickListener { itemPosition, dataObject ->

        }

    }

    private fun makeToast(ctx: Context?, s: String?) {
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show()
    }

    // animation when the user presses the likebutton
    private fun likeFadeInAndOut() {
        dislikeAnimationCancelled = true
        mDislikeImageView.visibility = View.GONE
        mLikeImageView.visibility = View.VISIBLE
        mLikeImageView.startAnimation(fadeIn)
        Handler().postDelayed({
            if(likeAnimationCancelled){
                mLikeImageView.visibility = View.GONE
            }else{
                mLikeImageView.startAnimation(fadeOut)
                Handler().postDelayed({
                    mLikeImageView.visibility = View.GONE
                }, 500)
            }
        }, 1500)
    }

    // animation when the user presses the dislikebutton
    private fun dislikeFadeInAndOut() {
        likeAnimationCancelled = true
        mLikeImageView.visibility = View.GONE
        mDislikeImageView.visibility = View.VISIBLE
        mDislikeImageView.startAnimation(fadeIn)
        Handler().postDelayed({
            if(dislikeAnimationCancelled){
                mDislikeImageView.visibility = View.GONE
            }else{
                mDislikeImageView.startAnimation(fadeOut)
                Handler().postDelayed({
                    mDislikeImageView.visibility = View.GONE
                }, 500)
            }
        }, 1500)
    }

    // when the user swipes right or presses the like button
    fun likeSong() {
        likeFadeInAndOut()
        dislikeAnimationCancelled = false
        SpotifyService.getCurrentTrack {
            val uri = it.uri
            SpotifyService.addToLibrary(uri)
            makeToast(this@PlayerActivity, "${it.name} was added to your library!")
        }
        SpotifyService.play(recommendationList[0])
        recommendationList.removeAt(0)
        showPauseButton()
        Log.d("SWIPE", "Swiped to the right!")
    }

    // when the user swipes left or presses the dislike button
    fun dislikeSong() {
        dislikeFadeInAndOut()
        dislikeAnimationCancelled = false
        makeToast(this@PlayerActivity, "Disliked!")
        SpotifyService.play(recommendationList[0])
        recommendationList.removeAt(0)
        showPauseButton()
        Log.d("SWIPE", "Swiped to the left!")
    }

    // when the user presses the like button
    fun likeButtonPressed(view: android.view.View) {
        removeCurrentCardView()
        likeSong()
    }

    // when the user presses the dislike button
    fun dislikeButtonPressed(view: android.view.View) {
        removeCurrentCardView()
        dislikeSong()
    }

    // removes the current cardView
    private fun removeCurrentCardView(){
        mCards.clear()
        mArrayAdapter.notifyDataSetChanged()
    }

    // updates the cardView with information and an image of the new song
    private fun updateCardView() {
        Log.d("Emil", "updateCardView()")
        // handler to let the Spotify SDK get the new song before adding it to the list
        SpotifyService.getCurrentTrack {
            val trackName = it.name
            val trackArtist = it.artist.name
            val imageUri = it.imageUri
            SpotifyService.getImage(imageUri) {
                val bitmap = it
                mCards.add(cards(trackName, trackArtist, bitmap))
                mArrayAdapter.notifyDataSetChanged()
            }
        }
    }


}