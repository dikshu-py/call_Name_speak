package com.example.call_voice

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.util.*


class Background : Service() {
    var tts: TextToSpeech? = null
    private var telephonyManager: TelephonyManager? = null
    var tospeak: Boolean = false
    lateinit var listener: PhoneStateListener
    var last: String = "Unknown Number"
    var data1 : String? = ""

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(
            Context.TELEPHONY_SERVICE
        ) as TelephonyManager
        val notification = createNotification()
        startForeground(1, notification)




    }


    override fun onBind(intent: Intent): IBinder? {
        data1 = intent.getStringExtra("stop")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        data1 = intent?.getStringExtra("stop")



        data1?.let { showToast(it) }
        listener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {

                if (state == TelephonyManager.CALL_STATE_RINGING) {

                    announceCaller(incomingNumber)
                    tts!!.speak(incomingNumber, TextToSpeech.QUEUE_FLUSH, null, "")
                    last = incomingNumber

                }
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.d("TTSs", "call state activated")
                    Toast.makeText(
                        applicationContext, "Phone in a call ",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (state == TelephonyManager.CALL_STATE_IDLE) {


                        Log.d("TTSs", "call idle activated")
                    //phone is neither ringing nor in a call
                }
            }
        }


        telephonyManager!!.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)

        if (data1 == "stop"){
            stopForeground(true)
            stopSelf()
            onDestroy()
        }

        return START_STICKY
    }
    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Call Speak Service")
            .setContentText("Call Name Speak is Working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_MIN) // for under android 26 compatibility
            .build()
    }
    fun getContactName(context: Context, phoneNumber: String?): String? {
        val cr = context.contentResolver
        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor =
            cr.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
                ?: return null
        var contactName: String? = null
        if (cursor.moveToFirst()) {
            contactName =
                cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        return contactName
    }
    fun speakString(str: String?) {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS Error", "This language is not supported")
                } else {
                    tts!!.speak(
                        str, TextToSpeech.QUEUE_FLUSH, null,
                        ""
                    )
                }
            } else Log.e("TTS Error", "Initialization Failed!")
        }
    }
    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    fun announceCaller(num: String) {
        if (num.isEmpty()) {
            showToast("Unknown Number")
            return
        }
        var cName = getContactName(this, num)
        if (cName == null) cName = num.replace("", " ").trim { it <= ' ' }
        val phoneNumber = "Call from $cName"
        speakString(phoneNumber)
    }
    fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("TTS", "This Language is not supported")
            } else {
                Log.e("TTSd", "This Language supported")
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun onDestroy() {

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
            tospeak = false
        }
        
        if (data1 == "stop"){

            stopForeground(true)
            stopSelf()
        }else {
            val it = Intent(baseContext, Background::class.java)
            startForegroundService(it)

        }



        super.onDestroy()

    }


}
