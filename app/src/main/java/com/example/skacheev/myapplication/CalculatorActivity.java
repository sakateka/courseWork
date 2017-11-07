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
    // тектосове поле результата
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

    // обработчик события клавиши backspace (удаление одного символа)
    public void backAction(View v) {
        String text = result.getText().toString();
        if (text.length() != 0) {
            text = text.substring(0, text.length()-1);
            setText(text);
            if (text.matches(".*["+ AVAILABLE_ACTIONS +"].*")) {
                // Если арифметическая операция еще присутсвует в тексте,
                // то текущая операция считается иницилизированной

                Log.d(TAG, "backAction: string contains 'operator' " + text);
                if (second_operand.length()> 0) {
                    second_operand = second_operand.substring(0, second_operand.length() - 1);
                }
            } else {
                // Иначе сбрасываем текущую операцию и второй аргумент.

                Log.d(TAG, "backAction: clear operator and second_operand");
                operator = "";
                second_operand = "";
                Log.d(TAG, "backAction: first_operand = " + text);
                first_operand = text;
            }
        } else {
            // Если текст кончился, вызываем сброс всего через clearAction
            this.clearAction(v);
        }
    }
    public void clearAction(View v) {
        // Сбрасываем все парамерты
        Log.d(TAG, "backAction: clear all operands and operator");
        first_operand = "";
        second_operand = "";
        operator = "";
        setText("");
    }

    public void addNumberAction(View view) {
        // Обработчик события нажатия цифр и точки
        Button button = (Button)view;
        String symbol = button.getText().toString();
        Log.d(TAG, "addNumberAction: type symbol " + symbol);

        // определяем куда нужно добавлять текст
        String where = "first";
        String currentText = first_operand;
        if (operator.length() != 0) {
            where = "second";
            currentText = second_operand;
        }
        Log.d(TAG, "addNumberAction: try add symbol '"+ symbol +"' for "+ where +" operand");

        // Если тут точка уже есть, то ничего не делаем
        if (symbol.equals(".")) {
            if (currentText.indexOf('.') >= 0) {
                return;
            }
        }

        // добавляем текст.
        currentText += symbol;
        Log.d(TAG, "addNumberAction: "+where+"_operand = " + currentText);
        // определяем куда записать его обратно
        if (operator.length() == 0) {
            first_operand = currentText;
            setText(currentText);
        } else {
            second_operand = currentText;
            setText(result.getText() + symbol);
        }
    }

    // обработчик событий нажатия арифметических операций
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
        // Если текст на кнопке не односимволтный,
        // то преобразуем его в символ операции
        String opText = button.getText().toString();
        switch (opText) {
            case "mod":
                opText = "%";
                break;
            case "pow":
                opText = "^";
                break;
        }

        // Если второго операнда еще нет, то просто запоминаем операцию
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
            // Пробуем распарсить первый и второй операнды
            res = Double.parseDouble(first_operand);
            second = Double.parseDouble(second_operand);
        } catch (NumberFormatException e) {
            Log.d(TAG, "computeAction: failed to parse " + e);
            res = Double.NaN;
            second = Double.NaN;
            operator = "";
        }

        // Выполняем необходимую арифметическую операцию
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
                // Если случилось деление на 0 то возвращем NaN
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
        // Если арифметическая операция вызвана не через равно,
        // то результат помещаем в первый операнд
        if (! opText.equals("=")) {
            Log.d(TAG, "computeAction: preserve first operand value: " + resultText);
            first_operand = resultText;
            resultText += opText;
            operator = opText;
        }
        setText(resultText);
    }

    // Ресайзим текст в тексотом поле результата, чтобы при малом кол-ве цифр они были крупными
    // а при большом кол-ве цифр помещались на экране.
    private void setText(String text) {
        if (text.length() > 13) {
            result.setTextSize(20);
        } else if (text.length() > 8) {
            result.setTextSize(40);
        } else if (text.length() > 5) {
            result.setTextSize(60);
        } else {
            result.setTextSize(80);
        }
        result.setText(text);
    }

    // Удобное форматирование числа или строки в строку.
    private String fmt(double d) {
        if (d == (long)d)
            return format(Locale.getDefault(), "%d", (long) d);
        else
            return format(Locale.getDefault(), "%s", d);
    }
}