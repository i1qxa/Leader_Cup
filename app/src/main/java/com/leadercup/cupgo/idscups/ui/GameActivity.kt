package com.leadercup.cupgo.idscups.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.leadercup.cupgo.idscups.R
import com.leadercup.cupgo.idscups.data.Coordinates
import com.leadercup.cupgo.idscups.databinding.ActivityGameBinding
import com.leadercup.cupgo.idscups.databinding.ActivityStartGameBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGameBinding.inflate(layoutInflater) }
    private val prefs by lazy { getSharedPreferences("leader_prefs", Context.MODE_PRIVATE) }
    private val ballStartCoordinate by lazy { Coordinates(
        binding.ivBall.x,
        binding.ivBall.y
    ) }
    private lateinit var cupRotated:View
    private var balance = 100
    private val viewModel by lazy { ViewModelProvider(this)[GameViewModel::class.java] }
    private val randomCupNumber by lazy { Random.nextInt(3) }
    private val cupWithBall by lazy {
        when (randomCupNumber) {
            1 -> cup1
            2 -> cup2
            else -> cup3
        }
    }
    private val cup1 by lazy { binding.cup1 }
    private val cup2 by lazy { binding.cup2 }
    private val cup3 by lazy { binding.cup3 }

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
    }

    private fun setupBalance(){
        balance = prefs.getInt("balance", 100)
        binding.tvBalanceValue.text = balance.toString()
    }

    private fun setupBtnClickListeners(){
        binding.btnReplay.setOnClickListener {
            cupRotated.animate().apply {
                duration = 2
                rotation(180F)
                start()
            }
            binding.ivBall.animate().apply {
                duration = 2
                x(ballStartCoordinate.x)
                y(ballStartCoordinate.y)
                setVisible(true)
                start()
            }
            playGame()
        }
        binding.btnBackToStart.setOnClickListener {
            finish()
        }
        binding.ivBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun observeGameState(){
        viewModel.gameStateLD.observe(this){ gameState ->
            removeCupClickListeners()
            when(gameState){
                GameState.IN_GAME ->{
                    setInGameVisibility()
                }
                GameState.WIN ->{
                    setFinishGameVisibility()
                    binding.tvGameResult.text = getString(R.string.win)
                    binding.tvCoinResult.text = "+50"
                    balance+=40
                    prefs.edit().putInt("balance", balance).apply()
                    setupBalance()
                }
                GameState.LOSE ->{
                    setFinishGameVisibility()
                    binding.tvGameResult.text = getString(R.string.lose)
                    binding.tvCoinResult.text = "-10"
                    balance-=10
                    if (balance<=0) balance = 100
                    prefs.edit().putInt("balance", balance).apply()
                    setupBalance()
                }
            }
        }
    }

    private fun setInGameVisibility(){
        binding.tvGameResult.visibility = View.GONE
        binding.tvCoinResult.visibility = View.GONE
        binding.ivBallGoldResult.visibility = View.GONE
        binding.btnBackToStart.visibility = View.GONE
        binding.btnReplay.visibility = View.GONE
        binding.btnPlay.visibility = View.GONE
    }

    private fun setFinishGameVisibility(){
        binding.tvGameResult.visibility = View.VISIBLE
        binding.tvCoinResult.visibility = View.VISIBLE
        binding.ivBallGoldResult.visibility = View.VISIBLE
        binding.btnBackToStart.visibility = View.VISIBLE
        binding.btnReplay.visibility = View.VISIBLE
    }

    private fun removeCupClickListeners(){
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
        viewModel.setInGame()
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

    private fun getCupCoordinates(): Coordinates {
        val xAdded = binding.ivBall.width / 2
        val x = cupWithBall.x + xAdded
        val y = cupWithBall.y + (cupWithBall.height / 2)
        return Coordinates(x, y)
    }

    private fun launchRotateAnimation() {
        var amountRotation = (Random.nextInt(5) + 3) * 3
        lifecycleScope.launch {
            while (amountRotation > 0) {
                rotateCup()
                delay(520)
                amountRotation--
            }
            setupCupsClickListener()
        }
    }

    private fun setupCupsClickListener(){
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

    private fun checkRes(cup:View){
        cupRotated = cup
        cup.animate().apply {
            duration = 500
            rotation(180F)
            withEndAction {
                if (cup == cupWithBall){
                    binding.ivBall.visibility = View.VISIBLE
                    binding.ivBall.x = cup.x + cup.width/2 - binding.ivBall.width/2
                    binding.ivBall.y = cup.y - binding.ivBall.height/2
                    viewModel.setWin()
                }else{
                    viewModel.setLose()
                }
            }
        }


    }

    private fun rotateCup() {
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