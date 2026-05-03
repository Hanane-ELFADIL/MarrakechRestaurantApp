package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.adapters.CommandesAdapter;
import com.example.marrakechrestaurantapp.models.Commande;
import com.example.marrakechrestaurantapp.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GestionCommandeActivity extends AppCompatActivity {

    private static final String TAG = "GestionCommande";

    private RecyclerView recyclerViewCommandes;
    private View emptyView;
    private ImageView btnBack;

    private DatabaseReference commandesRef;
    private CommandesAdapter adapter;
    private List<Commande> commandesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_commande);

        // Firebase
        commandesRef = FirebaseDatabase.getInstance().getReference("commandes");

        // Views
        recyclerViewCommandes = findViewById(R.id.recyclerViewCommandes);
        emptyView = findViewById(R.id.emptyView);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // RecyclerView
        recyclerViewCommandes.setLayoutManager(new LinearLayoutManager(this));
        commandesList = new ArrayList<>();
        adapter = new CommandesAdapter(commandesList, this::onStatusChangeClick);
        recyclerViewCommandes.setAdapter(adapter);

        loadCommandes();
    }

    private void loadCommandes() {
        commandesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                commandesList.clear();

                for (DataSnapshot commandeSnapshot : snapshot.getChildren()) {

                    Commande commande = commandeSnapshot.getValue(Commande.class);

                    if (commande != null) {

                        // Transformer items (Map) en List<Item>
                        if (commandeSnapshot.hasChild("items")) {
                            List<Item> itemsList = new ArrayList<>();
                            for (DataSnapshot itemSnap : commandeSnapshot.child("items").getChildren()) {
                                Item item = itemSnap.getValue(Item.class);
                                if (item != null) itemsList.add(item);
                            }
                            commande.setItems(itemsList);
                        }

                        commandesList.add(commande);
                    }
                }

                adapter.notifyDataSetChanged();

                emptyView.setVisibility(commandesList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewCommandes.setVisibility(commandesList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionCommandeActivity.this,
                        "Erreur Firebase: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void onStatusChangeClick(Commande commande) {
        String[] statuts = {"ATTENTE", "EN_COURS", "PRETE", "LIVREE", "ANNULEE"};
        String[] statutsDisplay = {"En attente", "En cours", "Prête", "Livrée", "Annulée"};

        new AlertDialog.Builder(this)
                .setTitle("Modifier le statut")
                .setItems(statutsDisplay, (dialog, which) ->
                        updateCommandeStatus(commande, statuts[which]))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateCommandeStatus(Commande commande, String nouveauStatut) {

        commandesRef.child(commande.getIdCommande())
                .child("status")
                .setValue(nouveauStatut)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Statut mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
