package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImageFullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);

        ImageView imageView = findViewById(R.id.fullScreenImage);

        String imageName = getIntent().getStringExtra("imageName");
        if (imageName != null) {
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resId != 0) imageView.setImageResource(resId);
        }
    }
}
