package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private TextView btnBack, tabRestaurants, tabPlats;
    private RecyclerView recyclerRestaurants, recyclerPlats;
    private View emptyRestaurants, emptyPlats;
    private ProgressBar progressBar;

    private com.example.marrakechrestaurantapp.RestaurantAdapter restaurantAdapter;
    private PlatAdapter platAdapter;
    private List<Restaurant> favoriteRestaurantsList;
    private List<Plat> favoritePlatsList;

    private DatabaseReference favoritesRef, restaurantsRef, platsRef;
    private FirebaseAuth mAuth;

    private boolean isRestaurantsTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef = FirebaseDatabase.getInstance().getReference("favoris").child(userId);
        restaurantsRef = FirebaseDatabase.getInstance().getReference("restaurants");
        platsRef = FirebaseDatabase.getInstance().getReference("plats");

        // Initialisation des vues
        btnBack = findViewById(R.id.btnBack);
        tabRestaurants = findViewById(R.id.tabRestaurants);
        tabPlats = findViewById(R.id.tabPlats);
        recyclerRestaurants = findViewById(R.id.recyclerRestaurants);
        recyclerPlats = findViewById(R.id.recyclerPlats);
        emptyRestaurants = findViewById(R.id.emptyRestaurants);
        emptyPlats = findViewById(R.id.emptyPlats);
        progressBar = findViewById(R.id.progressBar);

        // Configuration des RecyclerViews
        favoriteRestaurantsList = new ArrayList<>();
        favoritePlatsList = new ArrayList<>();

        restaurantAdapter = new com.example.marrakechrestaurantapp.RestaurantAdapter(this, favoriteRestaurantsList);
        recyclerRestaurants.setLayoutManager(new LinearLayoutManager(this));
        recyclerRestaurants.setAdapter(restaurantAdapter);

        platAdapter = new PlatAdapter(this, favoritePlatsList);
        recyclerPlats.setLayoutManager(new LinearLayoutManager(this));
        recyclerPlats.setAdapter(platAdapter);

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Gestion des onglets
        tabRestaurants.setOnClickListener(v -> showRestaurantsTab());
        tabPlats.setOnClickListener(v -> showPlatsTab());

        // Boutons explorer
        findViewById(R.id.btnExploreRestaurants).setOnClickListener(v -> {
            startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
        });

        findViewById(R.id.btnExplorePlats).setOnClickListener(v -> {
            startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
        });

        // Charger les restaurants favoris par défaut
        loadFavoriteRestaurants();
    }

    private void showRestaurantsTab() {
        isRestaurantsTab = true;

        // Changer l'apparence des onglets
        tabRestaurants.setBackgroundColor(getResources().getColor(R.color.primary_red));
        tabRestaurants.setTextColor(getResources().getColor(android.R.color.white));

        tabPlats.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabPlats.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Afficher/cacher les vues
        recyclerRestaurants.setVisibility(View.VISIBLE);
        recyclerPlats.setVisibility(View.GONE);

        loadFavoriteRestaurants();
    }

    private void showPlatsTab() {
        isRestaurantsTab = false;

        // Changer l'apparence des onglets
        tabPlats.setBackgroundColor(getResources().getColor(R.color.primary_red));
        tabPlats.setTextColor(getResources().getColor(android.R.color.white));

        tabRestaurants.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabRestaurants.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Afficher/cacher les vues
        recyclerPlats.setVisibility(View.VISIBLE);
        recyclerRestaurants.setVisibility(View.GONE);

        loadFavoritePlats();
    }

    private void loadFavoriteRestaurants() {
        progressBar.setVisibility(View.VISIBLE);
        emptyRestaurants.setVisibility(View.GONE);

        favoritesRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRestaurantsList.clear();

                if (snapshot.exists()) {
                    // Compter le nombre de favoris à charger
                    final long totalFavorites = snapshot.getChildrenCount();
                    final long[] loadedCount = {0};

                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                        String restaurantId = favoriteSnapshot.getKey();

                        // Charger les détails du restaurant
                        restaurantsRef.child(restaurantId).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot restaurantSnapshot) {
                                        if (restaurantSnapshot.exists()) {
                                            String id = restaurantSnapshot.getKey();
                                            String name = restaurantSnapshot.child("name").getValue(String.class);
                                            String category = restaurantSnapshot.child("category").getValue(String.class);
                                            String description = restaurantSnapshot.child("description").getValue(String.class);
                                            String imageName= restaurantSnapshot.child("imageName").getValue(String.class);

                                            Integer deliveryTime = restaurantSnapshot.child("deliveryTime").getValue(Integer.class);
                                            Double rating = restaurantSnapshot.child("rating").getValue(Double.class);

                                            if (deliveryTime == null) deliveryTime = 30;
                                            if (rating == null) rating = 4.0;

                                            Restaurant restaurant = new Restaurant(
                                                    id, name, category, description,
                                                    imageName, deliveryTime, rating, false
                                            );
                                            favoriteRestaurantsList.add(restaurant);
                                        }

                                        loadedCount[0]++;
                                        if (loadedCount[0] == totalFavorites) {
                                            restaurantAdapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);

                                            if (favoriteRestaurantsList.isEmpty()) {
                                                emptyRestaurants.setVisibility(View.VISIBLE);
                                                recyclerRestaurants.setVisibility(View.GONE);
                                            } else {
                                                emptyRestaurants.setVisibility(View.GONE);
                                                recyclerRestaurants.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        loadedCount[0]++;
                                        if (loadedCount[0] == totalFavorites) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    emptyRestaurants.setVisibility(View.VISIBLE);
                    recyclerRestaurants.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoriteActivity.this,
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoritePlats() {
        progressBar.setVisibility(View.VISIBLE);
        emptyPlats.setVisibility(View.GONE);

        favoritesRef.child("plats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoritePlatsList.clear();

                if (snapshot.exists()) {
                    final long totalFavorites = snapshot.getChildrenCount();
                    final long[] loadedCount = {0};

                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                        String platId = favoriteSnapshot.getKey();

                        // Charger les détails du plat
                        platsRef.child(platId).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot platSnapshot) {
                                        if (platSnapshot.exists()) {
                                            String id = platSnapshot.getKey();
                                            String name = platSnapshot.child("name").getValue(String.class);
                                            String description = platSnapshot.child("description").getValue(String.class);
                                            String imageName = platSnapshot.child("imageName").getValue(String.class);
                                            String category = platSnapshot.child("category").getValue(String.class);
                                            String restId = platSnapshot.child("restaurantId").getValue(String.class);

                                            Double price = platSnapshot.child("price").getValue(Double.class);
                                            if (price == null) price = 0.0;

                                            Plat plat = new Plat(id, name, description, price, imageName, category, restId);

                                            favoritePlatsList.add(plat);
                                        }

                                        loadedCount[0]++;
                                        if (loadedCount[0] == totalFavorites) {
                                            platAdapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);

                                            if (favoritePlatsList.isEmpty()) {
                                                emptyPlats.setVisibility(View.VISIBLE);
                                                recyclerPlats.setVisibility(View.GONE);
                                            } else {
                                                emptyPlats.setVisibility(View.GONE);
                                                recyclerPlats.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        loadedCount[0]++;
                                        if (loadedCount[0] == totalFavorites) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    emptyPlats.setVisibility(View.VISIBLE);
                    recyclerPlats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoriteActivity.this,
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}