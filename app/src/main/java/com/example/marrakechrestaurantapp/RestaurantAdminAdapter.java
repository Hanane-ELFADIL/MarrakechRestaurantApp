package com.example.marrakechrestaurantapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class RestaurantAdminAdapter extends RecyclerView.Adapter<RestaurantAdminAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Restaurant> restaurantList;

    public RestaurantAdminAdapter(Context context, ArrayList<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_restaurant_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.txtNom.setText(restaurant.getName());
        holder.txtCategorie.setText(restaurant.getCategory());
        holder.txtDescription.setText(restaurant.getDescription());
        holder.txtTempsLivraison.setText(restaurant.getDeliveryTime() + " min");
        holder.txtStatut.setText(restaurant.isOpen() ? "Ouvert" : "Fermé");
        holder.txtStatut.setTextColor(context.getResources().getColor(
                restaurant.isOpen() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        // Charger l'image depuis drawable ou URI
//        if (restaurant.getImageName() != null) {
//            if (restaurant.getImageName().startsWith("content://")) {
//                // Image depuis galerie
//                holder.imageRestaurant.setImageURI(Uri.parse(restaurant.getImageName()));
//            } else {
//                // Image depuis drawable
//                int resId = context.getResources().getIdentifier(
//                        restaurant.getImageName(),
//                        "drawable",
//                        context.getPackageName()
//                );
//                if (resId != 0) {
//                    holder.imageRestaurant.setImageResource(resId);
//                } else {
//                    // image par défaut si introuvable
//                    holder.imageRestaurant.setImageResource(R.drawable.ic_image_placeholder);
//                }
//            }
//        } else {
//            // image par défaut
//            holder.imageRestaurant.setImageResource(R.drawable.ic_image_placeholder);
//        }
        if (restaurant.getImageName() != null && !restaurant.getImageName().isEmpty()) {
            int resId = context.getResources().getIdentifier(
                    restaurant.getImageName().toLowerCase(),  // toujours en minuscule
                    "drawable",
                    context.getPackageName()
            );
            if (resId != 0) {
                holder.imageRestaurant.setImageResource(resId);
            } else {
                holder.imageRestaurant.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.imageRestaurant.setImageResource(R.drawable.ic_image_placeholder);
        }


        // Supprimer un restaurant
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("restaurants")
                    .child(restaurant.getRestaurantId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        restaurantList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, restaurantList.size());
                    });
        });

        // Editer un restaurant
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEditRestaurantActivity.class);
            intent.putExtra("restaurant", restaurant);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRestaurant;
        TextView txtNom, txtCategorie, txtDescription, txtTempsLivraison, txtStatut;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRestaurant = itemView.findViewById(R.id.image_restaurant);
            txtNom = itemView.findViewById(R.id.text_nom);
            txtCategorie = itemView.findViewById(R.id.text_categorie);
            txtDescription = itemView.findViewById(R.id.text_description);
            txtTempsLivraison = itemView.findViewById(R.id.text_temps_livraison);
            txtStatut = itemView.findViewById(R.id.text_statut);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
