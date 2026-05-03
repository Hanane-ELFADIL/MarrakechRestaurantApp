package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    private TextView tvRestaurantName, tvRestaurantCategory, tvRestaurantDescription;
    private TextView tvDeliveryTime, tvRating, tvEmptyMessage;
    private ImageView btnBack;
    private RecyclerView rvPlats;
    private FloatingActionButton fabChatbot;

    private DatabaseReference databaseReference;
    private PlatAdapter platAdapter;
    private List<Plat> platList;

    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        // Initialisation Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialisation des vues
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvRestaurantCategory = findViewById(R.id.tvRestaurantCategory);
        tvRestaurantDescription = findViewById(R.id.tvRestaurantDescription);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        tvRating = findViewById(R.id.tvRating);
        btnBack = findViewById(R.id.btnBack);
        rvPlats = findViewById(R.id.rvPlats);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        fabChatbot = findViewById(R.id.fabChatbot);

        // Récupérer les données du restaurant depuis l'Intent
        restaurantId = getIntent().getStringExtra("RESTAURANT_ID");
        String restaurantName = getIntent().getStringExtra("RESTAURANT_NAME");
        String category = getIntent().getStringExtra("RESTAURANT_CATEGORY");
        String description = getIntent().getStringExtra("RESTAURANT_DESCRIPTION");
        int deliveryTime = getIntent().getIntExtra("RESTAURANT_DELIVERY_TIME", 30);
        double rating = getIntent().getDoubleExtra("RESTAURANT_RATING", 4.0);

        // Afficher les informations du restaurant
        tvRestaurantName.setText(restaurantName != null ? restaurantName : "Restaurant");
        tvRestaurantCategory.setText(category != null ? category : "");
        tvRestaurantDescription.setText(description != null ? description : "");
        tvDeliveryTime.setText("⏱️ " + deliveryTime + " min");
        tvRating.setText("⭐ " + rating);

        // Configuration du RecyclerView
        platList = new ArrayList<>();
        platAdapter = new PlatAdapter(this, platList);
        rvPlats.setLayoutManager(new LinearLayoutManager(this));
        rvPlats.setAdapter(platAdapter);

        // Charger les plats depuis Firebase
        loadPlats();

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton Chatbot
        fabChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantDetailActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
    }

    private void loadPlats() {
        Query query = databaseReference.child("plats")
                .orderByChild("restaurantId")
                .equalTo(restaurantId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                platList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot platSnapshot : snapshot.getChildren()) {
                        String id = platSnapshot.getKey();
                        String name = platSnapshot.child("name").getValue(String.class);
                        String description = platSnapshot.child("description").getValue(String.class);

                        // ✅ CORRECTION : Utiliser "imageName" au lieu de "imageUrl"
                        String imageName = platSnapshot.child("imageName").getValue(String.class);

                        String category = platSnapshot.child("category").getValue(String.class);
                        String restId = platSnapshot.child("restaurantId").getValue(String.class);

                        Double price = platSnapshot.child("price").getValue(Double.class);
                        if (price == null) price = 0.0;

                        // ✅ CORRECTION : Passer imageName au lieu de imageUrl
                        Plat plat = new Plat(id, name, description, price,
                                imageName, category, restId);
                        platList.add(plat);
                    }

                    platAdapter.notifyDataSetChanged();

                    if (platList.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        rvPlats.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        rvPlats.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    rvPlats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantDetailActivity.this,
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}