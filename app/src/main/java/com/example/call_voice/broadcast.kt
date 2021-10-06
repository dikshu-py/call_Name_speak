package com.example.call_voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class Broadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(
            Broadcast::class.java.simpleName,
            "Service Stopped, but this is a never ending service."
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, Background::class.java))
        } else {
            context.startService(Intent(context, Background::class.java))
        }
    }
}