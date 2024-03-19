package com.leadercup.cupgo.idscups.ui

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.leadercup.cupgo.idscups.R
import com.leadercup.cupgo.idscups.data.Coordinates
import com.leadercup.cupgo.idscups.databinding.ActivityGameBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGameBinding.inflate(layoutInflater) }
    private val prefs by lazy { getSharedPreferences("leader_prefs", Context.MODE_PRIVATE) }
    private val isSoundOn by lazy { prefs.getBoolean("isSoundOn", true) }
    private var listOfStartCoordinates = mutableListOf<Coordinates>()
    private lateinit var cupRotated: View
    private var balance = 100
    private val viewModel by lazy { ViewModelProvider(this)[GameViewModel::class.java] }
    private lateinit var cupWithBall: ImageView
    private val cup1 by lazy { binding.cup1 }
    private val cup2 by lazy { binding.cup2 }
    private val cup3 by lazy { binding.cup3 }
    private val ball by lazy { binding.ivBall }

    private var soundPool: SoundPool? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBtnPlayClickListener()
        observeGameState()
        setupBtnClickListeners()
        setupBalance()
        soundPool = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
    }

    private fun setupCupWithBall() {
        val randomCupNumber = Random.nextInt(3)
        cupWithBall =
            when (randomCupNumber) {
                1 -> cup1
                2 -> cup2
                else -> cup3
            }


    }


    private fun setupStartCoordinates() {
        listOfStartCoordinates.add(
            Coordinates(ball.x, ball.y)
        )
        listOfStartCoordinates.add(
            Coordinates(cup1.x, cup1.y)
        )
        listOfStartCoordinates.add(
            Coordinates(cup2.x, cup2.y)
        )
        listOfStartCoordinates.add(
            Coordinates(cup3.x, cup3.y)
        )
    }

    private fun setupBalance() {
        balance = prefs.getInt("balance", 100)
        binding.tvBalanceValue.text = balance.toString()
    }

    private fun setupBtnClickListeners() {
        binding.btnReplay.setOnClickListener {
            viewModel.setReady()
        }
        binding.btnBackToStart.setOnClickListener {
            finish()
        }
        binding.ivBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun animationToStart() {
        lifecycleScope.launch {
            cupRotated.animate().apply {
                duration = 100
                rotation(0F)
                withEndAction {
                    moveToStartPositions()
                }
            }

        }
    }

    private fun moveToStartPositions() {
        ball.visibility = View.VISIBLE
        ball.animate().apply {
            duration = 100
            x(listOfStartCoordinates[0].x)
            y(listOfStartCoordinates[0].y)
        }
        cup1.animate().apply {
            duration = 100
            x(listOfStartCoordinates[1].x)
            y(listOfStartCoordinates[1].y)
        }
        cup2.animate().apply {
            duration = 100
            x(listOfStartCoordinates[2].x)
            y(listOfStartCoordinates[2].y)
        }
        cup3.animate().apply {
            duration = 100
            x(listOfStartCoordinates[3].x)
            y(listOfStartCoordinates[3].y)
        }
    }

    private fun observeGameState() {
        viewModel.gameStateLD.observe(this) { gameState ->
            removeCupClickListeners()
            when (gameState) {
                GameState.IN_GAME -> {
                    setInGameVisibility()
                }

                GameState.WIN -> {
                    playMoveSound(soundPool!!.load(baseContext, R.raw.win_sound, 1))
                    setFinishGameVisibility()
                    binding.tvGameResult.text = getString(R.string.win)
                    binding.tvCoinResult.text = "+50"
                    balance += 40
                    prefs.edit().putInt("balance", balance).apply()
                    setupBalance()
                }

                GameState.LOSE -> {
                    playMoveSound(soundPool!!.load(baseContext, R.raw.lose_sound, 1))
                    setFinishGameVisibility()
                    binding.tvGameResult.text = getString(R.string.lose)
                    binding.tvCoinResult.text = "-10"
                    balance -= 10
                    if (balance <= 0) balance = 100
                    prefs.edit().putInt("balance", balance).apply()
                    setupBalance()
                }

                GameState.READY -> {
                    binding.tvGameResult.visibility = View.GONE
                    binding.tvCoinResult.visibility = View.GONE
                    binding.ivBallGoldResult.visibility = View.GONE
                    binding.btnBackToStart.visibility = View.GONE
                    binding.btnReplay.visibility = View.GONE
                    binding.btnPlay.visibility = View.VISIBLE
                    animationToStart()
                }
            }
        }
    }

    private fun setInGameVisibility() {
        binding.tvGameResult.visibility = View.GONE
        binding.tvCoinResult.visibility = View.GONE
        binding.ivBallGoldResult.visibility = View.GONE
        binding.btnBackToStart.visibility = View.GONE
        binding.btnReplay.visibility = View.GONE
        binding.btnPlay.visibility = View.GONE
    }

    private fun setFinishGameVisibility() {
        binding.tvGameResult.visibility = View.VISIBLE
        binding.tvCoinResult.visibility = View.VISIBLE
        binding.ivBallGoldResult.visibility = View.VISIBLE
        binding.btnBackToStart.visibility = View.VISIBLE
        binding.btnReplay.visibility = View.VISIBLE
    }

    private fun removeCupClickListeners() {
        cup1.setOnClickListener(null)
        cup2.setOnClickListener(null)
        cup3.setOnClickListener(null)
    }

    private fun setupBtnPlayClickListener() {
        binding.btnPlay.setOnClickListener {
            playGame()
        }
    }

    private fun playGame() {
        setupCupWithBall()
        viewModel.setInGame()
        if (listOfStartCoordinates.isEmpty()) {
            setupStartCoordinates()
        }
        binding.ivBall.animate().apply {
            duration = 500
            val coordinates = getCupCoordinates()
            x(coordinates.x)
            y(coordinates.y)
            withEndAction {
                binding.ivBall.visibility = View.GONE
                launchRotateAnimation()
            }
        }
    }

    private fun playMoveSound(sound: Int) {
        if (isSoundOn) {
            soundPool!!.setOnLoadCompleteListener { soundPool, sampleId, status ->
                if (status == 0) {
                    soundPool?.play(sound, 1F, 1F, 0, 0, 1F)
                }
            }
        }
    }

    private fun getCupCoordinates(): Coordinates {
        val xAdded = binding.ivBall.width / 2
        val x = cupWithBall.x + xAdded
        val y = cupWithBall.y + (cupWithBall.height / 2)
        return Coordinates(x, y)
    }

    private fun launchRotateAnimation() {
        var amountRotation = (Random.nextInt(3) + 3)
        lifecycleScope.launch {
            while (amountRotation > 0) {
                rotateCup()
                delay(520)
                amountRotation--
            }
            setupCupsClickListener()
        }
    }

    private fun setupCupsClickListener() {
        cup1.setOnClickListener {
            checkRes(it)
        }
        cup2.setOnClickListener {
            checkRes(it)
        }
        cup3.setOnClickListener {
            checkRes(it)
        }
    }

    private fun checkRes(cup: View) {
        cupRotated = cup
        cup.animate().apply {
            duration = 500
            rotation(180F)
            withEndAction {
                if (cup == cupWithBall) {
                    binding.ivBall.visibility = View.VISIBLE
                    binding.ivBall.x = cup.x + cup.width / 2 - binding.ivBall.width / 2
                    binding.ivBall.y = cup.y - binding.ivBall.height / 2
                    viewModel.setWin()
                } else {
                    viewModel.setLose()
                }
            }
        }


    }

    private fun rotateCup() {
        playMoveSound(soundPool!!.load(baseContext, R.raw.move_sound, 1))
        if (Random.nextBoolean()) {
            cup1.animate().apply {
                duration = 500
                x(cup2.x)
                y(cup2.y)
            }
            cup2.animate().apply {
                duration = 500
                x(cup3.x)
                y(cup3.y)
            }
            cup3.animate().apply {
                duration = 500
                x(cup1.x)
                y(cup1.y)
            }
        } else {
            cup1.animate().apply {
                duration = 500
                x(cup3.x)
                y(cup3.y)
            }
            cup2.animate().apply {
                duration = 500
                x(cup1.x)
                y(cup1.y)
            }
            cup3.animate().apply {
                duration = 500
                x(cup2.x)
                y(cup2.y)
            }
        }

    }

}