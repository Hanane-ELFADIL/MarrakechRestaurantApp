package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // Déclaration des composants
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private CheckBox checkBoxAdmin;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(
                        "https://marrakechrestaurant-2297e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference();

        // Initialisation des composants
        initViews();

        // Gestion des clics
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        checkBoxAdmin = findViewById(R.id.checkBoxAdmin); // Checkbox admin
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Bouton d'inscription
        btnRegister.setOnClickListener(v -> registerUser());

        // Lien "Déjà un compte ? Se connecter"
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        // Récupérer les valeurs
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isAdmin = checkBoxAdmin.isChecked(); // Vérifier si admin
        String role = isAdmin ? "admin" : "user";     // Définir le rôle

        // Validation
        if (!validateInputs(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        // Afficher le loading
        showLoading(true);

        // Créer l'utilisateur dans Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Créer l'objet utilisateur pour Realtime Database
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("userId", userId);
                            userMap.put("fullName", fullName);
                            userMap.put("email", email);
                            userMap.put("phone", phone);
                            userMap.put("role", role); // Ajouter le rôle
                            userMap.put("profileImage", "");
                            userMap.put("createdAt", System.currentTimeMillis());

                            // Sauvegarder dans Realtime Database
                            mDatabase.child("Users").child(userId).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Inscription réussie !",
                                                    Toast.LENGTH_SHORT).show();

                                            // Redirection selon le rôle
                                            if (role.equals("admin")) {
                                                startActivity(new Intent(RegisterActivity.this, AdminActivity.class));
                                            } else {
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Erreur lors de la sauvegarde des données",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        // Échec de l'inscription
                        String errorMessage = "Erreur lors de l'inscription";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String fullName, String email, String phone,
                                   String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Nom complet requis");
            etFullName.requestFocus();
            return false;
        }
        if (fullName.length() < 3) {
            etFullName.setError("Le nom doit contenir au moins 3 caractères");
            etFullName.requestFocus();
            return false;
        }
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
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Téléphone requis");
            etPhone.requestFocus();
            return false;
        }
        if (phone.length() < 10) {
            etPhone.setError("Numéro de téléphone invalide");
            etPhone.requestFocus();
            return false;
        }
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
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirmation requise");
            etConfirmPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        btnRegister.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        // Empêcher le retour arrière depuis l'écran de register
        super.onBackPressed();
        finishAffinity();
    }
}
