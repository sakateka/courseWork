package com.example.skacheev.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.concurrent.CopyOnWriteArrayList;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    int boomID;
    public SoundPool sp;
    float boostForX = (float)0.20;
    float boostForY = (float)0.20;

    int width, height, octahedronSize, barHeight = 0;
    ImageView frame;
    Bitmap bitmap;
    int backgroundColor = Color.GRAY;
    Paint paint = new Paint();
    int octahedronColor = Color.GREEN;
    final Canvas canvas = new Canvas();


    CopyOnWriteArrayList<Octahedron> octas = new CopyOnWriteArrayList<>();

    public RedrawTask t;

    class RedrawTask extends Thread {
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
                Log.d(TAG, "redraw thread destroyed");
            }
        }
        RedrawTask setInvalidator(Runnable r) {
            invalidator = r;
            return this;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            width = extras.getInt("width");
            height = extras.getInt("height");
        } else {
            width = 600;
            height = 800;
        }
        octahedronSize = width/100*10;

        paint.setColor(octahedronColor);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        canvas.drawColor(backgroundColor);
        frame = findViewById(R.id.gameView);
        frame.setImageBitmap(bitmap);
        t = new RedrawTask().setInvalidator(
                new Runnable() {
                    @Override
                    public void run() {
                        redraw();
                    }
                }
        );
        Log.d(TAG, "start redraw thread");
        t.start();

        // определяем размер акшенБара, чтобы правильно вычислять координаты нажатия
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            barHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Octahedron o: octas) {
            o.interrupt();
        }
        t.interrupt();
    }

    public void setSound(SoundPool s, int sampleID) {
        sp = s;
        boomID = sampleID;
    }

    public void setXYBoost(float x, float y) {
        boostForX = x;
        boostForY = y;
    }
    public void setColors(int background, int octahedron) {
        this.backgroundColor = background;
        this.octahedronColor = octahedron;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Обработчик нажатия на экран добавляет новый октаэдр
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            y -= barHeight;
            Log.d(TAG, String.format("Event on %fx%f", x, y));
            Octahedron o = new Octahedron(x, y).setVelocityBoost(boostForX, boostForY);
            octas.add(o);
            o.start();
            return true;
        }
        return false;
    }

    public void redraw(){
        // Перерисовывает фон и все октаэдры
        canvas.drawColor(backgroundColor);
        for (Octahedron octa : octas) {
            float x = octa.getPosOctaX();
            float y = octa.getPosOctaY();
            // Если октаэдр остановился, то удаляем его
            if (Float.isNaN(x) || Float.isNaN(y)) {
                octas.remove(octa);
            } else {
                drawOctahedron((int)x, (int)y);
                octa.isHidden = false;
            }
        }
        frame.postInvalidate();
    }
    void drawOctahedron(int x, int y) {
        // Рисует один октаэдр
        float oHalf = octahedronSize/(float)2;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(oHalf/(float)10);
        Path p = new Path();
        p.moveTo(x - oHalf, y);
        p.lineTo(x, y - oHalf);
        p.lineTo(x + oHalf, y);
        p.lineTo(x, y + oHalf);
        p.lineTo(x - oHalf, y);
        p.lineTo(x+oHalf/(float)4, y+oHalf/(float)3);
        p.lineTo(x+oHalf, y);
        p.moveTo(x, y - oHalf);
        p.lineTo(x+oHalf/(float)4, y+oHalf/(float)3);
        p.lineTo(x, y+oHalf);
        canvas.drawPath(p, paint);
    }


    class Octahedron extends Thread {
        // Тред октаэдра, в цикле занимается персчетом своей позиции
        // Между пересчетами засыпает на короткий интервал (40ms)
        private static final String TAG = "Octahedron";
        float directionY = 1, directionX = 1;
        volatile float posX, posY;
        volatile boolean isHidden = true;

        float xVelocity, yVelocity;
        float boostY, boostX;

        Octahedron(float px, float py) {
            posX = px;
            posY = py - octahedronSize;
            xVelocity = octahedronSize/3;
            yVelocity = octahedronSize/5;
        }
        @Override
        public void run(){
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(40);
                    if (! update()) {
                        break;
                    }
                }
                Log.d(TAG, "stop Octahedron");
            } catch (InterruptedException ignored) {
                Log.d(TAG, "destroy Octahedron");
            }
        }
        boolean update() {
            if (isHidden) {
                return true;
            }

            // Y coordinates
            yVelocity += yVelocity* boostX * directionY;
            if (Math.abs(yVelocity) <= 1.5) {
                // reverse direction when octahedron lost too much speed
                directionY = -directionY;
                if (yVelocity > 0) {
                    yVelocity /= yVelocity;
                } else {
                    yVelocity *= yVelocity;
                }
            }

            posY += yVelocity;
            if (posY >= height-octahedronSize/2 && directionY > 0) {
                posY = height - octahedronSize/2;
                directionY = -directionY;
                // bottom boom decrease speed by half
                yVelocity = Math.abs(yVelocity)/(float)2 * directionY;

                if (Math.abs(yVelocity) > 2 && sp != null) {
                    //Log.d(TAG, "Play boom Bottom");
                    sp.play(boomID, 1, 1, 0, 0, 1);
                }
            }
            if (Math.abs(yVelocity) <= 0.1 && Math.abs(xVelocity) <= 0.1){
                posX = Float.NaN;
                posY = Float.NaN;
                return false;
            }


            // X coordinates
            posX += xVelocity * directionX;
            xVelocity -= xVelocity*(float)0.03;
            if (posX > width-octahedronSize/2){
                if (sp != null) {
                    //Log.d(TAG, "Play boom Right");
                    sp.play(boomID, 1, 1, 0, 0, 1);
                }
                posX = width - octahedronSize/2;
                directionX = -directionX;
            }
            if (posX < octahedronSize/2) {
                if (sp != null) {
                    //Log.d(TAG, "Play boom Left");
                    sp.play(boomID, 1, 1, 0, 0, 1);
                }
                posX = octahedronSize/2;
                directionX = -directionX;
            }
            return true;
        }

        float getPosOctaX() {
            return posX;
        }
        float getPosOctaY() { return posY; }

        Octahedron setVelocityBoost(float boostX, float boostY) {
            this.boostX = boostX;
            this.boostY = boostY;
            return this;
        }

    }
}
