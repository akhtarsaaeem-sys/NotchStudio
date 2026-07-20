package com.example.customnotchdisplay

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var btnApplyText: Button
    private lateinit var btnPickImage: Button
    private lateinit var btnStopService: Button
    private lateinit var btnResetPosition: Button
    private lateinit var seekWidth: SeekBar
    private lateinit var seekHeight: SeekBar
    private lateinit var seekX: SeekBar
    private lateinit var seekY: SeekBar
    private lateinit var switchFreeCrop: SwitchMaterial

    // Variables to hold the live colors
    private var currentBgColor = Color.parseColor("#000000")
    private var currentTextColor = Color.parseColor("#FFFFFF")

    private val positionUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_SLIDERS") {
                seekX.progress = intent.getIntExtra("posX", seekX.progress)
                seekY.progress = intent.getIntExtra("posY", seekY.progress)
            }
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            uriContent?.let { startOverlayService(imageUri = it) }
        } else {
            Toast.makeText(this, "Cropping canceled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        animateContentEntrance()

        inputEditText = findViewById(R.id.inputEditText)
        btnApplyText = findViewById(R.id.btnApplyText)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnStopService = findViewById(R.id.btnStopService)
        btnResetPosition = findViewById(R.id.btnResetPosition)
        seekWidth = findViewById(R.id.seekWidth)
        seekHeight = findViewById(R.id.seekHeight)
        seekX = findViewById(R.id.seekX)
        seekY = findViewById(R.id.seekY)
        switchFreeCrop = findViewById(R.id.switchFreeCrop)

        val prefs = getSharedPreferences("NotchPrefs", Context.MODE_PRIVATE)
        switchFreeCrop.isChecked = prefs.getBoolean("freeCrop", false)
        currentBgColor = prefs.getInt("bgColor", Color.parseColor("#000000"))
        currentTextColor = prefs.getInt("textColor", Color.parseColor("#FFFFFF"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        btnApplyText.setOnClickListener {
            val text = inputEditText.text.toString()
            if (text.isNotEmpty() && checkOverlayPermission()) startOverlayService(text = text)
        }

        btnStopService.setOnClickListener {
            stopService(Intent(this, NotificationService::class.java))
        }

        btnResetPosition.setOnClickListener {
            val displayMetrics = resources.displayMetrics
            seekX.progress = (displayMetrics.widthPixels / 2) - (seekWidth.progress / 2)
            seekY.progress = 150
            sendLiveUpdate()
        }

        btnPickImage.setOnClickListener {
            if (checkOverlayPermission()) {
                val isFreeCrop = switchFreeCrop.isChecked
                cropImage.launch(
                    CropImageContractOptions(uri = null, cropImageOptions = CropImageOptions(
                        fixAspectRatio = !isFreeCrop, aspectRatioX = if(isFreeCrop) 1 else 3, aspectRatioY = 1, guidelines = CropImageView.Guidelines.ON
                    ))
                )
            }
        }

        switchFreeCrop.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("freeCrop", isChecked).apply()
        }

        // --- COLOR BUTTON LISTENERS ---
        // Background Colors
        findViewById<MaterialCardView>(R.id.colorBgBlack).setOnClickListener { updateBgColor(Color.parseColor("#000000")) }
        findViewById<MaterialCardView>(R.id.colorBgWhite).setOnClickListener { updateBgColor(Color.parseColor("#FFFFFF")) }
        findViewById<MaterialCardView>(R.id.colorBgPurple).setOnClickListener { updateBgColor(Color.parseColor("#6200EA")) }
        findViewById<MaterialCardView>(R.id.colorBgBlue).setOnClickListener { updateBgColor(Color.parseColor("#0D47A1")) }
        findViewById<MaterialCardView>(R.id.colorBgRed).setOnClickListener { updateBgColor(Color.parseColor("#B71C1C")) }

        // Text Colors
        findViewById<MaterialCardView>(R.id.colorTextWhite).setOnClickListener { updateTextColor(Color.parseColor("#FFFFFF")) }
        findViewById<MaterialCardView>(R.id.colorTextBlack).setOnClickListener { updateTextColor(Color.parseColor("#000000")) }
        findViewById<MaterialCardView>(R.id.colorTextYellow).setOnClickListener { updateTextColor(Color.parseColor("#FFEA00")) }
        findViewById<MaterialCardView>(R.id.colorTextCyan).setOnClickListener { updateTextColor(Color.parseColor("#00E5FF")) }
        findViewById<MaterialCardView>(R.id.colorTextPink).setOnClickListener { updateTextColor(Color.parseColor("#FF007F")) }

        val slideListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) sendLiveUpdate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekWidth.setOnSeekBarChangeListener(slideListener)
        seekHeight.setOnSeekBarChangeListener(slideListener)
        seekX.setOnSeekBarChangeListener(slideListener)
        seekY.setOnSeekBarChangeListener(slideListener)
    }

    // Purely cosmetic: staggers a quick fade + rise-in for the header and each card
    // when the screen first appears. Does not affect any app logic or state.
    private fun animateContentEntrance() {
        val container = findViewById<android.widget.LinearLayout>(R.id.contentContainer)
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            child.alpha = 0f
            child.translationY = 40f
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(60L * i)
                .setDuration(320)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    private fun updateBgColor(color: Int) {
        currentBgColor = color
        getSharedPreferences("NotchPrefs", Context.MODE_PRIVATE).edit().putInt("bgColor", color).apply()
        sendLiveUpdate()
    }

    private fun updateTextColor(color: Int) {
        currentTextColor = color
        getSharedPreferences("NotchPrefs", Context.MODE_PRIVATE).edit().putInt("textColor", color).apply()
        sendLiveUpdate()
    }

    // Sends the exact Width, Height, X, Y, and Colors straight to the background service!
    private fun sendLiveUpdate() {
        val intent = Intent(this, NotificationService::class.java).apply {
            action = NotificationService.ACTION_UPDATE_LAYOUT
            putExtra("width", seekWidth.progress)
            putExtra("height", seekHeight.progress)
            putExtra("posX", seekX.progress)
            putExtra("posY", seekY.progress)
            putExtra("bgColor", currentBgColor)
            putExtra("textColor", currentTextColor)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("UPDATE_SLIDERS")
        ContextCompat.registerReceiver(this, positionUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val prefs = getSharedPreferences("NotchPrefs", Context.MODE_PRIVATE)
        seekWidth.progress = prefs.getInt("width", seekWidth.progress)
        seekHeight.progress = prefs.getInt("height", seekHeight.progress)
        seekX.progress = prefs.getInt("posX", seekX.progress)
        seekY.progress = prefs.getInt("posY", seekY.progress)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(positionUpdateReceiver)
    }

    private fun checkOverlayPermission(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Please allow 'Display over other apps'", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun startOverlayService(text: String? = null, imageUri: Uri? = null) {
        val intent = Intent(this, NotificationService::class.java).apply {
            text?.let { putExtra(NotificationService.EXTRA_TEXT, it) }
            imageUri?.let { putExtra(NotificationService.EXTRA_IMAGE_URI, it.toString()) }
            putExtra("bgColor", currentBgColor)
            putExtra("textColor", currentTextColor)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}