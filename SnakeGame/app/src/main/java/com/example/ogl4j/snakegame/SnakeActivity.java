package com.example.ogl4j.snakegame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by ogl4j on 2015/11/26.
 */
public class SnakeActivity extends AppCompatActivity {

    public String level = "1";
    private TextView scoreTextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                level = bundle.getString("level");
            }
        }
        setContentView(R.layout.activity_game);
        //scoreTextview = (TextView) findViewById(R.id.score);
        //scoreTextview.setText(getResources().getString(R.string.score, score));

    }
}


