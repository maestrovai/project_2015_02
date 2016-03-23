package com.irgups.sabina.irgupsinfo;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

// подключение библиотек работы с камерой
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

// подключение библиотек сканирования Zbar
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


import android.graphics.Color;
import android.hardware.Camera.CameraInfo;

/**
 * Класс MainActivity
 * Исмайлова Сабина ПО-10-1 06.15
 */

public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private FrameLayout preview;
    private TextView scanText;
    private ImageScanner scanner;
    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private String lastScannedCode;
    private Image codeImage;
    private String[] scanCodeKeyWord, scanCodeCut;
    public static String nameRoom, idRoom;
    public Intent intent;
    public static String userId = "", secretToken = "", secret_code="";
    public static String sessionId = "", systemId = "", stud_id="";
    public static int studId=0;
    // Работа с настройками
    public static final String APP_PREFERENCES = "irgupsinfo";
    public static final String APP_PREFERENCES_UID = "uid";
    public static final String APP_PREFERENCES_ST = "st";
    private SharedPreferences mSettings;

    static {
        System.loadLibrary("iconv"); //загрузка native library
    }

    public void onCreate(Bundle savedInstanceState) {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        intent = new Intent(this, TimeTable.class);
        // установка портретной ориентации Activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();
        preview = (FrameLayout) findViewById(R.id.cameraPreview);
        // Инициализируем сканер
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        scanText = (TextView) findViewById(R.id.scanText);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSettings.contains(APP_PREFERENCES_UID)) {
            // Получаем число из настроек
            userId = mSettings.getString(APP_PREFERENCES_UID, "Nothing has been entered");
            //studentId = mSettings.getString(APP_PREFERENCES_UID, "Nothing has been entered");
        }
        if (mSettings.contains(APP_PREFERENCES_ST)) {
            // Получаем число из настроек
            secretToken = mSettings.getString(APP_PREFERENCES_ST, "Nothing has been entered");
            //code = mSettings.getString(APP_PREFERENCES_ST, "Nothing has been entered");
        }

        resumeCamera();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void resumeCamera() {
        scanText.setText(getString(R.string.scan_process_label));
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        preview.removeAllViews();
        preview.addView(mPreview);
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            Size size = parameters.getPreviewSize();
            codeImage = new Image(size.width, size.height, "Y800");
            previewing = true;
            mPreview.refreshDrawableState();
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing && mCamera != null) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    public PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            codeImage.setData(data);
            AlertDialog.Builder ad;
            Context context;

            int result = scanner.scanImage(codeImage);
            if (result != 0) {
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    lastScannedCode = sym.getData();
                    // если результат сканирования не пуст
                    if (lastScannedCode != null) {
                        mCamera.stopPreview(); //остановка превью
                        // Проверка типа содержимого
                        if (lastScannedCode.startsWith("VCARD")) {
                            // получение id помещения
                            scanCodeCut = lastScannedCode.split("KEY;ENCODING=b:");
                            scanCodeKeyWord = scanCodeCut[1].split("\n");
                            idRoom = scanCodeKeyWord[0];
                            // получаем название помещения
                            scanCodeCut = lastScannedCode.split("ADR:TYPE=work:");
                            scanCodeKeyWord = scanCodeCut[1].split("\n");
                            nameRoom = scanCodeKeyWord[0];
                            context = MainActivity.this;
                            String title = nameRoom;
                            String message = "Вы можете просмотреть расписание занятий в " + nameRoom + ", либо продолжить работу с приложением";
                            String buttonTimeTable = "Просмотреть расписание";
                            String buttonBack = "Продолжить";
                            ad = new AlertDialog.Builder(context);
                            ad.setTitle(title);  // заголовок
                            ad.setMessage(message); // сообщение
                            ad.setIcon(R.drawable.door);
                            ad.setPositiveButton(buttonTimeTable, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    // переход на интерфейс просмотра расписания
                                    dialog.cancel();
                                    startActivity(intent);
                                }
                            });
                            // вернуться назад
                            ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.cancel();
                                    mCamera.startPreview();
                                }
                            });
                            ad.setCancelable(false);
                            ad.show();
                        } else {
                            if (lastScannedCode.startsWith("UID:")) {
                                // получение id сотрудника
                                scanCodeCut = lastScannedCode.split("\n");
                                scanCodeKeyWord = scanCodeCut[0].split(":");
                                userId = scanCodeKeyWord[1];
                                scanCodeKeyWord = scanCodeCut[1].split(":");
                                secretToken = scanCodeKeyWord[1];
                                SharedPreferences.Editor editor = mSettings.edit();
                                editor.putString(APP_PREFERENCES_UID, userId);
                                editor.putString(APP_PREFERENCES_ST, secretToken);
                                editor.commit();
                                context = MainActivity.this;
                                String title = "Ключ";
                                String message = "Вы получили секретный ключ";
                                String buttonBack = "Продолжить";
                                ad = new AlertDialog.Builder(context);
                                ad.setTitle(title);  // заголовок
                                ad.setMessage(message); // сообщение
                                ad.setIcon(R.drawable.door);
                                // вернуться назад
                                ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        dialog.cancel();
                                        mCamera.startPreview();
                                    }
                                });
                                ad.setCancelable(false);
                                ad.show();

                            } else if(lastScannedCode.startsWith("UID:")){
                                // получение id студента
                                scanCodeCut = lastScannedCode.split("\n");
                                scanCodeKeyWord = scanCodeCut[0].split(":");
                                stud_id = scanCodeKeyWord[1];
                                scanCodeKeyWord = scanCodeCut[1].split(":");
                                secret_code = scanCodeKeyWord[1];
                                SharedPreferences.Editor editor = mSettings.edit();
                                editor.putString(APP_PREFERENCES_UID, userId);
                                editor.putString(APP_PREFERENCES_ST, secretToken);
                                editor.commit();
                                context = MainActivity.this;
                                String message = "Сканирование завершенно";
                                String buttonBack = "Продолжить";
                                ad = new AlertDialog.Builder(context);
                                ad.setMessage(message); // сообщение
                                ad.setIcon(R.drawable.door);
                                // вернуться назад
                                ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        dialog.cancel();
                                        mCamera.startPreview();
                                    }
                                });
                                ad.setCancelable(false);
                                ad.show();

                            } else {
                                if (lastScannedCode.startsWith("ID:")) {
                                    // получение id системы
                                    scanCodeCut = lastScannedCode.split("\n");
                                    scanCodeKeyWord = scanCodeCut[0].split(":");
                                    systemId = scanCodeKeyWord[1];
                                    scanCodeKeyWord = scanCodeCut[1].split(":");
                                    sessionId = scanCodeKeyWord[1];

                                    String message;
                                    if (secretToken.length() > 10) {
                                        SOAPAuth soap = new SOAPAuth();
                                        String res = soap.getAccess(systemId, sessionId);
                                        message = "Вы получили ключ для входа" + res;
                                    } else {
                                        message = "Нет секретного ключа";
                                    }
                                    context = MainActivity.this;
                                    String title = "Авторизация";

                                    String buttonBack = "Продолжить";
                                    ad = new AlertDialog.Builder(context);
                                    ad.setTitle(title);  // заголовок
                                    ad.setMessage(message); // сообщение
                                    ad.setIcon(R.drawable.door);
                                    // вернуться назад
                                    ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            dialog.cancel();
                                            mCamera.startPreview();
                                        }
                                    });
                                    ad.setCancelable(false);
                                    ad.show();
                                } else {
                                    // получение id системы
                                    scanCodeCut = lastScannedCode.split("\n");
                                    int docId = Integer.parseInt(scanCodeCut[0]);
                                    if (docId > 0) {
                                        String message;
                                        if (sessionId.equals("")) {
                                            message = "Вы не прошли авторизацию в ЕИС";
                                        } else {
                                            SOAPAuth soap = new SOAPAuth();
                                            soap.getDocument(systemId, sessionId, scanCodeCut[0]);
                                            message = "Вы смотрите на документ из " + scanCodeCut[3];
                                        }
                                        context = MainActivity.this;
                                        String title = "Документ";

                                        String buttonBack = "Продолжить";
                                        ad = new AlertDialog.Builder(context);
                                        ad.setTitle(title);  // заголовок
                                        ad.setMessage(message); // сообщение
                                        ad.setIcon(R.drawable.door);
                                        // вернуться назад
                                        ad.setNegativeButton(buttonBack, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int arg1) {
                                                dialog.cancel();
                                                mCamera.startPreview();
                                            }
                                        });
                                        ad.setCancelable(false);
                                        ad.show();
                                    }
                                }
                            }
                        }
                        barcodeScanned = true;
                    }
                }
            }
            camera.addCallbackBuffer(data);
        }
    };
    final AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    // главное меню
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timetable:
                Intent intent = new Intent(this, TimeTable.class);
                startActivity(intent);
                break;
            case R.id.login:
                intent = new Intent(this, Login.class);
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
