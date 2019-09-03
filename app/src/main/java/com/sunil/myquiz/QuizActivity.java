package com.sunil.myquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String FINAL_SCORE = "finalScore";
    private static final long COUNTDOWN_IN_MILLIS = 30000;
    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private TextView textViewCategory;
    private TextView textViewDifficulty;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private ArrayList<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;
    private int score;
    private boolean answered;

    QuizDBHelper quizDBHelper;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        textViewCategory = findViewById(R.id.text_view_category);
        textViewDifficulty = findViewById(R.id.text_view_difficulty);
        radioGroup = findViewById(R.id.radio_group);
        radioButton1 = findViewById(R.id.radio_button1);
        radioButton2 = findViewById(R.id.radio_button2);
        radioButton3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRb = radioButton1.getTextColors();
        textColorDefaultCd = textViewCountDown.getTextColors();
        Intent intent = getIntent();
        int categoryID = intent.getIntExtra(StartingScreenActivity.EXTRA_CATEGORY_ID, 0);
        String categoryName = intent.getStringExtra(StartingScreenActivity.EXTRA_CATEGORY_NAME);
        String difficulty = intent.getStringExtra(StartingScreenActivity.EXTRA_DIFFICULTY);

        textViewCategory.setText("Category: " + categoryName);
        textViewDifficulty.setText("Difficulty: " + difficulty);

        if (savedInstanceState == null)
        {
            QuizDBHelper quizDBHelper = QuizDBHelper.getInstance(this);
            questionList = quizDBHelper.getQuestions(categoryID , difficulty);

            questionCountTotal = questionList.size();

            //Here we have to shuffle all my questions mean we can get random question
            Collections.shuffle(questionList);

            showNextQuestion();
        }
        else
        {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            if (questionList == null)
            {
                finish();
            }
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);

            if (!answered)
            {
                startCountDown();
            }
            else
            {
                updateCountDownText();
                showSolution();
            }
        }



        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!answered)
                {
                    if (radioButton1.isChecked() || radioButton2.isChecked() || radioButton3.isChecked())
                    {
                        checkedAnswer();
                    }
                    else
                    {
                        Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    showNextQuestion();
                }
            }
        });
    }

    private void checkedAnswer()
    {
        answered = true;
        countDownTimer.cancel();

        RadioButton radioButtonSelected = findViewById(radioGroup.getCheckedRadioButtonId());

        int answerNumber = radioGroup.indexOfChild(radioButtonSelected) + 1;

        if (answerNumber == currentQuestion.getAnswerNumber())
        {
            score++;
            textViewScore.setText("Score: " + score);
        }

        showSolution();
    }

    private void showSolution()
    {
        radioButton1.setTextColor(Color.RED);
        radioButton2.setTextColor(Color.RED);
        radioButton3.setTextColor(Color.RED);

        switch (currentQuestion.getAnswerNumber())
        {
            case 1:
                radioButton1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 1 is correct");
                break;
            case 2:
                radioButton2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 2 is correct");
                break;
            case 3:
                radioButton3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 3 is correct");
                break;

        }

        if (questionCounter < questionCountTotal)
        {
            buttonConfirmNext.setText("Next");
        }
        else
        {
            buttonConfirmNext.setText("FINISH");
        }
    }

    @SuppressLint("SetTextI18n")
    private void showNextQuestion() {
        radioButton1.setTextColor(textColorDefaultRb);
        radioButton2.setTextColor(textColorDefaultRb);
        radioButton3.setTextColor(textColorDefaultRb);


        radioGroup.clearCheck();

        if (questionCounter < questionCountTotal)
        {
            currentQuestion = questionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            radioButton1.setText(currentQuestion.getOption1());
            radioButton2.setText(currentQuestion.getOption2());
            radioButton3.setText(currentQuestion.getOption3());

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            answered = false;
            buttonConfirmNext.setText("Confirm");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        }
        else
        {
            finishQuiz();
        }
    }

    private void startCountDown()
    {
        countDownTimer = new CountDownTimer(timeLeftInMillis , 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish()
            {
                timeLeftInMillis = 0;
                updateCountDownText();
                checkedAnswer();
            }
        }.start();
    }

    private void updateCountDownText()
    {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int)(timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault() , "%02d:%02d" , minutes , seconds);

        textViewCountDown.setText(timeFormatted);

        if (timeLeftInMillis < 10000)
        {
            textViewCountDown.setTextColor(Color.RED);
        }
        else
        {
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    private void finishQuiz()
    {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FINAL_SCORE , score);
        setResult(RESULT_OK , resultIntent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        if (backPressedTime + 2000 > System.currentTimeMillis())
        {
            finishQuiz();
        }
        else
        {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null)
        {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(KEY_SCORE , score);
        outState.putInt(KEY_QUESTION_COUNT , questionCounter);
        outState.putLong(KEY_MILLIS_LEFT , timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED , answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST , questionList);
    }
}
