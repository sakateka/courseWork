package com.example.skacheev.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.SoundPool;
import android.support.v7.app.ActionBar;
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

    int width, height, octahedronSize, barHeight = 0;
    ImageView frame;
    Bitmap bitmap;
    final Canvas canvas = new Canvas();
    Paint paint = new Paint();

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

        paint.setColor(Color.GREEN);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        canvas.drawColor(Color.GRAY);
        frame = (ImageView) findViewById(R.id.gameView);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            y -= barHeight;
            Log.d(TAG, String.format("Event on %fx%f", x, y));
            ActionBar actionBar = getSupportActionBar();
            Octahedron o = new Octahedron(x, y);
            octas.add(o);
            o.start();
            return true;
        }
        return false;
    }

    public void redraw(){
        canvas.drawColor(Color.GRAY);
        for (Octahedron octa : octas) {
            float x = octa.getPosOctaX();
            float y = octa.getPosOctaY();
            if (Float.isNaN(x) || Float.isNaN(y)) {
                octas.remove(octa);
            } else {
                drawOctahedron((int)x, (int)y);
                octa.isHidden = false;
            }
        }
        frame.postInvalidate();
        for (Octahedron octa : octas) {
            octa.isHidden = false;
        }
    }
    void drawOctahedron(int x, int y) {
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
        private static final String TAG = "Octahedron";
        float directionY = 1, directionX = 1;
        volatile float posX, posY;
        volatile boolean isHidden;
        float xVelocity, yVelocity;

        Octahedron(float px, float py) {
            posX = px;
            posY = py - octahedronSize;
            xVelocity = octahedronSize;
            yVelocity = octahedronSize/10;
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
            yVelocity = yVelocity + yVelocity/(float)3 * directionY;
            if (yVelocity > 0) {
                if (Math.abs(yVelocity) <= 0.5 || (xVelocity < 0.001 && posY > height - octahedronSize/3)){
                    posX = Float.NaN;
                    posY = Float.NaN;
                    return false;
                }
            }
            if (Math.abs(yVelocity) <= 2) {
                directionY = -directionY;
                if (yVelocity > 0) {
                    yVelocity /= yVelocity;
                } else {
                    yVelocity *= yVelocity;
                }
            }
            if (posY > height - octahedronSize/3) {
                yVelocity = Math.abs(yVelocity)/(float)2 * directionY;
            }

            posY += yVelocity;
            if (posY >= height-1) {
                if (sp != null) {
                    Log.d(TAG, "Play boom");
                    sp.play(boomID, 1, 1, 0, 0, 1);
                }
                posY = height - octahedronSize/2;
                directionY = -directionY;
                yVelocity = -yVelocity/2;
            }
            posX += (xVelocity-xVelocity/2) * directionX;
            xVelocity -= xVelocity*(float)0.05;
            if (posX > width-1){
                if (sp != null) {
                    Log.d(TAG, "Play boom");
                    sp.play(boomID, 1, 1, 0, 0, 1);
                }
                posX = width - octahedronSize/2;
                directionX = -directionX;
            }
            if (posX < 1) {
                posX = octahedronSize/2;
                directionX = -directionX;
            }
            return true;
        }

        float getPosOctaX() {
            return posX;
        }

        float getPosOctaY() {
            return posY;
        }
    }
}
