package com.yanry.lihua.sakura;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by rongyu.yan on 3/20/2017.
 */

public class PetalView extends View implements GestureDetector.OnGestureListener, Runnable {
    public static final int DRAW_INTERVAL = 30;
    private static final int DURATION = 5000;
    private static final String PREF_KEY_PETAL_NUMBER = "PETAL_NUMBER";
    private static final String PREF_NAME = "PREF";
    private static final int STATE_PLAYING = 0;
    private static final int STATE_PAUSE = 1;
    private static final int STATE_RELEASE = 2;

    private Random random;
    private int screenWidth;
    private int screenHeight;
    private Paint paint;
    private LinkedList<FallingPetal> petals;
    private Bitmap[] bitmaps;
    private Matrix matrix;
    private Camera camera;
    private Interpolator interpolator;
    private MotionEvent latestEvent;
    private GestureDetector detector;
    private int state;

    public PetalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        random = new Random();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        paint = new Paint();
        matrix = new Matrix();
        camera = new Camera();
        interpolator = new LinearInterpolator();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        bitmaps = new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.mmexport1489996195698, options),
                BitmapFactory.decodeResource(getResources(), R.drawable.mmexport1489996272778, options),
                BitmapFactory.decodeResource(getResources(), R.drawable.mmexport1489996268612, options),
                BitmapFactory.decodeResource(getResources(), R.drawable.mmexport1489996208426, options),
        };
        petals = new LinkedList<>();
        int petalNum = context.getSharedPreferences(PREF_NAME, 0).getInt(PREF_KEY_PETAL_NUMBER, 32);
        for (int i = 0; i < petalNum; i++) {
            FallingPetal petal = new FallingPetal();
            petal.init();
            petals.add(petal);
        }

        detector = new GestureDetector(context, this);
        state = STATE_PLAYING;
    }

    public void pause() {
        state = STATE_PAUSE;
        for (FallingPetal petal : petals) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                petal.animatorZ.pause();
                petal.animatorX.pause();
                petal.animatorY.pause();
            }
        }
    }

    public void resume() {
        state = STATE_PLAYING;
        for (FallingPetal petal : petals) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                petal.animatorZ.resume();
                petal.animatorX.resume();
                petal.animatorY.resume();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        state = STATE_RELEASE;
        for (FallingPetal petal : petals) {
            petal.animatorX.cancel();
            petal.animatorY.cancel();
            petal.animatorZ.cancel();
        }
        for (Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }
        getContext().getSharedPreferences(PREF_NAME, 0).edit().putInt(PREF_KEY_PETAL_NUMBER, petals.size()).commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        latestEvent = event;
        detector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == STATE_PLAYING) {
            for (FallingPetal petal : petals) {
                petal.draw(canvas);
            }
        }
        if (state != STATE_RELEASE) {
            postDelayed(this, DRAW_INTERVAL);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float y1 = e1.getRawY();
        float y2 = e2.getRawY();
        if (y1 - y2 > 0) {
            // up
            int step = (int) ((y1 - y2) * 10 / screenHeight);
            for (int i = 0; i < step; i++) {
                FallingPetal petal = petals.poll();
                if (petal == null) {
                    break;
                }
            }
        } else if (y2 - y1 > 0) {
            // down
            int step = (int) ((y2 - y1) * 10 / screenHeight);
            for (int i = 0; i < step; i++) {
                FallingPetal petal = new FallingPetal();
                petal.init();
                petals.add(petal);
            }
        }
        System.out.println(petals.size());
        return false;
    }

    @Override
    public void run() {
        invalidate();
    }

    public class FallingPetal implements TypeEvaluator<Integer>, Animator.AnimatorListener {
        private int[] yMedianValues;
        private ValueAnimator animatorX;
        private ValueAnimator animatorY;
        private ValueAnimator animatorZ;
        private int delay;
        private int duration;
        private Bitmap bitmap;

        void init() {
            int offset = 2000;
            duration = DURATION - offset + random.nextInt(offset);

            int endY = screenHeight;
            int endX = random.nextInt(screenWidth) + screenWidth / 4;
            if (latestEvent != null && latestEvent.getActionMasked() != MotionEvent.ACTION_UP) {
                endY = (int) latestEvent.getRawY();
                endX = (int) latestEvent.getRawX();
            }
            yMedianValues = new int[1 + random.nextInt(3)];
            for (int i = 0; i < yMedianValues.length; i++) {
                yMedianValues[i] = random.nextInt(screenHeight);
            }
            animatorY = ValueAnimator.ofObject(this, 0, endY);
            animatorY.setDuration(duration);
            animatorY.setInterpolator(interpolator);
            animatorY.addListener(this);

            int[] x = new int[1 + random.nextInt(3)];
            for (int i = 0; i < x.length; i++) {
                x[i] = random.nextInt(screenWidth);
            }
            animatorX = ValueAnimator.ofObject(new BezierEvaluator(x), random.nextInt(screenWidth) - screenWidth / 4,
                    endX);
            animatorX.setDuration(duration);
            animatorX.setInterpolator(interpolator);

            int[] z = new int[1 + random.nextInt(3)];
            for (int i = 0; i < z.length; i++) {
                z[i] = random.nextInt(screenWidth);
            }
            animatorZ = ValueAnimator.ofObject(new BezierEvaluator(z), random.nextInt(screenWidth),
                    random.nextInt(screenWidth));
            animatorZ.setDuration(duration);
            animatorZ.setInterpolator(interpolator);

            delay = random.nextInt(DURATION);

            bitmap = bitmaps[random.nextInt(bitmaps.length)];
        }

        void draw(Canvas canvas) {
            delay -= DRAW_INTERVAL;
            if (delay < 0) {
                if (!animatorY.isStarted()) {
                    animatorZ.start();
                    animatorX.start();
                    animatorY.start();
                }
                canvas.save();
                camera.save();
                int x = (int) animatorX.getAnimatedValue();
                int y = (int) animatorY.getAnimatedValue();
                int z = (int) animatorZ.getAnimatedValue();
                camera.translate(x, -y, z);
                camera.setLocation(0, 0, Integer.MAX_VALUE);
                camera.rotateX(z - screenWidth / 2);
                camera.rotateZ(x - screenWidth / 2);
                camera.rotateY(x - z);
                camera.getMatrix(matrix);
                camera.restore();
                int remainTime = duration - (int)animatorY.getCurrentPlayTime();
                if (remainTime > 512) {
                    paint.setAlpha(0xff);
                } else if (remainTime >= 0) {
                    paint.setAlpha(remainTime >> 1);
                }
                canvas.drawBitmap(bitmap, matrix, paint);
                canvas.restore();
            }
        }

        @Override
        public Integer evaluate(float t, Integer startValue, Integer endValue) {
            return MathUtil.bezierEvaluate(t, startValue, endValue, yMedianValues);
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (state != STATE_RELEASE) {
                init();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
