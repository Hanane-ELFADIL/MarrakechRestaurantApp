package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileUserActivity extends AppCompatActivity {

    private TextView btnBack, btnFavorites, btnSaveChanges;
    private ImageView ivProfileImage;
    private TextView ivEditPhoto;
    private EditText etName, etEmail, etPhone, etAddress;
    private LinearLayout btnChangePassword, btnLogout, btnRateApp;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseUser currentUser;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance(
                        "https://marrakechrestaurant-2297e-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        if (currentUser == null) {
            startActivity(new Intent(ProfileUserActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupImagePicker();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorites = findViewById(R.id.btnFavorites);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnRateApp = findViewById(R.id.btnRateApp);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfileImage.setImageURI(uri);
                        uploadProfileImage();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(ProfileUserActivity.this, FavoriteActivity.class));
        });

        ivEditPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        findViewById(R.id.cardProfileImage).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSaveChanges.setOnClickListener(v -> saveUserData());

        // ⭐ NOUVEAU : Rating de l'application
        btnRateApp.setOnClickListener(v -> showRatingDialog());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        showLoading(true);

        String uid = currentUser.getUid();
        mDatabase.child("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        showLoading(false);

                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);
                            String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                            etName.setText(name != null ? name : "");
                            etEmail.setText(email != null ? email : currentUser.getEmail());
                            etPhone.setText(phone != null ? phone : "");
                            etAddress.setText(address != null ? address : "");

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(ProfileUserActivity.this)
                                        .load(photoUrl)
                                        .centerCrop()
                                        .into(ivProfileImage);
                            }
                        } else {
                            etEmail.setText(currentUser.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(ProfileUserActivity.this,
                                "❌ Erreur de chargement: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "❌ Le nom est requis", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("updatedAt", System.currentTimeMillis());

        String uid = currentUser.getUid();
        mDatabase.child("Users").child(uid)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "✅ Profil mis à jour avec succès !",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "❌ Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void uploadProfileImage() {
        if (selectedImageUri == null) return;

        showLoading(true);

        String uid = currentUser.getUid();
        StorageReference imageRef = mStorage.child("profile_images/" + uid + ".jpg");

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();

                        mDatabase.child("Users").child(uid).child("photoUrl")
                                .setValue(photoUrl)
                                .addOnSuccessListener(aVoid -> {
                                    showLoading(false);
                                    Toast.makeText(ProfileUserActivity.this,
                                            "✅ Photo de profil mise à jour !",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Toast.makeText(ProfileUserActivity.this,
                                            "❌ Erreur de sauvegarde URL",
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "❌ Erreur upload: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ⭐ NOUVELLE MÉTHODE : Dialog de Rating
    private void showRatingDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 40, 60, 40);
        dialogLayout.setGravity(android.view.Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("⭐ Notez notre application");
        title.setTextSize(20);
        title.setTextColor(getResources().getColor(R.color.primary_red));
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5);

        EditText etComment = new EditText(this);
        etComment.setHint("Votre commentaire (optionnel)");
        etComment.setMinLines(3);
        etComment.setPadding(20, 20, 20, 20);
        etComment.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        etComment.setLayoutParams(params);

        dialogLayout.addView(title);
        dialogLayout.addView(ratingBar);
        dialogLayout.addView(etComment);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("Envoyer", null)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                String comment = etComment.getText().toString().trim();

                if (rating == 0) {
                    Toast.makeText(this, "⭐ Veuillez donner une note", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveRatingToFirebase(rating, comment);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // ⭐ Sauvegarder le rating dans Firebase
    private void saveRatingToFirebase(float rating, String comment) {
        showLoading(true);

        String uid = currentUser.getUid();
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", uid);
        ratingData.put("userName", etName.getText().toString());
        ratingData.put("rating", rating);
        ratingData.put("comment", comment);
        ratingData.put("timestamp", System.currentTimeMillis());

        mDatabase.child("AppRatings").child(uid)
                .setValue(ratingData)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "⭐ Merci pour votre note !",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "❌ Erreur lors de l'envoi",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showChangePasswordDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 40, 60, 20);

        EditText etCurrentPassword = new EditText(this);
        etCurrentPassword.setHint("Mot de passe actuel");
        etCurrentPassword.setInputType(129); // textPassword
        etCurrentPassword.setPadding(20, 20, 20, 20);
        etCurrentPassword.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

        EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Nouveau mot de passe");
        etNewPassword.setInputType(129); // textPassword
        etNewPassword.setPadding(20, 20, 20, 20);
        etNewPassword.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

        EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Confirmer le mot de passe");
        etConfirmPassword.setInputType(129); // textPassword
        etConfirmPassword.setPadding(20, 20, 20, 20);
        etConfirmPassword.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 16;

        etNewPassword.setLayoutParams(params);
        etConfirmPassword.setLayoutParams(params);

        dialogLayout.addView(etCurrentPassword);
        dialogLayout.addView(etNewPassword);
        dialogLayout.addView(etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🔒 Changer le mot de passe")
                .setView(dialogLayout)
                .setPositiveButton("Modifier", null)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(this, "❌ Entrez le mot de passe actuel", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(this, "❌ Entrez le nouveau mot de passe", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 6) {
                    Toast.makeText(this, "❌ Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "❌ Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                    return;
                }

                changePassword(currentPassword, newPassword);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        showLoading(true);

        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                showLoading(false);
                                Toast.makeText(ProfileUserActivity.this,
                                        "✅ Mot de passe modifié avec succès !",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(ProfileUserActivity.this,
                                        "❌ Erreur: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProfileUserActivity.this,
                            "❌ Mot de passe actuel incorrect",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🚪 Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> logout())
                .setNegativeButton("Non", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "✅ Déconnexion réussie", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ProfileUserActivity.this, LoginActivity.class));
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}