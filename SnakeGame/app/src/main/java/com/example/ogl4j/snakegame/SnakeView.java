package com.example.ogl4j.snakegame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ogl4j on 2015/11/25.
 */


public class SnakeView extends SurfaceView implements
        SurfaceHolder.Callback {

    private Context mContext;
    private static final String TAG = "CannonView";

    private CannonThread cannonThread;
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
    private int snakelevel;
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

    private SharedPreferences bestscore;
    private int bestint;

    private boolean snakeover;
    private boolean gameOver;
    private double timeLeft;
    //private int shotsFired;
    private double totalElapsedTime;
    private int screenWidth;
    private int screenHeight;
    ArrayList<Point> arrayList = new ArrayList<>();

    protected MyGestureListener myGestureListener;

    double time = 0;
    int speed = 250;

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

        Resources res = getResources();

        bestint = Memory.getInt(mContext, "BEST", 0);
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

        if (mContext instanceof SnakeActivity) {
            switch (((SnakeActivity) mContext).level) {
                case "2":
                    speed = 200;
                    snakelevel = 200;
                    break;
                case "3":
                    speed = 150;
                    snakelevel = 150;
                    break;
                case "4":
                    speed = 100;
                    snakelevel = 100;
                    break;
                case "5":
                    speed = 50;
                    snakelevel = 50;
                    break;

                default:
                    speed = 250;
                    snakelevel = 250;
            }
        }
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

        if (width_offset != 0 || height_offset != 0)
            newGame();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.setMargins((int) width_offset, (int) height_offset, (int) width_offset, (int) height_offset);
        setLayoutParams(lp);
    }

    public void newGame() {

        time = 0;
        score = 0;
        timeLeft = 10;
        speed = snakelevel;
        //shotsFired = 0;
        totalElapsedTime = 0.0;
        nowdirection = snakeUp;
        snakeover = false;
        targets = new Point(new Random().nextInt(width_count) * SIDE_LENGTH,
                new Random().nextInt(height_count) * SIDE_LENGTH);
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
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }
    }

    private void updatePositions(double elapsedTimeMS) {

        double interval = elapsedTimeMS / 1000.0;

        timeLeft -= interval;

        if (screenWidth - arrayList.get(0).x < SIDE_LENGTH || screenHeight - arrayList.get(0).y < SIDE_LENGTH ||
                arrayList.get(0).x < 0 || arrayList.get(0).y < 0) {
            snakeover = true;
        }

        if ((Math.abs(timeLeft - time) * 1000 > speed || time == 0) && !snakeover) {

            time = timeLeft;
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

            if (tmpList.contains(tmpPoint))
                snakeover = true;

        }

        if (snakeover) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
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
            if(speed>=50)
                speed = speed - 20;
            if (score > bestint) {
                bestint = score;
            }
        }
    }


    public void drawGameElements(Canvas canvas) {
        if (canvas == null)
            return;

        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);


        for (Point point : arrayList) {
            canvas.drawRect(point.x, point.y, point.x + SIDE_LENGTH, point.y + SIDE_LENGTH, snakePaint);
            canvas.drawRect(point.x + 5, point.y + 5, point.x + SIDE_LENGTH - 5, point.y + SIDE_LENGTH - 5, xPaint);
        }
        canvas.drawBitmap(newbm, targets.x, targets.y, applePaint);
        canvas.drawText(getResources().getString(R.string.score, score), screenWidth - 250, 60, textPaint);
        canvas.drawText(getResources().getString(R.string.best, bestint), 40, 60, textPaint);
    }


    private void showGameOverDialog(final int messageId) {

        final DialogFragment gameResult =
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle bundle) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(messageId));
                        builder.setMessage(getResources().getString(
                                R.string.results_format, score, bestint));

                        Memory.setInt(mContext, "BEST", bestint);

                        builder.setNegativeButton(R.string.back,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mContext instanceof SnakeActivity)
                                            ((SnakeActivity) mContext).onBackPressed();
                                    }
                                }
                        );

                        builder.setPositiveButton(R.string.reset_game,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialogIsDisplayed = false;
                                        newGame();
                                    }
                                }
                        );
                        return builder.create();
                    }
                };


        activity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        dialogIsDisplayed = true;
                        gameResult.setCancelable(false);
                        gameResult.show(activity.getFragmentManager(), "results");
                    }
                }
        );
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true);
            cannonThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        cannonThread.setRunning(false);
        while (retry) {
            try {
                cannonThread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        return myGestureListener.getDetector().onTouchEvent(e);
    }

    private class CannonThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private boolean threadIsRunning = true;

        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    canvas = surfaceHolder.lockCanvas();

                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS);
                        if (!snakeover) {
                            drawGameElements(canvas);
                        }
                        previousFrameTime = currentTime;
                    }
                } finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public static class MyGestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {
        Context context;
        GestureDetector gDetector;

        public MyGestureListener() {
            super();
        }

        public MyGestureListener(Context context) {
            this(context, null);
        }

        public MyGestureListener(Context context, GestureDetector gDetector) {

            if (gDetector == null)
                gDetector = new GestureDetector(context, this);

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
                if (nowdirection != snakeRight)
                    nowdirection = snakeLeft;
            } else if (direction == Direction.right) {
                if (nowdirection != snakeLeft)
                    nowdirection = snakeRight;
            } else if (direction == Direction.up) {
                if (nowdirection != snakeDown)
                    nowdirection = snakeUp;
            } else if (direction == Direction.down) {
                if (nowdirection != snakeUp)
                    nowdirection = snakeDown;
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
            up,
            down,
            left,
            right;

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
