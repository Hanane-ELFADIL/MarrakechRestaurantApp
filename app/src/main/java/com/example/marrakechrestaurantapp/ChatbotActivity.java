package com.example.marrakechrestaurantapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.adapters.ChatAdapter;
import com.example.marrakechrestaurantapp.models.ChatMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;

    // Firebase
    private DatabaseReference restaurantsRef;
    private DatabaseReference platsRef;

    // Cache des données
    private Map<String, RestaurantData> restaurantsCache;
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialiser Firebase
        restaurantsRef = FirebaseDatabase.getInstance().getReference("restaurants");
        platsRef = FirebaseDatabase.getInstance().getReference("plats");
        restaurantsCache = new HashMap<>();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadDataFromFirebase();
    }

    private void initViews() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);  // Messages du haut vers le bas
        layoutManager.setStackFromEnd(false);   // Commencer en haut
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadDataFromFirebase() {
        // Afficher message de chargement
        ChatMessage loading = new ChatMessage(
                "⏳ Chargement des données des restaurants...",
                ChatMessage.TYPE_BOT
        );
        chatAdapter.addMessage(loading);
        scrollToBottom();

        // Charger les restaurants
        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                        String id = restaurantSnapshot.getKey();
                        String name = restaurantSnapshot.child("name").getValue(String.class);
                        String category = restaurantSnapshot.child("category").getValue(String.class);
                        String description = restaurantSnapshot.child("description").getValue(String.class);
                        Integer deliveryTime = restaurantSnapshot.child("deliveryTime").getValue(Integer.class);
                        Double rating = restaurantSnapshot.child("rating").getValue(Double.class);

                        RestaurantData data = new RestaurantData(name, category, description,
                                deliveryTime != null ? deliveryTime : 30,
                                rating != null ? rating : 4.0);
                        restaurantsCache.put(id, data);
                        data.restaurantId = id;
                    }

                    // Charger les plats
                    loadPlatsFromFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                removeLastMessage();
                chatAdapter.addMessage(new ChatMessage(
                        "❌ Erreur lors du chargement des données.",
                        ChatMessage.TYPE_BOT
                ));
            }
        });
    }

    private void loadPlatsFromFirebase() {
        platsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot platSnapshot : snapshot.getChildren()) {
                        String restaurantId = platSnapshot.child("restaurantId").getValue(String.class);
                        String name = platSnapshot.child("name").getValue(String.class);
                        String description = platSnapshot.child("description").getValue(String.class);
                        Double price = platSnapshot.child("price").getValue(Double.class);

                        if (restaurantId != null && restaurantsCache.containsKey(restaurantId)) {
                            RestaurantData data = restaurantsCache.get(restaurantId);
                            if (data != null) {
                                data.plats.add(new PlatData(name, description, price != null ? price : 0.0));
                            }
                        }
                    }
                }

                dataLoaded = true;
                removeLastMessage();
                sendWelcomeMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dataLoaded = true;
                removeLastMessage();
                sendWelcomeMessage();
            }
        });
    }

    private void sendWelcomeMessage() {
        ChatMessage welcome = new ChatMessage(
                "👋 Bonjour ! Je suis votre assistant restaurant.\n\n" +
                        "Vous pouvez me demander :\n" +
                        "• Liste des restaurants\n" +
                        "• Menu d'un restaurant\n" +
                        "• Horaires de livraison\n" +
                        "• Prix des plats",
                ChatMessage.TYPE_BOT
        );
        chatAdapter.addMessage(welcome);
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dataLoaded) {
            Toast.makeText(this, "Chargement en cours...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Message utilisateur
        chatAdapter.addMessage(new ChatMessage(messageText, ChatMessage.TYPE_USER));
        scrollToBottom();
        etMessage.setText("");
        btnSend.setEnabled(false);

        // Typing indicator
        chatAdapter.addMessage(new ChatMessage("⏳ L'assistant écrit...", ChatMessage.TYPE_BOT));
        scrollToBottom();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            removeLastMessage();
            String response = getBotResponse(messageText);
            chatAdapter.addMessage(new ChatMessage(response, ChatMessage.TYPE_BOT));
            scrollToBottom();
            btnSend.setEnabled(true);
        }, 1200);
    }

    private String getBotResponse(String message) {
        message = message.toLowerCase();

        // Salutations
        if (message.contains("bonjour") || message.contains("salut") || message.contains("hello")) {
            return "👋 Bonjour ! Comment puis-je vous aider aujourd'hui ?";
        }

        // Aide
        if (message.contains("aide") || message.contains("help") || message.contains("commandes")) {
            return "🆘 Voici ce que je peux faire :\n\n" +
                    "• \"restaurants\" - Liste des restaurants\n" +
                    "• \"menu [nom]\" - Voir le menu\n" +
                    "• \"prix [nom]\" - Voir les prix\n" +
                    "• \"horaires\" - Horaires de livraison";
        }

        // Liste des restaurants
        if (message.contains("restaurant") && (message.contains("liste") || message.contains("disponible") ||
                message.contains("quels") || message.contains("tous"))) {
            StringBuilder sb = new StringBuilder("🍽️ Nos restaurants :\n\n");
            int count = 1;
            for (RestaurantData data : restaurantsCache.values()) {
                sb.append(count++).append(". ").append(data.name)
                        .append(" (").append(data.category).append(")\n")
                        .append("   ⏱️ ").append(data.deliveryTime).append(" min - ⭐ ")
                        .append(data.rating).append("\n\n");
            }
            return sb.toString();
        }

        // Menu d'un restaurant spécifique
        for (RestaurantData data : restaurantsCache.values()) {
            if (message.contains(data.name.toLowerCase()) ||
                    message.contains(data.name.toLowerCase().replace(" ", ""))) {

                if (message.contains("menu") || message.contains("plat") || message.contains("carte")) {
                    StringBuilder sb = new StringBuilder("🍴 Menu de ")
                            .append(data.name).append(" :\n\n");

                    if (data.plats.isEmpty()) {
                        return "❌ Aucun plat disponible pour ce restaurant.";
                    }

                    for (PlatData plat : data.plats) {
                        sb.append("• ").append(plat.name)
                                .append("\n  ").append(plat.description)
                                .append("\n  💰 ").append(plat.price).append(" DH\n\n");
                    }
                    return sb.toString();
                }

                // Info générale sur le restaurant
                if (message.contains("info") || message.contains("horaire") || message.contains("ouvert")) {
                    return "🏪 " + data.name + "\n\n" +
                            "📍 Catégorie : " + data.category + "\n" +
                            "⏱️ Livraison : " + data.deliveryTime + " minutes\n" +
                            "⭐ Note : " + data.rating + "/5\n" +
                            "📝 " + data.description;
                }
            }
        }

        // Horaires
        if (message.contains("horaire") || message.contains("heure") || message.contains("ouvert")) {
            return "⏰ Nos horaires de livraison :\n\n" +
                    "📍 La plupart de nos restaurants livrent de 12h à 23h.\n" +
                    "Demandez le menu d'un restaurant spécifique pour plus d'informations !";
        }

        // Prix
        if (message.contains("prix") || message.contains("coût") || message.contains("tarif")) {
            return "💰 Pour voir les prix :\n\n" +
                    "Demandez le menu d'un restaurant spécifique.\n" +
                    "Exemple : \"Menu Le Jardin\"";
        }

        // Merci
        if (message.contains("merci") || message.contains("thanks")) {
            return "😊 Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";
        }

        // Au revoir
        if (message.contains("au revoir") || message.contains("bye") || message.contains("aurevoir")) {
            return "👋 Au revoir ! À bientôt pour une nouvelle commande !";
        }

        // Message par défaut
        return "❓ Je n'ai pas compris votre demande.\n\n" +
                "Essayez :\n" +
                "• \"Liste des restaurants\"\n" +
                "• \"Menu Le Jardin\"\n" +
                "• \"Aide\"";
    }

    private void removeLastMessage() {
        if (chatAdapter.getItemCount() > 0) {
            chatAdapter.removeLastMessage();
        }
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            rvChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    // Classes internes pour stocker les données
    private static class RestaurantData {
        String restaurantId;
        String name;
        String category;
        String description;
        int deliveryTime;
        double rating;
        List<PlatData> plats;

        RestaurantData(String name, String category, String description, int deliveryTime, double rating) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.deliveryTime = deliveryTime;
            this.rating = rating;
            this.plats = new ArrayList<>();
        }
    }

    private static class PlatData {
        String name;
        String description;
        double price;

        PlatData(String name, String description, double price) {
            this.name = name;
            this.description = description;
            this.price = price;
        }
    }
}