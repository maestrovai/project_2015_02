package com.irgups.sabina.irgupsinfo;

import android.app.Activity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Bundle;

/**
 * Класс TimeTable - вывод расписания
 * Исмайлова Сабина ПО-10-1 06.15
 */

public class TimeTable extends Activity {

    private WebView mWebView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);
        //ссылка для поиска расписания аудитории, согласно полученноу id
        String urlRoom = "http://www.irgups.ru/eis/rasp/index.php?action=aud&pid=";
        String url = urlRoom + MainActivity.idRoom;
        //если QR-код не сосканирован, то открываем основное расписание
        if (MainActivity.idRoom == null) {
            url = "http://www.irgups.ru/eis/rasp/index.php";
        }
        mWebView = (WebView) findViewById(R.id.webview);
        // включаем поддержку JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        // указываем ссылку на ресурс
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new HelloWebViewClient());
    }

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override

    // нажатие кнопки Назад на устройстве
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
