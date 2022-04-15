package com.yogeshkumar.musicplayer

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String,val title:String,val album:String,val artist:String, val duration: Long = 0, val pat: String,
val artUri: String)

//creating a function for display duration of song properly
fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

//creating global function for getting image
fun getImgArt(pat: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(pat)
        return retriever.embeddedPicture
}

//function for error control if song is null or your position is at last song
 fun setSongPosition(increment: Boolean){
   if (!PlayerActivity.repeat){
       if (increment){
           if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPostion){
               PlayerActivity.songPostion = 0

           }else{
               ++PlayerActivity.songPostion
           }

       }else{
           if (0 == PlayerActivity.songPostion){
               PlayerActivity.songPostion = PlayerActivity.musicListPA.size - 1

           }else{
               --PlayerActivity.songPostion
           }
       }
   }

}


//creating a function to exit the application
fun exitApplication(){
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
        exitProcess(1)
    }
}


//creating a global function for showing song is favourite or not
fun favouriteCheker(id:String): Int{
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed{ index, music ->
        if (id == music.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1

}

