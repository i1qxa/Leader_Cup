package com.leadercup.cupgo.idscups.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel:ViewModel() {

    val gameStateLD = MutableLiveData<GameState>()

    fun setInGame(){
        gameStateLD.value = GameState.IN_GAME
    }

    fun setWin(){
        gameStateLD.value = GameState.WIN
    }

    fun setLose(){
        gameStateLD.value = GameState.LOSE
    }

}

enum class GameState{
    IN_GAME, WIN, LOSE
}