package com.example.skacheev.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void switchActivity(View v) {
        Class klass;
        switch (v.getId()) {
            case R.id.btn_task_1:
                klass = CalculatorActivity.class;
                break;
            case R.id.btn_task_2:
                klass = DrawActivity.class;
                break;
            case R.id.btn_task_3:
                klass = GameActivity.class;
                break;
            case R.id.btn_task_4:
                klass = GameSettingsActivity.class;
                break;
            case R.id.btn_pref:
                klass = PreferencesActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(this, klass);
        View root = (View)v.getParent();
        intent.putExtra("width", root.getMeasuredWidth());
        intent.putExtra("height", root.getMeasuredHeight());

        startActivity(intent);
    }
}
