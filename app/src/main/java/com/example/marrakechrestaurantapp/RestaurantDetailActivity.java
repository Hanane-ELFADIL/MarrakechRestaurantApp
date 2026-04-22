package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.adapters.PlatAdapter;
import com.example.marrakechrestaurantapp.models.Plat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantDetail";

    private ImageView imgRestaurant, btnBack;
    private TextView tvRestaurantName, tvRestaurantDescription, tvRestaurantRating, tvRestaurantDelivery;
    private RecyclerView recyclerPlats;
    private PlatAdapter adapter;
    private List<Plat> platList;
    private DatabaseReference databaseRef;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        // Récupérer les données du restaurant
        restaurantId = getIntent().getStringExtra("restaurantId");
        String restaurantName = getIntent().getStringExtra("restaurantName");
        String restaurantImage = getIntent().getStringExtra("restaurantImage");
        String restaurantDescription = getIntent().getStringExtra("restaurantDescription");
        double restaurantRating = getIntent().getDoubleExtra("restaurantRating", 0.0);
        int restaurantDeliveryTime = getIntent().getIntExtra("restaurantDeliveryTime", 0);

        // LOG pour vérifier les données reçues
        Log.d(TAG, "Restaurant ID: " + restaurantId);
        Log.d(TAG, "Restaurant Name: " + restaurantName);

        // Initialiser les vues
        imgRestaurant = findViewById(R.id.img_restaurant);
        btnBack = findViewById(R.id.btn_back);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvRestaurantDescription = findViewById(R.id.tv_restaurant_description);
        tvRestaurantRating = findViewById(R.id.tv_restaurant_rating);
        tvRestaurantDelivery = findViewById(R.id.tv_restaurant_delivery);
        recyclerPlats = findViewById(R.id.recycler_plats);

        // Afficher les informations du restaurant
        tvRestaurantName.setText(restaurantName);
        tvRestaurantDescription.setText(restaurantDescription);
        tvRestaurantRating.setText("★ " + restaurantRating);
        tvRestaurantDelivery.setText(restaurantDeliveryTime + " min");

        // Charger l'image depuis drawable
        int resId = getResources().getIdentifier(
                restaurantImage,
                "drawable",
                getPackageName()
        );
        if (resId != 0) {
            imgRestaurant.setImageResource(resId);
        } else {
            Log.e(TAG, "Image non trouvée: " + restaurantImage);
        }

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Configuration du RecyclerView
        recyclerPlats.setLayoutManager(new LinearLayoutManager(this));
        platList = new ArrayList<>();
        adapter = new PlatAdapter(this, platList, plat -> {
            Toast.makeText(this, plat.getName() + " ajouté au panier", Toast.LENGTH_SHORT).show();
        });
        recyclerPlats.setAdapter(adapter);

        // Charger les plats
        loadPlats();
    }

    private void loadPlats() {
        Log.d(TAG, "Début chargement des plats pour restaurant: " + restaurantId);

        databaseRef = FirebaseDatabase.getInstance().getReference("plats");

        // LOG de l'URL Firebase
        Log.d(TAG, "Firebase URL: " + databaseRef.toString());

        Query query = databaseRef.orderByChild("restaurantId").equalTo(restaurantId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange appelé");
                Log.d(TAG, "Nombre d'enfants: " + snapshot.getChildrenCount());

                platList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d(TAG, "Plat trouvé: " + data.getKey());

                    Plat plat = data.getValue(Plat.class);
                    if (plat != null) {
                        Log.d(TAG, "Nom du plat: " + plat.getName());
                        Log.d(TAG, "Prix: " + plat.getPrice());

                        if (plat.isAvailable()) {
                            platList.add(plat);
                        }
                    } else {
                        Log.e(TAG, "Erreur conversion Plat pour: " + data.getKey());
                    }
                }

                Log.d(TAG, "Total plats dans la liste: " + platList.size());
                adapter.notifyDataSetChanged();

                if (platList.isEmpty()) {
                    Toast.makeText(RestaurantDetailActivity.this,
                            "Aucun plat disponible pour ce restaurant", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Aucun plat trouvé pour restaurantId: " + restaurantId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur Firebase: " + error.getMessage());
                Log.e(TAG, "Code erreur: " + error.getCode());
                Toast.makeText(RestaurantDetailActivity.this,
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}