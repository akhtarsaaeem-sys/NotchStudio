package com.example.customnotchdisplay

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "OverlayServiceChannel"
        const val NOTIFICATION_ID = 9912
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val ACTION_UPDATE_LAYOUT = "update_layout"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("NotchPrefs", Context.MODE_PRIVATE)
        createOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle("Notch Studio Active")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Apply Colors from the Intent
        val bgColor = intent?.getIntExtra("bgColor", Color.parseColor("#000000")) ?: Color.parseColor("#000000")
        val textColor = intent?.getIntExtra("textColor", Color.parseColor("#FFFFFF")) ?: Color.parseColor("#FFFFFF")

        overlayView?.findViewById<CardView>(R.id.overlayCardView)?.setCardBackgroundColor(bgColor)
        overlayView?.findViewById<TextView>(R.id.overlayTextView)?.setTextColor(textColor)

        // Handle LIVE Resize and Movement
        if (intent?.action == ACTION_UPDATE_LAYOUT) {
            val width = intent.getIntExtra("width", layoutParams.width)
            val height = intent.getIntExtra("height", layoutParams.height)
            val posX = intent.getIntExtra("posX", layoutParams.x)
            val posY = intent.getIntExtra("posY", layoutParams.y)

            layoutParams.width = width
            layoutParams.height = height
            layoutParams.x = posX
            layoutParams.y = posY

            windowManager?.updateViewLayout(overlayView, layoutParams)

            prefs.edit()
                .putInt("width", width)
                .putInt("height", height)
                .putInt("posX", posX)
                .putInt("posY", posY)
                .apply()

            return START_STICKY
        }

        // Apply Text or Image Data
        val textData = intent?.getStringExtra(EXTRA_TEXT)
        val imageUriString = intent?.getStringExtra(EXTRA_IMAGE_URI)
        val textView = overlayView?.findViewById<TextView>(R.id.overlayTextView)
        val imageView = overlayView?.findViewById<ImageView>(R.id.overlayImageView)

        if (imageUriString != null) {
            try {
                val inputStream = contentResolver.openInputStream(Uri.parse(imageUriString))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                textView?.visibility = View.GONE
                imageView?.visibility = View.VISIBLE
                imageView?.setImageBitmap(bitmap)
            } catch (e: Exception) { e.printStackTrace() }
        } else if (textData != null) {
            imageView?.visibility = View.GONE
            textView?.visibility = View.VISIBLE
            textView?.text = textData
        }

        return START_STICKY
    }

    private fun createOverlayView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_display, null)

        val displayMetrics = resources.displayMetrics
        val defaultX = (displayMetrics.widthPixels / 2) - 175

        val savedX = prefs.getInt("posX", defaultX)
        val savedY = prefs.getInt("posY", 50)
        val savedWidth = prefs.getInt("width", 350)
        val savedHeight = prefs.getInt("height", 90)
        val savedBgColor = prefs.getInt("bgColor", Color.parseColor("#000000"))
        val savedTextColor = prefs.getInt("textColor", Color.parseColor("#FFFFFF"))

        // Apply saved colors on startup
        overlayView?.findViewById<CardView>(R.id.overlayCardView)?.setCardBackgroundColor(savedBgColor)
        overlayView?.findViewById<TextView>(R.id.overlayTextView)?.setTextColor(savedTextColor)

        layoutParams = WindowManager.LayoutParams(
            savedWidth, savedHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        setupDraggable()
        windowManager?.addView(overlayView, layoutParams)
    }

    private fun setupDraggable() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(overlayView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit()
                        .putInt("posX", layoutParams.x)
                        .putInt("posY", layoutParams.y)
                        .apply()

                    val intent = Intent("UPDATE_SLIDERS")
                    intent.putExtra("posX", layoutParams.x)
                    intent.putExtra("posY", layoutParams.y)
                    sendBroadcast(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            overlayView?.visibility = View.GONE
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            overlayView?.visibility = View.VISIBLE
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Overlay", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}