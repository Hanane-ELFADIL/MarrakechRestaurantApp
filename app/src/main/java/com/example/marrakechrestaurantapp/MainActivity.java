package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu, btnClearSearch;
    private View btnCart;
    private TextView tvCartBadge, tvEmptyMessage, tvUserName, tvUserEmail;
    private EditText etSearch;
    private RecyclerView rvRestaurants;
    private FloatingActionButton fabChatbot;

    // Déclaration des vues du panier
    private ImageView iconPanier;
    private TextView badgePanier;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> restaurantList;
    private List<Restaurant> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("restaurants");

        // Initialisation des vues APRÈS setContentView
        initViews();

        // Afficher les infos utilisateur dans la sidebar
        if (currentUser != null) {
            String email = currentUser.getEmail();
            tvUserEmail.setText(email);

            String name = email != null ? email.split("@")[0] : "Utilisateur";
            tvUserName.setText(name);
        }

        // Configuration du RecyclerView
        restaurantList = new ArrayList<>();
        filteredList = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(this, filteredList);
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurants.setAdapter(restaurantAdapter);

        // Charger les restaurants
        loadRestaurants();

        // Setup des listeners
        setupListeners();
    }

    /**
     * Initialise toutes les vues
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        rvRestaurants = findViewById(R.id.rvRestaurants);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        fabChatbot = findViewById(R.id.fabChatbot);

        // Initialisation des vues du panier
        iconPanier = findViewById(R.id.iconPanier);
        badgePanier = findViewById(R.id.badgePanier);
    }

    /**
     * Configure tous les listeners
     */
    private void setupListeners() {
        // Menu hamburger - ouvrir la sidebar
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Bouton panier (le RelativeLayout parent)
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, PanierActivity.class);
            startActivity(intent);
        });

        // Icône panier individuelle (au cas où)
        if (iconPanier != null) {
            iconPanier.setOnClickListener(v -> {
                Intent intent = new Intent(this, PanierActivity.class);
                startActivity(intent);
            });
        }

        // Bouton Chatbot
        fabChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        // Gestion de la recherche
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRestaurants(s.toString());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bouton clear search
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // Menu items de la sidebar
        findViewById(R.id.menuProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, ProfileUserActivity.class));
        });

        findViewById(R.id.menuFavorites).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
        });

        findViewById(R.id.menuHistory).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, HistoriqueCommandesActivity.class));
        });

        findViewById(R.id.menuLogout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            logoutUser();
        });
    }

    /**
     * Met à jour le badge du panier
     */
    private void updatePanierBadge() {
        if (badgePanier == null) return;

        int nombreArticles = PanierManager.getInstance().getNombreArticles();
        if (nombreArticles > 0) {
            badgePanier.setVisibility(View.VISIBLE);
            badgePanier.setText(String.valueOf(nombreArticles));
        } else {
            badgePanier.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePanierBadge();
    }

    /**
     * Charge les restaurants depuis Firebase
     */
    private void loadRestaurants() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                restaurantList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                        String id = restaurantSnapshot.getKey();
                        String name = restaurantSnapshot.child("name").getValue(String.class);
                        String category = restaurantSnapshot.child("category").getValue(String.class);
                        String description = restaurantSnapshot.child("description").getValue(String.class);

                        // Utiliser "imageName" au lieu de "imageUrl"
                        String imageName = restaurantSnapshot.child("imageName").getValue(String.class);

                        Integer deliveryTime = restaurantSnapshot.child("deliveryTime").getValue(Integer.class);
                        Double rating = restaurantSnapshot.child("rating").getValue(Double.class);
                        Boolean isOpen = restaurantSnapshot.child("isOpen").getValue(Boolean.class);

                        if (deliveryTime == null) deliveryTime = 30;
                        if (rating == null) rating = 4.0;
                        if (isOpen == null) isOpen = false;

                        Restaurant restaurant = new Restaurant(
                                id, name, category, description,
                                imageName,
                                deliveryTime, rating, isOpen
                        );
                        restaurantList.add(restaurant);
                    }

                    // Appliquer le filtre actuel
                    filterRestaurants(etSearch.getText().toString());

                    if (filteredList.isEmpty() && etSearch.getText().toString().isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        rvRestaurants.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        rvRestaurants.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    rvRestaurants.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filtre les restaurants selon la recherche
     */
    private void filterRestaurants(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(restaurantList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Restaurant restaurant : restaurantList) {
                if (restaurant.getName() != null &&
                        restaurant.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(restaurant);
                } else if (restaurant.getCategory() != null &&
                        restaurant.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(restaurant);
                } else if (restaurant.getDescription() != null &&
                        restaurant.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(restaurant);
                }
            }
        }

        restaurantAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty() && !query.isEmpty()) {
            tvEmptyMessage.setText("Aucun résultat pour \"" + query + "\"");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            rvRestaurants.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Déconnecte l'utilisateur
     */
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Déconnecté avec succès", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}