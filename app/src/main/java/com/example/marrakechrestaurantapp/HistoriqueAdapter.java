package com.example.marrakechrestaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.models.Commande;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoriqueAdapter extends RecyclerView.Adapter<HistoriqueAdapter.ViewHolder> {

    private List<Commande> commandes;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    public HistoriqueAdapter(List<Commande> commandes) {
        this.commandes = commandes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commande_historique, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Commande c = commandes.get(position);

        holder.tvDate.setText("Date : " + c.getDate());
        holder.tvTotal.setText("Total : " + currencyFormat.format(c.getTotal()));

        // ✅ CORRECTION ICI
        holder.tvStatut.setText("Statut : " + c.getStatus());

        // ✅ CORRECTION ICI
        if (c.getItems() != null) {
            holder.tvNbArticles.setText("Articles : " + c.getItems().size());
        } else {
            holder.tvNbArticles.setText("Articles : 0");
        }
    }

    @Override
    public int getItemCount() {
        return commandes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotal, tvStatut, tvNbArticles;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatut = itemView.findViewById(R.id.tvStatut);
            tvNbArticles = itemView.findViewById(R.id.tvNbArticles);
        }
    }
}