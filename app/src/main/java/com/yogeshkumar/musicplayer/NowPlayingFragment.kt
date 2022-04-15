package com.yogeshkumar.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yogeshkumar.musicplayer.databinding.FragmentNowPlayingBinding


class NowPlayingFragment : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)

        //initializing binding
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        //setting clickOnListener on playPause Button
        binding.playPauseBtnNP.setOnClickListener{
            if (PlayerActivity.isPlaying){
                //calling function
                pauseMusic()
            }else{
                //calling function
                playMusic()
            }
        }

        //setting clickOnListener on next Button
        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment = true)

            PlayerActivity.musicService!!.createMediaPlayer()

            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPostion].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
                .into(binding.songImgNP)

            //setting title
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPostion].title
            //notification
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            playMusic()

        }

        // code for viewing current song from now playing fragment
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)

            //sending data
            intent.putExtra("index", PlayerActivity.songPostion)
            intent.putExtra("class", "NowPlayingFragment")


            ContextCompat.startActivity(requireContext(), intent, null)
        }

        return view
    }



    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE

            //making the title or name of song moving on now playing fragment
            binding.songNameNP.isSelected = true

            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPostion].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_icon_splash_screen).centerCrop())
                .into(binding.songImgNP)

            //setting title
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPostion].title

            //setting play pause icon
            if (PlayerActivity.isPlaying){
                binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            }else{
                binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
            }
        }
    }

    //creating functions for play pause buttons
    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }

}