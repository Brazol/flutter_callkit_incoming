package com.hiennv.flutter_callkit_incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

class CallkitIncomingBroadcastReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_CALL_INCOMING =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_INCOMING"
        const val ACTION_CALL_ACCEPT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        const val ACTION_CALL_DECLINE =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        const val ACTION_CALL_ENDED =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_ENDED"
        const val ACTION_CALL_TIMEOUT =
            "com.hiennv.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"


        const val EXTRA_CALLKIT_INCOMING_DATA = "EXTRA_CALLKIT_INCOMING_DATA"

        const val EXTRA_CALLKIT_ID = "EXTRA_CALLKIT_ID"
        const val EXTRA_CALLKIT_NAME_CALLER = "EXTRA_CALLKIT_NAME_CALLER"
        const val EXTRA_CALLKIT_NUMBER = "EXTRA_CALLKIT_NUMBER"
        const val EXTRA_CALLKIT_TYPE = "EXTRA_CALLKIT_TYPE"
        const val EXTRA_CALLKIT_AVATAR = "EXTRA_CALLKIT_AVATAR"
        const val EXTRA_CALLKIT_DURATION = "EXTRA_CALLKIT_DURATION"
        const val EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION = "EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION"
        const val EXTRA_CALLKIT_SOUND = "EXTRA_CALLKIT_SOUND"
        const val EXTRA_CALLKIT_BACKGROUND_COLOR = "EXTRA_CALLKIT_BACKGROUND_COLOR"
        const val EXTRA_CALLKIT_BACKGROUND = "EXTRA_CALLKIT_BACKGROUND"
        const val EXTRA_CALLKIT_ACTION_COLOR = "EXTRA_CALLKIT_ACTION_COLOR"

        fun getIntentIncoming(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_INCOMING
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentAccept(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_ACCEPT
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentDecline(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_DECLINE
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }

        fun getIntentTimeout(context: Context, data: Bundle?) =
            Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                action = ACTION_CALL_TIMEOUT
                putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val callkitNotificationManager = CallkitNotificationManager(context)
        val callkitSoundPlayer = CallkitSoundPlayer(context)

        val action = intent.action ?: return
        val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA) ?: return
        Log.e("onReceive", action)
        when (action) {
            ACTION_CALL_INCOMING -> {
                try {
                    sendEventFlutter(ACTION_CALL_INCOMING, data)
                    val duration = data.getLong(EXTRA_CALLKIT_DURATION, 0L)
                    callkitSoundPlayer.setDuration(duration)
                    callkitSoundPlayer.play(data)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ACCEPT -> {
                try {
                    sendEventFlutter(ACTION_CALL_ACCEPT, data)
                    Utils.backToForeground(context)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_DECLINE -> {
                try {
                    sendEventFlutter(ACTION_CALL_DECLINE, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_ENDED -> {
                try {
                    sendEventFlutter(ACTION_CALL_ENDED, data)
                    callkitSoundPlayer.stop()
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            ACTION_CALL_TIMEOUT -> {
                try {
                    sendEventFlutter(ACTION_CALL_TIMEOUT, data)
                    callkitSoundPlayer.stop()
                    callkitNotificationManager.clearIncomingNotification(data)
                    Handler(Looper.getMainLooper()).postDelayed({
                        callkitNotificationManager.showMissCallNotification(data)
                    }, 700L)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }
    }

    private fun sendEventFlutter(event: String, data: Bundle) {
        val forwardData = mapOf(
            "id" to data.getString(EXTRA_CALLKIT_ID, ""),
            "nameCaller" to data.getString(EXTRA_CALLKIT_NAME_CALLER, ""),
            "avatar" to data.getString(EXTRA_CALLKIT_AVATAR, ""),
            "number" to data.getString(EXTRA_CALLKIT_NUMBER, ""),
            "type" to data.getInt(EXTRA_CALLKIT_TYPE, 0),
            "duration" to data.getLong(EXTRA_CALLKIT_DURATION, 0L)
        )
        FlutterCallkitIncomingPlugin.eventHandler.send(event, forwardData)
    }
}