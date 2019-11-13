package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String SCORE_ID = "score_prefs";
    private TextView questionTextview;
    private TextView questionCounterTextview;
    private Button trueButton;
    private Button falseButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private int currentQuestionIndex = 0;
    private List<Question> questionList;

    private TextView scoreTextview;
    private TextView highestScoreTextview;
    private Score score;
    private int scoreCounter = 0;
    private Prefs prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();

        prefs = new Prefs(MainActivity.this);

//        prefs.saveHighScore(scoreCounter);
//        Log.d("Second", "onClick: " + prefs.getHighScore());

        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        falseButton = findViewById(R.id.false_button);
        trueButton = findViewById(R.id.true_button);
        questionCounterTextview = findViewById(R.id.counter_text);
        questionTextview = findViewById(R.id.question_textView);
        scoreTextview = findViewById(R.id.currentScore_textView);
        highestScoreTextview = findViewById(R.id.highestScore_textView);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);

        scoreTextview.setText(MessageFormat.format("Score : {0}", String.valueOf(score.getScore())));
        highestScoreTextview.setText(MessageFormat.format("Highest Score : {0}", String.valueOf(prefs.getHighScore())));

        //get previous state
        currentQuestionIndex  = prefs.getState();
        Log.d("State", "onCreate: " + currentQuestionIndex);

            questionList = new QuestionBank().qetQuestion(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounterTextview.setText(MessageFormat.format("{0} / {1}", currentQuestionIndex + 1, questionArrayList.size())); // 0 / 234

                //Log.d("Inside", "processFinished: " + questionArrayList);
            }

        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.prev_button:
                if (currentQuestionIndex > 0){              // preventing crash while in index 0
                    currentQuestionIndex = (currentQuestionIndex - 1)% questionList.size();
                    updateQuestion();

                }
                else if (currentQuestionIndex == 0){                // go to the last question
                    currentQuestionIndex = (questionList.size() - 1);
                    updateQuestion();
                }
                break;
            case R.id.next_button:
                goNext();
                break;
            case R.id.true_button:
                checkAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button:
                checkAnswer(false);
                updateQuestion();
                break;
        }
    }

    private void checkAnswer(boolean userChooseCorrect) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;
        if (userChooseCorrect == answerIsTrue){
            addPoints();
            fadeView();
            toastMessageId = R.string.correct_answer;
        }else {
            decrementPoints();
            shakeAnimation();
            toastMessageId = R.string.wrong_answer;
        }
        Toast.makeText(MainActivity.this, toastMessageId,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void addPoints(){
        scoreCounter += 100;
        score.setScore(scoreCounter);
        scoreTextview.setText(MessageFormat.format("Score : {0}", String.valueOf(score.getScore())));

//        Log.d("scoreadd", "addPoints: " + scoreCounter);
    }

    private void decrementPoints(){
        scoreCounter -= 50;

        if (scoreCounter > 0){
            score.setScore(scoreCounter);
        }else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
        }
        scoreTextview.setText(MessageFormat.format("Score: {0}", String.valueOf(score.getScore())));

//        Log.d("scoreDecrement", "decrementPoints: " + scoreCounter);
    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextview.setText(question);
        questionCounterTextview.setText(MessageFormat.format("{0} / {1}", currentQuestionIndex + 1, questionList.size())); // 0 / 234

    }

    private void fadeView(){
        final CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();

    }

    private void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
    private void goNext(){
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    @Override
    protected void onPause() {
        Log.d("State pause", "onCreate: " + currentQuestionIndex);
        prefs.saveHighScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        super.onPause();
    }
}

