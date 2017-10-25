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
                klass = CalculatorActivity.class;
                break;
            case R.id.btn_task_3:
                klass = CalculatorActivity.class;
                break;
            case R.id.btn_task_4:
                klass = CalculatorActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(this, klass);
        startActivity(intent);
    }
}
