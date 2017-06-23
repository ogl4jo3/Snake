package com.example.ogl4j.snakegame.scoreboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.example.ogl4j.snakegame.MainActivity;
import com.example.ogl4j.snakegame.R;
import com.example.ogl4j.utility.database.MyDBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreBoardActivity extends AppCompatActivity {

	private Context mContext;
	private Spinner spLevel;
	private String[] levelArray = {"ALL", "1", "2", "3", "4", "5"};
	private Spinner spMode;
	private String[] modeArray = {"ALL", MainActivity.NORMAL_MODE, MainActivity.WITHOUT_WALL_MODE};
	private ListView scoreBoardListView;
	private CheckBox checkBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_board);
		mContext = this;
		scoreBoardListView = (ListView) findViewById(R.id.lv_scoreBoard);
		SQLiteDatabase db = MyDBHelper.getInstance(this).getReadableDatabase();
		List<ScoreBoard> scoreBoardList = new ScoreBoardDAO(db).getAll();
		final List<Map<String, Object>> items = new ArrayList<>();
		for (ScoreBoard scoreBoard : scoreBoardList) {
			Map<String, Object> item = new HashMap<>();
			item.put("mode", scoreBoard.getMode());
			item.put("level", scoreBoard.getLevel());
			item.put("name", scoreBoard.getName());
			item.put("score", scoreBoard.getScore());
			item.put("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
					.format(scoreBoard.getUpdateTime()));
			items.add(item);
		}

		final SimpleAdapter simpleAdapter =
				new SimpleAdapter(this, items, R.layout.item_score_board,
						new String[]{"mode", "level", "name", "score", "updateTime"},
						new int[]{R.id.tv_mode, R.id.tv_level, R.id.tv_name, R.id.tv_score,
								R.id.tv_update_time});
		scoreBoardListView.setAdapter(simpleAdapter);

		spMode = (Spinner) findViewById(R.id.sp_mode);
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, R.layout.myspinner, modeArray);
		spMode.setAdapter(modeAdapter);

		spMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//Toast.makeText(mContext, "level " + levelArray[position], Toast.LENGTH_SHORT).show();
				SQLiteDatabase db = MyDBHelper.getInstance(mContext).getReadableDatabase();
				List<ScoreBoard> scoreBoardList = new ScoreBoardDAO(db)
						.getAllByModeNLevel(modeArray[position],
								levelArray[spLevel.getSelectedItemPosition()]);
				//List<Map<String, Object>> items = new ArrayList<>();
				items.clear();
				for (ScoreBoard scoreBoard : scoreBoardList) {
					Map<String, Object> item = new HashMap<>();
					item.put("mode", scoreBoard.getMode());
					item.put("level", scoreBoard.getLevel());
					item.put("name", scoreBoard.getName());
					item.put("score", scoreBoard.getScore());
					item.put("updateTime",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
									.format(scoreBoard.getUpdateTime()));
					items.add(item);
				}
				simpleAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		spLevel = (Spinner) findViewById(R.id.sp_level);
		ArrayAdapter<String> levelAdapter =
				new ArrayAdapter<>(this, R.layout.myspinner, levelArray);
		spLevel.setAdapter(levelAdapter);

		spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//Toast.makeText(mContext, "level " + levelArray[position], Toast.LENGTH_SHORT).show();
				SQLiteDatabase db = MyDBHelper.getInstance(mContext).getReadableDatabase();
				List<ScoreBoard> scoreBoardList = new ScoreBoardDAO(db)
						.getAllByModeNLevel(modeArray[spMode.getSelectedItemPosition()],
								levelArray[position]);
				//List<Map<String, Object>> items = new ArrayList<>();
				items.clear();
				for (ScoreBoard scoreBoard : scoreBoardList) {
					Map<String, Object> item = new HashMap<>();
					item.put("mode", scoreBoard.getMode());
					item.put("level", scoreBoard.getLevel());
					item.put("name", scoreBoard.getName());
					item.put("score", scoreBoard.getScore());
					item.put("updateTime",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
									.format(scoreBoard.getUpdateTime()));
					items.add(item);
				}
				simpleAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

	}

}
