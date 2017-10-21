package com.example.ogl4j.snakegame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.ogl4j.snakegame.scoreboard.ScoreBoardActivity;
import com.example.ogl4j.snakegame.game.SnakeActivity;
import com.example.ogl4j.utility.sharedpreferences.SharedPreferencesTag;

public class MainActivity extends AppCompatActivity {

	public static final String NORMAL_MODE = "0";
	public static final String WITHOUT_WALL_MODE = "1";

	//private Context mContext;

	private Button startButton;
	private Button scoreBoardButton;
	private Spinner spLevel;
	private String[] levelArray = {"1", "2", "3", "4", "5"};
	private Spinner spMode;
	private String[] modeArray = {NORMAL_MODE, WITHOUT_WALL_MODE};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//mContext = this.getApplicationContext();

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SnakeActivity.class);
				startActivity(i);
			}
		});

		scoreBoardButton = (Button) findViewById(R.id.scoreBoardButton);
		scoreBoardButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ScoreBoardActivity.class);
				startActivity(i);
			}
		});

		spMode = (Spinner) findViewById(R.id.sp_mode);
		ArrayAdapter<CharSequence> modeAdapter =
				ArrayAdapter.createFromResource(this, R.array.game_mode, R.layout.myspinner);
		spMode.setAdapter(modeAdapter);
		spMode.setSelection(Integer.parseInt(
				getSharedPreferences(SharedPreferencesTag.preferenceData, 0)
						.getString(SharedPreferencesTag.preferenceMode, "0")));

		spMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//Toast.makeText(mContext, "level " + levelArray[position], Toast.LENGTH_SHORT).show();
				getSharedPreferences(SharedPreferencesTag.preferenceData, 0).edit()
						.putString(SharedPreferencesTag.preferenceMode, String.valueOf(position))
						.apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		spLevel = (Spinner) findViewById(R.id.sp_level);
		ArrayAdapter<String> levelAdapter =
				new ArrayAdapter<>(this, R.layout.myspinner, levelArray);
		//levelAdapter.setDropDownViewResource(R.layout.myspinner);
		spLevel.setAdapter(levelAdapter);
		spLevel.setSelection(Integer.parseInt(
				getSharedPreferences(SharedPreferencesTag.preferenceData, 0)
						.getString(SharedPreferencesTag.preferenceLevel, "1")) - 1);

		spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//Toast.makeText(mContext, "level " + levelArray[position], Toast.LENGTH_SHORT).show();
				getSharedPreferences(SharedPreferencesTag.preferenceData, 0).edit()
						.putString(SharedPreferencesTag.preferenceLevel, levelArray[position])
						.apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

	}
}
