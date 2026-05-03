package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Déclaration des composants
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(
                        "https://marrakechrestaurant-2297e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference();

        // Vérifier si l'utilisateur est déjà connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.getUid());
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
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Bouton de connexion
        btnLogin.setOnClickListener(v -> loginUser());

        // Lien "Mot de passe oublié"
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Lien "S'inscrire"
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void loginUser() {
        // Récupérer les valeurs
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (!validateInputs(email, password)) return;

        // Afficher le loading
        showLoading(true);

        // Connexion Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this,
                                    "Connexion réussie !",
                                    Toast.LENGTH_SHORT).show();
                            // Vérifier le rôle depuis la DB
                            checkUserRoleAndRedirect(user.getUid());
                        }
                    } else {
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

    private void checkUserRoleAndRedirect(String uid) {
        Log.d(TAG, "Vérification du rôle pour UID: " + uid);

        showLoading(true);

        // Référence vers l'utilisateur dans la base de données
        DatabaseReference userRef = mDatabase.child("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                showLoading(false);

                Log.d(TAG, "Snapshot existe: " + snapshot.exists());

                if (snapshot.exists()) {
                    // Récupérer le rôle (essayer différents noms de champs)
                    String role = null;

                    if (snapshot.hasChild("role")) {
                        role = snapshot.child("role").getValue(String.class);
                    } else if (snapshot.hasChild("userType")) {
                        role = snapshot.child("userType").getValue(String.class);
                    } else if (snapshot.hasChild("type")) {
                        role = snapshot.child("type").getValue(String.class);
                    }

                    Log.d(TAG, "Rôle récupéré: " + role);

                    // Vérifier si c'est un admin
                    if (role != null && role.trim().equalsIgnoreCase("admin")) {
                        Log.d(TAG, "Redirection vers AdminActivity");
                        Toast.makeText(LoginActivity.this,
                                "Bienvenue Admin !",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        // Utilisateur normal
                        Log.d(TAG, "Redirection vers MainActivity");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    // L'utilisateur n'existe pas dans la base de données
                    Log.w(TAG, "Utilisateur non trouvé dans la base de données");
                    Toast.makeText(LoginActivity.this,
                            "Profil utilisateur introuvable",
                            Toast.LENGTH_SHORT).show();
                    // Redirection par défaut vers MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showLoading(false);

                Log.e(TAG, "Erreur Firebase: " + error.getMessage());
                Toast.makeText(LoginActivity.this,
                        "Erreur de connexion à la base de données",
                        Toast.LENGTH_LONG).show();

                // En cas d'erreur, rediriger vers MainActivity par sécurité
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
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
        if (progressBar != null) {
            progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!show);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}