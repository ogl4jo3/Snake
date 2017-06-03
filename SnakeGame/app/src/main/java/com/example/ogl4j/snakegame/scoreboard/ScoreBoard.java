package com.example.ogl4j.snakegame.scoreboard;

import java.util.Date;

/**
 * 記分板
 * Created by ogl4jo3 on 2017/6/3.
 */

public class ScoreBoard {

	private String mode;
	private String level;
	private String name;
	private int score;
	private Date updateTime;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
