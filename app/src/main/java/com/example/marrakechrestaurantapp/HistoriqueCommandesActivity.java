//package com.example.marrakechrestaurantapp;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.marrakechrestaurantapp.models.Commande;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DatabaseError;
//
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DatabaseError;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class HistoriqueCommandesActivity extends AppCompatActivity {
//
//    private static final String TAG = "HISTORIQUE_DEBUG";
//
//    private RecyclerView recyclerViewHistorique;
//    private HistoriqueAdapter adapter;
//    private List<Commande> commandes = new ArrayList<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_historique_commandes);
//
//        recyclerViewHistorique = findViewById(R.id.recyclerViewHistorique);
//        recyclerViewHistorique.setLayoutManager(new LinearLayoutManager(this));
//
//        adapter = new HistoriqueAdapter(commandes);
//        recyclerViewHistorique.setAdapter(adapter);
//
//
//        chargerHistorique();
//    }
//    private void chargerHistorique() {
//
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//
//        String userId = currentUser.getUid();
//
//        DatabaseReference commandesRef =
//                FirebaseDatabase.getInstance()
//                        .getReference("commandes")
//                        .child(userId);
//
//        commandesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//
//                commandes.clear();
//
//                if (!snapshot.exists()) {
//                    Toast.makeText(
//                            HistoriqueCommandesActivity.this,
//                            "Aucune commande trouvée.",
//                            Toast.LENGTH_LONG
//                    ).show();
//                    adapter.notifyDataSetChanged();
//                    return;
//                }
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Commande commande = ds.getValue(Commande.class);
//                    commandes.add(commande);
//                }
//
//                // 🔽 Trier par date (desc)
//                commandes.sort((c1, c2) ->
//                        c2.getDate().compareTo(c1.getDate())
//                );
//
//                adapter.notifyDataSetChanged();
//                Toast.makeText(
//                        HistoriqueCommandesActivity.this,
//                        commandes.size() + " commande(s) trouvée(s)",
//                        Toast.LENGTH_SHORT
//                ).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Toast.makeText(
//                        HistoriqueCommandesActivity.this,
//                        "Erreur : " + error.getMessage(),
//                        Toast.LENGTH_LONG
//                ).show();
//            }
//        });
//    }
//
//    // Recharge à chaque fois que tu reviens sur l'écran (très utile après une nouvelle commande)
//    @Override
//    protected void onResume() {
//        super.onResume();
//        chargerHistorique();
//    }
//}
package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.models.Commande;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class HistoriqueCommandesActivity extends AppCompatActivity {

    private static final String TAG = "HistoriqueCommandes";

    private RecyclerView recyclerViewHistorique;
    private TextView tvEmptyMessage;  // ✨ Ajout d'un message si vide
    private HistoriqueAdapter adapter;
    private List<Commande> commandes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_commandes);

        // Initialiser les vues
        recyclerViewHistorique = findViewById(R.id.recyclerViewHistorique);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);  // ✨ À ajouter dans le XML

        // Configurer le RecyclerView
        recyclerViewHistorique.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoriqueAdapter(commandes);
        recyclerViewHistorique.setAdapter(adapter);

        // Charger l'historique
        chargerHistorique();
    }

    /**
     * Charge l'historique des commandes depuis Firebase
     */
    private void chargerHistorique() {
        Log.d(TAG, "═══════════════════════════════════════");
        Log.d(TAG, "🔍 Début chargement historique");
        Log.d(TAG, "═══════════════════════════════════════");

        // Vérifier que l'utilisateur est connecté
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ Utilisateur non connecté");
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "✅ User ID: " + userId);

        // ⚠️ IMPORTANT : Structure Firebase
        // Ta base Firebase a : commandes/userId/commandeId
        DatabaseReference commandesRef = FirebaseDatabase.getInstance()
                .getReference("commandes")
                .child(userId);

        Log.d(TAG, "📡 Référence Firebase: " + commandesRef.toString());

        commandesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "");
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "📥 onDataChange - Données reçues");
                Log.d(TAG, "═══════════════════════════════════════");

                commandes.clear();

                // Vérifier si le snapshot existe
                if (!snapshot.exists()) {
                    Log.w(TAG, "⚠️ Aucune commande trouvée dans Firebase");
                    Log.d(TAG, "Path vérifié : commandes/" + userId);

                    showEmptyState();

                    Toast.makeText(
                            HistoriqueCommandesActivity.this,
                            "Aucune commande trouvée",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                Log.d(TAG, "✅ Nombre d'enfants dans snapshot: " + snapshot.getChildrenCount());

                // Parcourir toutes les commandes
                int compteur = 0;
                for (DataSnapshot commandeSnapshot : snapshot.getChildren()) {
                    compteur++;
                    String commandeId = commandeSnapshot.getKey();
                    Log.d(TAG, "");
                    Log.d(TAG, "📦 Commande #" + compteur + " - ID: " + commandeId);

                    try {
                        // Essayer de parser la commande
                        Commande commande = commandeSnapshot.getValue(Commande.class);

                        if (commande != null) {
                            Log.d(TAG, "  ✅ Parsing réussi");
                            Log.d(TAG, "    • Date: " + commande.getDate());
                            Log.d(TAG, "    • Total: " + commande.getTotal() + " DH");
                            Log.d(TAG, "    • Status: " + commande.getStatus());
                            Log.d(TAG, "    • Client: " + commande.getClientName());

                            commandes.add(commande);
                        } else {
                            Log.e(TAG, "  ❌ Commande NULL après parsing");
                            Log.e(TAG, "  Données brutes: " + commandeSnapshot.getValue());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "  ❌ ERREUR parsing commande " + commandeId, e);
                        Log.e(TAG, "  Données: " + commandeSnapshot.getValue());
                    }
                }

                Log.d(TAG, "");
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "📊 RÉSUMÉ");
                Log.d(TAG, "═══════════════════════════════════════");
                Log.d(TAG, "Total dans Firebase: " + snapshot.getChildrenCount());
                Log.d(TAG, "Chargées avec succès: " + commandes.size());
                Log.d(TAG, "═══════════════════════════════════════");

                if (commandes.isEmpty()) {
                    showEmptyState();
                } else {
                    // Trier par date (plus récent en premier)
                    commandes.sort((c1, c2) -> {
                        if (c1.getDate() == null || c2.getDate() == null) return 0;
                        return c2.getDate().compareTo(c1.getDate());
                    });

                    showCommandesList();
                    adapter.notifyDataSetChanged();

                    String message = commandes.size() + " commande(s) trouvée(s)";
                    Log.d(TAG, "✅ " + message);
                    Toast.makeText(
                            HistoriqueCommandesActivity.this,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "");
                Log.e(TAG, "═══════════════════════════════════════");
                Log.e(TAG, "❌ ERREUR FIREBASE");
                Log.e(TAG, "═══════════════════════════════════════");
                Log.e(TAG, "Code: " + error.getCode());
                Log.e(TAG, "Message: " + error.getMessage());
                Log.e(TAG, "Détails: " + error.getDetails());
                Log.e(TAG, "═══════════════════════════════════════");

                Toast.makeText(
                        HistoriqueCommandesActivity.this,
                        "Erreur : " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                showEmptyState();
            }
        });
    }

    /**
     * Affiche l'état vide (aucune commande)
     */
    private void showEmptyState() {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
        }
        recyclerViewHistorique.setVisibility(View.GONE);
    }

    /**
     * Affiche la liste des commandes
     */
    private void showCommandesList() {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.GONE);
        }
        recyclerViewHistorique.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Rechargement de l'historique");
        chargerHistorique();
    }
}