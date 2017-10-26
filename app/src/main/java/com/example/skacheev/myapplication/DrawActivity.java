package com.example.skacheev.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DrawActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TrafficLight(this));
    }
    class TrafficLight extends View {
        public TrafficLight(Context context) {
              super(context);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight();
            int radius = 100;

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // first circle
            paint.setColor(Color.RED);
            canvas.drawCircle(x/2, y/2 - 220, radius, paint);
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(x/2, y/2, radius, paint);
            paint.setColor(Color.GREEN);
            canvas.drawCircle(x/2, y/2 + 220, radius, paint);
        }
    }
}
