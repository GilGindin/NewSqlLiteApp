package com.gil.newsqlliteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUESR_CODE_QUIZ = 1;
    public static final String CATEGORY_ID = "categoryID";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String SPINNER_DIFFICULT = "spinnerDifficult";
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String KEY_HIGHSCORE = "key_highscore";

    private TextView textViewHighScore;
    private int highScore;
    private Spinner difficultSpinner;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewHighScore = findViewById(R.id.text_view_highscore);
        difficultSpinner = findViewById(R.id.spinner_difficult);
        categorySpinner = findViewById(R.id.spinner_category);

        loadCategories();
        loadDifficultLevels();
        loadHighScore();

        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });

    }


    private void startQuiz() {
        Category selectedCategory = (Category) categorySpinner.getSelectedItem();
        int categoryID = selectedCategory.getId();
        String categoryName = selectedCategory.getName();

        String difficult = difficultSpinner.getSelectedItem().toString();

        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra(CATEGORY_ID , categoryID);
        intent.putExtra(CATEGORY_NAME , categoryName);
        intent.putExtra(SPINNER_DIFFICULT , difficult);
        startActivityForResult(intent, REQUESR_CODE_QUIZ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUESR_CODE_QUIZ) {
            if (resultCode == RESULT_OK) {
                int score = data.getIntExtra(QuizActivity.EXTRA_SCORE_TO_RESULT, 0);
                if (score > highScore) {
                    updateHighScore(score);
                }
            }
        }
    }

    private void loadCategories() {
        QuizDbHelper quizDbHelper = QuizDbHelper.getInstance(this);
        List<Category> categories = quizDbHelper.getAllCategories();

        ArrayAdapter<Category> adpterCategory = new ArrayAdapter<>(this , android.R.layout.simple_spinner_item , categories);
        adpterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adpterCategory);

    }

    private void loadDifficultLevels() {
        String[] difficultLevels = Question.getAllDifficultLvl();
        ArrayAdapter<String> spinnerAdpter = new ArrayAdapter<>(this ,android.R.layout.simple_spinner_item , difficultLevels);
        spinnerAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultSpinner.setAdapter(spinnerAdpter);
    }

    private void loadHighScore() {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highScore = preferences.getInt(KEY_HIGHSCORE, 0);
        textViewHighScore.setText("Highscore: " + highScore);
    }

    private void updateHighScore(int highScoreNew) {
        highScore = highScoreNew;
        textViewHighScore.setText("Highscore: " + highScore);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_HIGHSCORE, highScore);
        editor.apply();
    }
}
