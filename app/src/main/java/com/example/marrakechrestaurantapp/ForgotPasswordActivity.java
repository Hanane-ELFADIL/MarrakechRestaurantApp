package com.example.marrakechrestaurantapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Vues
    private TextInputEditText etEmail;
    private MaterialButton btnSendCode;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSendCode.setOnClickListener(v -> sendPasswordResetEmail());
    }

    // ==================== MÉTHODE SIMPLIFIÉE ====================
    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Validation de l'email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        // Envoyer directement l'email de réinitialisation Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // ✅ Email envoyé avec succès
                        Toast.makeText(this,
                                "✅ Email de réinitialisation envoyé à " + email + "\n" +
                                        "Vérifiez votre boîte mail (et les spams)",
                                Toast.LENGTH_LONG).show();

                        // Retour à la page de connexion
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // ❌ Erreur
                        handleError(task.getException());
                    }
                })
                .addOnFailureListener(this::handleError);
    }

    private void handleError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "❌ Erreur inconnue", Toast.LENGTH_SHORT).show();
            return;
        }

        String errorMessage;

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "❌ Aucun compte trouvé avec cet email";
        } else {
            errorMessage = "❌ Erreur : " + exception.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

        // Log pour debug
        System.out.println("Erreur Firebase : " + exception.getClass().getName());
        System.out.println("Message : " + exception.getMessage());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendCode.setEnabled(!show);
        etEmail.setEnabled(!show);
    }


    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}