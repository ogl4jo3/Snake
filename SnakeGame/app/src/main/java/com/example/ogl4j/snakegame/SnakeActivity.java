package com.example.ogl4j.snakegame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ogl4j on 2015/11/26.
 */
public class SnakeActivity extends AppCompatActivity {

    public String level = "1";

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
    }

}


