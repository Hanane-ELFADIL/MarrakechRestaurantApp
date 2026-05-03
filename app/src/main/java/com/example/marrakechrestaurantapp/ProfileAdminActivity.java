package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvAdminEmail, tvAdminId, tvAdminCreationDate;
    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        btnBack = findViewById(R.id.btnBack);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminId = findViewById(R.id.tvAdminId);
        tvAdminCreationDate = findViewById(R.id.tvAdminCreationDate);
        btnLogout = findViewById(R.id.btnLogout);

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton déconnexion
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Charger les informations de l'admin
        loadAdminInfo();
    }

    private void loadAdminInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Email
            String email = currentUser.getEmail();
            tvAdminEmail.setText(email != null ? email : "Non disponible");

            // UID
            String uid = currentUser.getUid();
            tvAdminId.setText(uid);

            // Date de création
            long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
            String creationDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                    java.util.Locale.FRANCE).format(new java.util.Date(creationTimestamp));
            tvAdminCreationDate.setText(creationDate);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> logout())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileAdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}