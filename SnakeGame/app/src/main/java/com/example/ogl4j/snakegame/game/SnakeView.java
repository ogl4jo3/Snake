package com.example.ogl4j.snakegame.game;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.ogl4j.snakegame.MainActivity;
import com.example.ogl4j.snakegame.R;
import com.example.ogl4j.snakegame.scoreboard.ScoreBoard;
import com.example.ogl4j.snakegame.scoreboard.ScoreBoardDAO;
import com.example.ogl4j.utility.database.MyDBHelper;
import com.example.ogl4j.utility.sharedpreferences.SharedPreferencesTag;

import java.util.ArrayList;
import java.util.Random;

/**
 * 遊戲畫面
 * Created by ogl4j on 2015/11/25.
 */

public class SnakeView extends SurfaceView implements SurfaceHolder.Callback {

	private Context mContext;
	private static final String TAG = "SnakeGameView";

	private String mode;
	private String level;
	private long SPEED = 3;

	private SnakeGameThread snakeGameThread;
	private Activity activity;
	private boolean dialogIsDisplayed = false;
	private Paint snakePaint;
	private Paint xPaint;
	private int SIDE_LENGTH = 40;
	private int width_count;
	private int height_count;
	private float width_offset;
	private float height_offset;
	private int startx;
	private int starty;
	private int score = 0;
	private static int nowdirection;
	private static int snakeUp = 0;
	private static int snakeDown = 1;
	private static int snakeLeft = 2;
	private static int snakeRight = 3;

	private Point targets;
	private Paint targetsPaint;
	private Paint backgroundPaint;
	private Paint textPaint;

	private Bitmap apple;
	private Paint applePaint;
	private Bitmap newbm;

	private int bestint;

	private boolean snakeover;
	private boolean gameOver;
	private int screenWidth;
	private int screenHeight;
	ArrayList<Point> arrayList = new ArrayList<>();

	protected MyGestureListener myGestureListener;

	public SnakeView(Context context) {
		super(context);
		init(context);
	}

	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SnakeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(21)
	public SnakeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		activity = (Activity) context;
		myGestureListener = new MyGestureListener(context);
		mContext = context;
		getHolder().addCallback(this);

		mode = activity.getSharedPreferences(SharedPreferencesTag.preferenceData, 0)
				.getString(SharedPreferencesTag.preferenceMode, MainActivity.NORMAL_MODE);

		level = getContext().getSharedPreferences(SharedPreferencesTag.preferenceData, 0)
				.getString(SharedPreferencesTag.preferenceLevel, "1");
		SPEED = Integer.parseInt(level) * SPEED;

		Resources res = getResources();

		SQLiteDatabase db = MyDBHelper.getInstance(mContext).getReadableDatabase();
		bestint = new ScoreBoardDAO(db).getBestScore(mode, level);
		Log.d(TAG, "getBestScore: " + bestint);

		apple = BitmapFactory.decodeResource(res, R.drawable.apple);
		int width = apple.getWidth();
		int height = apple.getHeight();
		int newWidth = SIDE_LENGTH;
		int newHeight = SIDE_LENGTH;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		newbm = Bitmap.createBitmap(apple, 0, 0, width, height, matrix, true);
		applePaint = new Paint();

		snakePaint = new Paint();
		xPaint = new Paint();
		targets = new Point();
		targetsPaint = new Paint();
		backgroundPaint = new Paint();
		textPaint = new Paint();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenWidth = w;
		screenHeight = h;
		width_count = w / SIDE_LENGTH;
		height_count = h / SIDE_LENGTH;
		backgroundPaint.setColor(Color.WHITE);
		snakePaint.setColor(Color.BLACK);
		xPaint.setColor(Color.YELLOW);
		targetsPaint.setColor(Color.RED);
		textPaint.setTextSize(50);

		width_offset = (screenWidth - width_count * SIDE_LENGTH) / 2;
		height_offset = (screenHeight - height_count * SIDE_LENGTH) / 2;

		if ((width_offset != 0 || height_offset != 0) && !dialogIsDisplayed) {
			newGame();
		}

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
		lp.setMargins((int) width_offset, (int) height_offset, (int) width_offset,
				(int) height_offset);
		setLayoutParams(lp);
	}

	public void newGame() {
		score = 0;
		nowdirection = snakeUp;
		snakeover = false;
		targets = new Point((new Random().nextInt(width_count - 4) + 2) * SIDE_LENGTH,
				(new Random().nextInt(height_count - 4) + 2) * SIDE_LENGTH);
		startx = (width_count) / 2 * SIDE_LENGTH;
		starty = (height_count) / 2 * SIDE_LENGTH;
		arrayList.clear();
		arrayList.add(new Point(startx, starty));
		arrayList.add(new Point(startx, starty + SIDE_LENGTH));
		arrayList.add(new Point(startx, starty + 2 * SIDE_LENGTH));
		arrayList.add(new Point(startx, starty + 3 * SIDE_LENGTH));
		arrayList.add(new Point(startx, starty + 4 * SIDE_LENGTH));

		if (gameOver) {
			gameOver = false;
			snakeGameThread = new SnakeGameThread(getHolder());
			snakeGameThread.start();
		}
	}

	/**
	 * 一般模式
	 */
	private void normalMode() {

		if (screenWidth - arrayList.get(0).x < SIDE_LENGTH ||
				screenHeight - arrayList.get(0).y < SIDE_LENGTH || arrayList.get(0).x < 0 ||
				arrayList.get(0).y < 0) {
			gameOver = true;
			snakeGameThread.setRunning(false);
			showGameOverDialog(R.string.lose);
			return;
		}

		if (!snakeover) {

			for (int i = arrayList.size() - 1; i > 0; i--) {
				arrayList.set(i, arrayList.get(i - 1));
			}
			if (nowdirection == 0) {
				arrayList.set(0, new Point(arrayList.get(0).x, arrayList.get(0).y - SIDE_LENGTH));
			} else if (nowdirection == 1) {
				arrayList.set(0, new Point(arrayList.get(0).x, arrayList.get(0).y + SIDE_LENGTH));
			} else if (nowdirection == 2) {
				arrayList.set(0, new Point(arrayList.get(0).x - SIDE_LENGTH, arrayList.get(0).y));
			} else if (nowdirection == 3) {
				arrayList.set(0, new Point(arrayList.get(0).x + SIDE_LENGTH, arrayList.get(0).y));
			}

			ArrayList<Point> tmpList = new ArrayList<>();
			tmpList.addAll(arrayList);
			Point tmpPoint = tmpList.remove(0);

			if (tmpList.contains(tmpPoint)) {
				snakeover = true;
			}

		}

		if (snakeover) {
			gameOver = true;
			snakeGameThread.setRunning(false);
			showGameOverDialog(R.string.lose);
			return;
		}

		if (arrayList.contains(targets)) {
			arrayList.add(arrayList.get(arrayList.size() - 1));
			for (int i = arrayList.size() - 2; i > 0; i--) {
				arrayList.set(i, arrayList.get(i - 1));
			}
			if (nowdirection == 0) {
				arrayList.set(0, new Point(arrayList.get(1).x, arrayList.get(1).y - SIDE_LENGTH));
			} else if (nowdirection == 1) {
				arrayList.set(0, new Point(arrayList.get(1).x, arrayList.get(1).y + SIDE_LENGTH));
			} else if (nowdirection == 2) {
				arrayList.set(0, new Point(arrayList.get(1).x - SIDE_LENGTH, arrayList.get(1).y));
			} else if (nowdirection == 3) {
				arrayList.set(0, new Point(arrayList.get(1).x + SIDE_LENGTH, arrayList.get(1).y));
			}
			targets = new Point((new Random().nextInt(width_count - 4) + 2) * SIDE_LENGTH,
					(new Random().nextInt(height_count - 4) + 2) * SIDE_LENGTH);
			score++;
			if (SPEED >= 3) {
				SPEED = SPEED + 1;
			}
			if (score > bestint) {
				bestint = score;
			}
		}
	}

	/**
	 * 穿過牆壁的模式
	 */
	private void withoutWallMode() {

		for (int i = arrayList.size() - 1; i > 0; i--) {
			arrayList.set(i, arrayList.get(i - 1));
		}

		if (arrayList.get(0).x < 0) {
			arrayList.set(0, new Point((width_count - 1) * SIDE_LENGTH, arrayList.get(0).y));
		} else if (arrayList.get(0).y < 0) {
			arrayList.set(0, new Point(arrayList.get(0).x, (height_count - 1) * SIDE_LENGTH));
		} else if (screenWidth - arrayList.get(0).x < SIDE_LENGTH) {
			arrayList.set(0, new Point(0, arrayList.get(0).y));
		} else if (screenHeight - arrayList.get(0).y < SIDE_LENGTH) {
			arrayList.set(0, new Point(arrayList.get(0).x, 0));
		} else {
			if (nowdirection == 0) {
				arrayList.set(0, new Point(arrayList.get(0).x, arrayList.get(0).y - SIDE_LENGTH));
			} else if (nowdirection == 1) {
				arrayList.set(0, new Point(arrayList.get(0).x, arrayList.get(0).y + SIDE_LENGTH));
			} else if (nowdirection == 2) {
				arrayList.set(0, new Point(arrayList.get(0).x - SIDE_LENGTH, arrayList.get(0).y));
			} else if (nowdirection == 3) {
				arrayList.set(0, new Point(arrayList.get(0).x + SIDE_LENGTH, arrayList.get(0).y));
			}

			ArrayList<Point> tmpList = new ArrayList<>();
			tmpList.addAll(arrayList);
			Point tmpPoint = tmpList.remove(0);

			if (tmpList.contains(tmpPoint)) {
				snakeover = true;
			}

		}

		if (snakeover) {
			gameOver = true;
			snakeGameThread.setRunning(false);
			showGameOverDialog(R.string.lose);
		}

		if (arrayList.contains(targets)) {
			arrayList.add(arrayList.get(arrayList.size() - 1));
			for (int i = arrayList.size() - 2; i > 0; i--) {
				arrayList.set(i, arrayList.get(i - 1));
			}
			if (nowdirection == 0) {
				arrayList.set(0, new Point(arrayList.get(1).x, arrayList.get(1).y - SIDE_LENGTH));
			} else if (nowdirection == 1) {
				arrayList.set(0, new Point(arrayList.get(1).x, arrayList.get(1).y + SIDE_LENGTH));
			} else if (nowdirection == 2) {
				arrayList.set(0, new Point(arrayList.get(1).x - SIDE_LENGTH, arrayList.get(1).y));
			} else if (nowdirection == 3) {
				arrayList.set(0, new Point(arrayList.get(1).x + SIDE_LENGTH, arrayList.get(1).y));
			}
			targets = new Point(new Random().nextInt(width_count) * SIDE_LENGTH,
					new Random().nextInt(height_count) * SIDE_LENGTH);
			score++;
			if (SPEED >= 3) {
				SPEED = SPEED + 1;
			}
			if (score > bestint) {
				bestint = score;
			}
		}
	}

	public void drawGameElements(Canvas canvas) {
		if (canvas == null) {
			return;
		}

		canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);

		for (Point point : arrayList) {
			canvas.drawRect(point.x, point.y, point.x + SIDE_LENGTH, point.y + SIDE_LENGTH,
					snakePaint);
			canvas.drawRect(point.x + 5, point.y + 5, point.x + SIDE_LENGTH - 5,
					point.y + SIDE_LENGTH - 5, xPaint);
		}
		canvas.drawBitmap(newbm, targets.x, targets.y, applePaint);
		canvas.drawText(getResources().getString(R.string.score, score), screenWidth - 250, 60,
				textPaint);
		canvas.drawText(getResources().getString(R.string.best, bestint), 40, 60, textPaint);
	}

	/**
	 * 顯示遊戲結束對話框
	 *
	 * @param messageId 對話框Title
	 */
	private void showGameOverDialog(final int messageId) {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				dialogIsDisplayed = true;
				GameResultDialogFragment newFragment = GameResultDialogFragment
						.newInstance(getResources().getString(messageId),
								getResources().getString(R.string.results_format, bestint, score));
				newFragment.setOnClickListener(new GameResultDialogFragment.DialogClickListener() {

					@Override
					public void doPositiveClick(String name) {
						recordScore(name, score);
						dialogIsDisplayed = false;
						newGame();
						Log.d(TAG, "doPositiveClick: " + name);

					}

					@Override
					public void doNegativeClick(String name) {
						if (mContext instanceof SnakeActivity) {
							recordScore(name, score);
							((SnakeActivity) mContext).onBackPressed();
							Log.d(TAG, "doNegativeClick: " + name);
						}
					}
				});
				newFragment.show(activity.getFragmentManager(), "dialog");
			}
		});
	}

	/**
	 * 紀錄分數
	 * mode  模式
	 * level 等級
	 *
	 * @param name  名稱
	 * @param score 分數
	 */
	public void recordScore(String name, int score) {
		ScoreBoard scoreBoard = new ScoreBoard();
		scoreBoard.setMode(mode);
		scoreBoard.setLevel(level);
		scoreBoard.setName(name);
		scoreBoard.setScore(score);
		SQLiteDatabase db = MyDBHelper.getInstance(mContext).getWritableDatabase();
		boolean isSuccess = new ScoreBoardDAO(db).insertData(scoreBoard);
		Log.d(TAG, "insert is successful: " + isSuccess);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!dialogIsDisplayed) {
			snakeGameThread = new SnakeGameThread(holder);
			snakeGameThread.setRunning(true);
			snakeGameThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		snakeGameThread.setRunning(false);
		while (retry) {
			try {
				snakeGameThread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.e(TAG, "Thread interrupted", e);
			}
		}
	}

	private class SnakeGameThread extends Thread {

		private final SurfaceHolder surfaceHolder;
		private boolean threadIsRunning = true;

		public SnakeGameThread(SurfaceHolder holder) {
			surfaceHolder = holder;
			setName("SnakeGameThread");
		}

		public void setRunning(boolean running) {
			threadIsRunning = running;
		}

		@Override
		public void run() {
			Canvas canvas = null;

			long ticksPS = 1000 / SPEED;
			long startTime;
			long sleepTime;

			while (threadIsRunning) {
				try {
					canvas = surfaceHolder.lockCanvas();
					startTime = System.currentTimeMillis();

					synchronized (surfaceHolder) {
						switch (mode) {
							case MainActivity.NORMAL_MODE:
								normalMode();
								break;
							case MainActivity.WITHOUT_WALL_MODE:
								withoutWallMode();
								break;
							default:
								normalMode();
						}
						if (!snakeover) {
							drawGameElements(canvas);
						}
					}
				} finally {
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
				sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
				try {
					if (sleepTime > 0) {
						sleep(sleepTime);
					} else {
						sleep(10);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent e) {
		return myGestureListener.getDetector().onTouchEvent(e);
	}

	public static class MyGestureListener extends GestureDetector.SimpleOnGestureListener
			implements OnTouchListener {

		Context context;
		GestureDetector gDetector;

		public MyGestureListener() {
			super();
		}

		public MyGestureListener(Context context) {
			this(context, null);
		}

		public MyGestureListener(Context context, GestureDetector gDetector) {

			if (gDetector == null) {
				gDetector = new GestureDetector(context, this);
			}

			this.context = context;
			this.gDetector = gDetector;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			float x1 = e1.getX();
			float y1 = e1.getY();
			float x2 = e2.getX();
			float y2 = e2.getY();

			Direction direction = getDirection(x1, y1, x2, y2);
			if (direction == Direction.left) {
				if (nowdirection != snakeRight) {
					nowdirection = snakeLeft;
				}
			} else if (direction == Direction.right) {
				if (nowdirection != snakeLeft) {
					nowdirection = snakeRight;
				}
			} else if (direction == Direction.up) {
				if (nowdirection != snakeDown) {
					nowdirection = snakeUp;
				}
			} else if (direction == Direction.down) {
				if (nowdirection != snakeUp) {
					nowdirection = snakeDown;
				}
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		/**
		 * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
		 * returns the direction that an arrow pointing from p1 to p2 would have.
		 *
		 * @param x1 the x position of the first point
		 * @param y1 the y position of the first point
		 * @param x2 the x position of the second point
		 * @param y2 the y position of the second point
		 * @return the direction
		 */
		public Direction getDirection(float x1, float y1, float x2, float y2) {
			double angle = getAngle(x1, y1, x2, y2);
			return Direction.get(angle);
		}

		/**
		 * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
		 * The angle is measured with 0/360 being the X-axis to the right, angles
		 * increase counter clockwise.
		 *
		 * @param x1 the x position of the first point
		 * @param y1 the y position of the first point
		 * @param x2 the x position of the second point
		 * @param y2 the y position of the second point
		 * @return the angle between two points
		 */
		public double getAngle(float x1, float y1, float x2, float y2) {

			double rad = Math.atan2(y1 - y2, x2 - x1) + Math.PI;
			return (rad * 180 / Math.PI + 180) % 360;
		}

		public enum Direction {
			up, down, left, right;

			/**
			 * Returns a direction given an angle.
			 * Directions are defined as follows:
			 * <p/>
			 * Up: [45, 135]
			 * Right: [0,45] and [315, 360]
			 * Down: [225, 315]
			 * Left: [135, 225]
			 *
			 * @param angle an angle from 0 to 360 - e
			 * @return the direction of an angle
			 */
			public static Direction get(double angle) {
				if (inRange(angle, 45, 135)) {
					return Direction.up;
				} else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
					return Direction.right;
				} else if (inRange(angle, 225, 315)) {
					return Direction.down;
				} else {
					return Direction.left;
				}

			}

			/**
			 * @param angle an angle
			 * @param init  the initial bound
			 * @param end   the final bound
			 * @return returns true if the given angle is in the interval [init, end).
			 */
			private static boolean inRange(double angle, float init, float end) {
				return (angle >= init) && (angle < end);
			}
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			return super.onSingleTapConfirmed(e);
		}

		public boolean onTouch(View v, MotionEvent event) {
			return gDetector.onTouchEvent(event);
		}

		public GestureDetector getDetector() {
			return gDetector;
		}
	}
}
