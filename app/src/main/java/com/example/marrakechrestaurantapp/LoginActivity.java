package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Déclaration des composants
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private TextView tvForgotPassword, tvRegister;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();

        // Vérifier si l'utilisateur est déjà connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Rediriger vers l'écran principal
            goToMainActivity();
            return;
        }

        // Initialisation des composants
        initViews();

        // Gestion des clics
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Bouton de connexion
        btnLogin.setOnClickListener(v -> loginUser());

        // Lien "Mot de passe oublié"
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Lien "S'inscrire"
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Bouton Google (optionnel pour plus tard)
        btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Connexion Google à venir", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        // Récupérer les valeurs
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (!validateInputs(email, password)) {
            return;
        }

        // Afficher le loading
        showLoading(true);

        // Connexion Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Connexion réussie
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this,
                                "Bienvenue !",
                                Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        // Échec de connexion
                        String errorMessage = "Email ou mot de passe incorrect";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        // Vérifier email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            etEmail.requestFocus();
            return false;
        }

        // Vérifier mot de passe
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 caractères");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnGoogleLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnGoogleLogin.setEnabled(true);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Empêcher le retour arrière depuis l'écran de login
        super.onBackPressed();
        finishAffinity();
    }
}