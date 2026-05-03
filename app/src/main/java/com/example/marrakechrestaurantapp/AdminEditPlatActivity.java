package com.example.marrakechrestaurantapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminEditPlatActivity extends AppCompatActivity {

    private EditText edtNom, edtPrix, edtDescription;
    private Spinner spinnerCategorie, spinnerRestaurants, spinnerImages;
    private Switch switchDisponible;
    private Button btnSave;
    private ImageView imagePreview;

    private List<Restaurant> restaurantList = new ArrayList<>();
    private ArrayAdapter<String> restaurantAdapter;
    private ArrayAdapter<String> categorieAdapter;
    private Plat plat;

    // Catégories
    private final String[] categories = {
            "Entrées", "Plats principaux", "Desserts", "Boissons",
            "Salades", "Sandwichs", "Tajines", "Couscous",
            "Grillades", "Pâtes", "Pizzas", "Sushis",
            "Apéritifs", "Soupes"
    };

    // Images locales
    private final String[] imageNames = {
            "tajine_poulet",
            "couscous_viande",
            "pizza_margherita",
            "pastilla_poulet",
            "pastilla_fruits_de_mer",
            "jus_orange"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_plat);

        // Views
        edtNom = findViewById(R.id.edtNom);
        edtPrix = findViewById(R.id.edtPrix);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerCategorie = findViewById(R.id.spinnerCategorie);
        spinnerRestaurants = findViewById(R.id.spinnerRestaurants);
        spinnerImages = findViewById(R.id.spinnerImages);
        switchDisponible = findViewById(R.id.switchDisponible);
        btnSave = findViewById(R.id.btnSave);
        imagePreview = findViewById(R.id.imagePreview);

        // Spinner catégories
        categorieAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(categorieAdapter);

        // Spinner images
        ArrayAdapter<String> imageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, imageNames);
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImages.setAdapter(imageAdapter);

        spinnerImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedImage = imageNames[position];
                plat.setImageUrl(selectedImage);

                int resId = getResources()
                        .getIdentifier(selectedImage, "drawable", getPackageName());
                if (resId != 0) {
                    imagePreview.setImageResource(resId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner restaurants
        restaurantAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        restaurantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestaurants.setAdapter(restaurantAdapter);
        loadRestaurants();

        // Plat existant ou nouveau
        plat = (Plat) getIntent().getSerializableExtra("plat");
        if (plat != null) {
            loadPlatData();
            btnSave.setText("Mettre à jour");
        } else {
            plat = new Plat();
            plat.setId(UUID.randomUUID().toString());
            btnSave.setText("Ajouter le plat");
        }

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                savePlatData();
                saveToFirebase();
            }
        });
    }

    private void loadRestaurants() {
        FirebaseDatabase.getInstance().getReference("restaurants")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        restaurantList.clear();
                        List<String> names = new ArrayList<>();
                        names.add("Sélectionner un restaurant");

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Restaurant r = ds.getValue(Restaurant.class);
                            if (r != null) {
                                r.setRestaurantId(ds.getKey());
                                restaurantList.add(r);
                                names.add(r.getName());
                            }
                        }

                        restaurantAdapter.clear();
                        restaurantAdapter.addAll(names);
                        restaurantAdapter.notifyDataSetChanged();

                        if (plat.getRestaurantId() != null) {
                            for (int i = 0; i < restaurantList.size(); i++) {
                                if (restaurantList.get(i).getRestaurantId()
                                        .equals(plat.getRestaurantId())) {
                                    spinnerRestaurants.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AdminEditPlatActivity.this,
                                "Erreur de chargement des restaurants",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPlatData() {
        edtNom.setText(plat.getName());
        edtPrix.setText(String.valueOf(plat.getPrice()));
        edtDescription.setText(plat.getDescription());

        if (plat.getCategory() != null) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(plat.getCategory())) {
                    spinnerCategorie.setSelection(i);
                    break;
                }
            }
        }

        if (plat.getImageName() != null) {
            int resId = getResources()
                    .getIdentifier(plat.getImageName(), "drawable", getPackageName());
            if (resId != 0) {
                imagePreview.setImageResource(resId);
            }
        }
    }

    private void savePlatData() {
        plat.setName(edtNom.getText().toString().trim());
        plat.setDescription(edtDescription.getText().toString().trim());
        plat.setCategory(spinnerCategorie.getSelectedItem().toString());

        try {
            plat.setPrice(Double.parseDouble(edtPrix.getText().toString().trim()));
        } catch (NumberFormatException e) {
            plat.setPrice(0);
        }

        int pos = spinnerRestaurants.getSelectedItemPosition();
        if (pos > 0 && restaurantList.size() >= pos) {
            plat.setRestaurantId(
                    restaurantList.get(pos - 1).getRestaurantId()
            );
        }
    }

    private boolean validateForm() {
        if (edtNom.getText().toString().trim().isEmpty()) {
            edtNom.setError("Le nom est requis");
            return false;
        }

        try {
            double prix = Double.parseDouble(edtPrix.getText().toString().trim());
            if (prix <= 0) {
                edtPrix.setError("Prix invalide");
                return false;
            }
        } catch (Exception e) {
            edtPrix.setError("Prix invalide");
            return false;
        }

        if (spinnerRestaurants.getSelectedItemPosition() <= 0) {
            Toast.makeText(this,
                    "Veuillez sélectionner un restaurant",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveToFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("plats")
                .child(plat.getPlatId());

        ref.setValue(plat)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Plat enregistré avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
