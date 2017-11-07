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

    ColorChangerTask t;

    class ColorChangerTask extends Thread {
        // Тред занимающийся посылкой сигналов перерисовки в DrawActivity
        private static final String TAG = "ColorChangerTask";

        Runnable invalidator;

        @Override
        public void run(){
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    invalidator.run();
                    Thread.sleep(40); // 40 ms give 25 fps for us
                }
            } catch (InterruptedException ignored) {
                Log.d(TAG, "invalidator thread destroyed");
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
        int redColor = 255;
        int yellowColor = 255;
        int greenColor = 255;
        int colorDiff = 5;

        String redUp = "down";
        String yellowUp = "down";
        String greenUp = "down";

        public TrafficLight(Context context) {
              super(context);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth()/2;
            int cy = getHeight()/2;
            int paddingCircle = cy/4;
            int radius = paddingCircle - paddingCircle/6;
            int paddingRect = radius + radius/6;
            if (t == null) {
                rect.set(
                        cx - paddingRect,
                        cy - (paddingCircle*2 + paddingRect) ,
                        cx + paddingRect,
                        cy + (paddingCircle*2 + paddingRect)
                );

                Log.d(TAG, "Create new invalidator thread");
                t = new ColorChangerTask();
                t.setInvalidator(new Runnable() {
                    public void run() {
                        postInvalidate();
                    }
                });
                t.start();
            }

            // Прорисовка светофора
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // first circle
            paint.setColor(Color.argb(255, redColor, 0, 0));
            canvas.drawCircle(cx, cy - paddingCircle*2, radius, paint);
            paint.setColor(Color.argb(255, yellowColor, yellowColor, 0));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setColor(Color.argb(255, 0, greenColor, 0));
            canvas.drawCircle(cx, cy + paddingCircle*2, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            canvas.drawRect(rect, paint);

            //Log.d(TAG, "red: "+ redUp +", yellow: "+ yellowUp + ", green: "+greenUp);

            // Вычисление нового цвета светофора
            // эти цвета будут нарисованы на следующей итерации
            if (redUp.equals("up")) {
                redColor += colorDiff;
                if (redColor > 255) {redColor = 255; redUp = "down";}
            } else {
                redColor -= colorDiff;
                if (redColor <= 100) {redColor = 100;redUp = "up";}
            }
            if (yellowUp.equals("up")) {
                yellowColor += colorDiff * 2;
                if (yellowColor > 255) {yellowColor = 255;yellowUp = "down";}
            } else {
                yellowColor -= colorDiff * 2;
                if (yellowColor <= 100) {yellowColor = 100;yellowUp = "up";}
            }
            if (greenUp.equals("up")) {
                greenColor += colorDiff * 3;
                if (greenColor > 255) {greenColor = 255; greenUp = "down";}
            } else {
                greenColor -= colorDiff * 3;
                if (greenColor <= 100) {greenColor = 100; greenUp = "up";}
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
    }
}
