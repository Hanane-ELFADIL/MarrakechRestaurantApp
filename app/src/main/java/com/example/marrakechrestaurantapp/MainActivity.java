package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.adapters.RestaurantAdapter;
import com.example.marrakechrestaurantapp.models.Restaurant;
import com.google.android.material.navigation.NavigationView;
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
    private NavigationView navigationView;
    private ImageView btnMenu;
    private CardView btnProfile, btnPanier;
    private EditText searchBar;
    private RecyclerView recyclerRestaurants;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    private List<Restaurant> restaurantList;
    private RestaurantAdapter adapter;

    private TextView navUserName, navUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate démarré");

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        initViews();
        setupNavigationDrawer();
        loadUserInfo();
        loadRestaurants();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnMenu = findViewById(R.id.btn_menu);
        btnProfile = findViewById(R.id.btn_profile);
        btnPanier = findViewById(R.id.btn_panier);
        searchBar = findViewById(R.id.search_bar);
        recyclerRestaurants = findViewById(R.id.recycler_restaurants);

        recyclerRestaurants.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();

        adapter = new RestaurantAdapter(this, restaurantList, restaurant -> {
            Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
            intent.putExtra("restaurantId", restaurant.restaurantId);
            intent.putExtra("restaurantName", restaurant.name);
            intent.putExtra("restaurantImage", restaurant.imageUrl);
            startActivity(intent);
        });

        recyclerRestaurants.setAdapter(adapter);

        View headerView = navigationView.getHeaderView(0);
        navUserName = headerView.findViewById(R.id.nav_user_name);
        navUserEmail = headerView.findViewById(R.id.nav_user_email);
    }

    private void setupNavigationDrawer() {
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_deconnexion) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v ->
                Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
        );

        btnPanier.setOnClickListener(v ->
                Toast.makeText(this, "Panier", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserInfo() {
        if (currentUser != null && currentUser.getEmail() != null) {
            navUserEmail.setText(currentUser.getEmail());
            navUserName.setText(currentUser.getEmail().split("@")[0]);
        }
    }

    private void loadRestaurants() {

        databaseRef.child("restaurants")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        restaurantList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            Restaurant r = data.getValue(Restaurant.class);

                            if (r != null) {
                                r.restaurantId = data.getKey();
                                restaurantList.add(r);
                                Log.e("FIREBASE_OK", "Restaurant chargé : " + r.name);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE_ERROR", error.getMessage());
                    }
                });
    }


    private void logout() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
