package com.hiennv.flutter_callkit_incoming

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class CallkitSoundPlayer(private val context: Context) {

    private var data: Bundle? = null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null
    private var duration: Long = 0L

    private var runnableTimeout = Runnable {
        if (mediaPlayer?.isPlaying == true) {
            val intent = CallkitIncomingBroadcastReceiver.getIntentTimeout(context, data)
            context.sendBroadcast(intent)
            stop()
        }
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }


    fun play(data: Bundle?) {
        this.data = data
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stop()
        }
        playSound()
        handler.postDelayed(runnableTimeout, duration)
    }

    fun stop() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stopMusic()
        }
        handler.removeCallbacks(runnableTimeout)
    }

    private fun stopMusic() {
        mediaPlayer?.run {
            stop()
            seekTo(0)
        }
    }

    private fun playSound() {
        val sound = this.data?.getString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_SOUND,
            "ringtone_default"
        )
        val uri = sound?.let { getRingtoneUri(it) }
        mediaPlayer = MediaPlayer.create(context, uri).apply {
            isLooping = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attribution = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setAudioAttributes(attribution)
            }
            start()
        }
    }


    private fun getRingtoneUri(fileName: String) = try {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            Uri.parse("android.resource://${context.packageName}/${R.raw.ringtone_default}")
        }
    } catch (e: Exception) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.ringtone_default}")
    }


}