package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminOrdersActivity extends AppCompatActivity {

    private static final String TAG = "AdminOrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "✅ onCreate appelé");
        Toast.makeText(this, "AdminOrdersActivity lancée !", Toast.LENGTH_SHORT).show();

        try {
            setContentView(R.layout.activity_admin_orders);
            Log.d(TAG, "✅ Layout chargé");

            findViewById(R.id.btnBack).setOnClickListener(v -> {
                Log.d(TAG, "Bouton retour cliqué");
                finish();
            });

            Log.d(TAG, "✅ Initialisation complète");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERREUR lors de l'initialisation", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
