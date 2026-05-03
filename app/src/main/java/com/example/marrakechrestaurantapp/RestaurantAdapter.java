package com.example.marrakechrestaurantapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private Context context;
    private List<Restaurant> restaurantList;
    private FavoriteManager favoriteManager;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.favoriteManager = FavoriteManager.getInstance();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.tvName.setText(restaurant.getName());
        holder.tvCategory.setText(restaurant.getCategory());
        holder.tvDescription.setText(restaurant.getDescription());
        holder.tvDeliveryTime.setText("⏱️ " + restaurant.getDeliveryTime() + " min");
        holder.tvRating.setText("⭐ " + restaurant.getRating());

        // Charger l'image avec Glide
        loadImage(holder.ivImage, restaurant.getImageName());

        // Vérifier si le restaurant est dans les favoris
        favoriteManager.isRestaurantFavorite(restaurant.getRestaurantId(), isFavorite -> {
            holder.btnFavorite.setText(isFavorite ? "❤️" : "🤍");
        });

        // Gérer le clic sur le bouton favori
        holder.btnFavorite.setOnClickListener(v -> {
            favoriteManager.isRestaurantFavorite(restaurant.getRestaurantId(), isFavorite -> {
                if (isFavorite) {
                    // Retirer des favoris
                    favoriteManager.removeRestaurantFromFavorites(restaurant.getRestaurantId(),
                            new FavoriteManager.FavoriteCallback() {
                                @Override
                                public void onSuccess() {
                                    holder.btnFavorite.setText("🤍");
                                    Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(context, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Ajouter aux favoris
                    favoriteManager.addRestaurantToFavorites(restaurant.getRestaurantId(),
                            new FavoriteManager.FavoriteCallback() {
                                @Override
                                public void onSuccess() {
                                    holder.btnFavorite.setText("❤️");
                                    Toast.makeText(context, "Ajouté aux favoris ❤️", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(context, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        });

        // Gérer le clic sur l'item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailActivity.class);
            intent.putExtra("RESTAURANT_ID", restaurant.getRestaurantId());
            intent.putExtra("RESTAURANT_NAME", restaurant.getName());
            intent.putExtra("RESTAURANT_CATEGORY", restaurant.getCategory());
            intent.putExtra("RESTAURANT_DESCRIPTION", restaurant.getDescription());
            intent.putExtra("RESTAURANT_DELIVERY_TIME", restaurant.getDeliveryTime());
            intent.putExtra("RESTAURANT_RATING", restaurant.getRating());
            intent.putExtra("RESTAURANT_IMAGE", restaurant.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    // Méthode pour charger les images depuis drawable
    private void loadImage(ImageView imageView, String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        try {
            // Récupérer l'ID de la ressource drawable
            int resourceId = context.getResources().getIdentifier(
                    imageName,           // nom de l'image (ex: "restaurant_le_jardin")
                    "drawable",          // type de ressource
                    context.getPackageName()
            );

            if (resourceId != 0) {
                // Charger l'image avec Glide
                Glide.with(context)
                        .load(resourceId)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView);
            } else {
                // Image non trouvée, utiliser l'image par défaut
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCategory, tvDescription, tvDeliveryTime, tvRating;
        TextView btnFavorite;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRestaurantImage);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvCategory = itemView.findViewById(R.id.tvRestaurantCategory);
            tvDescription = itemView.findViewById(R.id.tvRestaurantDescription);
            tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}