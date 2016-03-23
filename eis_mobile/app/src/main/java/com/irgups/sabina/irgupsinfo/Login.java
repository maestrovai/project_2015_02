package com.irgups.sabina.irgupsinfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;

import java.util.Vector;

/**
 * Класс Login - авторизация пользователя
 * Исмайлова Сабина ПО-10-1 06.15
 */
public class Login extends Activity {

    private static final String METHOD_NAME_CHEKLOGIN = "CheckUserLogin";

    EditText login, password;
    SoapObject ret;
    TextView errorMsg;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.login);

        // помещаем в переменные значения полей Edit согласно найденому id
        login = (EditText) findViewById(R.id.login_name);
        password = (EditText) findViewById(R.id.password);

        errorMsg = (TextView) findViewById(R.id.errorMsg);

        // предотвращение ощибок вызваных версией SDK
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    // Проверка введенных данных
    public void userAuthorization(View v) {
        // определение диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        // если login и password запонены
        if (login.getText().length() != 0 || password.getText().length() != 0) {
            /**
             * формировние SOAP-запроса для проверки аутентификации
             */

            // формирование SOAP-объекта
            SoapObject Request = new SoapObject(SOAP.NAMESPACE, METHOD_NAME_CHEKLOGIN);
            Request.addProperty("login", login.getText().toString());
            Request.addProperty("password", password.getText().toString());
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER10);
            soapEnvelope.setOutputSoapObject(Request);
            soapEnvelope.bodyOut = Request;

            HttpTransportSE aht = new HttpTransportSE(SOAP.URL);
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.debug = true;

            try {
                // вызов SOAP-функции
                aht.call(SOAP.SOAP_ACTION, soapEnvelope);
                ret = (SoapObject) soapEnvelope.bodyIn;
                // полученный результат
                String access = ret.getProperty(0).toString();

                if (access.equals("1")) {
                    builder.setTitle("Авторизация прошла успешно!")
                            .setIcon(R.drawable.accept)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    builder.setTitle("Ошибка авторизации!")
                            .setMessage("Неправильно введен логин или пароль. Пожалуйста, " +
                                    "проверьте правильность ввода и повторите попытку.")
                            .setIcon(R.drawable.error)
                            .setCancelable(false)
                            .setNegativeButton("Ок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                errorMsg.setText("Ошибка SOAP:" + " " + e);
                errorMsg.setTextColor(Color.RED);
                e.printStackTrace();
            }

        }
        // если login и password не заполнены
        else {
            builder.setTitle("Поля не заполнены!")
                    .setMessage("Пожалуйста, заполните поля и повторите попытку.")
                    .setIcon(R.drawable.attention)
                    .setCancelable(false)
                    .setNegativeButton("Ок",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // главное меню
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanner:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.timetable:
                intent = new Intent(this, TimeTable.class);
                startActivity(intent);
                break;
            case R.id.info:
                intent = new Intent(this, Info.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}