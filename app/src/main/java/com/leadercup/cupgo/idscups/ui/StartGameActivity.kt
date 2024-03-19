package com.leadercup.cupgo.idscups.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.leadercup.cupgo.idscups.R
import com.leadercup.cupgo.idscups.databinding.ActivityStartGameBinding

class StartGameActivity : AppCompatActivity() {

    private val binding by lazy { ActivityStartGameBinding.inflate(layoutInflater) }
    private val prefs by lazy { getSharedPreferences("leader_prefs", Context.MODE_PRIVATE) }

    private var isSoundOn = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.backgroundBtnStart.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
        binding.ivInfoBtn.setOnClickListener {
            val uri =
                Uri.parse("https://doc-hosting.flycricket.io/leader-cup-privacy-policy/d382f9ba-f8ba-4325-bbc6-dc81ba83089e/privacy")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        isSoundOn = prefs.getBoolean("isSoundOn", true)
        setupSoundIcon(isSoundOn)
        setMusicOnClickListener()
        setupBalance()
    }

    override fun onResume() {
        super.onResume()
        setupBalance()
    }

    private fun setMusicOnClickListener() {
        binding.ivMusicBtn.setOnClickListener {
            val newMusicState = !isSoundOn
            prefs.edit().putBoolean("isSoundOn", newMusicState).apply()
            isSoundOn = newMusicState
            setupSoundIcon(newMusicState)
        }
    }

    private fun setupSoundIcon(isMusicOn: Boolean) {
        if (isMusicOn) {
            binding.ivMusicBtn.setImageResource(R.drawable.icon_music_off)
        } else {
            binding.ivMusicBtn.setImageResource(R.drawable.icon_music_on)
        }
    }

    private fun setupBalance() {
        val balance = prefs.getInt("balance", 100)
        binding.tvBalanceValue.text = balance.toString()
    }

}