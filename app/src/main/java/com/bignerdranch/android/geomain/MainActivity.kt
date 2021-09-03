package com.bignerdranch.android.geomain

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import android.annotation.*
import android.app.ActivityOptions

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val KEY2 = "key"
private const val KEY3 = "kkey"
private const val REQUEST_CODE_CHEAT = 0


class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var numPrompt: TextView
    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }
    private lateinit var cheaters: Array<Boolean>
    var numOfPrompt = 3

    @SuppressLint("RestrictAPI", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)
        cheaters = Array(quizViewModel.size) { false }
        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        val isCheater = savedInstanceState?.getBoolean(KEY2, false) ?: false
        quizViewModel.currentIndex = currentIndex
        cheaters[quizViewModel.currentIndex] = isCheater
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatButton = findViewById(R.id.cheat_button)
        numPrompt = findViewById(R.id.num_prompt)
        val value = savedInstanceState?.getInt(KEY3, 3) ?: 3
        numOfPrompt = value
        Log.d(TAG,numOfPrompt.toString())
        numPrompt.setText("Num of the remaining prompts: $numOfPrompt")
        trueButton.setOnClickListener {
            checkAnswer(true)
            trueButton.setEnabled(false)
            falseButton.setEnabled(false)
        }
        falseButton.setOnClickListener {
            checkAnswer(false)
            trueButton.setEnabled(false)
            falseButton.setEnabled(false)
        }
        nextButton.setOnClickListener {
            quizViewModel.isCheater = false
            quizViewModel.moveToNext()
            updateQuestion()
        }
        cheatButton.setOnClickListener { view ->
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val options =
                    ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.width, view.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }
        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            cheaters[quizViewModel.currentIndex] = quizViewModel.isCheater
            --numOfPrompt
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        if(numOfPrompt == 0) cheatButton.setEnabled(false)
        if (numOfPrompt >= 0) {
            numPrompt.setText("Num of the remaining prompts: $numOfPrompt")
        }
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "OnPause() called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")
        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        outState.putBoolean(KEY2, cheaters[quizViewModel.currentIndex])
        outState.putInt(KEY3, numOfPrompt)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "OnStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OnDestroy() called")
    }

    private fun updateQuestion() {
        trueButton.setEnabled(true)
        falseButton.setEnabled(true)
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)

    }

    private fun checkAnswer(userAnswer: Boolean) {
        val messageResId = when {
            cheaters[quizViewModel.currentIndex] -> {
                R.string.judgment_toast
            }
            userAnswer == quizViewModel.currentQuestionAnswer -> {
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }

}