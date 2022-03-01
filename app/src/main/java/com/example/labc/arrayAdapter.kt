package com.example.labc

import android.content.Context
import com.example.labc.model.cards
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.example.labc.R
import android.widget.TextView

class arrayAdapter(context: Context?, resourceId: Int, items: List<cards?>?) : ArrayAdapter<cards?>(
    context!!, resourceId, items!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val card_item = getItem(position)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false)
        }
        val songName = convertView!!.findViewById<View>(R.id.songName) as TextView
        val artist = convertView.findViewById<View>(R.id.artist) as TextView
        val image = convertView.findViewById<View>(R.id.image) as ImageView
        songName.text = card_item!!.getSongName()
        artist.text = "by: " + card_item.getArtist()
        image.setImageBitmap(card_item.getBitmap())
        return convertView
    }
}