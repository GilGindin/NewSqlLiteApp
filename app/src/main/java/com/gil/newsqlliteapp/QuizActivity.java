package com.gil.newsqlliteapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
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
    public static final String EXTRA_SCORE_TO_RESULT = "score_extra";
    public static final long COUNT_DOWN_MILLIS = 30000;

    public static final String KEY_SCORE = "keyScore";
    public static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    public static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    public static final String KEY_ANSWERD = "keyAnswerd";
    public static final String KEY_QUESTION_LIST = "keyQuestionnList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCategory;
    private TextView textViewDifficult;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;

    private CountDownTimer mCountDownTimer;
    private long timeLeftMillis;

    private ArrayList<Question> mQuestionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;

    private int score;
    private boolean isAnswerd;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewCountDown = findViewById(R.id.txet_view_countdown);
        textViewQuestion = findViewById(R.id.text_view_question);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCategory = findViewById(R.id.text_view_category);
        textViewDifficult = findViewById(R.id.text_view_difficult);
        textViewScore = findViewById(R.id.text_view_score);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.radio_button_1);
        rb2 = findViewById(R.id.radio_button_2);
        rb3 = findViewById(R.id.radio_button_3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRb = rb1.getTextColors();
        textColorDefaultCd = textViewCountDown.getHintTextColors();

        Intent intent = getIntent();
        int categoryID = intent.getIntExtra(MainActivity.CATEGORY_ID , 0);
        String categoryName = intent.getStringExtra(MainActivity.CATEGORY_NAME );
        String difficult = intent.getStringExtra(MainActivity.SPINNER_DIFFICULT);

        textViewCategory.setText("Category: " + categoryName);
        textViewDifficult.setText("Difficult: " + difficult);

        if (savedInstanceState == null) {
            QuizDbHelper quizDbHelper = QuizDbHelper.getInstance(this);
            mQuestionList = quizDbHelper.getQuestionsByDifficult(difficult , categoryID);
            questionCountTotal = mQuestionList.size();
            Collections.shuffle(mQuestionList);
            showNextQuestion();
        }else {
            mQuestionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal = mQuestionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = mQuestionList.get(questionCounter -1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            isAnswerd = savedInstanceState.getBoolean(KEY_ANSWERD);

            if (!isAnswerd){
                startCountDown();
            }else {
                updateCountDown();
                showSolution();
            }
        }

        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswerd) {
                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked()) {
                        checkAnswer();
                    } else {
                        Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNextQuestion();
                }
            }
        });
    }


    private void showNextQuestion() {
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        rbGroup.clearCheck();

        if (questionCounter < questionCountTotal) {
            currentQuestion = mQuestionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            isAnswerd = false;
            buttonConfirmNext.setText("Confirm");

            timeLeftMillis = COUNT_DOWN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown() {
        mCountDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateCountDown();

            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateCountDown();
                checkAnswer();
            }
        }.start();
    }

    private void updateCountDown() {
        int minutes = (int) ((timeLeftMillis / 1000) / 60);
        int seconds = (int) ((timeLeftMillis / 1000) % 60);

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeFormatted);

        if (timeLeftMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    private void checkAnswer() {
        isAnswerd = true;

        mCountDownTimer.cancel();
        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr = rbGroup.indexOfChild(rbSelected) + 1;

        if (answerNr == currentQuestion.getAnswerNr()) {
            score++;

            textViewScore.setText("Score: " + score);
        }
        showSolution();
    }

    private void showSolution() {
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);

        switch (currentQuestion.getAnswerNr()) {
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 1 is correct");
                break;
            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 2 is correct");
                break;
            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 3 is correct");
                break;
        }

        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.setText("Next");
        } else {
            buttonConfirmNext.setText("Finish");
        }
    }

    private void finishQuiz() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE_TO_RESULT, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null){
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    outState.putInt(KEY_SCORE , score);
    outState.putInt(KEY_QUESTION_COUNT , questionCounter);
    outState.putLong(KEY_MILLIS_LEFT , timeLeftMillis);
    outState.putBoolean(KEY_ANSWERD ,isAnswerd);
    outState.putParcelableArrayList(KEY_QUESTION_LIST ,  mQuestionList);
    }
}
