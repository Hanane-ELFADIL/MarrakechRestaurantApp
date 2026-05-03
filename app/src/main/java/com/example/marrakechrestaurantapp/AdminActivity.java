package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private TextView tvTotalCommandes, tvEnCours, tvLivrees, tvRevenuTotal;

    private FirebaseAuth mAuth;
    private DatabaseReference commandesRef;

    // Sidebar
    private LinearLayout sidebarGestionRestaurant;
    private LinearLayout sidebarGestionPlats;        // ✨ NOUVEAU
    private LinearLayout sidebarGestionCommande;
    private LinearLayout sidebarProfile;
    private LinearLayout sidebarDeconnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        commandesRef = FirebaseDatabase.getInstance().getReference("commandes");

        // Initialisation
        initViews();
        setupSidebar();
        loadStatistics();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        tvTotalCommandes = findViewById(R.id.tvTotalCommandes);
        tvEnCours = findViewById(R.id.tvEnCours);
        tvLivrees = findViewById(R.id.tvLivrees);
        tvRevenuTotal = findViewById(R.id.tvRevenuTotal);

        // Sidebar
        sidebarGestionRestaurant = findViewById(R.id.sidebarGestionRestaurant);
        sidebarGestionPlats = findViewById(R.id.sidebarGestionPlats);        // ✨ NOUVEAU
        sidebarGestionCommande = findViewById(R.id.sidebarGestionCommande);
        sidebarProfile = findViewById(R.id.sidebarProfile);
        sidebarDeconnexion = findViewById(R.id.sidebarDeconnexion);

        // Bouton menu (burger)
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar));
            }
        });
    }

    private void setupSidebar() {

        // Navigation vers Gestion Restaurant
        sidebarGestionRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminRestaurantsActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        // ✨ NOUVEAU : Navigation vers Gestion des Plats
        sidebarGestionPlats.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminPlatsActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        // Navigation vers Gestion Commandes
        sidebarGestionCommande.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, GestionCommandeActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        // Navigation vers Profile
        sidebarProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ProfileAdminActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        // Déconnexion
        sidebarDeconnexion.setOnClickListener(v -> logout());
    }

    private void loadStatistics() {
        commandesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int total = 0;
                int enCours = 0;
                int livrees = 0;
                double revenuTotal = 0;

                for (DataSnapshot commandeSnapshot : dataSnapshot.getChildren()) {

                    total++;

                    String status = commandeSnapshot.child("status").getValue(String.class);
                    Double totalCommande = commandeSnapshot.child("total").getValue(Double.class);

                    if (status != null) {
                        switch (status) {
                            case "ATTENTE":
                            case "EN_COURS":
                            case "PRETE":
                                enCours++;
                                break;
                            case "LIVREE":
                                livrees++;
                                break;
                        }
                    }

                    if (totalCommande != null) {
                        revenuTotal += totalCommande;
                    }
                }

                tvTotalCommandes.setText(String.valueOf(total));
                tvEnCours.setText(String.valueOf(enCours));
                tvLivrees.setText(String.valueOf(livrees));
                tvRevenuTotal.setText(String.format("%.2f DH", revenuTotal));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log ou Toast si besoin
            }
        });
    }



    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}