package com.yogeshkumar.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogeshkumar.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListPA : ArrayList<Music>
        var songPostion: Int = 0
//        var mediaPlayer:MediaPlayer? = null
        var isPlaying:Boolean = false

        //object for services
        var musicService: MusicService? = null

        lateinit var binding: ActivityPlayerBinding

        //repeat
        var repeat : Boolean = false

        //timer variables
        var min15:Boolean = false
        var min30:Boolean = false
        var min60:Boolean = false

        //current playing song ID
        var nowPlayingID:String = ""

        //favourite icon
        var isFavourite:Boolean = false
        var fIndex: Int = -1

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        //calling services or starting services
//        val intent = Intent(this, MusicService::class.java)
//        bindService(intent, this, BIND_AUTO_CREATE)
//        startService(intent)

//        setContentView(R.layout.activity_player)

        initializeLayout()

        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying){
                pauseMusic()
            }else {
                playMusic()
            }
        }

        //next or perivous button
        binding.previousBtnPA.setOnClickListener {
                prevNextSong(increment = false)
        }

        binding.nextBtnPA.setOnClickListener {
                prevNextSong(increment = true)
        }

        //text view start,end and Seek Bar
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })

        //repeat button binding
        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            }else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            }
        }

        //binding Back Button Player Activity
        binding.backBtnPA.setOnClickListener {
            finish()
        }

        //binding Equalizer Button Player Activity
        binding.equalizerBtnPA.setOnClickListener {
           try {
               val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
               eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
               eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
               eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
               startActivityForResult(eqIntent, 13)
           }catch (e:java.lang.Exception)
           {
               Toast.makeText(this,"Equalizer Feature not supported",Toast.LENGTH_SHORT).show()
           }
        }

        //binding Timer Button
        binding.timerBtnPA.setOnClickListener {

            val timer = min15 || min30 || min60

            if (!timer){
                showBottomSheetDialog()
            }
            else{
                    //creating a Dialog Box asking want to exit = Yes | Not
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Stop Timer")
                        .setMessage("Do you want to stop timer?")
                        .setPositiveButton("Yes"){_, _ ->
                            min15 = false
                            min30 = false
                            min60 = false

                            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
                        }
                        .setNegativeButton("No"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }           //timerBtnPA END


        //Share Button
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPostion].pat))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File!!"))
        }

        //Favourite Button
        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }else{
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favorite_icon)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPostion])
            }
        }

    }

    private fun initializeLayout(){
        //Receiving data from music adapter
        songPostion = intent .getIntExtra("index",0)
        when (intent.getStringExtra("class")){
            "FavouriteAdapter" ->{
                //calling services or starting services
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }

            "NowPlayingFragment"->{
                setLayout()

                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

                if (isPlaying){
                    binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                }else{
                    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
                }

            }
            "MusicAdapterSearch" ->{
                //calling services or starting services
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" ->{
                //calling services or starting services
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
//                createMediaPlayer()
            }
            "MainActivity" ->{
                //calling services or starting services
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
//                createMediaPlayer()
            }
        }
    }

    //creating a function for setting image
    private fun setLayout(){
        fIndex = favouriteCheker(musicListPA[songPostion].id)
        Glide.with(this@PlayerActivity)
            .load(musicListPA[songPostion].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPostion].title
        if (repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))

        //changing the color of timer or showing it is active or not by changing the color
        if (min15 || min30 || min60 ){
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        }

        //
        if (isFavourite){
            binding.favouriteBtnPA.setImageResource(R.drawable.favorite_icon)
        }else{
            binding.favouriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
        }

    }



    //
    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPostion].pat)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingID = musicListPA[songPostion].id
        }catch (e:Exception){return}
    }

    //


    //function for play
    private fun playMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    //function for pause
    private fun pauseMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()

    }


    //function for next or perivous button
    private fun prevNextSong(increment:Boolean){
        if (increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

//    //function for error control if song is null or your position is at last song
//    private fun setSongPosition(increment: Boolean){
//        if (increment){
//                if (musicListPA.size - 1 == songPostion){
//                    songPostion = 0
//
//                }else{
//                    ++songPostion
//                }
//
//        }else{
//            if (0 == songPostion){
//                songPostion = musicListPA.size - 1
//
//            }else{
//                --songPostion
//            }
//        }
//    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e:java.lang.Exception){return}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK){
            return
        }
    }

    //Timer function
    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            //changing the color of option of timer
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min15 = true
            Thread {Thread.sleep((15 * 60000).toLong())
                //starting the timer
            if (min15) exitApplication()}.start()

            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            //changing the color of option of timer
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min30 = true
            Thread {Thread.sleep((30 * 60000).toLong())
                //starting the timer
                if (min30) exitApplication()}.start()

            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            //changing the color of option of timer
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min60 = true
            Thread {Thread.sleep((60 * 60000).toLong())
                //starting the timer
                if (min60) exitApplication()}.start()

            dialog.dismiss()
        }
    }

}