package com.sunil.myquiz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class StartingScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QUIZ= 1;
    public static final String EXTRA_CATEGORY_ID = "extraCategoryID";
    public static final String EXTRA_CATEGORY_NAME = "extraCategoryName";
    public static final String EXTRA_DIFFICULTY = "extraDifficulty";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_HIGHSCORE = "keyHighScore";

    private TextView textViewHighScore;
    private Spinner spinnerCategory;
    private Spinner spinnerDifficulty;
    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen);

        textViewHighScore = findViewById(R.id.text_view_highscore);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);

//        String[] difficultyLevels = Question.getAllDifficultyLevels();
//
//        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<>(
//                this , android.R.layout.simple_spinner_item , difficultyLevels
//        );
//        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerDifficulty.setAdapter(adapterDifficulty);


        loadCategories();
        loadDifficultyLevels();
        loadHighScore();
        Button buttonStartQuiz = findViewById(R.id.button_start_quiz);

        buttonStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQuiz();
            }
        });
    }

    private void startQuiz()
    {
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryID = selectedCategory.getId();
        String categoryName = selectedCategory.getName();
        String difficulty = spinnerDifficulty.getSelectedItem().toString();

        Intent quizIntent = new Intent(StartingScreenActivity.this , QuizActivity.class);
        quizIntent.putExtra(EXTRA_CATEGORY_ID, categoryID);
        quizIntent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        quizIntent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivityForResult(quizIntent , REQUEST_CODE_QUIZ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QUIZ)
        {
            if (resultCode == RESULT_OK)
            {
                assert data != null;
                int score = data.getIntExtra(QuizActivity.FINAL_SCORE , 0);
                if (score > highScore)
                {
                    updateHighScore(score);
                }
            }
        }
    }

    private void loadCategories() {
        QuizDBHelper dbHelper = QuizDBHelper.getInstance(this);
        List<Category> categories = dbHelper.getAllCategories();

        ArrayAdapter<Category> adapterCategories = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategories);
    }

    private void loadDifficultyLevels() {
        String[] difficultyLevels = Question.getAllDifficultyLevels();

        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, difficultyLevels);
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapterDifficulty);
    }

    private void loadHighScore()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS , MODE_PRIVATE);
        highScore = sharedPreferences.getInt(KEY_HIGHSCORE , 0);
        textViewHighScore.setText("HighScore: " + highScore);
    }

    private void updateHighScore(int score) {
        highScore = score;
        textViewHighScore.setText("HighScore: " + highScore);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS , MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_HIGHSCORE, highScore);
        editor.apply();
    }

}
