package com.example.marrakechrestaurantapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AdminEditRestaurantActivity extends AppCompatActivity {

    private EditText edtName, edtCategory, edtDescription, edtDeliveryTime;
    private Switch switchIsOpen;
    private Button btnSave, btnBack;
    private ImageView imgRestaurant;
    private Spinner spinnerImages;

    private Restaurant restaurant;
    private boolean isEditMode = false;

    // Images depuis drawable
    private final String[] imageNames = {
            "restaurant_le_jardin",
            "restaurant_dar_yacout",
            "restaurant_nomad"
    };
    private final String[] imageDisplayNames = {"Le Jardin", "Dar Yacout", "Restaurant Nomad"};

    // Pour sélectionner image depuis la galerie
    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri; // stocke l'image sélectionnée
    private boolean isImageFromGallery = false; // si vrai, ne pas changer l'image via Spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_restaurant);

        // Views
        edtName = findViewById(R.id.edtName);
        edtCategory = findViewById(R.id.edtCategory);
        edtDescription = findViewById(R.id.edtDescription);
        edtDeliveryTime = findViewById(R.id.edtDeliveryTime);
        switchIsOpen = findViewById(R.id.switchIsOpen);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        imgRestaurant = findViewById(R.id.imgRestaurant);
        spinnerImages = findViewById(R.id.spinnerImages);

        // Bouton Retour
        btnBack.setOnClickListener(v -> finish());

        // Spinner images
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                imageDisplayNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImages.setAdapter(adapter);

        spinnerImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (!isImageFromGallery) { // Ne pas écraser image choisie depuis galerie
//                    int resId = getResources().getIdentifier(imageNames[position], "drawable", getPackageName());
//                    if (resId != 0) {
//                        imgRestaurant.setImageResource(resId);
//                    } else {
//                        Log.e("AdminEditRestaurant", "Image non trouvée: " + imageNames[position]);
//                    }
                    int resId = getResources().getIdentifier(
                            imageNames[spinnerImages.getSelectedItemPosition()].toLowerCase(),
                            "drawable",
                            getPackageName()
                    );
                    if (resId != 0) {
                        imgRestaurant.setImageResource(resId);
                    } else {
                        imgRestaurant.setImageResource(R.drawable.ic_image_placeholder);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Cliquer sur l'image pour choisir depuis galerie
        imgRestaurant.setOnClickListener(v -> openImageChooser());

        // Mode édition / ajout
        if (getIntent().hasExtra("restaurant")) {
            restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
            isEditMode = true;
            loadRestaurantData();
            btnSave.setText("Mettre à jour");
        } else {
            restaurant = new Restaurant();
            restaurant.setRestaurantId(UUID.randomUUID().toString());
            btnSave.setText("Ajouter");

            // Afficher image par défaut au premier emplacement
            int resId = getResources().getIdentifier(imageNames[0], "drawable", getPackageName());
            if (resId != 0) {
                imgRestaurant.setImageResource(resId);
            }
        }

        // Bouton Enregistrer
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                saveRestaurantData();
                saveToFirebase();
            }
        });
    }

    // Ouvrir la galerie pour choisir une image
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
    }

    // Récupérer l'image sélectionnée
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgRestaurant.setImageURI(selectedImageUri);
            isImageFromGallery = true;
        }
    }

    private void loadRestaurantData() {
        edtName.setText(restaurant.getName());
        edtCategory.setText(restaurant.getCategory());
        edtDescription.setText(restaurant.getDescription());
        edtDeliveryTime.setText(String.valueOf(restaurant.getDeliveryTime()));
        switchIsOpen.setChecked(restaurant.isOpen());

        if (restaurant.getImageName() != null) {
            int pos = 0;
            for (int i = 0; i < imageNames.length; i++) {
                if (imageNames[i].equals(restaurant.getImageName())) {
                    pos = i;
                    break;
                }
            }
            spinnerImages.setSelection(pos);

            int resId = getResources().getIdentifier(restaurant.getImageName(), "drawable", getPackageName());
            if (resId != 0) {
                imgRestaurant.setImageResource(resId);
            } else {
                Log.e("AdminEditRestaurant", "Image restaurant non trouvée: " + restaurant.getImageName());
            }
        }
    }

    private boolean validateForm() {
        if (edtName.getText().toString().trim().isEmpty()) {
            edtName.setError("Le nom est requis");
            return false;
        }
        if (edtCategory.getText().toString().trim().isEmpty()) {
            edtCategory.setError("La catégorie est requise");
            return false;
        }
        if (edtDeliveryTime.getText().toString().trim().isEmpty()) {
            edtDeliveryTime.setError("Temps de livraison requis");
            return false;
        }
        return true;
    }

    private void saveRestaurantData() {
        restaurant.setName(edtName.getText().toString().trim());
        restaurant.setCategory(edtCategory.getText().toString().trim());
        restaurant.setDescription(edtDescription.getText().toString().trim());
        restaurant.setOpen(switchIsOpen.isChecked());

        try {
            restaurant.setDeliveryTime(Integer.parseInt(edtDeliveryTime.getText().toString().trim()));
        } catch (NumberFormatException e) {
            restaurant.setDeliveryTime(0);
        }

        // Si l'utilisateur a choisi une image depuis galerie, on pourrait stocker l'URI dans Firebase
        // Sinon, on prend l'image du Spinner
        if (isImageFromGallery && selectedImageUri != null) {
            restaurant.setImageName(selectedImageUri.toString()); // stocke URI comme String
        } else {
            restaurant.setImageName(imageNames[spinnerImages.getSelectedItemPosition()]);
        }
    }

    private void saveToFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("restaurants")
                .child(restaurant.getRestaurantId());

        ref.setValue(restaurant)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            isEditMode ? "Restaurant mis à jour !" : "Restaurant ajouté !",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
