package com.example.expiretrack.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.expiretrack.LoginActivity
import com.example.expiretrack.MainActivity
import com.example.expiretrack.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private var navigateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateRunnable = Runnable {
            if (!isFinishing && !isDestroyed) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        handler.postDelayed(navigateRunnable!!, 1800)
    }

    override fun onDestroy() {
        navigateRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}