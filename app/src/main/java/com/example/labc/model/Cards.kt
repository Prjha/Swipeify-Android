package com.example.labc.model

import android.graphics.Bitmap
import com.spotify.protocol.types.ImageUri

class cards constructor(private var songName: String, private var artist: String, private var bitmap: Bitmap){


    fun getSongName(): String{
        return songName
    }

    fun getArtist(): String {
        return artist
    }

    fun getBitmap(): Bitmap{
        return bitmap
    }

}