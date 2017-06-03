package com.example.ogl4j.snakegame.scoreboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ogl4j.utility.database.MyDBHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ogl4jo3 on 2017/6/3.
 */

public class ScoreBoardDAO {

	private final SQLiteDatabase db;
	private final String TABLE_NAME = MyDBHelper.SCORE_TABLE;
	private final String MODE_COLUMN = MyDBHelper.MODE_COLUMN;
	private final String LEVEL_COLUMN = MyDBHelper.LEVEL_COLUMN;
	private final String NAME_COLUMN = MyDBHelper.NAME_COLUMN;
	private final String SCORE_COLUMN = MyDBHelper.SCORE_COLUMN;
	private final String DATETIME_COLUMN = MyDBHelper.DATETIME_COLUMN;

	/**
	 * 建構元
	 *
	 * @param db 資料庫
	 */
	public ScoreBoardDAO(SQLiteDatabase db) {
		this.db = db;
	}

	/**
	 * 新增
	 */
	public boolean insertData(ScoreBoard scoreBoard) {
		// 建立準備新增資料的ContentValues物件
		ContentValues cv = new ContentValues();

		// 加入ContentValues物件包裝的新增資料
		// 第一個參數是欄位名稱， 第二個參數是欄位的資料
		cv.put(MODE_COLUMN, scoreBoard.getMode());
		cv.put(LEVEL_COLUMN, scoreBoard.getLevel());
		cv.put(NAME_COLUMN, scoreBoard.getName());
		cv.put(SCORE_COLUMN, scoreBoard.getScore());
		cv.put(DATETIME_COLUMN, new Date().getTime());

		// 第一個參數是表格名稱
		// 第二個參數是沒有指定欄位值的預設值
		// 第三個參數是包裝新增資料的ContentValues物件
		boolean isSuccess = db.insert(TABLE_NAME, null, cv) > 0;
		db.close();
		return isSuccess;
	}

	/**
	 * 取得全部資料
	 *
	 * @return listScoreBoard
	 */
	public List<ScoreBoard> getAll() {
		String orderBy = SCORE_COLUMN + " DESC";
		List<ScoreBoard> scoreBoardList = new ArrayList<>();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, orderBy, null);

		while (cursor.moveToNext()) {
			scoreBoardList.add(getscoreBoard(cursor));
		}

		cursor.close();
		db.close();

		return scoreBoardList;
	}

	/**
	 * 依照模式和等級取得部分資料
	 *
	 * @return listScoreBoard
	 */
	public List<ScoreBoard> getAllByModeNLevel(String mode, String level) {
		String selection;
		String[] selectionArgs;

		if (mode.equals("ALL") && level.equals("ALL")) {
			selection = null;
			selectionArgs = null;
		} else if (mode.equals("ALL") && !level.equals("ALL")) {
			selection = LEVEL_COLUMN + "=?";
			selectionArgs = new String[]{level};
		} else if (!mode.equals("ALL") && level.equals("ALL")) {
			selection = MODE_COLUMN + "=?";
			selectionArgs = new String[]{mode};
		} else {
			selection = MODE_COLUMN + "=? and " + LEVEL_COLUMN + "=?";
			selectionArgs = new String[]{mode, level};
		}
		String orderBy = SCORE_COLUMN + " DESC";
		List<ScoreBoard> scoreBoardList = new ArrayList<>();
		Cursor cursor =
				db.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy, null);

		while (cursor.moveToNext())

		{
			scoreBoardList.add(getscoreBoard(cursor));
		}

		cursor.close();
		db.close();

		return scoreBoardList;
	}

	/**
	 * 取得分數最高得一筆
	 *
	 * @return bestScoreBoard
	 */
	public ScoreBoard getBest() {
		String orderBy = SCORE_COLUMN + " DESC";
		String limit = "1";
		ScoreBoard bestScoreBoard = new ScoreBoard();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, orderBy, limit);

		if (cursor.moveToFirst()) {
			bestScoreBoard = getscoreBoard(cursor);
		}

		cursor.close();
		db.close();

		return bestScoreBoard;
	}

	/**
	 * 取得此模式此等級的最高分數
	 *
	 * @return bestScore
	 */
	public int getBestScore(String mode, String level) {
		String selection = MODE_COLUMN + "=? and " + LEVEL_COLUMN + "=?";
		String[] selectionArgs = new String[]{mode, level};

		int bestScore = 0;
		String[] projection = {SCORE_COLUMN};
		String orderBy = SCORE_COLUMN + " DESC";
		String limit = "1";
		Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy, limit);

		if (cursor.moveToFirst()) {
			bestScore = cursor.getInt(0);
		}

		cursor.close();
		db.close();

		return bestScore;
	}

	/**
	 * 取得最高分數
	 *
	 * @return bestScore
	 */
	public int getBestScore() {
		int bestScore = 0;
		String[] projection = {SCORE_COLUMN};
		String orderBy = SCORE_COLUMN + " DESC";
		String limit = "1";
		Cursor cursor = db.query(TABLE_NAME, projection, null, null, null, null, orderBy, limit);

		if (cursor.moveToFirst()) {
			bestScore = cursor.getInt(0);
		}

		cursor.close();
		db.close();

		return bestScore;
	}

	// 把Cursor目前的資料包裝為物件
	public ScoreBoard getscoreBoard(Cursor cursor) {
		// 準備回傳結果用的物件
		ScoreBoard result = new ScoreBoard();

		result.setMode(cursor.getString(0));
		result.setLevel(cursor.getString(1));
		result.setName(cursor.getString(2));
		result.setScore(cursor.getInt(3));
		result.setUpdateTime(new Date(cursor.getLong(4)));

		// 回傳結果
		return result;
	}
}
