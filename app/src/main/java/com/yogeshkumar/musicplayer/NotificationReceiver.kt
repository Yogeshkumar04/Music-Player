package com.yogeshkumar.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = context!!)
            ApplicationClass.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.EXIT -> {

                //calling the function to exit the application
                exitApplication()
//                PlayerActivity.musicService!!.stopForeground(true)
//                PlayerActivity.musicService!!.mediaPlayer!!.release()
//                PlayerActivity.musicService = null
//                exitProcess(1)
            }
        }

    }

    //creating function for play and pause
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)

        //changing icon
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)

        //changing icon
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    }


    //creating a function for song next or previous
    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)
//        PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPostion].pat)
//        PlayerActivity.musicService!!.mediaPlayer!!.prepare()
//        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
//        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPostion].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(PlayerActivity.binding.songImgPA)
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPostion].title
        //
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPostion].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(NowPlayingFragment.binding.songImgNP)

        //setting title
        NowPlayingFragment.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPostion].title
        playMusic()


        //
        PlayerActivity.fIndex = favouriteCheker(PlayerActivity.musicListPA[PlayerActivity.songPostion].id)

        if (PlayerActivity.isFavourite){
            PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favorite_icon)
        }else{
            PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
        }

    }




}