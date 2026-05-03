package com.example.marrakechrestaurantapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PlatAdminAdapter extends RecyclerView.Adapter<PlatAdminAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Plat> platList;

    public PlatAdminAdapter(Context context, ArrayList<Plat> platList) {
        this.context = context;
        this.platList = platList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_plat_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Plat plat = platList.get(position);

        // Textes
        holder.txtNom.setText(plat.getName() != null ? plat.getName() : "");
        holder.txtPrix.setText(String.format("%.2f MAD", plat.getPrice()));
        holder.txtCategorie.setText(plat.getCategory() != null ? plat.getCategory() : "Non spécifiée");
        holder.txtDescription.setText(plat.getDescription() != null ? plat.getDescription() : "");

        // Disponibilité
        if (plat.isAvailable()) {
            holder.txtDisponible.setText("Disponible");
            holder.txtDisponible.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.txtDisponible.setText("Indisponible");
            holder.txtDisponible.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        // Image
        loadPlatImage(holder, plat);

        // Supprimer
        holder.btnDelete.setOnClickListener(v -> deletePlat(position, plat));

        // Éditer
        holder.btnEdit.setOnClickListener(v -> editPlat(plat));

        // Plein écran
        holder.imagePlat.setOnClickListener(v -> showImageFullScreen(plat));
    }

    private void loadPlatImage(ViewHolder holder, Plat plat) {
        if (plat.getImageName() != null && !plat.getImageName().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(
                    plat.getImageName(),
                    "drawable",
                    context.getPackageName()
            );

            if (imageResId != 0) {
                holder.imagePlat.setImageResource(imageResId);
            } else {
                holder.imagePlat.setImageResource(R.drawable.ic_food_placeholder);
            }
        } else {
            holder.imagePlat.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    private void deletePlat(int position, Plat plat) {
        FirebaseDatabase.getInstance()
                .getReference("plats")
                .child(plat.getPlatId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    platList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, platList.size());
                    Toast.makeText(context, "Plat supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void editPlat(Plat plat) {
        Intent intent = new Intent(context, AdminEditPlatActivity.class);
        intent.putExtra("plat", plat);
        context.startActivity(intent);
    }

    private void showImageFullScreen(Plat plat) {
        if (plat.getImageName() != null && !plat.getImageName().isEmpty()) {
            Intent intent = new Intent(context, ImageFullScreenActivity.class);
            intent.putExtra("imageName", plat.getImageName());
            intent.putExtra("platNom", plat.getName());
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Aucune image disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return platList.size();
    }

    public void updateList(ArrayList<Plat> newList) {
        platList = newList;
        notifyDataSetChanged();
    }

    public void filterList(ArrayList<Plat> filteredList) {
        platList = filteredList;
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePlat;
        TextView txtNom, txtPrix, txtCategorie, txtDescription, txtDisponible;
        ImageButton btnDelete, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePlat = itemView.findViewById(R.id.image_plat);
            txtNom = itemView.findViewById(R.id.text_nom);
            txtPrix = itemView.findViewById(R.id.text_prix);
            txtCategorie = itemView.findViewById(R.id.text_categorie);
            txtDescription = itemView.findViewById(R.id.text_description);
            txtDisponible = itemView.findViewById(R.id.text_disponible);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);

            if (imagePlat == null) Log.e("ViewHolder", "image_plat manquant");
        }
    }
}
