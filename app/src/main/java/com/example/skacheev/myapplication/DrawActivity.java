package com.example.skacheev.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class DrawActivity extends AppCompatActivity {
    private static final String TAG = "DrawActivity";

    int redColor = 255;
    int yellowColor = 255;
    int greenColor = 255;
    int colorDiff = 5;

    Task t;

    class Task extends Thread {
        Runnable invalidator;
        Boolean redUp = false;
        Boolean yellowUp = false;
        Boolean greenUp = false;

        @Override
        public void run(){
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    if (redUp) {
                        redColor += colorDiff;
                        if (redColor > 255) {redColor = 255; redUp = false;}
                    } else {
                        redColor -= colorDiff;
                        if (redColor <= 100) {redColor = 100;redUp = true;}
                    }
                    if (yellowUp) {
                        yellowColor += colorDiff * 2;
                        if (yellowColor > 255) {yellowColor = 255;yellowUp = false;}
                    } else {
                        yellowColor -= colorDiff * 2;
                        if (yellowColor <= 100) {yellowColor = 100;yellowUp = true;}
                    }
                    if (greenUp) {
                        greenColor += colorDiff * 3;
                        if (greenColor > 255) {greenColor = 255; greenUp = false;}
                    } else {
                        greenColor -= colorDiff * 3;
                        if (greenColor <= 100) {greenColor = 100; greenUp = true;}
                    }
                    invalidator.run();
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {
                Log.d(TAG, "Color changer thread interrupted");
            }
        }
        void setInvalidator(Runnable r) {
            invalidator = r;
        }
    }

    class TrafficLight extends View {
        private static final String TAG = "TrafficLight";

        Paint paint = new Paint();
        Rect rect = new Rect();


        public TrafficLight(Context context) {
              super(context);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth()/2;
            int cy = getHeight()/2;
            int radius = 100;
            rect.set(cx - 120, cy - 340, cx + 120, cy + 340);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // first circle
            paint.setColor(Color.argb(255, redColor, 0, 0));
            canvas.drawCircle(cx, cy - 220, radius, paint);
            paint.setColor(Color.argb(255, yellowColor, yellowColor, 0));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(Color.argb(255, 0, greenColor, 0));
            canvas.drawCircle(cx, cy + 220, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            canvas.drawRect(rect, paint);

            if (t == null) {
                Log.d(TAG, "Create new color changer thread");
                t = new Task();
                t.setInvalidator(new Runnable() {public void run() {
                    Log.d(TAG, "Invalidate view");
                    postInvalidate();
                }});
                t.start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TrafficLight(this));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        t.interrupt();
        Log.d(TAG, "Color changer thread destroyed");
    }
}
