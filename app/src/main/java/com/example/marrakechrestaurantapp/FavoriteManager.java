package com.example.marrakechrestaurantapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class FavoriteManager {

    private static FavoriteManager instance;
    private DatabaseReference favoritesRef;
    private FirebaseAuth mAuth;

    private FavoriteManager() {
        mAuth = FirebaseAuth.getInstance();
        favoritesRef = FirebaseDatabase.getInstance().getReference("favoris");
    }

    public static FavoriteManager getInstance() {
        if (instance == null) {
            instance = new FavoriteManager();
        }
        return instance;
    }

    // Ajouter un restaurant aux favoris
    public void addRestaurantToFavorites(String restaurantId, FavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que restaurantId n'est pas null
        if (restaurantId == null || restaurantId.isEmpty()) {
            callback.onError("ID du restaurant invalide");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("restaurants").child(restaurantId).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Retirer un restaurant des favoris
    public void removeRestaurantFromFavorites(String restaurantId, FavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que restaurantId n'est pas null
        if (restaurantId == null || restaurantId.isEmpty()) {
            callback.onError("ID du restaurant invalide");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("restaurants").child(restaurantId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Ajouter un plat aux favoris
    public void addPlatToFavorites(String platId, FavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que platId n'est pas null
        if (platId == null || platId.isEmpty()) {
            callback.onError("ID du plat invalide");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("plats").child(platId).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Retirer un plat des favoris
    public void removePlatFromFavorites(String platId, FavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que platId n'est pas null
        if (platId == null || platId.isEmpty()) {
            callback.onError("ID du plat invalide");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("plats").child(platId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Vérifier si un restaurant est dans les favoris
    public void isRestaurantFavorite(String restaurantId, CheckFavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que restaurantId n'est pas null
        if (restaurantId == null || restaurantId.isEmpty()) {
            callback.onResult(false);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("restaurants").child(restaurantId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(false);
                    }
                });
    }

    // Vérifier si un plat est dans les favoris
    public void isPlatFavorite(String platId, CheckFavoriteCallback callback) {
        // ✅ CORRECTION : Vérifier que platId n'est pas null (LIGNE 82 - FIX DU CRASH)
        if (platId == null || platId.isEmpty()) {
            callback.onResult(false);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        favoritesRef.child(userId).child("plats").child(platId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(false);
                    }
                });
    }

    // Interface pour les callbacks
    public interface FavoriteCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface CheckFavoriteCallback {
        void onResult(boolean isFavorite);
    }
}