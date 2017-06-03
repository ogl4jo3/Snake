package com.example.ogl4j.utility.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ogl4jo3 on 2017/6/3.
 */

public class MyDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mydata"; // 資料庫名稱
	private static final int DATABASE_VERSION = 2; // 版本,此一數字一改(不管變大變小),資料即刪並重建

	public static final String SCORE_TABLE = "scoreboard"; //     記分板TABLE
	public static final String MODE_COLUMN = "mode";//            記分板TABLE  MODE_COLUMN
	public static final String LEVEL_COLUMN = "level";//          記分板TABLE  LEVEL_COLUMN
	public static final String NAME_COLUMN = "name";//            記分板TABLE  NAME_COLUMN
	public static final String SCORE_COLUMN = "score";//          記分板TABLE  SCORE_COLUMN
	public static final String DATETIME_COLUMN = "updateTime";//  記分板TABLE  DATETIME_COLUMN

	private static MyDBHelper instance;

	public static MyDBHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new MyDBHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}
		return instance;
	}

	public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
	                  int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + SCORE_TABLE + " (" + MODE_COLUMN + " VARCHAR ," +
				LEVEL_COLUMN + " VARCHAR ," + NAME_COLUMN + " VARCHAR ," +
				SCORE_COLUMN + " INTEGER , " + DATETIME_COLUMN + " DATETIME NOT NULL);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE);
	}
}
