package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    companion object {
        const val DONE = 0L
        const val COUNTDOWN_TIME = 60000L
        const val ONE_SECOND = 1000L
    }

    private val timer: CountDownTimer

    // The current time
    private val _currentTime = MutableLiveData<Long>()

    // The current time mapped to string format
    val currentTimeString: LiveData<String> = Transformations.map(_currentTime) { time ->
        DateUtils.formatElapsedTime(time / 1000)
    }

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // Buzz type
    private val _buzzType = MutableLiveData<BuzzType>()
    val buzzType: LiveData<BuzzType>
        get() = _buzzType

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    // The state of the game
    private val _gameFinished = MutableLiveData<Boolean>()
    val gameFinished: LiveData<Boolean>
        get() = _gameFinished

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    init {
        Log.i("GameViewModel", "GameViewModel created here!")
        resetList()
        nextWord()
        _score.value = 0
        _currentTime.value = COUNTDOWN_TIME
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onFinish() {
                _buzzType.value = BuzzType.GAME_OVER
                _gameFinished.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished <= 10000L) {
                    _buzzType.value = BuzzType.COUNTDOWN_PANIC
                }
                _currentTime.value = millisUntilFinished
            }
        }
        timer.start()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = score.value?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _buzzType.value = BuzzType.CORRECT
        _score.value = score.value?.plus(1)
        nextWord()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Log.i("GameViewModel", "GameViewModel destroyed here!")
    }

    fun onGameFinishComplete() {
        _gameFinished.value = false
    }

    fun onBuzzComplete() {
        _buzzType.value = BuzzType.NO_BUZZ
    }
}