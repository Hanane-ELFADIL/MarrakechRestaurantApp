package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PayPalActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.137.1:3000";

    private WebView paypalWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);

        paypalWebView = findViewById(R.id.paypalWebView);
        paypalWebView.getSettings().setJavaScriptEnabled(true);
        paypalWebView.setWebViewClient(new WebViewClient());

        createOrder();
    }

    private void createOrder() {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("amount", "10.00");

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/create-order")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(
                                    PayPalActivity.this,
                                    "Backend inaccessible",
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String res = response.body().string();
                        JSONObject data = new JSONObject(res);
                        String approveUrl = data.getString("approveUrl");

                        runOnUiThread(() -> paypalWebView.loadUrl(approveUrl));

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        PayPalActivity.this,
                                        "Erreur PayPal",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
