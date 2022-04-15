package com.yogeshkumar.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yogeshkumar.musicplayer.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding

private lateinit var toggle: ActionBarDrawerToggle

private lateinit var musicAdapter: MusicAdapter

companion object{
    lateinit var MusicListMA : ArrayList<Music>

    //search
    lateinit var musicListSearch : ArrayList<Music>

    var search: Boolean = false
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermission()
        setTheme(R.style.coolPinkNav)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for nav drawer
        toggle = ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (requestRuntimePermission()){
            initializeLayout()

            //for retrieving favourites data using shared preferences
            FavouriteActivity.favouriteSongs = ArrayList()
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
            if(jsonString != null){
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }
        }


        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.favouriteBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,FavouriteActivity::class.java)
            startActivity(intent)
        }

        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,PlaylistActivity::class.java)
            startActivity(intent)
        }

        binding.navView.setNavigationItemSelectedListener{
            when (it.itemId)
            {
                R.id.nav_feedback-> Toast.makeText(baseContext,"Feedback",Toast.LENGTH_SHORT).show()
                R.id.nav_about -> Toast.makeText(baseContext,"About",Toast.LENGTH_SHORT).show()
                R.id.nav_setting -> Toast.makeText(baseContext,"Setting",Toast.LENGTH_SHORT).show()
                R.id.nav_exit -> {

                    //creating a Dialog Box asking want to exit = Yes | Not
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to exit app?")
                        .setPositiveButton("Yes"){_, _ ->

                            //calling a function to exit the application
                            exitApplication()
//                            if (PlayerActivity.musicService != null) {
//                                PlayerActivity.musicService!!.stopForeground(true)
//                                PlayerActivity.musicService!!.mediaPlayer!!.release()
//                                PlayerActivity.musicService = null
//                            }
//                            exitProcess(1)
                        }
                        .setNegativeButton("No"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

                }
            }
            true
        }







    }

    //For requesting permission
    private fun requestRuntimePermission():Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                initializeLayout()
            }
            else
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("SetTextI18n")
    private fun initializeLayout(){

        //initializing search
        search = false

//creating array list with items
//        val musicList = ArrayList<String>()
//        musicList.add("First Song")
//        musicList.add("Second Song")
//        musicList.add("Third Song")
//        musicList.add("Forth Song")
//        musicList.add("Fifth Song")

        MusicListMA = getAllAudio()

        //Recycler View // setting music adapter into recycler view
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA )
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : "+musicAdapter.itemCount

    }

    //creating a function for calling all the music or audio file form storage

    @SuppressLint("Recycle", "Range")
    private fun getAllAudio():ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)

        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,
        MediaStore.Audio.Media.DATE_ADDED + " DESC",null)

        if (cursor != null){
            if (cursor.moveToFirst()){
                do {
                    val title_C = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val id_C = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val album_C = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artist_C = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pat_C = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val duration_C = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumID_C = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri_C = Uri.withAppendedPath(uri, albumID_C).toString()
                    //creating object
                    val music = Music(id = id_C, title = title_C, album = album_C, artist = artist_C, pat = pat_C,
                        duration = duration_C, artUri = artUri_C)
                    val file = File(music.pat)
                    if (file.exists()){
                        tempList.add(music)
                    }

                }while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null){

            //calling the function to exit the application
                exitApplication()
//            PlayerActivity.musicService!!.stopForeground(true)
//            PlayerActivity.musicService!!.mediaPlayer!!.release()
//            PlayerActivity.musicService = null
//            exitProcess(1)
        }


    }

    override fun onResume() {
        super.onResume()
        // for storing favourites data using shared preference
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouritesSongs", jsonString)
        editor.apply()
    }

    //this method add search function and show results of search
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
//                Toast.makeText(this@MainActivity, newText.toString(), Toast.LENGTH_SHORT).show()
                musicListSearch = ArrayList()

                if (newText != null){
                    val userInput = newText.lowercase()
                    for (song in MusicListMA){
                        if (song.title.lowercase().contains(userInput)){
                            musicListSearch.add(song)
                        }
                        search = true
                        musicAdapter.updateMusicList(searchList = musicListSearch)
                    }
                }

                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}