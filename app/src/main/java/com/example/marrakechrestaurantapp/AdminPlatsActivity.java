package com.example.marrakechrestaurantapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminPlatsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnAddPlat;
    ArrayList<Plat> platList = new ArrayList<>();
    DatabaseReference platsRef;
    PlatAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_plats);

        // Lier les vues
        recyclerView = findViewById(R.id.recyclerPlats);
        btnAddPlat = findViewById(R.id.btnAddPlat);

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlatAdminAdapter(this, platList);
        recyclerView.setAdapter(adapter);

        // Référence Firebase
        platsRef = FirebaseDatabase.getInstance().getReference("plats");

        // Charger la liste des plats depuis Firebase
        platsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                platList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Plat plat = snap.getValue(Plat.class);
                    if (plat != null) {
                        platList.add(plat);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                error.toException().printStackTrace();
            }
        });

        // Gestion du clic sur le bouton "Ajouter un plat"
        btnAddPlat.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPlatsActivity.this, AdminEditPlatActivity.class);
            startActivity(intent);
        });
    }
}
