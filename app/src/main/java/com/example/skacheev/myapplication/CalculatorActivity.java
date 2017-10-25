package com.example.skacheev.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import static java.lang.String.*;

public class CalculatorActivity extends AppCompatActivity {

    private static final String TAG = "CalculatorActivity";
    private static final String AVAILABLE_ACTIONS = "-+*/^%";
    private TextView result;

    private String first_operand = "";
    private String second_operand = "";
    private String operator = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        result = (TextView) findViewById(R.id.result);
    }

    public void backAction(View v) {
        String text = result.getText().toString();
        if (text.length() != 0) {
            text = text.substring(0, text.length()-1);
            setText(text);
            if (text.matches(".*["+ AVAILABLE_ACTIONS +"].*")) {
                Log.d(TAG, "backAction: string contains 'operator' " + text);
                if (second_operand.length()> 0) {
                    second_operand = second_operand.substring(0, second_operand.length() - 1);
                }
            } else {
                Log.d(TAG, "backAction: clear operator and second_operand");
                operator = "";
                second_operand = "";
                Log.d(TAG, "backAction: first_operand = " + text);
                first_operand = text;
            }
        } else {
            Log.d(TAG, "backAction: clear first_operand");
            operator = "";
            first_operand = "";
            second_operand = "";
        }
    }
    public void clearAction(View v) {
        first_operand = "";
        second_operand = "";
        operator = "";
        setText("");
    }

    public void addNumberAction(View view) {

        Button button = (Button)view;
        String symbol = button.getText().toString();
        Log.d(TAG, "addNumberAction: type symbol " + symbol);

        String where = "first";
        String currentText = first_operand;
        if (operator.length() != 0) {
            where = "second";
            currentText = second_operand;
        }
        Log.d(TAG, "addNumberAction: try add symbol '"+ symbol +"' for "+ where +" operand");

        if (symbol.equals(".")) {
            if (currentText.indexOf('.') >= 0) {
                return;
            }
        }

        currentText += symbol;
        Log.d(TAG, "addNumberAction: "+where+"_operand = " + currentText);
        if (operator.length() == 0) {
            first_operand = currentText;
            setText(currentText);
        } else {
            second_operand = currentText;
            setText(result.getText() + symbol);
        }
    }

    public void computeAction(View view){
        String resText = result.getText().toString();
        if (resText.length() == 0) {
            return;
        } else {
            if (operator.length() == 0){
                first_operand = resText;
            }
        }

        Button button = (Button)view;
        String opText = button.getText().toString();
        switch (opText) {
            case "mod":
                opText = "%";
                break;
            case "pow":
                opText = "^";
                break;
        }

        if (second_operand.length() == 0) {
            if (opText.equals("=")) {
                return;
            }

            Log.d(TAG, "computeAction: second operand not ready, set operator to: " + opText);
            operator = opText;
            if (resText.endsWith(opText)) {
                return;
            }
            char lastChar = resText.charAt(resText.length()-1);
            if (AVAILABLE_ACTIONS.indexOf(lastChar)>=0) {
                resText = resText.substring(0, resText.length() - 1);
            }
            setText(resText + opText);
            return;
        }
        double res;
        double second;
        try {
            res = Double.parseDouble(first_operand);
            second = Double.parseDouble(second_operand);
        } catch (NumberFormatException e) {
            Log.d(TAG, "computeAction: failed to parse " + e);
            res = Double.NaN;
            second = Double.NaN;
            operator = "";
        }

        Log.d(TAG, "computeAction: computation invoked by: " + opText);
        switch (operator) {
            case "+":
                res += second;
                break;
            case "-":
                res -= second;
                break;
            case "*":
                res *= second;
                break;
            case "/":
                if (second == 0) {
                    res = Double.NaN;
                } else {
                    res /= second;
                }
                break;
            case "^":
                res = Math.pow(res, second);
                break;
            case "%":
                res %= second;
                break;
        }

        operator = "";
        first_operand = "";
        second_operand = "";

        String resultText = format(Locale.getDefault(), "%s", fmt(res));
        if (! opText.equals("=")) {
            Log.d(TAG, "computeAction: preserve first operand value: " + resultText);
            first_operand = resultText;
            resultText += opText;
            operator = opText;
        }
        setText(resultText);
    }

    private void setText(String text) {
        if (text.length() > 15) {
            result.setTextSize(20);
        } else if (text.length() > 10) {
            result.setTextSize(40);
        } else if (text.length() > 7) {
            result.setTextSize(60);
        } else {
            result.setTextSize(80);
        }
        result.setText(text);
    }

    private String fmt(double d) {
        if (d == (long)d)
            return format(Locale.getDefault(), "%d", (long) d);
        else
            return format(Locale.getDefault(), "%s", d);
    }
}