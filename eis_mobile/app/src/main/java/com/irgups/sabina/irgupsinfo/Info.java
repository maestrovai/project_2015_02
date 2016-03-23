package com.irgups.sabina.irgupsinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Vector;


/**
 * Класс Info - информация о помещении
 * Исмайлова Сабина ПО-10-1 06.15
 */
public class Info extends Activity {

    private static final String METHOD_NAME_FINDEMPLOY = "asu_FindEmploy";
    private static final String METHOD_NAME_SHOWINVENTORY = "ShowInventory";
    String cabNum;
    String[] data = {"Инвентарная опись", "Список сотрудников"};
    AlertDialog.Builder ad;
    Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        //final Intent intent_main = new Intent(this, MainActivity.class);
        //final Intent intent_login = new Intent(this, Login.class);

        cabNum = "Д-518";
        //cabNum = MainActivity.nameRoom;

        // предотвращение ощибок вызваных версией SDK
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        // заголовок сообщения
        spinner.setPrompt("Сведения о помещении:");
        // элемент, отображающийся при запуске
        //spinner.setSelection(0);

        // обработчик выбора элемента из списка
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView responseSOAP = (TextView) findViewById(R.id.textView);
                // spinAct содержит выбраный элемент списка
                String spinAct = spinner.getSelectedItem().toString();
                if (spinAct.equals("Инвентарная опись")) {
                    /*формировние SOAP запроса для получения инвентарной описи */
                    SoapObject Request = new SoapObject(SOAP.NAMESPACE, METHOD_NAME_SHOWINVENTORY);
                    Request.addProperty("num_aud", cabNum);
                    SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER10);
                    soapEnvelope.setOutputSoapObject(Request);
                    soapEnvelope.bodyOut = Request;
                    HttpTransportSE aht = new HttpTransportSE(SOAP.URL);
                    aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    aht.debug = true;
                    try {
                        aht.call(SOAP.SOAP_ACTION, soapEnvelope);
                        SoapObject ret = (SoapObject) soapEnvelope.bodyIn;
                        Vector prepodList = (Vector) ret.getProperty(0);

                        SoapObject element;
                        SoapObject elementContent;
                        String value;
                        String rez = "";

                        rez = cabNum + "\n";
                        //Перебираем элементы вектора
                        for (int i = 0; i < prepodList.size(); i++) {
                            element = (SoapObject) prepodList.elementAt(i);
                            //Перебираемм содержимое массива, который находится в i-м элементе вектора
                            for (int j = 1; j < element.getPropertyCount(); j++) {
                                elementContent = (SoapObject) element.getProperty(j);
                                value = elementContent.getProperty(1).toString();
                                rez = rez + value + " ";
                            }
                            rez = rez + "\n";
                        }

                        responseSOAP.setText(rez);
                    } catch (Exception e) {
                        responseSOAP.setText("Ошибка SOAP:" + " " + e);
                        responseSOAP.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                } else {
                    /**
                     *  формировние SOAP запроса для получения списка сотрудников
                     */
                    // формирование запроса
                    SoapObject Request = new SoapObject(SOAP.NAMESPACE, METHOD_NAME_FINDEMPLOY);
                    Request.addProperty("num_aud", cabNum);

                    SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER10);
                    soapEnvelope.setOutputSoapObject(Request);
                    soapEnvelope.bodyOut = Request;

                    HttpTransportSE aht = new HttpTransportSE(SOAP.URL);
                    aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    aht.debug = true;
                    try {
                        aht.call(SOAP.SOAP_ACTION, soapEnvelope);
                        SoapObject ret = (SoapObject) soapEnvelope.bodyIn;
                        Vector prepodList = (Vector) ret.getProperty(0);
                        SoapObject element;
                        SoapObject elementContent;
                        String value;
                        String rez = "";
                        rez = cabNum + "\n";
                        //Перебираем элементы вектора
                        for (int i = 0; i < prepodList.size(); i++) {
                            element = (SoapObject) prepodList.elementAt(i);
                            //Перебираемм содержимое массива, который находится в i-м элементе вектора
                            for (int j = 1; j < element.getPropertyCount(); j++) {
                                elementContent = (SoapObject) element.getProperty(j);
                                value = elementContent.getProperty(1).toString();
                                rez = rez + value + " ";
                            }
                            rez = rez + "\n";
                        }
                        responseSOAP.setText(rez);
                    } catch (Exception e) {
                        responseSOAP.setText("Ошибка SOAP:" + " " + e);
                        responseSOAP.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

       /* context = Info.this;
        String title = "Требуется авторизация";
        String message = "Для просмотра сведений о помещении необходимо войти в систему. " +
                         "Авторизироваться?";
        String buttonLogin = "Да";
        String buttonBack = "Назад";

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);  // заголовок
        ad.setMessage(message); // сообщение
        ad.setIcon(R.drawable.attention);

        ad.setPositiveButton(buttonLogin, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // переход на интерфейс авторизации
                startActivity(intent);
            }

        });
        // вернуться назад
        ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        ad.setCancelable(false);*/
    }

    public void lookFor(View v) {
        // показать предупреждение
        ad.show();
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
            case R.id.login:
                intent = new Intent(this, Login.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}