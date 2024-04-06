package com.musicapp.jamtunes

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.musicapp.jamtunes.fregment.HomeFragment
import com.musicapp.jamtunes.fregment.MusicFragment
import com.musicapp.jamtunes.fregment.SettingFragment
import com.musicapp.jamtunes.fregment.VideoFragment

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_music -> {
                    replaceFragment(MusicFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_video -> {
                    replaceFragment(VideoFragment())
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_setting -> {
                    replaceFragment(SettingFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Set the initial fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Check and request permissions
        checkPermissions()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkPermissions() {
        val readPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = arrayListOf<String>()

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, proceed with loading audio and video files
                     loadAudioFiles()
                    loadVideoFiles()
                } else {
                    // Permission denied, handle accordingly
                }
            }
        }

    }
    private fun loadAudioFiles() {
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val audioCursor = contentResolver.query(audioUri, audioProjection, null, null, null)

        audioCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                val filePath = path.substringBeforeLast('/')
                val folderName = filePath.substringAfterLast('/')

               /* val existingAlbum = albumList.find { info ->
                    info.album == album && info.albumId == albumId
                }
                if (existingAlbum == null) {
                    // If not, add it along with the art URI
                    val albumInfo = AlbumInfo(albumId, album, albumArtUri.toString())
                    albumList.add(albumInfo)
                }*/

                val audioFile = AudioFile(
                    id,
                    title,
                    artist,
                    album,
                    albumId,
                    duration,
                    size,
                    path,
                    albumArtUri
                )
                audioList.add(audioFile)
                val audioFolder = AudioFolder(folderName)
                if (!audioFolderList.contains(audioFolder)) {
                    audioFolderList.add(audioFolder)
                }

                /*if (!artistList.contains(artist)) {
                    artistList.add(artist)
                }*/
            }
        }
    }


    private fun loadVideoFiles() {
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )
        val videoCursor = contentResolver.query(videoUri, videoProjection, null, null, null)

        videoCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val videoArtUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Extract folder information
                val filePath = path.substringBeforeLast('/')
                val folderName = filePath.substringAfterLast('/')

                val videoFile = VideoFile(id, title, size, duration, path, videoArtUri)
                videoList.add(videoFile)

                // Add folder to the folder list
                val videoFolder = VideoFolder(folderName)
                if (!folderList.contains(videoFolder)) {
                    folderList.add(videoFolder)
                }
            }
        }
    }



    companion object {

        private const val PERMISSIONS_REQUEST_CODE = 100
        val audioList = ArrayList<AudioFile>() // ArrayList to store audio files
        val videoList = ArrayList<VideoFile>() // ArrayList to store video files
        val audioFolderList = ArrayList<AudioFolder>()
        val folderList = ArrayList<VideoFolder>()
       /* val albumList = ArrayList<AlbumInfo>()
        val artistList = ArrayList<String>()*/
    }
}
