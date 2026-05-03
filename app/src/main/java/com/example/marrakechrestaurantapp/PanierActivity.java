package com.example.marrakechrestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PanierActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPanier;
    private View layoutPanierVide;
    private MaterialCardView cardRecapitulatif;
    private TextView textViewTotal;
    private RadioGroup radioGroupPaiement;
    private Button buttonValider;

    private List<ArticlePanier> articlesPanier;
    private PanierAdapter adapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    // Pour recevoir le résultat de PayPalActivity
    private ActivityResultLauncher<Intent> paypalLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        initViews();
        chargerDonneesPanier();
        setupRecyclerView();
        updateUI();

        // Initialiser le launcher pour PayPal
        paypalLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // PayPal est revenu → on considère le paiement réussi
                        enregistrerCommandePayPal();
                    }
                    // Si annulé ou erreur, on ne fait rien
                }
        );

        setupListeners();
    }

    private void initViews() {
        recyclerViewPanier = findViewById(R.id.recyclerViewPanier);
        layoutPanierVide = findViewById(R.id.layoutPanierVide);
        cardRecapitulatif = findViewById(R.id.cardRecapitulatif);
        textViewTotal = findViewById(R.id.textViewTotal);
        radioGroupPaiement = findViewById(R.id.radioGroupPaiement);
        buttonValider = findViewById(R.id.buttonValider);
    }

    private void chargerDonneesPanier() {
        articlesPanier = PanierManager.getInstance().getArticles();
    }

    private void setupRecyclerView() {
        adapter = new PanierAdapter(articlesPanier, new PanierAdapter.PanierListener() {
            @Override
            public void onQuantiteChange(int position, int nouvelleQuantite) {
                if (nouvelleQuantite > 0) {
                    articlesPanier.get(position).setQuantite(nouvelleQuantite);
                    adapter.notifyItemChanged(position);
                    updateUI();
                }
            }

            @Override
            public void onSupprimer(int position) {
                articlesPanier.remove(position);
                adapter.notifyItemRemoved(position);
                updateUI();
            }
        });

        recyclerViewPanier.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPanier.setAdapter(adapter);
    }

    private void updateUI() {
        if (articlesPanier.isEmpty()) {
            layoutPanierVide.setVisibility(View.VISIBLE);
            recyclerViewPanier.setVisibility(View.GONE);
            cardRecapitulatif.setVisibility(View.GONE);
        } else {
            layoutPanierVide.setVisibility(View.GONE);
            recyclerViewPanier.setVisibility(View.VISIBLE);
            cardRecapitulatif.setVisibility(View.VISIBLE);

            double total = 0;
            for (ArticlePanier article : articlesPanier) {
                total += article.getPrixTotal();
            }
            textViewTotal.setText(currencyFormat.format(total));
        }
    }

    private void setupListeners() {
        buttonValider.setOnClickListener(v -> {
            if (articlesPanier.isEmpty()) {
                Toast.makeText(this, "Votre panier est vide !", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vous devez être connecté pour commander.", Toast.LENGTH_LONG).show();
                return;
            }

            double total = 0;
            for (ArticlePanier article : articlesPanier) {
                total += article.getPrixTotal();
            }

            int selectedId = radioGroupPaiement.getCheckedRadioButtonId();

            if (selectedId == R.id.radioPaypal) {

                // Lancer PayPalActivity pour le paiement (sans attendre de résultat)
                Intent intent = new Intent(PanierActivity.this, PayPalActivity.class);
                intent.putExtra("total", total);
                startActivity(intent);

                // === PAYPAL : on enregistre et vide le panier IMMÉDIATEMENT ===
                String fullName = currentUser.getDisplayName();
                String email = currentUser.getEmail();
                String phone = currentUser.getPhoneNumber();
                if (fullName == null || fullName.isEmpty()) fullName = "Client anonyme";
                if (email == null || email.isEmpty()) email = "email non renseigné";
                if (phone == null || phone.isEmpty()) phone = "non renseigné";

                List<ArticlePanier> articlesCopie = new ArrayList<>(articlesPanier);
                enregistrerCommandeDansFirebase(articlesCopie, total, "PayPal", fullName, email, phone);




            } else if (selectedId == R.id.radioLivraison) {
                String fullName = currentUser.getDisplayName();
                String email = currentUser.getEmail();
                String phone = currentUser.getPhoneNumber();
                if (fullName == null || fullName.isEmpty()) fullName = "Client anonyme";
                if (email == null || email.isEmpty()) email = "email non renseigné";
                if (phone == null || phone.isEmpty()) phone = "non renseigné";

                List<ArticlePanier> articlesCopie = new ArrayList<>(articlesPanier);
                enregistrerCommandeDansFirebase(articlesCopie, total, "Paiement à la livraison", fullName, email, phone);
            }
        });
    }

    // Appelé quand PayPalActivity revient
    private void enregistrerCommandePayPal() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String fullName = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhoneNumber();
        if (fullName == null || fullName.isEmpty()) fullName = "Client anonyme";
        if (email == null || email.isEmpty()) email = "email non renseigné";
        if (phone == null || phone.isEmpty()) phone = "non renseigné";

        double total = 0;
        for (ArticlePanier article : articlesPanier) {
            total += article.getPrixTotal();
        }

        List<ArticlePanier> articlesCopie = new ArrayList<>(articlesPanier);

        enregistrerCommandeDansFirebase(articlesCopie, total, "PayPal", fullName, email, phone);
    }

    private void enregistrerCommandeDansFirebase(List<ArticlePanier> articles,
                                                 double total,
                                                 String modePaiement,
                                                 String fullName,
                                                 String email,
                                                 String phone) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String clientId = user.getUid();

        // ⚠️ Directement dans /commandes (pas /commandes/userId)
        DatabaseReference commandesRef = FirebaseDatabase.getInstance()
                .getReference("commandes")
                .child(clientId);

        String idCommande = commandesRef.push().getKey();

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(new Date());
        String paymentMethod = modePaiement.equals("Paiement à la livraison") ? "CASH" : "PayPal";

        // ⚠️ Convertir les ArticlePanier en Items pour Firebase
        Map<String, Object> items = new HashMap<>();
        for (int i = 0; i < articles.size(); i++) {
            ArticlePanier article = articles.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("platId", article.getPlatId());
            item.put("nom", article.getNom());
            item.put("prix", article.getPrixUnitaire());
            item.put("quantite", article.getQuantite());
            items.put(String.valueOf(i), item);
        }

        // Créer l'objet commande
        Map<String, Object> commande = new HashMap<>();
        commande.put("idCommande", idCommande);
        commande.put("clientId", clientId);
        commande.put("clientName", fullName);
        commande.put("restaurantId", "rest_001");  // ⚠️ À adapter selon le restaurant
        commande.put("restaurantName", "Le Jardin"); // ⚠️ À adapter
        commande.put("status", "ATTENTE");
        commande.put("total", total);
        commande.put("paymentMethod", paymentMethod);
        commande.put("date", date);
        commande.put("items", items);

        commandesRef.child(idCommande)
                .setValue(commande)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "✅ Commande validée !\nTotal : " + currencyFormat.format(total),
                            Toast.LENGTH_LONG).show();

                    PanierManager.getInstance().viderPanier();
                    articlesPanier.clear();
                    updateUI();
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Erreur enregistrement", Toast.LENGTH_LONG).show();
                });
    }
}