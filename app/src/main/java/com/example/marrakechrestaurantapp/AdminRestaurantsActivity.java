package com.example.marrakechrestaurantapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminRestaurantsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Restaurant> list = new ArrayList<>();
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_restaurants);


        recyclerView = findViewById(R.id.recyclerRestaurants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RestaurantAdminAdapter adapter = new RestaurantAdminAdapter(this, list);
        recyclerView.setAdapter(adapter);

        Button btnAddRestaurant = findViewById(R.id.btnAddRestaurant);
        btnAddRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRestaurantsActivity.this, AdminEditRestaurantActivity.class);
            startActivity(intent); // ⚠️ Pas d'extra, ça lance le mode ajout
        });
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // ferme l'activité et revient à l'écran précédent


        ref = FirebaseDatabase.getInstance().getReference("restaurants");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Restaurant restaurant = snap.getValue(Restaurant.class);
                    if (restaurant != null) list.add(restaurant);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
