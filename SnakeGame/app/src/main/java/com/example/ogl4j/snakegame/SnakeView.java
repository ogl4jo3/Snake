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

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ogl4j on 2015/11/25.
 */


public class SnakeView extends SurfaceView implements
        SurfaceHolder.Callback {

    private Context mContext;
    private static final String TAG = "CannonView"; // for logging errors

    private CannonThread cannonThread; // controls the game loop
    private Activity activity; // to display Game Over dialog in GUI thread
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

    private SharedPreferences bestscore;

    private Point targets;
    private Paint targetsPaint;
    private Paint backgroundPaint;

    private Bitmap apple;
    private Paint applePaint;
    private Bitmap newbm;

    // variables for the game loop and tracking statistics

    private boolean snakeover;
    private boolean gameOver; // is the game over?
    private double timeLeft; // time remaining in seconds
    private int shotsFired; // shots the user has fired
    private double totalElapsedTime; // elapsed seconds
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
        activity = (Activity) context; // store reference to MainActivity
        myGestureListener = new MyGestureListener(context);
        mContext = context;
        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        Resources res = getResources();
        apple = BitmapFactory.decodeResource(res, R.drawable.snake);
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

        if (mContext instanceof SnakeActivity) {
            switch (((SnakeActivity) mContext).level) {
                case "2":
                    speed = 200;
                    break;
                case "3":
                    speed = 150;
                    break;
                case "4":
                    speed = 100;
                    break;
                case "5":
                    speed = 50;
                    break;

                default:
                    speed = 250;
            }
        }
    }

    // called by surfaceChanged when the size of the SurfaceView changes,
    // such as when it's first added to the View hierarchy
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w; // store CannonView's width
        screenHeight = h; // store CannonView's height
        width_count = w / SIDE_LENGTH;
        height_count = h / SIDE_LENGTH;
        backgroundPaint.setColor(Color.WHITE); // set background color
        snakePaint.setColor(Color.BLACK);
        xPaint.setColor(Color.YELLOW);
        targetsPaint.setColor(Color.RED);

        width_offset = (screenWidth - width_count * SIDE_LENGTH) / 2;
        height_offset = (screenHeight - height_count * SIDE_LENGTH) / 2;

        newGame(); // set up and start a new game
    } // end method onSizeChanged

    // reset all the screen elements and start a new game
    public void newGame() {

        time = 0;
        score = 0; // no target pieces have been hit
        timeLeft = 20; // start the countdown at 10 seconds
        shotsFired = 0; // set the initial number of shots fired
        totalElapsedTime = 0.0; // set the time elapsed to zero
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


        if (gameOver) // starting a new game after the last game ended
        {
            gameOver = false;
            cannonThread = new CannonThread(getHolder()); // create thread
            cannonThread.start(); // start the game loop thread
        }
    } // end method newGame

    // called repeatedly by the CannonThread to update game elements
    private void updatePositions(double elapsedTimeMS) {

        double interval = elapsedTimeMS / 1000.0; // convert to seconds

        timeLeft -= interval; // subtract from time left

        if (screenWidth - arrayList.get(0).x < SIDE_LENGTH || screenHeight - arrayList.get(0).y < SIDE_LENGTH ||
                arrayList.get(0).x < 0 || arrayList.get(0).y < 0) {
            snakeover = true;
        }

        if ((Math.abs(timeLeft - time) * 1000 > speed || time == 0) && !snakeover ) {

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
            gameOver = true; // the game is over
            cannonThread.setRunning(false); // terminate thread
            showGameOverDialog(R.string.lose); // show the losing dialog
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
        }
    } // end method updatePositions

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {
        if (canvas == null)
            return;
        // clear the background
        canvas.drawRect(0, 0, screenWidth, screenHeight,
                snakePaint);
        canvas.drawRect(width_offset, height_offset, screenWidth - width_offset, screenHeight - height_offset,
                backgroundPaint);

        for (int i = 0; i < arrayList.size(); i++)
            canvas.drawRect(arrayList.get(i).x + width_offset, arrayList.get(i).y + height_offset,
                    arrayList.get(i).x + SIDE_LENGTH + width_offset, arrayList.get(i).y + SIDE_LENGTH + height_offset, snakePaint);
        for (int i = 0; i < arrayList.size(); i++)
            canvas.drawRect(arrayList.get(i).x + 5 + width_offset, arrayList.get(i).y + 5 + height_offset,
                    arrayList.get(i).x + SIDE_LENGTH - 5 + width_offset, arrayList.get(i).y + SIDE_LENGTH - 5 + height_offset, xPaint);
        //canvas.drawRect(targets.x + width_offset, targets.y + height_offset,
        //        targets.x + SIDE_LENGTH + width_offset, targets.y + SIDE_LENGTH + height_offset, targetsPaint);

        canvas.drawBitmap(newbm, targets.x + width_offset, targets.y + height_offset, applePaint);

    } // end method drawGameElements

    // display an AlertDialog when the game ends
    private void showGameOverDialog(final int messageId) {

        // DialogFragment to display quiz stats and start new quiz
        final DialogFragment gameResult =
                new DialogFragment() {
                    // create an AlertDialog and return it
                    @Override
                    public Dialog onCreateDialog(Bundle bundle) {
                        // create dialog displaying String resource for messageId
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(messageId));

                        // display number of shots fired and total time elapsed
                        builder.setMessage(getResources().getString(
                                R.string.results_format, score, totalElapsedTime));

                        builder.setNegativeButton(R.string.back,
                                new DialogInterface.OnClickListener() {
                                    // called when "Reset Game" Button is pressed
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mContext instanceof SnakeActivity)
                                            ((SnakeActivity) mContext).onBackPressed();
                                    }
                                } // end anonymous inner class
                        );

                        builder.setPositiveButton(R.string.reset_game,
                                new DialogInterface.OnClickListener() {
                                    // called when "Reset Game" Button is pressed
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialogIsDisplayed = false;
                                        newGame(); // set up and start a new game
                                    }
                                } // end anonymous inner class
                        ); // end call to setPositiveButton

                        return builder.create(); // return the AlertDialog
                    } // end method onCreateDialog
                }; // end DialogFragment anonymous inner class

        // in GUI thread, use FragmentManager to display the DialogFragment
        activity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        dialogIsDisplayed = true;
                        gameResult.setCancelable(false); // modal dialog
                        gameResult.show(activity.getFragmentManager(), "results");
                    }
                } // end Runnable
        ); // end call to runOnUiThread
    } // end method showGameOverDialog


    // called when surface changes size
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
    }

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            cannonThread = new CannonThread(holder); // create thread
            cannonThread.setRunning(true); // start game running
            cannonThread.start(); // start the game loop thread
        }
    }

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ensure that thread terminates properly
        boolean retry = true;
        cannonThread.setRunning(false); // terminate cannonThread

        while (retry) {
            try {
                cannonThread.join(); // wait for cannonThread to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    } // end method surfaceDestroyed

    // called when the user touches the screen in this Activity
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        return myGestureListener.getDetector().onTouchEvent(e);
    } // end method onTouchEvent

    // Thread subclass to control the game loop
    private class CannonThread extends Thread {
        private final SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        // changes running state
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        // controls the game loop
        @Override
        public void run() {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas();

                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS); // update game state
                        if (!snakeover) {
                            drawGameElements(canvas); // draw using the canvas
                        }
                        previousFrameTime = currentTime; // update previous time
                    }
                } finally {
                    // display canvas's contents on the CannonView
                    // and enable other threads to use the Canvas
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            } // end while
        } // end method run
    } // end nested class CannonThread

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
