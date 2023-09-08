package com.shreyansh.exoplayer_mux

import android.content.ContentProvider
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.mux.stats.sdk.core.model.CustomData
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import com.mux.stats.sdk.core.model.CustomerViewData
import com.mux.stats.sdk.core.model.CustomerViewerData
import com.mux.stats.sdk.muxstats.MuxStatsExoPlayer
import com.mux.stats.sdk.muxstats.monitorWithMuxData
import com.shreyansh.exoplayer_mux.databinding.ActivityMainBinding
import java.util.UUID
import javax.sql.DataSource

class MainActivity : AppCompatActivity() {

//    I recommend that ExoPlayer is now getting Deprecated so instead study and understand
//    the working of Media3ExoPlayer it is the newer player provided by Android
//    which has better features and video playing capabilities

    private lateinit var binding: ActivityMainBinding

    //initializing exoplayer
    private var exoPlayer : SimpleExoPlayer? =null

    private var muxStatsExoPlayer : MuxStatsExoPlayer ? =null

    private val videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
    private var customerData = CustomerData()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ExoplayerMUX)
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //here we are adding some elements to Customer Data
        customerData = CustomerData().apply {
            customerVideoData = CustomerVideoData().apply {
                // Data about this video
                // Add or change properties here to customize video metadata such as title,
                //   language, etc
                videoTitle = "Mux ExoPlayer Android"
                // ExoPlayer doesn't provide an API to obtain this, so it must be set manually
                videoSourceUrl = videoUrl
            }
            customerViewData = CustomerViewData().apply {
                // Data about this viewing session
                viewSessionId = UUID.randomUUID().toString()
            }
            customerViewerData = CustomerViewerData().apply {
                // Data about the Viewer and the device they are using
                muxViewerDeviceCategory = "kiosk"
                muxViewerDeviceManufacturer = "Example Device"
                muxViewerOsVersion = "0.0.1-dev"
            }
            customData = CustomData().apply {
                // Add values for your Custom Dimensions
                // Up to 5 strings can be set to track own data
                customData1 = "Hello"
                customData2 = "World"
                customData3 = "From"
                customData4 = "Mux"
                customData5 = "Data"
            }
        }
    }

    private fun initPlayer(){

        //initialize the player
        exoPlayer=SimpleExoPlayer.Builder(this).build()
        binding.exoPlayerView.player=exoPlayer
        exoPlayer!!.playWhenReady=true
        exoPlayer!!.setMediaSource(buildMediaSource())
        exoPlayer!!.prepare()

        muxStatsExoPlayer = exoPlayer!!.monitorWithMuxData(
            context = this,
            envKey = "5h5v06ok3neps8k66b1dppton",
            playerView = binding.exoPlayerView,
            customerData = customerData
        )
        muxStatsExoPlayer!!.setPlayerView(binding.exoPlayerView.getVideoSurfaceView());
    }

    private fun buildMediaSource(): MediaSource {
        val  dataSourceFactory : com.google.android.exoplayer2.upstream.DataSource.Factory= DefaultHttpDataSource.Factory()
         val mediaSource:MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
             MediaItem.fromUri(videoUrl))
        return mediaSource
    }

    private fun releaseExoPlayer() {
        if(exoPlayer!=null){
            exoPlayer!!.release()
            exoPlayer=null
            muxStatsExoPlayer!!.release()
        }
    }

    override fun onStop() {
        super.onStop()
        if(Util.SDK_INT < 24 || exoPlayer==null){
            releaseExoPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if(Util.SDK_INT < 24){
            releaseExoPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if(Util.SDK_INT < 24 || exoPlayer==null){
            initPlayer()
        }
    }

    override fun onStart() {
        super.onStart()
        if(Util.SDK_INT >= 24){
            initPlayer()
        }
    }
}